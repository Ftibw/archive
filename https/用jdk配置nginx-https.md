# 一、 JDK key-tool生成证书

测试服务器IP：`192.168.1.126`

证书相关文件根目录：`C:/Users/ftibw/Desktop/ssl/`

key的alias：`dmbd4`

storepass：`123456`

nginx的https端口映射的静态资源目录：`D: /static`



##  1.生成RSA类型密钥对keystore

```shell
keytool -genkey -alias dmbd4 -keypass 123456 -keyalg RSA -keysize 2048 -validity 3650 -keystore C:/Users/ftibw/Desktop/ssl/server.keystore -storepass 123456 -ext SAN=ip:192.168.1.126
```



## 2.转换为pkcs12类型keystore

```shell
keytool -importkeystore -srckeystore C:/Users/ftibw/Desktop/ssl/keystore.keystore -destkeystore C:/Users/ftibw/Desktop/ssl/server.keystore -deststoretype pkcs12
```



## 3.将keystore导出为证书文件(server.cer)

```shell
keytool -export -alias dmbd4 -storepass 123456 -file C:/Users/ftibw/Desktop/ssl/server.cer -keystore C:/Users/ftibw/Desktop/ssl/server.keystore
```

 

## 二、 OpenSSL证书转换

`使用openssl将jdk生成的证书转化为nginx配置文件中可以使用的证书类型`

## 1.server.cer文件转server.pem文件


```shell
x509 -inform der -in C:/Users/ftibw/Desktop/ssl/server.cer -out C:/Users/ftibw/Desktop/ssl/server.pem
```

附:

openssl命令将cer格式转换crt

CER是二进制形式的X.509证书，DER编码，jdk的keytool导出的.cer文件就是这种格式。

CRT是二进制X.509证书，封装在文本（base-64）编码中。

如下两种方式进行转换

1.1.jdk的keytool导出的.cer文件

```shell
x509 -inform DER -in C:/Users/ftibw/Desktop/ssl/server.cer -out C:/Users/ftibw/Desktop/ssl/server_cer.crt
```

1.2.若.cer文件格式已经是.pem文件格式，就直接执行下面命令进行转换，否则先将.cer文件转换为.pem文件格式再转换成.crt文件，显然jdk生成的.cer文件需要先转换为.pem文件

```shell
x509 -inform PEM -in C:/Users/ftibw/Desktop/ssl/server.pem -out C:/Users/ftibw/Desktop/ssl/server_pem.crt
```

可以用上述2种方式生成的.crt文件相互验证，如下：

```shell
verify -CAfile C:/Users/ftibw/Desktop/ssl/server_cer.crt C:/Users/ftibw/Desktop/ssl/server_pem.crt
```

 `响应：C:/Users/ftibw/Desktop/ssl/server_pem.crt: OK`

```shell
verify -CAfile C:/Users/ftibw/Desktop/ssl/server_pem.crt C:/Users/ftibw/Desktop/ssl/server_cer.crt
```

`响应：C:/Users/ftibw/Desktop/ssl/server_cer.crt: OK`



## 2.使用java工具类，将server.keystore转为server.pfx

`工具类如下：`

