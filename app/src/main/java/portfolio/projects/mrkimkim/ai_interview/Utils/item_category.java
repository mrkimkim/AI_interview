package portfolio.projects.mrkimkim.ai_interview.Utils;

/**
 * Created by JHG on 2018-01-04.
 */

public class item_category {
    int image;
    int idx;
    String title, title_describe;
    String n_subcategory, n_probset, n_problem;

    public int getImage() { return this.image; }
    public String gettitle() { return this.title; }
    public String getTitle_describe() { return this.title_describe; }
    public String getN_subcategory() { return this.n_subcategory; }
    public String getN_probset() { return this.n_probset; }
    public String getN_problem() { return this.n_problem; }

    public item_category(int image, int item_category, String title, String title_describe, int n_subcategory, int n_probset, int n_problem) {
        this.image = image;
        this.idx = item_category;
        this.title = title;
        this.title_describe = title_describe;
        this.n_subcategory = String.valueOf(n_subcategory);
        this.n_probset = String.valueOf(n_probset);
        this.n_problem = String.valueOf(n_problem);
    }
}
