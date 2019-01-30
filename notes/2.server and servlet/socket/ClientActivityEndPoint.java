package com.yufone.dmbd.action.client.activity;

import com.alibaba.druid.util.lang.Consumer;
import com.yufone.dmbd.RS;
import com.yufone.dmbd.action.client.ClientActivityAction;
import com.yufone.dmbd.entity.TerminalInfo;
import com.yufone.dmbd.entity.lottery.Activity;
import com.yufone.dmbd.entity.lottery.ActivityPlay;
import com.yufone.dmbd.entity.lottery.Prize;
import com.yufone.dmbd.entity.lottery.Record;
import com.yufone.dmbd.repository.lottery.ActivityPlayRepository;
import com.yufone.dmbd.repository.lottery.PrizeRepository;
import com.yufone.dmbd.repository.lottery.RecordRepository;
import com.yufone.dmbd.repository.ro.CB;
import com.yufone.dmbd.repository.ro.OB;
import com.yufone.dmbd.repository.ro.OButils;
import com.yufone.dmbd.repository.ro.Page;
import com.yufone.dmbd.service.ActivityService;
import com.yufone.dmbd.service.TerminalService;
import com.yufone.dmbd.utils.DateUT;
import com.yufone.dmbd.vo.ActionContext;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.yufone.dmbd.action.client.ClientActivityAction.SDF;
import static com.yufone.dmbd.action.client.ClientActivityAction.setIfAbsent;

/**
 * 客户端抽奖活动端点
 * 切面切了controller提取请求信息,
 * 直接注入controller调用,切面空指针,只好拷贝一个请求端点
 *
 * @author : Ftibw
 * @date : 2018/11/23 13:53
 */
@Conditional(ClientActivityCondition.class)
@Component
public class ClientActivityEndPoint {

    public static final String[] EP_VALID_PHONE = {"/client/activity/validPhone", "POST"};
    public static final String[] EP_GET_ACTIVITY = {"/client/activity/getCurrentActivity", "GET"};
    public static final String[] EP_DRAW_PRIZE = {"/client/activity/drawPrize", "POST"};
    public static final String[] EP_GET_BASE_URL = {"/client/activity/getBaseUrl", "GET"};

    private static final Logger LOGGER = LoggerFactory.getLogger("ActivityEndPoint");

    @Autowired
    TerminalService ts;
    @Autowired
    private ActivityService as;
    @Autowired
    private ActivityPlayRepository apr;
    @Autowired
    private RecordRepository rr;
    @Autowired
    private PrizeRepository pp;
    @Autowired
    private RedisTemplate<String, String> rt;
    @Value("${activity.client.port}")
    private int port;

    @PostConstruct
    public void startServer() {
        new Server(port, new Server.Handler<String, String>() {
            @Override
            public String handle(String request) {
                String resp = null;
                ReqBean rb = JaxbUtil.convertToJavaBean(request, ReqBean.class);
                String url;
                String method;
                if (null == rb
                        || null == (url = rb.getUrl())
                        || null == (method = rb.getMethod())) {
                    return SocketUtils.INVALID_PARAMS_RESPONSE;
                }
                ReqBean.Data data = rb.getData();
                String token = data.getToken();

                if (EP_GET_ACTIVITY[0].equals(url)
                        && EP_GET_ACTIVITY[1].equalsIgnoreCase(method)) {
                    resp = getCurrentActivity(token);

                } else if (EP_VALID_PHONE[0].equals(url)
                        && EP_VALID_PHONE[1].equalsIgnoreCase(method)) {
                    resp = validPhone(token, data.getAid(), data.getTel());

                } else if (EP_DRAW_PRIZE[0].equals(url)
                        && EP_DRAW_PRIZE[1].equalsIgnoreCase(method)) {
                    resp = drawPrize(data.getAid(), token, data.getTel(), data.getNonce());

                } else if (EP_GET_BASE_URL[0].equals(url)
                        && EP_GET_BASE_URL[1].equalsIgnoreCase(method)) {
                    resp = getApplicationBaseUrl(token);

                }
                return resp;
            }
        }).start();
    }

    /**
     * RS中content是某些常用的基础,包装,字符串类型时
     */
    public static String buildPrimTypeRespXml(final RS rs) {
        return buildRespXml(rs.getCode(), rs.getMessage(), new Consumer<Element>() {
            @Override
            public void accept(Element content) {
                Object cnt = rs.getContent();
                if (cnt instanceof String
                        || cnt instanceof Integer
                        || cnt instanceof Long
                        || cnt instanceof Boolean
                        || cnt instanceof Byte
                        || cnt instanceof Double) {
                    content.addText(cnt + "");
                }
            }
        });
    }

