package com.ftibw.https;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.Enumeration;

/**
 * @author duxin * @date 2018-12-13 * @desc pfx格式和keystore格式的证书互转
 */
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
