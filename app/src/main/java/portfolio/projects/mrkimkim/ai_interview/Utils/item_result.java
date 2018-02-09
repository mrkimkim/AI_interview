package portfolio.projects.mrkimkim.ai_interview.Utils;

/**
 * Created by 킹조 on 2018-01-03.
 */

public class item_result {
    long idx, user_idx;
    String video_path, emotion_path, stt_path, pitch_path;
    long task_idx, result_idx, question_idx;

    String title, history;
    String duration;
    String src_lang, dest_lang;
    String view_cnt, like_cnt;
    String markdown_uri;

    public long getIdx() { return this.idx;}
    public long getUser_idx() { return this.user_idx;}
    public String getVideo_path() { return this.video_path;}
    public String getEmotion_path() { return this.emotion_path;}
    public String getStt_path() { return this.stt_path;}
    public String getPitch_path() { return this.pitch_path; }
    public long getTask_idx() { return this.task_idx;}
    public long getResult_idx() { return this.result_idx;}
    public long getQuestion_idx() { return this.question_idx;}

    public String getTitle() { return this.title;}
    public String getHistory() { return this.history;}
    public String getDuration() { return this.duration;}
    public String getSrc_lang() { return this.src_lang;}
    public String getDest_lang() { return this.dest_lang;}
    public String getView_cnt() { return this.view_cnt;}
    public String getLike_cnt() { return this.like_cnt;}
    public String getMarkdown_uri() { return this.markdown_uri;}

    public void setTask_idx(long task_idx) { this.task_idx = task_idx; }
    public void setResult_idx(long result_idx) { this.result_idx = result_idx; }

    public item_result(long idx, long user_idx, String video_path, String emotion_path, String stt_path, String pitch_path,
                       long task_idx, long result_idx, long question_idx,
                       String title, String history, String duration, String src_lang, String dest_lang, String view_cnt, String like_cnt, String markdown_uri) {

        this.idx = idx;
        this.user_idx = user_idx;
        this.video_path = video_path;
        this.emotion_path = emotion_path;
        this.stt_path = stt_path;
        this.pitch_path = pitch_path;
        this.task_idx = task_idx;
        this.result_idx = result_idx;
        this.question_idx = question_idx;

        this.title = title;
        this.history = history;
        this.duration = duration;
        this.src_lang = src_lang;
        this.dest_lang = dest_lang;
        this.view_cnt = view_cnt;
        this.like_cnt = like_cnt;
        this.markdown_uri = markdown_uri;
    }
}