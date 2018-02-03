package portfolio.projects.mrkimkim.ai_interview.Utils;

/**
 * Created by 킹조 on 2018-02-02.
 */

public class item_subtitle {
    private String timestamp;
    private String text;

    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public void setText(String text) { this.text = text;}

    public String getTimestamp() { return this.timestamp; }
    public String getText() { return this.text;}

    public item_subtitle(String timestamp, String text) {
        this.timestamp = timestamp;
        this.text = text;
    }
}
