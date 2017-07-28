package credit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Created by ychai on 7/27/17.
 */
public class BEService {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

    List<MailSender> senders = Util.initSenders();;

    Map<String, EmailContent> emailCache = new HashMap<>();

    boolean sendByMailSender(String receiver, String subject, String body) {
        EmailContent ec = new EmailContent(receiver, subject, body);

        int maxTry = 3;
        Collections.shuffle(senders);
        for (MailSender ms : senders) {
            if (ms.send(ec)) {
                return true;
            }
            if (--maxTry == 0) {
                break;
            }
        }

        System.out.println("Send fail, add to cache for later retry: " + ec.subject);

        // Save to cache and wait for next time's send.
        emailCache.put(ec.fingerprint(), ec);

        return false;
    }

    // false: nothing to send at this round.
    // true: still something to send out at this round.
    boolean trySendCacheContent() {
        //System.out.println("handle mail in cache: " + emailCache.size());
        if (emailCache.isEmpty()) {
            return false;
        }

        List<EmailContent> le = new ArrayList<>();
        for (EmailContent ec : emailCache.values()) {
            le.add(ec);
        }
        for (EmailContent ec : le) {
            if (sendByMailSender(ec.receiver, ec.subject, ec.body)) {
                System.out.println("Resend cache succeed: " + ec.subject);
                emailCache.remove(ec.fingerprint());
            }
        }

        return emailCache.isEmpty();
    }

    public Calendar getCalendar() {
        return Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
    }

    public void run() throws Exception {
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+8"));

        final String checkpointFileName = "/Users/ychai/credit_alert_data/log.txt";

        File f = new File(checkpointFileName);
        if (!f.exists()) {
            f.createNewFile();
        }
        BufferedReader br = new BufferedReader(new FileReader(checkpointFileName));
        String line;
        long currentTimestamp = System.currentTimeMillis() / 1000;
        if ((line = br.readLine()) != null) {
            // timestamp in seconds.
            long lastProcessTimestamp = Long.parseLong(line);
            // Send not long before, skip this round.
            // Send interval: 1 hour
            if (currentTimestamp - lastProcessTimestamp <= 60 * 60 * 4) {
                System.out.format("Skip this round, current ts: %d, saved ts: %d%n", currentTimestamp, lastProcessTimestamp);
                return;
            }
        }


        try {
            System.out.println("start to handleUnionReminder test.");
            handleUnionReminder();
            System.out.println("start to handleCreditCardReminder test.");
            handleCreditCardReminder();
        } catch (IOException e) {
            e.printStackTrace();
        }

        int retryCacheCount = 3;
        boolean succeed = false;
        System.out.println(succeed);
        while (!succeed && retryCacheCount-- != 0) {
            if (!trySendCacheContent()) {
                Thread.sleep(1000 * 60 * 15);
                continue;
            }
            succeed = true;
        }

        System.out.println(succeed);

        if (succeed) {
            System.out.println("Update checkpoint file.");
            PrintWriter pw = new PrintWriter(checkpointFileName, "UTF-8");
            pw.write(String.valueOf(currentTimestamp));
            pw.flush();
            pw.close();
        }
    }

    private void handleUnionReminder() throws IOException {
        List<String> buffer = new ArrayList<>();
        List<UnionActivity> unionActivities = new ArrayList<>();
        for (String line : Util.readFileContent(new FileInputStream(new File("/Users/ychai/credit_alert_data/union.txt")))) {
            if (line.startsWith("#")) {
                continue;
            }

            if (line.isEmpty() && !buffer.isEmpty()) {
                unionActivities.add(generateUnionActivity(buffer));
                buffer.clear();
                continue;
            }

            if (!line.isEmpty()) {
                buffer.add(line);
            }
        }

        if (!buffer.isEmpty()) {
            unionActivities.add(generateUnionActivity(buffer));
        }

        for (UnionActivity activity : unionActivities) {
            sendNotification(activity);
        }
    }

