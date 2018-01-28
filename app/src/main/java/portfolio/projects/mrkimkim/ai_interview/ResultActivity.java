package portfolio.projects.mrkimkim.ai_interview;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;
import java.util.ArrayList;

import portfolio.projects.mrkimkim.ai_interview.DBHelper.DBHelper;
import portfolio.projects.mrkimkim.ai_interview.Utils.LoadingDialog;
import portfolio.projects.mrkimkim.ai_interview.Utils.item_question;
import portfolio.projects.mrkimkim.ai_interview.Utils.item_result;

public class ResultActivity extends AppCompatActivity {
    Context mContext;
    RecyclerView recyclerView;
    ResultAdapter Adapter;
    RecyclerView.LayoutManager layoutManager;

    LoadingDialog loadingDialog;
    ArrayList items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // RecyclerView
        mContext = getApplicationContext();
        recyclerView = (RecyclerView)findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);

        // DB에서 InterviewData를 로드함
        _LoadResult();
    }

    private void _LoadResult() {
        class t_LoadResult implements Runnable {
            @Override
            public void run() {
                items = new ArrayList<>();

                DBHelper mDBHelper = DBHelper.getInstance(getApplicationContext());
                ContentValues[] values = mDBHelper.select("InterviewData", DBHelper.column_interviewdata, null, null, null, null, null);
                for (int i = 0; i < values.length; ++i) {

                    // InterviewData를 로드.
                    long idx = values[i].getAsLong("idx");
                    long user_idx = 0;
                    //long user_idx = values[i].getAsLong("user_idx");
                    String video_path = values[i].getAsString("video_path");
                    String emotion_path = values[i].getAsString("emotion_path");
                    String stt_path = values[i].getAsString("stt_path");
                    long task_idx = values[i].getAsLong("task_idx");
                    long result_idx = values[i].getAsLong("result_idx");
                    long question_idx = values[i].getAsLong("question_idx");

                    // Question 데이터를 로드함
                    ContentValues[] question = mDBHelper.select("Question", DBHelper.column_questioninfo, "idx=?", new String[]{String.valueOf(question_idx)}, null, null, null);
                    if (question.length > 0) {
                        String title = question[0].getAsString("title");
                        String history = question[0].getAsString("history");
                        String duration = question[0].getAsString("duration");
                        String src_lang = question[0].getAsString("src_lang");
                        String dest_lang = question[0].getAsString("dest_lang");
                        String like_cnt = question[0].getAsString("like_cnt");
                        String view_cnt = question[0].getAsString("view_cnt");
                        String markdown_uri = question[0].getAsString("markdown_uri");
                        items.add(new item_result(idx, user_idx, video_path, emotion_path, stt_path, task_idx, result_idx, question_idx, title, history, duration, src_lang, dest_lang, view_cnt, like_cnt, markdown_uri));
                    }

                }

                Adapter = new ResultAdapter(items, mContext);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(Adapter);
            }
        }
        Runnable t = new t_LoadResult();
        t.run();
    }

    class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.ViewHolder> {
        private Context context;
        private ArrayList<item_result> mItems;

        private int lastPosition = -1;

        public ResultAdapter(ArrayList items, Context mContext) {
            mItems = items;
            context = mContext;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_result, parent, false);
            recyclerView.setMinimumWidth(parent.getMeasuredWidth());
            ViewHolder holder = new ViewHolder(v);
            return holder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.t1.setText(mItems.get(position).getTitle());
            holder.t2.setText(mItems.get(position).getSrc_lang() + " -> " + mItems.get(position).getDest_lang() + "\n"
                    + mItems.get(position).getDuration() + "\n"
                    + mItems.get(position).getHistory());
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            public TextView t1, t2;
            protected ImageButton ib_download, ib_share, ib_delete;

            public ViewHolder(View view) {
                super(view);
                t1 = (TextView) view.findViewById(R.id.text_interview_question);
                t2 = (TextView) view.findViewById(R.id.text_interview_meta);

                ib_download = (ImageButton) view.findViewById(R.id.ib_download);
                ib_share = (ImageButton) view.findViewById(R.id.ib_share);
                ib_delete = (ImageButton) view.findViewById(R.id.ib_delete);

                ib_download.setTag(R.integer.result_ib_download, view);
                ib_share.setTag(R.integer.result_ib_share, view);
                ib_delete.setTag(R.integer.result_ib_delete, view);

                ib_download.setOnClickListener(this);
                ib_share.setOnClickListener(this);
                ib_delete.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                if (v.getId() == ib_download.getId()) {
                    Log.d("Button Click Event", "Download " + String.valueOf(getAdapterPosition()));

                    // Case 1 데이터 파일이 아직 없는 경우

                    // Case 2 데이터 파일이 존재하는 경우


                }
                else if (v.getId() == ib_share.getId()) {
                    Log.d("Button Click Event", "Share " + String.valueOf(getAdapterPosition()));
                }

                else if (v.getId() == ib_delete.getId()) {
                    Log.d("Button Click Event", "Delete " + String.valueOf(getAdapterPosition()));
                }
            }
        }

        private void setAnimation(View viewToAnimate, int position) {
            // 새로 보여지는 뷰라면 애니메이션을 해줍니다
            if (position > lastPosition) {
                Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
                viewToAnimate.startAnimation(animation);
                lastPosition = position; }
        }
    }
}
