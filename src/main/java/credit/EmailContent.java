package credit;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by ychai on 7/27/17.
 */
public class EmailContent {
    public String receiver;
    public String subject;
    public String body;

    public EmailContent(String rec, String sub, String bod) {
        receiver = rec;
        subject = sub;
        body = bod;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("receiver: " + receiver + "\n");
        sb.append("subject: " + subject + "\n");
        sb.append("body: " + body + "\n");
        return sb.toString();
    }

    public String fingerprint() {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update((receiver + subject + body).getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : md.digest()) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

}