    private void sendNotification(UnionActivity activity) {
        DateFormat df = new SimpleDateFormat("yyyyMMdd");
        try {
            Date startDate = df.parse(activity.startDate);
            Date endDate = df.parse(activity.endDate);
            Calendar endCal = getCalendar();
            endCal.setTime(endDate);
            endCal.add(Calendar.DATE, 1);

            Calendar cal = getCalendar();
            cal.setTime(startDate);

            while (cal.before(endCal)) {
                // Skip useless entries.
                if ((cal.getTimeInMillis() - System.currentTimeMillis())/1000 >= 60 * 60 * 24) {
                    cal.add(Calendar.DATE, 1);
                    continue;
                }

                for (String activeTime : activity.activeTime) {
                    if (isActiveDate(cal, activeTime)) {
                        for (String remindTime : activity.remindTime) {
                            int diff = Integer.parseInt(remindTime.split(" ")[0]);
                            String sendTimeStr = remindTime.split(" ")[1].trim();
                            Calendar candidateCal = (Calendar) cal.clone();
                            Calendar today = getCalendar();
                            candidateCal.add(Calendar.DATE, diff);
                            if (candidateCal.get(Calendar.DATE) == today.get(Calendar.DATE) &&
                                    candidateCal.get(Calendar.MONTH) == today.get(Calendar.MONTH)) {
                                if (Util.testMode || (int) today.get(Calendar.HOUR_OF_DAY) ==
                                        Integer.parseInt(sendTimeStr)) {
                                    boolean send = false;
                                    for (String receiver : activity.receivers) {
                                        send = true;
                                        sendByMailSender(receiver, activity.title, activity.description);
                                    }
                                    if (send) {
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
                cal.add(Calendar.DATE, 1);
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private boolean isActiveDate(Calendar cal, String activeTime) {
        DateFormat df = new SimpleDateFormat("yyyyMMdd");

        {
            // Treat it as day of week.
            String dayOfWeek = activeTime.split(" ")[0];
            // 1-Sun, 2-Mon, 3-Tue, 4-Wed, 5-Thur, 6-Fri, 7-Sat
            int dow = cal.get(Calendar.DAY_OF_WEEK);
            boolean ret = dow == 1 & dayOfWeek.equals("Sun") ||
                    dow == 2 & dayOfWeek.equals("Mon") ||
                    dow == 3 & dayOfWeek.equals("Tue") ||
                    dow == 4 & dayOfWeek.equals("Wed") ||
                    dow == 5 & dayOfWeek.equals("Thur") ||
                    dow == 6 & dayOfWeek.equals("Fri") ||
                    dow == 7 & dayOfWeek.equals("Sat");
            if (ret == true) {
                return ret;
            }
        }

        {
            try {
                // Treat it as date
                int date = Integer.parseInt(activeTime.split(" ")[0]);
                // 1-Sun, 2-Mon, 3-Tue, 4-Wed, 5-Thur, 6-Fri, 7-Sat
                int d = cal.get(Calendar.DATE);
                boolean ret = date == d;
                if (ret == true) {
                    return ret;
                }
            } catch (Exception e) {
                // By pass this case.
            }
        }

        {
            if (activeTime.split(" ")[0].equals("*")) {
                return true;
            }
        }

        return false;
    }

    private UnionActivity generateUnionActivity(List<String> buffer) {
        UnionActivity ret = new UnionActivity();
        ret.startDate = buffer.get(0).split("-")[0];
        ret.endDate = buffer.get(0).split("-")[1];
        ret.activeTime = Arrays.asList(buffer.get(1).split(","));
        ret.remindTime = Arrays.asList(buffer.get(2).split(","));
        ret.receivers = Arrays.asList(buffer.get(3).split(","));
        ret.title = buffer.get(4);
        for (int i = 5; i < buffer.size(); ++i) {
            ret.description += buffer.get(i) + "\n";
        }

        return ret;
    }

    // Get the nearest bill date in Calendar format from the given billDate
    private Calendar getNextBillDateInCalendarFormat(String billDateString) {
        return getPrevBillDateInCalendarFormatImpl(billDateString, 1);
    }

    private Calendar getPrevBillDateInCalendarFormat(String billDateString) {
        return getPrevBillDateInCalendarFormatImpl(billDateString, -1);
    }

    private Calendar getPrevBillDateInCalendarFormatImpl(String billDateString, int inc) {
        Calendar current = getCalendar();
        int billDate = Integer.parseInt(billDateString);
        while (true) {
            int dayOfMonth = current.get(Calendar.DAY_OF_MONTH);
            if (dayOfMonth == billDate) {
                break;
            }
            current.add(Calendar.DATE, inc);
        }

        return current;
    }

    private void handleCreditCardReminder() throws IOException {
        List<CreditCard> cards = new ArrayList<>();
        List<CreditCard> cardsToPay = new ArrayList<>();
        Calendar today = getCalendar();
        today.setTimeInMillis(System.currentTimeMillis());

        for (String line : Util.readFileContent(new FileInputStream(new File("/Users/ychai/credit_alert_data/credit.txt")))) {
            CreditCard cardInfo = extractCreditCardInformation(line.trim());

            Calendar firstPayDate = getNearestPayDateOfGivenBillDate(
                    getPrevBillDateInCalendarFormat(cardInfo.billDate), cardInfo.payInterval);
            Calendar secondPayDate = getNearestPayDateOfGivenBillDate(
                    getNextBillDateInCalendarFormat(cardInfo.billDate), cardInfo.payInterval);

            cardInfo.timeForFree = getDifferenceDays(today.getTime(), secondPayDate.getTime());
            cardInfo.setTimeToPay(getDifferenceDays(today.getTime(), firstPayDate.getTime()));

            if (cardInfo.timeToPay <= 2 && cardInfo.timeToPay != 0) {
                cardsToPay.add(cardInfo);
            }

            cards.add(cardInfo);
        }

        Collections.sort(cards, new Comparator<CreditCard>() {
            @Override
            public int compare(CreditCard o1, CreditCard o2) {
                return (int)(o1.timeToPay - o2.timeToPay);
            }});

        for (Map.Entry<String, StringBuffer> entry : generateMail(cards).entrySet()) {
            sendByMailSender(entry.getKey(), "到期还款日", entry.getValue().toString());
        }

        Collections.sort(cards, new Comparator<CreditCard>() {
            @Override
            public int compare(CreditCard o1, CreditCard o2) {
                return (int)(o2.timeForFree - o1.timeForFree);
            }});

        for (Map.Entry<String, StringBuffer> entry : generateMail(cards).entrySet()) {
            sendByMailSender(entry.getKey(), "免息期", entry.getValue().toString());
        }

        for (Map.Entry<String, StringBuffer> entry : generateSMS(cardsToPay).entrySet()) {
            sendByMailSender(entry.getKey(), "还款提醒", entry.getValue().toString());
        }
    }

    private Map<String, StringBuffer> generateSMS(List<CreditCard> cards) {
        Map<String, StringBuffer> ret = new HashMap<>();
        for (CreditCard card : cards) {
            Set<String> receivers = new HashSet<>();
            receivers.addAll(card.emailAddress);
            receivers.addAll(card.smsAddress);
            for (String mail : new ArrayList<>(receivers)) {
                if (ret.containsKey(mail)) {
                    ret.get(mail).append(card.toString() + "\n");
                } else {
                    ret.put(mail, new StringBuffer(card.toString() + "\n"));
                }
            }
        }
        return ret;
    }

    private Map<String, StringBuffer> generateMail(List<CreditCard> cards) {
        Map<String, StringBuffer> ret = new HashMap<>();
        for (CreditCard card : cards) {
            for (String mail : card.emailAddress) {
                if (ret.containsKey(mail)) {
                    ret.get(mail).append(card.toString() + "\n");
                } else {
                    ret.put(mail, new StringBuffer(card.toString() + "\n"));
                }
            }
        }
        return ret;
    }

    private CreditCard extractCreditCardInformation(String line) {
        String[] fields = line.split("#");
        CreditCard creditCard = new CreditCard();
        creditCard.bankName = fields[0];
        creditCard.cardNumber = fields[1];
        creditCard.billDate = fields[2];
        creditCard.payInterval = fields[3];
        creditCard.holderName = fields[4];
        creditCard.emailAddress = Arrays.asList(fields[5].split(","));
        creditCard.smsAddress = Arrays.asList(fields[6].split(","));
        return creditCard;
    }

    public static long getDifferenceDays(Date d1, Date d2) {
        if (d2.getTime() - d1.getTime() < 0) {
            return -137;
        }

        return TimeUnit.DAYS.convert(d2.getTime() - d1.getTime(), TimeUnit.MILLISECONDS);
    }

    // Given the calendar, then get the real payDay by payDay(20, or +20)
    private Calendar getNearestPayDateOfGivenBillDate(Calendar billDate, String payInterval) {
        // Just count
        if (payInterval.startsWith("+")) {
            int increment = Integer.parseInt(payInterval.substring(1));
            while (increment-- != 0) {
                billDate.add(Calendar.DATE, 1);
            }
            return billDate;
        } else {
            while (true) {
                int d = Integer.parseInt(payInterval);
                if ((int)billDate.get(Calendar.DAY_OF_MONTH) == d) {
                    return billDate;
                }
                billDate.add(Calendar.DATE, 1);
            }
        }
    }
}
