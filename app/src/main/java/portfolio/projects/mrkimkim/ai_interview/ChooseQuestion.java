package portfolio.projects.mrkimkim.ai_interview;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.roger.catloadinglibrary.CatLoadingView;

import java.util.ArrayList;

import portfolio.projects.mrkimkim.ai_interview.DBHelper.DBHelper;
import portfolio.projects.mrkimkim.ai_interview.InterviewModule.Interview;
import portfolio.projects.mrkimkim.ai_interview.Utils.item_category;
import portfolio.projects.mrkimkim.ai_interview.Utils.item_question;
import us.feras.mdv.MarkdownView;

/**
 * Created by mrkimkim on 2017-12-06.
 */

public class ChooseQuestion extends AppCompatActivity{
    Context mContext;
    DBHelper mDBHelper;

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    Adapter_Question mAdapter_Question;
    ArrayList items_question;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choosequestion);
        Intent intent = new Intent(this.getIntent());


        items_question = new ArrayList<item_question>();
        recyclerView = (RecyclerView)findViewById(R.id.cq_recycler_view);
        recyclerView.setHasFixedSize(true);
        layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);



        items_question.add(new item_question(0, 0, "배우가 된 이유에 대해서 뮤지컬 형식을 사용해서 설명해주세요", "2014년 한예종 연극영화과", "30초", "한국어", "1 크레딧", "300만", "21만", "www.naver.com"));
        items_question.add(new item_question(0, 0, "배우가 된 이유에 대해서 설명해주세요", "2014년 한예종 연극영화과", "30초", "한국어", "1 크레딧", "300만", "21만", "www.naver.com"));
        items_question.add(new item_question(0, 0, "배우가 된 이유에 대해서 설명해주세요", "2014년 한예종 연극영화과", "30초", "한국어", "1 크레딧", "300만", "21만", "www.naver.com"));
        items_question.add(new item_question(0, 0, "배우가 된 이유에 대해서 설명해주세요", "2014년 한예종 연극영화과", "30초", "한국어", "1 크레딧", "300만", "21만", "www.naver.com"));
        items_question.add(new item_question(0, 0, "배우가 된 이유에 대해서 설명해주세요", "2014년 한예종 연극영화과", "30초", "한국어", "1 크레딧", "300만", "21만", "www.naver.com"));
        items_question.add(new item_question(0, 0, "배우가 된 이유에 대해서 설명해주세요", "2014년 한예종 연극영화과", "30초", "한국어", "1 크레딧", "300만", "21만", "www.naver.com"));
        items_question.add(new item_question(0, 0, "배우가 된 이유에 대해서 설명해주세요", "2014년 한예종 연극영화과", "30초", "한국어", "1 크레딧", "300만", "21만", "www.naver.com"));
        items_question.add(new item_question(0, 0, "배우가 된 이유에 대해서 설명해주세요", "2014년 한예종 연극영화과", "30초", "한국어", "1 크레딧", "300만", "21만", "www.naver.com"));

        mAdapter_Question = new Adapter_Question(items_question, mContext);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mAdapter_Question);
    }

    public class Adapter_Question extends RecyclerView.Adapter<Adapter_Question.ViewHolder> {
        private Context context;
        private ArrayList<item_question> mItems;
        private int lastPosition = -1;

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
            String description = mItems.get(position).getDuration() + "  |  " + mItems.get(position).getLanguage() + "  |  " + mItems.get(position).getPrice();
            holder.tv_description.setText(description);

            // 출처 정보
            holder.tv_history.setText(mItems.get(position).getHistory());

            // 뷰 카운트와 좋아요 수
            holder.tv_totalView.setText(mItems.get(position).getTotal_view());
            holder.tv_totalLike.setText(mItems.get(position).getTotal_like());
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

                    Dialog dialog = new Dialog(ChooseQuestion.this);
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
            }
        }
    }
}
