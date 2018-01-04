package portfolio.projects.mrkimkim.ai_interview.Utils;

/**
 * Created by 킹조 on 2018-01-03.
 */

public class item_result {
    String interview_question;
    String src_lang, dest_lang;
    String interview_time;
    String interview_category;

    public String getInterview_question() {return interview_question;}
    public String getSrc_lang() { return src_lang;}
    public String getDest_lang() { return dest_lang;}
    public String getInterview_time() { return interview_time;}
    public String getInterview_category() { return interview_category;}

    public item_result(String interview_question, String src_lang, String dest_lang, String interview_time, String interview_category) {
        this.interview_question = interview_question;
        this.src_lang = src_lang;
        this.dest_lang = dest_lang;
        this.interview_time = interview_time;
        this.interview_category = interview_category;
    }
}