package portfolio.projects.mrkimkim.ai_interview;

import android.content.ContentValues;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import portfolio.projects.mrkimkim.ai_interview.DBHelper.DBHelper;
import portfolio.projects.mrkimkim.ai_interview.Utils.item_category;

public class SearchActivity extends AppCompatActivity {
    static final int category_num = 4;

    Context mContext;
    DBHelper mDBHelper;
    RecyclerView recyclerView;
    Adapter_search mAdapter;
    RecyclerView.LayoutManager layoutManager;
    ArrayList[] items = new ArrayList[4];;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // DB 인스턴스를 가져온다
        mDBHelper = DBHelper.getInstance(getApplicationContext());

        // RecyclerView를 초기화한다.
        recyclerView = (RecyclerView)findViewById(R.id.search_recycler_view);
        recyclerView.setHasFixedSize(true);
        layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);

        // DB에서 카테고리 정보를 가져와 Adapter에 연결시킨다.
        _LoadCategory();
    }

    private void _LoadCategory() {
        class t_LoadCategory implements Runnable {
            @Override
            public void run() {
                // ArrayList를 초기화한다.
                for(int i = 0; i < category_num; ++i) items[i] = new ArrayList<>();

                // DB의 모든 카테고리를 불러온다.
                ContentValues[] values = mDBHelper.select("Category", DBHelper.column_category, null, null, null, null, null);
                for (int i = 0; i < values.length; ++i) {
                    int idx = values[i].getAsInteger("parent_idx");
                    if (idx != -1) {
                        --idx;
                        Log.d("IDX :", String.valueOf(idx));
                        items[idx].add(new item_category(R.drawable.icon_category_card_1,
                                values[i].getAsInteger("idx"),
                                values[i].getAsString("title"),
                                values[i].getAsString("description"),
                                values[i].getAsInteger("n_subcategory"),
                                values[i].getAsInteger("n_probset"),
                                values[i].getAsInteger("n_problem")));
                    }
                }
                // DEFAULT로 보여줄 카테고리 설정
                mAdapter = new Adapter_search(items[0], mContext);

                // RecyclerView와 Adapter를 연결
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(mAdapter);
            }
        }
        Runnable t = new t_LoadCategory();
        t.run();
    }

    // 화면에서 좌측 대분류 버튼을 클릭 시 RecyclerView의 데이터를 갱신한다.
    public void changeCategory(View v) {
        int id = v.getId();
        if (R.id.search_menu1 == id) mAdapter.refreshItems(items[0]);
        else if (R.id.search_menu2 == id) mAdapter.refreshItems(items[1]);
        else if (R.id.search_menu3 == id) mAdapter.refreshItems(items[2]);
        else if (R.id.search_menu4 == id) mAdapter.refreshItems(items[3]);
    }

    public class Adapter_search extends RecyclerView.Adapter<Adapter_search.ViewHolder_search> {
        private Context context;
        private ArrayList<item_category> mItems;
        private int lastPosition = -1;

        public Adapter_search(ArrayList items, Context mContext) {
            mItems = items;
            context = mContext;
        }

        @Override
        public ViewHolder_search onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
            ViewHolder_search holder = new ViewHolder_search(v);
            return holder;
        }

        @Override
        public void onBindViewHolder(ViewHolder_search holder, int position) {
            int idx = position % 5;
            // 카드 배경 색상 지정
            if (idx == 0) holder.card.setCardBackgroundColor(getResources().getColor(R.color.category_card_background_0));
            else if (idx == 1) holder.card.setCardBackgroundColor(getResources().getColor(R.color.category_card_background_1));
            else if (idx == 2) holder.card.setCardBackgroundColor(getResources().getColor(R.color.category_card_background_2));
            else if (idx == 3) holder.card.setCardBackgroundColor(getResources().getColor(R.color.category_card_background_3));
            else holder.card.setCardBackgroundColor(getResources().getColor(R.color.category_card_background_4));

            // 카드 아이콘
            holder.icon.setImageResource(mItems.get(position).getImage());

            // 타이틀 및 설명
            holder.title.setText(mItems.get(position).gettitle());
            holder.title_describe.setText(mItems.get(position).getTitle_describe());

            // 카테고리 수치 정보
            holder.n_subcategory.setText(mItems.get(position).getN_subcategory());
            holder.n_probset.setText(mItems.get(position).getN_probset());
            holder.n_problem.setText(mItems.get(position).getN_problem());
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

        public void refreshItems(ArrayList items) {
            mItems = items;
            notifyDataSetChanged();
        }

        public class ViewHolder_search extends RecyclerView.ViewHolder implements View.OnClickListener {
            public CardView card;
            public ImageView icon;
            public TextView title, title_describe, n_subcategory, n_probset, n_problem;

            public ViewHolder_search(View view) {
                super(view);
                card = (CardView) view.findViewById(R.id.category_card);
                icon = (ImageView)view.findViewById(R.id.category_card_left_icon);
                title = (TextView) view.findViewById(R.id.category_card_right_title);
                title_describe = (TextView) view.findViewById(R.id.category_card_right_describe);
                n_subcategory = (TextView) view.findViewById(R.id.category_card_right_tv1);
                n_probset = (TextView) view.findViewById(R.id.category_card_right_tv2);
                n_problem = (TextView) view.findViewById(R.id.category_card_right_tv3);

                card.setTag(R.integer.category_card, view);
                card.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                // 클릭된 카드의 idx를 찾아온다.

                // Dialog를 띄워서 상세 직렬을 넣는다.

                // DB에서 검색해서 문제 리스트를 받아온다.
                // 이것은 Loading Dialog를 띄워서 구현한다.
            }
        }
    }
}

