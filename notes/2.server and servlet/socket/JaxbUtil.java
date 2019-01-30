package com.yufone.dmbd.action.client.activity;

import com.yufone.dmbd.entity.TerminalInfo;

import javax.xml.bind.*;
import javax.xml.namespace.QName;
import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Jaxb2工具类 （xml和bean互转）
 *
 * @author zhuc
 * @create 2013-3-29 下午2:40:14
 */
public class JaxbUtil {

    /**
     * JavaBean转换成xml
     * 默认编码UTF-8
     *
     * @param obj
     */
    public static String convertToXml(Object obj) {
        return convertToXml(obj, "UTF-8").replace(" standalone=\"yes\"", "");
    }

    /**
     * JavaBean转换成xml
     *
     * @param obj
     * @param encoding
     * @return
     */
    public static String convertToXml(Object obj, String encoding) {
        String result = null;
        try {
            JAXBContext context = JAXBContext.newInstance(obj.getClass());
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, encoding);

            StringWriter writer = new StringWriter();
            marshaller.marshal(obj, writer);
            result = writer.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        assert result != null;
        return result.replace(" standalone=\"yes\"", "");
    }

    /**
     * 获取父系类+类本身
     *
     * @param clazz JAXB注解过的实体类
     * @author Ftibw
     * @create 2018-10-12 下午1:36:21
     */
    public static Class[] getSuperclassesWithSelf(Class clazz) {
        List<Class> list = new ArrayList<>();
        new Object() {
            /**
             * 必须先序递归保证父类在前,JAXBContext才能正确解析到子类
             */
            void getSuperClasses(Class clazz, List<Class> list) {
                Class parent = clazz.getSuperclass();
                if (Object.class.equals(parent)) {
                    return;
                }
                getSuperClasses(parent, list);
                list.add(parent);
            }
        }.getSuperClasses(clazz, list);
        list.add(clazz);
        return list.toArray(new Class[0]);
    }

    /**
     * xml转换成JavaBean,支持解构子类
     *
     * @param xml
     * @param c
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T convertToJavaBean(String xml, Class<T> c) {
        T t = null;
        try {
            JAXBContext context = JAXBContext.newInstance(getSuperclassesWithSelf(c));
            Unmarshaller unmarshaller = context.createUnmarshaller();
            t = (T) unmarshaller.unmarshal(new StringReader(xml));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return t;
    }

    /**
     * xml转换成JavaBean
     *
     * @param xmlFile
     * @param c
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T convertToJavaBean(File xmlFile, Class<T> c) {
        T t = null;
        try {
            JAXBContext context = JAXBContext.newInstance(c);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            t = (T) unmarshaller.unmarshal(xmlFile);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return t;
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws JAXBException {
        QName qName = new QName("Command");
        JAXBElement jaxbElement = new JAXBElement(qName, TerminalInfo.class, new TerminalInfo());
        Marshaller m = JAXBContext.newInstance(TerminalInfo.class).createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        StringWriter writer = new StringWriter();
        m.marshal(jaxbElement, writer);
        String xml = writer.toString();
        System.out.println(xml);
    }
}
