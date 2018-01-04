package portfolio.projects.mrkimkim.ai_interview;

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

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import portfolio.projects.mrkimkim.ai_interview.Utils.LoadingDialog;
import portfolio.projects.mrkimkim.ai_interview.Utils.Item;

public class ResultActivity extends AppCompatActivity {
    Context mContext;

    RecyclerView recyclerView;
    RecyclerView.Adapter Adapter;
    RecyclerView.LayoutManager layoutManager;

    LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseMessaging.getInstance().subscribeToTopic("news");
        FirebaseInstanceId.getInstance().getToken();

        Log.d("MainActivity", "Token : " + FirebaseInstanceId.getInstance().getToken());

        // RecyclerView
        mContext = getApplicationContext();

        recyclerView = (RecyclerView)findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);

        ArrayList items = new ArrayList<>();
        items.add(new Item("자신에 대해 소개해주세요.\n어떤 답변이든지 좋습니다.", "한국어", "한국어", "발표시간 (60초)  |  2017-12-24 09:00", "2017 상반기 삼성전자 공채"));
        items.add(new Item("자신에 대해 소개해주세요.\n어떤 답변이든지 좋습니다.", "한국어", "한국어", "발표시간 (60초)  |  2017-12-24 09:00", "2017 상반기 삼성전자 공채"));
        items.add(new Item("자신에 대해 소개해주세요.\n어떤 답변이든지 좋습니다.", "한국어", "한국어", "발표시간 (60초)  |  2017-12-24 09:00", "2017 상반기 삼성전자 공채"));
        items.add(new Item("자신에 대해 소개해주세요.\n어떤 답변이든지 좋습니다.", "한국어", "한국어", "발표시간 (60초)  |  2017-12-24 09:00", "2017 상반기 삼성전자 공채"));
        items.add(new Item("자신에 대해 소개해주세요.\n어떤 답변이든지 좋습니다.", "한국어", "한국어", "발표시간 (60초)  |  2017-12-24 09:00", "2017 상반기 삼성전자 공채"));
        items.add(new Item("자신에 대해 소개해주세요.\n어떤 답변이든지 좋습니다.", "한국어", "한국어", "발표시간 (60초)  |  2017-12-24 09:00", "2017 상반기 삼성전자 공채"));

        layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        Adapter = new MyAdapter(items, mContext);
        recyclerView.setAdapter(Adapter);

        // Dialog setting
        loadingDialog = new LoadingDialog(ResultActivity.this);
        loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable((Color.TRANSPARENT)));
    }

    public void updateResult(View v) {
        loadingDialog.show();
    }

    class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private Context context;
        private ArrayList<Item> mItems;

        private int lastPosition = -1;

        public MyAdapter(ArrayList items, Context mContext) {
            mItems = items;
            context = mContext;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cardview, parent, false);
            recyclerView.setMinimumWidth(parent.getMeasuredWidth());
            ViewHolder holder = new ViewHolder(v);
            return holder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.t1.setText(mItems.get(position).getInterview_question());
            holder.t2.setText(mItems.get(position).getSrc_lang() + " -> " + mItems.get(position).getDest_lang() + "\n"
                    + mItems.get(position).getInterview_time() + "\n"
                    + mItems.get(position).getInterview_category());
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

                ib_download.setTag(R.integer.ib_download, view);
                ib_share.setTag(R.integer.ib_share, view);
                ib_delete.setTag(R.integer.ib_delete, view);

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