```java
public class ConvertPFXToKeystoreUtil {

    public static final String PKCS12 = "PKCS12";
    public static final String JKS = "JKS";

    public static final String PFX_KEYSTORE_FILE = "C:\\Users\\ftibw\\Desktop\\ssl\\server.pfx";
    public static final String KEYSTORE_PASSWORD = "123456";
    public static final String JKS_KEYSTORE_FILE = "C:\\Users\\ftibw\\Desktop\\ssl\\server.keystore";

    /**
     * 将pfx或p12的文件转为keystore
     */
    public static void coverTokeyStore() {
        try {
            KeyStore inputKeyStore = KeyStore.getInstance("PKCS12");
            FileInputStream fis = new FileInputStream(PFX_KEYSTORE_FILE);
            char[] nPassword = null;
            nPassword = KEYSTORE_PASSWORD.toCharArray();
            inputKeyStore.load(fis, nPassword);
            fis.close();
            KeyStore outputKeyStore = KeyStore.getInstance("JKS");
            outputKeyStore.load(null, KEYSTORE_PASSWORD.toCharArray());
            Enumeration enums = inputKeyStore.aliases();
            // we are readin just one
            // certificate.
            while (enums.hasMoreElements()) {
                String keyAlias = (String) enums.nextElement();
                System.out.println("alias=[" + keyAlias + "]");
                if (inputKeyStore.isKeyEntry(keyAlias)) {
                    Key key = inputKeyStore.getKey(keyAlias, nPassword);
                    Certificate[] certChain = inputKeyStore.getCertificateChain(keyAlias);
                    outputKeyStore.setKeyEntry(keyAlias, key, KEYSTORE_PASSWORD.toCharArray(), certChain);
                }
            }
            FileOutputStream out = new FileOutputStream(JKS_KEYSTORE_FILE);
            outputKeyStore.store(out, nPassword);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 将keystore转为pfx
     */
    public static void coverToPfx() {
        try {
            KeyStore inputKeyStore = KeyStore.getInstance("JKS");
            FileInputStream fis = new FileInputStream(JKS_KEYSTORE_FILE);
            char[] nPassword = null;
            nPassword = KEYSTORE_PASSWORD.toCharArray();
            inputKeyStore.load(fis, nPassword);
            fis.close();
            KeyStore outputKeyStore = KeyStore.getInstance("PKCS12");
            outputKeyStore.load(null, KEYSTORE_PASSWORD.toCharArray());
            Enumeration enums = inputKeyStore.aliases();
            // we are readin just one
            // certificate.
            while (enums.hasMoreElements()) {
                String keyAlias = (String) enums.nextElement();
                System.out.println("alias=[" + keyAlias + "]");
                if (inputKeyStore.isKeyEntry(keyAlias)) {
                    Key key = inputKeyStore.getKey(keyAlias, nPassword);
                    Certificate[] certChain = inputKeyStore.getCertificateChain(keyAlias);
                    outputKeyStore.setKeyEntry(keyAlias, key, KEYSTORE_PASSWORD.toCharArray(), certChain);
                }
            }
            FileOutputStream out = new FileOutputStream(PFX_KEYSTORE_FILE);
            outputKeyStore.store(out, nPassword);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        coverToPfx();
        //coverTokeyStore();
    }
}
```



## 3.使用server.pfx生成server.key文件

```shell
pkcs12 -in C:/Users/ftibw/Desktop/ssl/server.pfx -nocerts -nodes -out C:/Users/ftibw/Desktop/ssl/server.key
```



## 4.配置nginx

```nginx
http{
#其他配置省略
	server {
		listen       443 ssl;
		server_name  localhost;
		ssl_certificate      C:/Users/ftibw/Desktop/ssl/server.pem;
		ssl_certificate_key  C:/Users/ftibw/Desktop/ssl/server.key;
		ssl_session_cache    shared:SSL:1m;
		ssl_session_timeout  5m;
		ssl_ciphers  HIGH:MD5;
		ssl_prefer_server_ciphers  on;
	
		location / {
			add_header 'Access-Control-Allow-Origin' '*';
			root D: /static;
		}
	}
}

```

 

## 5.将C:/Users/ftibw/Desktop/ssl/server.cer证书文件颁发给客户端

执行addcert.bat脚本，即可将证书导入到受信任的根证书颁发机构中。然后重启浏览器。

addcert.bat脚本如下：

```shell
@echo off
%1 mshta vbscript:CreateObject("Shell.Application").ShellExecute("cmd.exe","/c %~s0 ::","","runas",1)(window.close)&&exit
cd /d "%~dp0"
certutil -addstore -f root C:/Users/ftibw/Desktop/ssl/server.cer
```

 