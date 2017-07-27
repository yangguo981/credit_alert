package credit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by ychai on 7/27/17.
 */
public class Util {
    public static List<String> readFileContent(InputStream is) {
        List<String> ret = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF8"));
            String line = reader.readLine();
            while (line != null) {
                ret.add(line);
                line = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static List<MailSender> initSenders() {
        List<MailSender> senders = new ArrayList<>();

        {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.126.com");
            props.put("mail.smtp.port", "25");
            //MailSender ms = new MailSender(props, 20, 50, "yypublic@126.com", "shouquanma12345");
            //MailSender ms = new MailSender(props, 2, 100, "yypublic@126.com", "shouquanma12345");
            MailSender ms = new MailSender(props, 20, 10, "yypublic@126.com", "shouquanma12345");
            senders.add(ms);
        }

        {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.126.com");
            props.put("mail.smtp.port", "25");
            //MailSender ms = new MailSender(props, 20, 50, "yangguo981@126.com", "shouquanma12345");
            //MailSender ms = new MailSender(props, 2, 100, "yangguo981@126.com", "shouquanma12345");
            MailSender ms = new MailSender(props, 20, 10, "yangguo981@126.com", "shouquanma12345");
            senders.add(ms);
        }

        {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.163.com");
            props.put("mail.smtp.port", "25");
            //MailSender ms = new MailSender(props, 20, 50, "yangguo981@163.com", "shouquanma12345");
            //MailSender ms = new MailSender(props, 2, 100, "yangguo981@163.com", "shouquanma12345");
            MailSender ms = new MailSender(props, 20, 10, "yangguo981@163.com", "shouquanma12345");
            senders.add(ms);
        }

        {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.qq.com");
            props.put("mail.smtp.port", "25");
            //MailSender ms = new MailSender(props, 45, 50, "29066812@qq.com", "lbixzcdcbrsobjhf");
            //MailSender ms = new MailSender(props, 2, 100, "29066812@qq.com", "lbixzcdcbrsobjhf");
            MailSender ms = new MailSender(props, 45, 10, "29066812@qq.com", "lbixzcdcbrsobjhf");
            senders.add(ms);
        }

        {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.qq.com");
            props.put("mail.smtp.port", "25");
            //MailSender ms = new MailSender(props, 45, 50, "24463066@qq.com", "ubaizspgqqkobgbb");
            //MailSender ms = new MailSender(props, 2, 100, "24463066@qq.com", "ubaizspgqqkobgbb");
            MailSender ms = new MailSender(props, 45, 10, "24463066@qq.com", "ubaizspgqqkobgbb");
            senders.add(ms);
        }

        // 64430648@qq.com 待激活

        {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.qq.com");
            props.put("mail.smtp.port", "25");
            //MailSender ms = new MailSender(props, 45, 50, "14872736@qq.com", "mpahebappkdsbiad");
            //MailSender ms = new MailSender(props, 2, 100, "14872736@qq.com", "mpahebappkdsbiad");
            MailSender ms = new MailSender(props, 45, 10, "14872736@qq.com", "mpahebappkdsbiad");
            senders.add(ms);
        }

//        {
//            Properties props = new Properties();
//            props.put("mail.smtp.auth", "true");
//            props.put("mail.smtp.starttls.enable", "true");
//            props.put("mail.smtp.host", "smtp.189.cn");
//            props.put("mail.smtp.port", "25");
//            MailSender ms = new MailSender(props, 30, 10, "13321973119@189.cn", "the common string");
//            senders.add(ms);
//        }

        return senders;
    }

    public static boolean testMode = false;
}