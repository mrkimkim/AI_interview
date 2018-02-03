package portfolio.projects.mrkimkim.ai_interview;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubeStandalonePlayer;

import java.util.ArrayList;

import portfolio.projects.mrkimkim.ai_interview.DBHelper.DBHelper;
import portfolio.projects.mrkimkim.ai_interview.InterviewModule.StartInterviewActivity;
import portfolio.projects.mrkimkim.ai_interview.Utils.item_question;
import us.feras.mdv.MarkdownView;

/**
 * Created by mrkimkim on 2017-12-06.
 */

public class ActivityChooseQuestion extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener {
    private static final int RECOVERY_REQUEST = 1;
    private static final String YOUTUBE_API_KEY = "AIzaSyCsBC6KFCzSWwA9qy7byEZ7tElx0EEjHos";
    private static final String VIDEO_ID = "hOLVzFm-nPk";
    Context mContext;

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    Adapter_Question mAdapter_Question;
    ArrayList items_question;

    int category_idx;

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player, boolean wasRestored) {
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult errorReason) {
        if (errorReason.isUserRecoverableError()) {
            errorReason.getErrorDialog(this, RECOVERY_REQUEST).show();
        } else {
            String error = String.format(getString(R.string.player_error), errorReason.toString());
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    }


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choosequestion);

        Intent intent = getIntent();
        category_idx = intent.getIntExtra("category_idx", 0);

        items_question = new ArrayList<item_question>();

        recyclerView = (RecyclerView)findViewById(R.id.cq_recycler_view);
        recyclerView.setHasFixedSize(true);
        layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);

        _LoadQuestion();
    }


    private void _LoadQuestion() {
        class t_LoadQuestion implements Runnable {
            @Override
            public void run() {
                DBHelper mDBhelper = DBHelper.getInstance(getApplicationContext());
                ContentValues[] values = mDBhelper.select("Question", DBHelper.column_questioninfo, "category_idx=?", new String[]{String.valueOf(category_idx)}, null, null, null);

                for(int i = 0; i < values.length; ++i) {
                    items_question.add(new item_question(values[i].getAsInteger("idx"),
                            values[i].getAsInteger("category_idx"),
                            values[i].getAsString("title"),
                            values[i].getAsString("history"),
                            values[i].getAsString("duration"),
                            values[i].getAsString("src_lang"),
                            values[i].getAsString("dest_lang"),
                            values[i].getAsString("price"),
                            values[i].getAsString("view_cnt"),
                            values[i].getAsString("like_cnt"),
                            values[i].getAsString("markdown_uri")));
                }

                mAdapter_Question = new Adapter_Question(items_question, mContext);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(mAdapter_Question);
            }
        }
        Runnable t = new t_LoadQuestion();
        t.run();
    }

    public class Adapter_Question extends RecyclerView.Adapter<Adapter_Question.ViewHolder> {
        private Context context;
        private ArrayList<item_question> mItems;

        public Adapter_Question(ArrayList items, Context mContext) {
            mItems = items;
            context = mContext;
        }

        @Override
        public Adapter_Question.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_question, parent, false);
            Adapter_Question.ViewHolder holder = new Adapter_Question.ViewHolder(v);
            return holder;
        }

        @Override
        public void onBindViewHolder(Adapter_Question.ViewHolder holder, int position) {
            int idx = position % 5;
            // 카드 배경 색상 지정
            if (idx == 0) holder.card.setCardBackgroundColor(getResources().getColor(R.color.category_card_background_0));
            else if (idx == 1) holder.card.setCardBackgroundColor(getResources().getColor(R.color.category_card_background_1));
            else if (idx == 2) holder.card.setCardBackgroundColor(getResources().getColor(R.color.category_card_background_2));
            else if (idx == 3) holder.card.setCardBackgroundColor(getResources().getColor(R.color.category_card_background_3));
            else holder.card.setCardBackgroundColor(getResources().getColor(R.color.category_card_background_4));

            // 카드 타이틀
            holder.tv_title.setText(mItems.get(position).getTitle());
            holder.tv_title.setMaxLines(2);
            holder.tv_title.setEllipsize(TextUtils.TruncateAt.END);

            // 디스크립션 정보
            String description = mItems.get(position).getDuration() + "  |  " + mItems.get(position).getSrc_lang() + "  |  " + mItems.get(position).getPrice();
            holder.tv_description.setText(description);

            // 출처 정보
            holder.tv_history.setText(mItems.get(position).getHistory());

            // 뷰 카운트와 좋아요 수
            holder.tv_totalView.setText(mItems.get(position).getView_cnt());
            holder.tv_totalLike.setText(mItems.get(position).getLike_cnt());
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

        public void refreshItems(ArrayList items) {
            mItems = items;
            notifyDataSetChanged();
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            public CardView card;
            public TextView tv_title, tv_description, tv_history, tv_totalView, tv_totalLike;
            public Button btn_guide, btn_viewQuestion;

            public ViewHolder(View view) {
                super(view);
                card = (CardView)view.findViewById(R.id.question_card);

                tv_title = (TextView)view.findViewById(R.id.question_title);
                tv_description = (TextView)view.findViewById(R.id.question_description);
                tv_history = (TextView)view.findViewById(R.id.question_history);

                tv_totalView = (TextView)view.findViewById(R.id.question_totalView);
                tv_totalLike = (TextView)view.findViewById(R.id.question_totalLike);

                btn_guide = (Button)view.findViewById(R.id.question_card_left_btn_guide);
                btn_viewQuestion = (Button)view.findViewById(R.id.question_card_left_btn_viewQuestion);

                card.setOnClickListener(this);
                btn_guide.setOnClickListener(this);
                btn_viewQuestion.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                // 문제 보기를 클릭한 경우
                if (v.getId() == R.id.question_card_left_btn_viewQuestion) {
                    Log.d("This", "Clicked");
                    // 클릭된 개체를 받아옴
                    item_question instance = mItems.get(getAdapterPosition());

                    // 마크다운 URI를 얻음
                    String markdown_uri = instance.getMarkdown_uri();

                    Dialog dialog = new Dialog(ActivityChooseQuestion.this);
                    dialog.setContentView(R.layout.dialog_question);
                    dialog.setTitle("문제 보기");

                    MarkdownView markdownView = (MarkdownView) dialog.findViewById(R.id.markdown_view);
                    markdownView.loadMarkdown("***\n" +
                            "### 면접 시간 (60초)\n\n" +
                            "***\n" +
                            "###문제\n" +
                            "간단하게 지원동기와 자기소개에 대하여 말해보시오.\n\n" +
                            "***");

                    dialog.show();
                }

                // 샘플 가이드 영상을 클릭한 경우
                else if (v.getId() == R.id.question_card_left_btn_guide) {
                    Intent intent = YouTubeStandalonePlayer.createVideoIntent(ActivityChooseQuestion.this, YOUTUBE_API_KEY, VIDEO_ID);
                    startActivity(intent);
                }

                else if (v.getId() == R.id.question_card) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ActivityChooseQuestion.this);
                    builder.setTitle("면접 시작");
                    builder.setMessage("1 크레딧을 차감하여 면접을 시작하시겠습니까?");
                    builder.setPositiveButton("예",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Toast.makeText(getApplicationContext(),"면접장으로 이동합니다.",Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(ActivityChooseQuestion.this, StartInterviewActivity.class);
                                    intent.putExtra("question_idx", mItems.get(getAdapterPosition()).getidx());
                                    startActivity(intent);
                                }
                            });
                    builder.setNegativeButton("아니오",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            });
                    builder.show();
                }
            }
        }
    }
}
