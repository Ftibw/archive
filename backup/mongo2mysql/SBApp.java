package com.yufone.dmbd;

import com.yufone.dmbd.vo.ActionContext;
import org.apache.commons.lang.text.StrBuilder;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.persister.entity.SingleTableEntityPersister;
import org.hibernate.persister.walking.spi.AttributeDefinition;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManagerFactory;
import java.io.*;
import java.nio.charset.Charset;
import java.util.*;


@EnableEncryptableProperties
@SpringBootApplication
@EnableScheduling
@EnableAsync
public class Dmbd4Application extends WebMvcConfigurerAdapter {

    private static Logger log = LoggerFactory.getLogger("Start-Class");

    public static void main(String[] args) {
        createDatabase(args);
        SpringApplication.run(Dmbd4Application.class, args);
    }


    public static boolean exec(String cmd) {
        List<String> list = new ArrayList<>();
        Charset cs = Charset.forName("UTF8");
        String os = System.getProperty("os.name");
        if (os.toLowerCase().startsWith("win")) {
            list.add("cmd.exe");
            list.add("/c");
            cs = Charset.forName("GBK");
        } else if (os.toLowerCase().contains("linux")) {
            list.add("/bin/sh");
            list.add("-c");
        }
        list.add(cmd);
        try {
            ProcessBuilder pb = new ProcessBuilder(list);
            //方法的作用:2>&1将错误输出合并到标准输出,清空错误输出的缓冲,这样下面只需要读取来清空标准输出即可
            pb.redirectErrorStream(true);
            Process ps = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(ps.getInputStream(), cs));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            int len = sb.length();
            if (len > 0) {
                log.info("<exec cmd respond> ===> [{}]", sb.substring(0, len - 1));
            }
            boolean ret = 0 == ps.waitFor();
            //(.*\s+-p\s*)[^\s]+(\s+.*)   ### " -pxxx "或" -p xxx "前后必须有至少一个空格
            log.info("[exec cmd {}] ===> [{}]", ret ? "success" : "failed",
                    cmd.replaceAll("(.*\\s+-p\\s*)[^\\s]+(\\s+.*)", "$1******$2"));
            return ret;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static final String MONGO_EXPORT_CSV_CMD_FORMAT_5PARAM = "mongoexport -h%s -d%s -c%s -f%s --type=csv -o%s";
    public static final String MYSQL_LOAD_CMD_FORMAT_4PARAM = "mysql --local-infile -h%s -u%s -p%s < %s";

    /**
     * 数据源连接前创建数据库(create DATABASE if not exists ${dbname} default charset utf8 COLLATE utf8_general_ci;)
     * IDE获取vm参数---List<String> params = ManagementFactory.getRuntimeMXBean().getInputArguments();
     * JAR启动获取vm参数---main方法的args
     */
    public static void createDatabase(String[] args) {
        //vm参数优先于properties参数
        String vmProfiles = null;
        String vmWorkDir = null;
        String vmBackHost = null;
        //创建库时可能会用到的vm参数都需要在这里覆盖掉配置文件中的值
        for (String param : args) {
            if (param.contains("install-path")) {
                vmWorkDir = param.split("=")[1];
            } else if (param.contains("spring.profiles.active")) {
                vmProfiles = param.split("=")[1];
            } else if (param.contains("backup.host")) {
                vmBackHost = param.split("=")[1];
            }
            // vm参数覆盖默认配置
        }

        Properties env = new Properties();
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            env.load(classLoader.getResourceAsStream("application.properties"));
            String profiles = env.getProperty("spring.profiles.active");
            if (null != vmProfiles) {
                profiles = vmProfiles;
            }
            if (null != profiles) {
                for (String profile : profiles.split(",")) {
                    //profile中的配置会覆盖主体properties中的key-value
                    env.load(classLoader.getResourceAsStream("application-" + profile + ".properties"));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        String host = null == vmBackHost ? env.getProperty("backup.host") : vmBackHost;
        String user = env.getProperty("spring.datasource.username");
        String psw = env.getProperty("spring.datasource.password");
        StandardPBEStringEncryptor stringEncryptor = new StandardPBEStringEncryptor();
        stringEncryptor.setPassword(env.getProperty("jasypt.encryptor.password"));
        user = stringEncryptor.decrypt(user.substring(4, user.length() - 1));
        psw = stringEncryptor.decrypt(psw.substring(4, psw.length() - 1));
        String dbname = env.getProperty("spring.data.mongodb.database");
        String defaultWorkDir = env.getProperty("default-install-workspace");
        String actualWorkDir;
        if (null == vmWorkDir) {
            actualWorkDir = defaultWorkDir;
        } else {
            actualWorkDir = vmWorkDir;
        }
        String configure = env.getProperty("createdb.config").replace("${install-workspace}", actualWorkDir);
        String createDBSql = env.getProperty("createdb.sql").replace("${install-workspace}", actualWorkDir);

        String cmd = configure + " " + dbname;
        exec(cmd);
        cmd = String.format(MYSQL_LOAD_CMD_FORMAT_4PARAM, host, user, psw, createDBSql);
        exec(cmd);
    }


    //================== mongodb export --> load into mysql =========================

    @Value("${spring.data.mongodb.database}")
    private String dbname;
    @Value("${mognodb.bin}")
    private String mongodb_bin;
    @Value("${backup.host}")
    private String backupHost;
    @Value("${spring.datasource.username}")
    private String backupUser;
    @Value("${spring.datasource.password}")
    private String backupPassword;
    @Value("${backup.interval}")
    private String backupInterval;//暂时没用上
    @Value("${backup.config}")
    private String configure;
    @Value("${backup.sql}")
    private String loadSql;
    @Value("${backup.dump-dir}")
    private String dumpDir;
    @Value("${install-workspace}")
    private String workDir;

    /**
     * 备份指令流水线,循环执行一遍就完成备份,
     * 关键是java用process循环执行,还是将list发送到shell中循环执行
     * <p>
     * 3个指令完成1个集合备份,总共36个集合,需要108个指令
     * 目前数据库完成一次备份需要调用108次shell
     */
    private final List<String> cmdLine = new ArrayList<>();

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @PostConstruct
    public void buildCmdLine() {
        String mongoHost = ActionContext.getInstance().getIp() + ":27017";
        String fieldList;
        String bakFilePath;
        StrBuilder sb = new StrBuilder();

        SessionFactoryImpl sessionFactory = (SessionFactoryImpl) entityManagerFactory.unwrap(org.hibernate.SessionFactory.class);
        Map<String, EntityPersister> persisterMap = sessionFactory.getEntityPersisters();
        for (Map.Entry<String, EntityPersister> entity : persisterMap.entrySet()) {
            SingleTableEntityPersister persister = (SingleTableEntityPersister) entity.getValue();
            String tableName = persister.getTableName();
            String pkName = persister.getIdentifierColumnNames()[0];
            sb.clear();
            sb.append(pkName).append(",");
            for (AttributeDefinition attr : persister.getAttributes()) {
                String[] columnName = persister.getPropertyColumnNames(attr.getName());
                sb.append(columnName[0]).append(",");
            }
            fieldList = sb.substring(0, sb.length() - 1);
            bakFilePath = dumpDir + "/" + dbname + "/" + tableName + ".csv";
            cmdLine.add(mongodb_bin + String.format(MONGO_EXPORT_CSV_CMD_FORMAT_5PARAM, mongoHost, dbname, tableName, fieldList, bakFilePath));
            cmdLine.add(configure + " " + dbname + " " + bakFilePath + " " + tableName);
            cmdLine.add(String.format(MYSQL_LOAD_CMD_FORMAT_4PARAM, backupHost, backupUser, backupPassword, loadSql));
        }

        //debug模式将指令全部打印到文件里
        if (log.isDebugEnabled()) {
            File cmdLineFile = new File(workDir + "/data/cmdLine");
            if (cmdLineFile.exists()) {
                cmdLineFile.delete();
            }
            try (PrintStream ps = new PrintStream(new FileOutputStream(cmdLineFile, true))) {
                for (String cmd : cmdLine) {
                    ps.println(cmd);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Scheduled(initialDelay = 9000, fixedRate = 1000 * 10 * 15)
    public void backup() {
        long start = System.currentTimeMillis();
        for (String cmd : cmdLine) {
            exec(cmd);
        }
        log.info("monogodb[{}]向mysql[{}]数据备份完成,共耗时{}ms",
                ActionContext.getInstance().getIp() + ":27017", backupHost + ":3306", System.currentTimeMillis() - start);
    }
}
