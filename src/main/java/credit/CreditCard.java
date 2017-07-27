package credit;

import java.util.List;

/**
 * Created by ychai on 7/27/17.
 */
public class CreditCard {
    //兴业#8101#1#+20#柴阳阳#yangguo981@126.com#13774317710@139.com,13321973119@189.cn
    public String bankName;
    public String cardNumber;
    public String billDate;
    public String payInterval;
    public String holderName;
    public List<String> emailAddress;
    public List<String> smsAddress;
    public long timeToPay;
    public long timeForFree;
    public void setTimeToPay(long t) {
        if (t < 0) {
            timeToPay = 9999;
        } else {
            timeToPay = t;
        }
    }
    public String toString() {
        StringBuffer s = new StringBuffer();
        s.append(bankName + " " + cardNumber + " " + holderName + "\n");
        s.append("还款到期:" + timeToPay + " 天\n");
        s.append("免息期:" + timeForFree + " 天\n");
        return s.toString();
    }
}
