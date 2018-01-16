package portfolio.projects.mrkimkim.ai_interview.Utils;

/**
 * Created by 킹조 on 2018-01-09.
 */

public class item_question {
    int idx, category_idx;
    String title, history;
    String duration, language, price;
    String total_view, total_like;
    String markdown_uri;

    public int getidx() { return this.idx;}
    public int getCategory_idx_idx() { return this.category_idx;}
    public String getTitle() { return this.title; }
    public String getHistory() { return this.history; }
    public String getDuration() { return this.duration; }
    public String getLanguage() { return this.language; }
    public String getPrice() { return this.price; }
    public String getTotal_view() { return this.total_view; }
    public String getTotal_like() { return this.total_like; }
    public String getMarkdown_uri() { return this.markdown_uri; }
    public item_question(int idx, int category_idx, String title, String history, String duration, String language, String price, String total_view, String total_like, String markdown_uri) {
        this.idx= idx;
        this.category_idx = idx;

        this.title = title;
        this.history = history;

        this.duration = duration;
        this.language = language;
        this.price = price;

        this.total_view = total_view;
        this.total_like = total_like;

        this.markdown_uri = markdown_uri;
    }
}
