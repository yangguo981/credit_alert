package credit;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Calendar;
import java.util.Properties;
import java.util.TimeZone;

/**
 * Created by ychai on 7/27/17.
 */
public class MailSender {
    String user;
    String password;
    Properties props;
    int delayInterval;
    final int hourlyMaxSendLimit;
    int hourlySendCount;
    int lastSendHour;

    public String toString() {
        return user + " " + hourlySendCount;
    }

    public MailSender(Properties props, int delayInterval, int hourlyMaxSendLimit, String user,
                      String password) {
        this.props = props;
        this.hourlyMaxSendLimit = hourlyMaxSendLimit;
        this.delayInterval = delayInterval;
        lastSendHour = -1;
        this.user = user;
        this.password = password;
        this.hourlySendCount = 0;
    }

    private boolean isValid() {
        int currentHour = getCalendar().get(Calendar.HOUR_OF_DAY);

        System.out.println("is valid: " + hourlySendCount + " " + hourlyMaxSendLimit);
        // A new day comes, clear all the status and prepare to send, wow!
        if (currentHour != lastSendHour) {
            System.out.println("reset is valid counter");
            hourlySendCount = 0;
            lastSendHour = currentHour;
            return true;
        }

        if (hourlySendCount < hourlyMaxSendLimit) {
            return true;
        }

        return false;
    }

    public boolean send(EmailContent ec) {
        return send(ec.receiver, ec.subject, ec.body);
    }

    public boolean send(String receiver, String subject, String body) {
        if (!isValid()) {
            System.out.println("no valid smtp server available this hour");
            return false;
        }

        try {
            //System.out.println("delay before send action. " + subject + " " + user + " " + receiver);
            if (!Util.testMode) {
                Thread.sleep(1000 * delayInterval);
            }
        } catch (InterruptedException e) {
        }

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(user, password);
                    }
                });
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(user, "yy"));
            message.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(receiver));
            message.setSubject(subject);
            message.setText(body);
            if (!Util.testMode) {
                Transport.send(message);
                //if (Math.random() < 0.1) {
                //    throw new Exception();
                //}
            }
            System.out.println("mail send succeed: " + subject + " " + user + " " + receiver);
            System.out.println("mail content: " + body);
            hourlySendCount = hourlySendCount + 1;
        } catch (Exception e) {
            System.out.println("failed to send mail: " + subject + " " + user + " " + receiver);
            return false;
        } finally {
            // Increase the amount no matter fail or success.
            // hourlySendCount = hourlySendCount + 1;
        }

        return true;
    }

    public Calendar getCalendar() {
        return Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
    }
}
