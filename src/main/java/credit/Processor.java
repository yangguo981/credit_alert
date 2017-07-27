package credit;

import java.io.File;

/**
 * Created by ychai on 7/27/17.
 */
public class Processor {
    public static void main(String[] args) throws Exception {
        final String lockFile = "/Users/ychai/credit_alert_data/lock";
        if (!new File(lockFile).createNewFile()) {
            System.out.println("Another thread is processing, skip this round.");
            return;
        }

        new BEService().run();

        System.out.println("Deleting lock file.");
        new File(lockFile).delete();
    }
}
