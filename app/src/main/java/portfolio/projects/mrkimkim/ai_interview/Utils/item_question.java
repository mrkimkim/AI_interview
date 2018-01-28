package portfolio.projects.mrkimkim.ai_interview.Utils;

/**
 * Created by 킹조 on 2018-01-09.
 */

public class item_question {
    int idx, category_idx;
    String title, history;
    String duration, src_lang, dest_lang, price;
    String view_cnt, like_cnt;
    String markdown_uri;

    public int getidx() { return this.idx;}
    public int getCategoryIdx() { return this.category_idx;}
    public String getTitle() { return this.title; }
    public String getHistory() { return this.history; }
    public String getDuration() { return this.duration; }
    public String getSrc_lang() { return this.src_lang; }
    public String getDest_lang() { return this.dest_lang;}
    public String getPrice() { return this.price; }
    public String getView_cnt() { return this.view_cnt; }
    public String getLike_cnt() { return this.like_cnt; }
    public String getMarkdown_uri() { return this.markdown_uri; }

    public item_question(int idx, int category_idx, String title, String history, String duration, String src_lang, String dest_lang, String price, String view_cnt, String like_cnt, String markdown_uri) {
        this.idx= idx;
        this.category_idx = idx;

        this.title = title;
        this.history = history;

        this.duration = duration;
        this.src_lang = src_lang;
        this.dest_lang = dest_lang;
        this.price = price;

        this.view_cnt = view_cnt;
        this.like_cnt = like_cnt;

        this.markdown_uri = markdown_uri;
    }
}