    public static String buildRespXml(Integer code, String message, Consumer<Element> consumer) {
        Document doc = DocumentHelper.createDocument();
        Element root = doc.addElement("response");
        if (null != code) {
            root.addElement("code").setText(code + "");
        }
        if (null != message) {
            root.addElement("message").setText(message);
        }
        Element cntEle = root.addElement("content");
        consumer.accept(cntEle);
        OutputFormat format = OutputFormat.createCompactFormat();
        StringWriter writer = new StringWriter();
        XMLWriter output = new XMLWriter(writer, format);
        try {
            output.write(doc);
        } catch (IOException e) {
            LOGGER.info("生成响应xml字符串异常", e);
            return null;
        } finally {
            SocketUtils.close(writer);
        }
        return writer.toString();
    }

    /**
     * /client/activity/getCurrentActivity
     * 获取正在进行的活动数据以及活动记录
     *
     * @param token 终端token
     */
    public String getCurrentActivity(String token) {
        if (null == token) {
            return buildPrimTypeRespXml(ClientActivityAction.ResultEnum.INVALID_PARAMS.getResult("Token不合法，请重新激活终端"));
        }
        TerminalInfo ter = ts.getTerminalByToken(token);
        if (null == ter) {
            return buildPrimTypeRespXml(ClientActivityAction.ResultEnum.INVALID_PARAMS.getResult("Token不合法，请重新激活终端"));
        }
        String domain = ter.getDomain();
        String tid = ter.getId();
        ActivityPlay play = apr.findOne(domain, new CB().is("tid", tid), new Sort(new Sort.Order(Sort.Direction.DESC, "createTime")));
        if (null == play)
            return buildPrimTypeRespXml(new RS(RS.FAILED, "该终端未发布活动"));
        String aid = play.getAid();
//        String domain = "kmsz";
//        String aid = "5c4ee5fb16f49fb6d4c1e79f";

        Activity activity = as.getOneActivity(domain, aid);
        if (activity == null
                || null == activity.getStartTime()
                || null == activity.getEndTime()) {
            return buildPrimTypeRespXml(ClientActivityAction.ResultEnum.ACTIVITY_CANCELED.getResult("活动不存在"));
        }
        List<Prize> prizes = pp.findAll(new CB().is("aid", aid), OButils.getDefaultOB());
        Integer type = activity.getType();
        String apiRootUrl = ActionContext.getInstance().getApiRootUrl();
        String queryString = "?token=" + token + "&aid=" + aid;
        if (Activity.ACTIVITY_TYPE_DIAL.equals(type)) {
            activity.setLink(apiRootUrl + "prize/a.html#/rotate" + queryString);
        } else if (Activity.ACTIVITY_TYPE_NINE_GRID.equals(type)) {
            activity.setLink(apiRootUrl + "prize/a.html#/rotate_turn" + queryString);
        }
        activity.setPrizes(prizes);
        activity.setOid(null);
        activity.setStatus(null);
        activity.setCreator(null);
        activity.setApplyUid(null);
        activity.setApproveUid(null);
        activity.setApplyTime(null);
        activity.setApproveTime(null);
        //RS组装后再返回records,取最新的100行数据
        Page<Record> page = new Page<>();
        page.setLength(100);
        rr.findPage(domain, page, new CB().is("aid", aid).isNotNull("code"), new OB().add(Sort.Direction.DESC, "createTime"),
                "telephone", "prizeId", "prizeName", "prizeLevel", "chance", "createTime", "status");
        List<Record> records = page.getData();
        for (Record r : records) {
            r.setTelephone(r.getTelephone().replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2"));
        }
        return buildCurActivityXml(1, null, activity, records);
    }

    private String buildCurActivityXml(Integer code, String message, final Activity activity, final List<Record> records) {
        return buildRespXml(code, message, new Consumer<Element>() {
            @Override
            public void accept(Element cntEle) {
                Element actiEle = cntEle.addElement("activity");
                //添加活动的字段
                actiEle.addElement("id").setText(activity.getId());
                actiEle.addElement("name").setText(activity.getName());
                Integer joinNum = activity.getJoinNum();
                if (null != joinNum) {
                    actiEle.addElement("joinNum").setText(joinNum + "");
                }
                Double aChance = activity.getChance();
                if (null != aChance) {
                    actiEle.addElement("chance").setText(aChance + "");
                }
                Integer type = activity.getType();
                if (null != type) {
                    actiEle.addElement("type").setText(type + "");
                }
                Integer startTime = activity.getStartTime();
                Integer endTime = activity.getEndTime();
                if (null != startTime) {
                    actiEle.addElement("startTime").setText(startTime + "");
                }
                if (null != endTime) {
                    actiEle.addElement("endTime").setText(endTime + "");
                }
                //添加活动奖品列表字段内奖品的字段
                Element prisEle = actiEle.addElement("prizes");
                for (Prize p : activity.getPrizes()) {
                    Element priEle = prisEle.addElement("prize");
                    priEle.addElement("id").setText(p.getId());
                    priEle.addElement("name").setText(p.getName());
                    Integer level = p.getLevel();
                    if (null != level) {
                        priEle.addElement("level").setText(level + "");
                    }
                    Integer amount = p.getAmount();
                    if (null != amount) {
                        priEle.addElement("amount").setText(amount + "");
                    }
                    Integer hitCount = p.getHitCount();
                    if (null != hitCount) {
                        priEle.addElement("hitCount").setText(hitCount + "");
                    }
                    priEle.addElement("aid").setText(p.getAid());
                    //prize.chance是临时属性,只有在中奖过的奖品才标注几率
                }
                //添加抽奖记录列表字段内抽奖记录的字段
                Element recsEle = cntEle.addElement("records");
                for (Record r : records) {
                    Element recEle = recsEle.addElement("record");
                    recEle.addElement("id").setText(r.getId());
                    recEle.addElement("telephone").setText(r.getTelephone());
                    recEle.addElement("prizeId").setText(r.getPrizeId());
                    recEle.addElement("prizeName").setText(r.getPrizeName());
                    Integer pl = r.getPrizeLevel();
                    if (null != pl) {
                        recEle.addElement("prizeLevel").setText(pl + "");
                    }
                    Double chance = r.getChance();
                    if (null != chance) {
                        recEle.addElement("chance").setText(chance + "");
                    }
                    recEle.addElement("code").setText(r.getCode());
//                    recEle.addElement("tid").setText(r.getTid());
//                    recEle.addElement("aid").setText(r.getAid());
                    Date createTime = r.getCreateTime();
                    if (null != createTime) {
                        recEle.addElement("createTime").setText(createTime + "");
                    }
                    Integer status = r.getStatus();
                    if (null != status) {
                        recEle.addElement("status").setText(status + "");
                    }
                }
            }
        });
    }


    /**
     * /client/activity/validPhone
     * 验证手机号码是否有抽奖次数
     */
    public String validPhone(String token, String aid, String tel) {
        if (null == aid || null == tel || null == token) {
            return buildPrimTypeRespXml(ClientActivityAction.ResultEnum.INVALID_PARAMS.getResult(
                    "aid=" + aid, "tel=" + tel, "token=" + token));
        }

        String key = aid + tel;//aid*
        BoundValueOperations<String, String> valueOps = rt.boundValueOps(key);

        TerminalInfo ter = ts.getTerminalByToken(token);
        if (null == ter) {
            return buildPrimTypeRespXml(ClientActivityAction.ResultEnum.INVALID_PARAMS.getResult("token不合法,请重新激活终端"));
        }

        String domain = ter.getDomain();
        Record record = rr.findOne(domain, new CB().is("telephone", tel).is("aid", aid));
        if (null != record) {
            return formatDrawResult(record, key);
        }

        //有抽奖次数返回nonce
        String nonce = UUID.randomUUID().toString().replace("-", "");
        valueOps.set(nonce, 2, TimeUnit.HOURS);
        RS rs = new RS(nonce, RS.SUCCESS);
        rs.setMessage("手机验证2小时内有效");
        return buildPrimTypeRespXml(rs);
    }

    private String formatDrawResult(final Record record, String nonceKey) {
        final String code = record.getCode();
        if (null == code) {
            rt.delete(nonceKey);
            return buildPrimTypeRespXml(ClientActivityAction.ResultEnum.NO_COUNT.getResult());
        } else {
            final RS ret = ClientActivityAction.ResultEnum.HIT_PRIZE.getResult();
            return buildRespXml(ret.getCode(), ret.getMessage(), new Consumer<Element>() {
                @Override
                public void accept(Element cntEle) {
                    cntEle.addElement("hitCode").setText(code);
                    String pn = record.getPrizeName();
                    if (null != pn) {
                        cntEle.addElement("prizeName").setText(pn);
                    }
                    Integer pl = record.getPrizeLevel();
                    if (null != pl) {
                        cntEle.addElement("prizeLevel").setText(pl + "");
                    }
                }
            });
        }
    }

    /**
     * /client/activity/drawPrize
     * H5页面进行抽奖
     * 手机用户的号码未通过验证,返回无抽奖次数
     * 手机用户号码通过验证,但第一次请求未中奖,以后的请求返回无抽奖次数
     * 手机用户号码通过验证,第一次请求中奖,之后2小时内的请求返回兑奖码,2小时之后返回无抽奖次数
     *
     * @param aid   活动ID
     * @param token 终端token
     * @param tel   电话号码
     * @param nonce 电话号码验证随机数
     * @return 抽奖结果
     */
    ///drawPrize
    public String drawPrize(String aid, String token, String tel, String nonce) {
        if (null == aid || null == token || null == tel || null == nonce) {
            return buildPrimTypeRespXml(ClientActivityAction.ResultEnum.INVALID_PARAMS.getResult(
                    "aid=" + aid, "token=" + token, "tel=" + tel, "nonce=" + nonce));
        }
        String key = aid + tel;//aid*
        if (!nonce.equals(rt.boundValueOps(key).get()))
            return buildPrimTypeRespXml(ClientActivityAction.ResultEnum.NO_COUNT.getResult());

        //验证终端是否有效
        TerminalInfo ter = ts.getTerminalByToken(token);
        if (null == ter) {
            return buildPrimTypeRespXml(ClientActivityAction.ResultEnum.INVALID_PARAMS.getResult("token不合法,请重新激活终端"));
        }

        String tid = ter.getId();
        String domain = ter.getDomain();

        //验证活动是否有效
        ActivityPlay play = apr.findOne(domain, new CB().is("tid", tid).is("aid", aid));
        if (null == play) {
            return buildPrimTypeRespXml(ClientActivityAction.ResultEnum.ACTIVITY_CANCELED.getResult());
        }
        Activity activity = as.getOneActivity(domain, aid);
        if (null == activity) {
            return buildPrimTypeRespXml(ClientActivityAction.ResultEnum.ACTIVITY_CANCELED.getResult("活动不存在"));
        }
        int dateNow = DateUT.date2DayIntNow();
        int startTime = activity.getStartTime();//etc.20180101
        int endTime = activity.getEndTime();    //etc.20181231
        if (dateNow < startTime) {
            return buildPrimTypeRespXml(ClientActivityAction.ResultEnum.NOT_IN_TIME.getResult("活动尚未开始"));
        } else if (dateNow > endTime) {
            return buildPrimTypeRespXml(ClientActivityAction.ResultEnum.NOT_IN_TIME.getResult("活动已经结束"));
        }

        long duration = -1;
        try {
            duration = SDF.parse(endTime + "").getTime() - System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1) + 30;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        activity.setDuration(duration);
        String limitKey = key + "limit";//aid*
        if (!setIfAbsent(rt, LOGGER, limitKey, "1", duration)) {
            Record record = rr.findOne(domain, new CB().is("telephone", tel).is("aid", aid));
            if (null == record) {
                return buildPrimTypeRespXml(ClientActivityAction.ResultEnum.DRAW_PENDING.getResult());
            }
            return formatDrawResult(record, key);
        }

        //进行抽奖
        final RS rs = as.drawPrize(domain, activity, tid, tel);
        return buildRespXml(rs.getCode(), rs.getMessage(), new Consumer<Element>() {
            @Override
            public void accept(Element cntEle) {
                Object cnt = rs.getContent();
                if (cnt instanceof Record) {
                    Record r = (Record) cnt;
                    cntEle.addElement("id").setText(r.getId());
                    cntEle.addElement("telephone").setText(r.getTelephone());
                    cntEle.addElement("prizeId").setText(r.getPrizeId());
                    cntEle.addElement("prizeName").setText(r.getPrizeName());
                    Integer pl = r.getPrizeLevel();
                    if (null != pl) {
                        cntEle.addElement("prizeLevel").setText(pl + "");
                    }
                    Double chance = r.getChance();
                    if (null != chance) {
                        cntEle.addElement("chance").setText(chance + "");
                    }
                    cntEle.addElement("code").setText(r.getCode());
//                    cntEle.addElement("tid").setText(r.getTid());
//                    cntEle.addElement("aid").setText(r.getAid());
                    Date createTime = r.getCreateTime();
                    if (null != createTime) {
                        cntEle.addElement("createTime").setText(createTime + "");
                    }
                    Integer status = r.getStatus();
                    if (null != status) {
                        cntEle.addElement("status").setText(status + "");
                    }
                } else if (null != cnt) {
                    cntEle.addText(cnt + "");
                }
            }
        });
    }

    //"/getBaseUrl"
    public String getApplicationBaseUrl(String token) {
        TerminalInfo ter = ts.getTerminalByToken(token);
        if (null == ter || null == token) {
            return buildPrimTypeRespXml(new RS(RS.TIMEOUT, "Token不合法，请重新激活终端"));
        }
        String apiRootUrl = ActionContext.getInstance().getApiRootUrl();
        String queryString = "?token=" + token;
        String link = apiRootUrl + "prize/a.html#/pcrotate" + queryString;
        return buildPrimTypeRespXml(new RS(link, RS.SUCCESS));
    }

}
