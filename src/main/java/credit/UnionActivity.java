package credit;

import java.util.List;

/**
 * Created by ychai on 7/27/17.
 */
public class UnionActivity {
    public String startDate;
    public String endDate;
    public List<String> activeTime;
    public List<String> remindTime;
    public List<String> receivers;
    public String description = "";
    public String title = "";
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("startDate: " + startDate + "\n");
        sb.append("endDate: " + endDate + "\n");
        sb.append("activeTime: " + activeTime + "\n");
        sb.append("remindTime: " + remindTime + "\n");
        sb.append("receivers: " + receivers + "\n");
        sb.append("title: " + title + "\n");
        sb.append("description: " + description + "\n");
        return sb.toString();
    }
}
