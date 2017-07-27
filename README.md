Daily email reminder for credit card related info.

To run:

* Package as jar:
  - intellij: File -> project structure, Project settings -> artifacts,
    add -> jar -> from modules and dependencies
  - Select the main class, set the Manifest File to root directory(yy.credit_alert),
    don’t use the default path, it’s doesn’t work.  Select checkbox “Build on make”.
  - Build -> build artifacts -> build, it will output to “out/artifacts/yy_credit_alert_jar”

* Run the jar: "java -jar yy.credit_alert.jar"

* Config: Currently the config file path is hardcoded:
  - "/Users/ychai/credit_alert_data"
  - Drop the predefined config file "credit.txt", "union.txt" to the directory.
  - "log.txt": keep last time run timestamp, "lock" is used to make sure only one instance is running
  - Now the run interval is hard coded in code, 1 hour in exp, will be set to 24 hours later.
* Create the cron job, and bingo!

* Execution flow:
  - Try to create lock file, if failed, skip the run
  - Compare if last time run is 24 hours before, if not, skip
  - Parse the config file of activity, compose the activity content and call mail sender
  - Mail sender will try different smtp account to send the mail, and retry 3 times.  If still fail,
 the mail content will be cached and wait for a while to resend.