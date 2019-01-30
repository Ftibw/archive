package com.yufone.dmbd.action.client.activity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author : Ftibw
 * @date : 2019/1/28 16:38
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "request")
public class ReqBean {

    private String url;//请求url

    private String method;//请求方法

    private Data data;//请求数据

    public ReqBean() {
    }

    public ReqBean(String url, String method, String tel, String aid, String token, String nonce) {
        this.url = url;
        this.method = method;
        this.data = new Data(tel, aid, token, nonce);
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement(name = "data")
    public static class Data {
        private String tel;//手机号
        private String aid;//活动编号
        private String token;//设备标识
        private String nonce;//手机验证返回的content

        public Data() {
        }

        private Data(String tel, String aid, String token, String nonce) {
            this.tel = tel;
            this.aid = aid;
            this.token = token;
            this.nonce = nonce;
        }

        public String getTel() {
            return tel;
        }

        public void setTel(String tel) {
            this.tel = tel;
        }

        public String getAid() {
            return aid;
        }

        public void setAid(String aid) {
            this.aid = aid;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getNonce() {
            return nonce;
        }

        public void setNonce(String nonce) {
            this.nonce = nonce;
        }
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }


}
