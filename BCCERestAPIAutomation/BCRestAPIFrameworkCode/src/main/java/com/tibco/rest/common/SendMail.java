package com.tibco.rest.common;

import org.apache.commons.configuration.ConfigurationConverter;
import org.apache.commons.io.IOUtils;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.*;

public class SendMail {

    public static boolean generateMailAndSendMail(String xslFile, String reportFile,String dbType,String buildNum) {

        Map<String, String> map = new HashMap<String, String>();
        try {
            String extra_fields = PropertiesUtil.getBundle().getProperty("extra.mail.fields").toString();

            if (extra_fields != null && extra_fields != "") {
                String[] extra_fields_array = extra_fields.split(";");
                for (String fields : extra_fields_array) {
                    try {
                        String key = fields.split("=")[0];
                        String value = fields.split("=")[1];
                        if (key != "" && value != "") {
                            map.put(key, value);
                        }
                    } catch (Exception e) {
                        System.err.println("Please check extra fields.");
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Extra fields are not provided.");
        }

        System.out.println("mail.output.dir : " + PropertiesUtil.getBundle().getPropertyValue("mail.output.dir"));

        try {
            Source schemaSourceXSL = new StreamSource(xslFile);
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = null;
            transformer = tFactory.newTransformer(schemaSourceXSL);
            transformer.setParameter("output.dir", PropertiesUtil.getBundle().getProperty("mail.output.dir").toString());
            transformer.setParameter("title", PropertiesUtil.getBundle().getProperty("mailer.report.title").toString());
            transformer.setParameter("platform_name", PropertiesUtil.getBundle().getString("mailer.product.platform.name"));
            //If buildNum is not null then use this , else use the default value defined in the properties file
            if(buildNum !=null) {
            transformer.setParameter("build_name",buildNum );
            }
            else {
            	 transformer.setParameter("build_name", PropertiesUtil.getBundle().getString("mailer.product.build.name").toString());	
            }
            
            transformer.setParameter("data_base",dbType);
            
            transformer.setParameter("reason_for_execution", PropertiesUtil.getBundle().getProperty("mailer.reason.execution").toString());

            if (PropertiesUtil.getBundle().getProperty("mailer.report.url") != null && PropertiesUtil.getBundle().getProperty("mailer.report.url") != "") {
                transformer.setParameter("report_url", PropertiesUtil.getBundle().getProperty("mailer.report.url").toString());
                transformer.setParameter("failure_report_url", PropertiesUtil.getBundle().getProperty("mailer.failure.report.url").toString());
            }
            if (PropertiesUtil.getBundle().getProperty("mailer.buildcomparereport.url") != null
                    && PropertiesUtil.getBundle().getProperty("mailer.buildcomparereport.url") != "")
                transformer.setParameter("test_compare_report_url",
                        PropertiesUtil.getBundle().getProperty("mailer.buildcomparereport.url").toString());
            if (!map.isEmpty()) {
                Set<String> keys = map.keySet();
                Iterator<String> itr = keys.iterator();
                while (itr.hasNext()) {
                    String key = itr.next();
                    transformer.setParameter(key, map.get(key));
                }
            }
            try {
                transformer.transform(new StreamSource(new File(reportFile).getAbsolutePath()),
                        new javax.xml.transform.stream.StreamResult(
                                new FileOutputStream(PropertiesUtil.getBundle().getProperty("mail.output.dir").toString() + "/email.html")));
            } catch (TransformerException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.err.println("Unable to parse XSL file.");
            return false;
        }
        String msg = "";
        try {
            if (PropertiesUtil.getBundle().getProperty("mailer.skip").toString().equalsIgnoreCase("false")) {

            InputStream inputStream = new FileInputStream(
                    new File(PropertiesUtil.getBundle().getProperty("mail.output.dir").toString() + "/email.html").getAbsolutePath());
            msg = IOUtils.toString(inputStream, "UTF-8");
            String tempList = PropertiesUtil.getBundle().getProperty("mailer.to.list").toString();
            System.out.println("mailer.to.list : " + tempList);
            String[] list1 = tempList.split(" ");
            String subwithDB=PropertiesUtil.getBundle().getProperty("mailer.msg.subject").toString()+"_"+dbType;
            sendMail(subwithDB, msg.toString(), list1,
                    PropertiesUtil.getBundle().getProperty("mailer.from").toString());
            }
        } catch (Exception e) {
            PropertiesUtil.getBundle().getPropertyValue("Unable to send mail.");
            System.out.println("Unable to send mail.");
            return false;
        }

        return true;
    }

    public static void sendMail(String sub, String msg, String[] to, String from) {
        try {
            final Properties props = new Properties();
            PropertiesUtil p = PropertiesUtil.getBundle();
           
            props.putAll(ConfigurationConverter.getMap(p.subset("mailer")));

            Session session = null;

            try {
                if (props.getProperty("mail.smtp.auth").toString().equalsIgnoreCase("true")) {
                    session = Session.getInstance(props, new javax.mail.Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(props.getProperty("username"),
                                    props.getProperty("password"));
                        }
                    });
                }
            } catch (Exception e) {
                session = Session.getInstance(props);
            }
            if (to != null) {
                InternetAddress[] addressTo = new InternetAddress[to.length];
                for (int i = 0; i < to.length; i++) {
                    addressTo[i] = new InternetAddress(to[i]);
                }

                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(from));
                message.setRecipients(Message.RecipientType.TO, addressTo);
                message.setSubject(sub);
                message.setContent(msg, "text/html");
                Transport.send(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String[] to = {"rgangani@tibco.com"};
        sendMail("Test", "test", to, "testlinkauto@tibco.com");
    }
}
