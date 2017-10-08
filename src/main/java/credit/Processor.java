package credit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import static credit.Util.getAbsoluteFilePath;
import static credit.Util.parentDirectory;

/**
 * Created by ychai on 7/27/17.
 */
public class Processor {
    public static void main(String[] args) throws IOException {
//        final String lockFileName = getAbsoluteFilePath("lock");
        // Hack, pass the arguments everywhere.  Should use singleton mode.
        Util.parentDirectory = args[0];
        Util.sendMailIntervalHour = Integer.parseInt(args[1]);

        final String lockFileName = getAbsoluteFilePath("lock");
        System.out.println(lockFileName);

        File lockFile = new File(lockFileName);
        if (!lockFile.createNewFile()) {
            // Create test file with specified timestamp: touch -t 201710061111 lock
            long minutes = (System.currentTimeMillis() - lockFile.lastModified()) / (1000 * 60);
            if (minutes > 60 * 1) {
                // TODO, find a way to clear the existing bad process.
                // The lock has been hold for too long time, something goes wrong.
                // Here we just run another processor directly and clear the lock, but the bad process is still running.
                System.out.println("Some thread is working too long");
                System.out.println("Delete existing lock file, waiting for next clean schedule");
                lockFile.delete();
            } else {
                System.out.println("Another thread is processing, skip this round.");
            }

            // Anyway, if lock file can't be created this round, just skip.  As the cron job will run every minute.
            return;
        }

        try {
            new BEService().run();
        } catch (Exception e) {
            System.out.println("Exception happens in BEService.run, fuck");
            e.printStackTrace();
        }

        System.out.println("Deleting lock file.");
        lockFile.delete();
    }
}
