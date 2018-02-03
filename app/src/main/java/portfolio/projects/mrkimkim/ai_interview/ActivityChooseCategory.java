package portfolio.projects.mrkimkim.ai_interview;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.roger.catloadinglibrary.CatLoadingView;

import java.util.ArrayList;

import portfolio.projects.mrkimkim.ai_interview.DBHelper.DBHelper;
import portfolio.projects.mrkimkim.ai_interview.Utils.item_category;

public class ActivityChooseCategory extends AppCompatActivity {
    static final int n_category_parent = 4;
    int n_subcategory_parent = 0;

    CatLoadingView mView;
    Context mContext;
    DBHelper mDBHelper;

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    Adapter_Category mAdapter_Category, mAdapter_SubCategory;;
    ArrayList[] items_category = new ArrayList[4];
    ArrayList[] items_subcategory;

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
                // Category의 ArrayList를 초기화한다.
                for(int i = 0; i < n_category_parent; ++i) items_category[i] = new ArrayList<item_category>();

                // DB의 모든 카테고리를 불러온다.
                ContentValues[] values = mDBHelper.select("Category", DBHelper.column_category, null, null, null, null, null);

                // 메인 카테고리를 찾는다
                for (int i = 0; i < values.length; ++i) {
                    int idx = values[i].getAsInteger("parent_idx");
                    if (1 <= idx && idx <= n_category_parent) {
                        --idx;
                        Log.d("IDX :", String.valueOf(idx));
                        items_category[idx].add(new item_category(R.drawable.icon_category_card_1,
                                values[i].getAsInteger("idx"),
                                values[i].getAsInteger("parent_idx"),
                                values[i].getAsString("title"),
                                values[i].getAsString("description"),
                                values[i].getAsInteger("n_subcategory"),
                                values[i].getAsInteger("n_probset"),
                                values[i].getAsInteger("n_problem")));
                        n_subcategory_parent += 1;
                    }
                }

                // SubCategory의 ArrayList를 초기화한다.
                items_subcategory = new ArrayList[n_subcategory_parent];
                for(int i = 0; i < n_subcategory_parent; ++i) items_subcategory[i] = new ArrayList<item_category>();

                // 서브 카테고리를 찾는다
                for (int i = 0; i < values.length; ++i) {
                    int idx = values[i].getAsInteger("parent_idx");
                    if (n_category_parent < idx && idx <= n_category_parent + n_subcategory_parent) {
                        items_subcategory[idx - n_category_parent - 1].add(new item_category(R.drawable.icon_category_card_1,
                                values[i].getAsInteger("idx"),
                                values[i].getAsInteger("parent_idx"),
                                values[i].getAsString("title"),
                                values[i].getAsString("description"),
                                values[i].getAsInteger("n_subcategory"),
                                values[i].getAsInteger("n_probset"),
                                values[i].getAsInteger("n_problem")));
                        Log.d("sub", values[i].getAsString("parent_idx") + "," + values[i].getAsString("title"));
                    }
                }

                // DEFAULT로 보여줄 카테고리 설정
                mAdapter_Category = new Adapter_Category(items_category[0], mContext);
                mAdapter_SubCategory = new Adapter_Category(items_subcategory[0], mContext);

                // RecyclerView와 Adapter를 연결
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(mAdapter_Category);
            }
        }
        Runnable t = new t_LoadCategory();
        t.run();
    }

    // 화면에서 좌측 대분류 버튼을 클릭 시 RecyclerView의 데이터를 갱신한다.
    public void changeCategory(View v) {
        int id = v.getId();
        if (!recyclerView.getAdapter().equals(mAdapter_Category)) {
            recyclerView.swapAdapter(mAdapter_Category, false);
        }
        if (R.id.search_menu1 == id) mAdapter_Category.refreshItems(items_category[0]);
        else if (R.id.search_menu2 == id) mAdapter_Category.refreshItems(items_category[1]);
        else if (R.id.search_menu3 == id) mAdapter_Category.refreshItems(items_category[2]);
        else if (R.id.search_menu4 == id) mAdapter_Category.refreshItems(items_category[3]);
    }

    public class Adapter_Category extends RecyclerView.Adapter<Adapter_Category.ViewHolder> {
        public ArrayList<item_category> mItems;

        public Adapter_Category(ArrayList items, Context mContext) {
            mItems = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
            ViewHolder holder = new ViewHolder(v);
            return holder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
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
            this.mItems = null;
            this.mItems = items;
            notifyDataSetChanged();
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            public CardView card;
            public ImageView icon;
            public TextView title, title_describe, n_subcategory, n_probset, n_problem;

            public ViewHolder(View view) {
                super(view);
                card = (CardView) view.findViewById(R.id.question_card);
                icon = (ImageView)view.findViewById(R.id.question_card_left_icon);
                title = (TextView) view.findViewById(R.id.question_card_right_title);
                title_describe = (TextView) view.findViewById(R.id.question_card_right_describe);
                n_subcategory = (TextView) view.findViewById(R.id.category_card_right_tv1);
                n_probset = (TextView) view.findViewById(R.id.category_card_right_tv2);
                n_problem = (TextView) view.findViewById(R.id.category_card_right_tv3);
                card.setOnClickListener(this);
            }


            @Override
            public void onClick(View v) {
                // 클릭된 카드의 인스턴스를 가져온다.
                int idx = 0;
                item_category instance;

                if (recyclerView.getAdapter().equals(mAdapter_Category)) instance = mAdapter_Category.mItems.get(getAdapterPosition());
                else instance = mAdapter_SubCategory.mItems.get(getAdapterPosition());

                // 부모의 idx를 찾아온다
                int parent_idx = instance.getParent_idx();

                Log.d("Title : ", instance.gettitle());
                Log.d("parent_idx", String.valueOf(parent_idx));
                Log.d("n_category_parent", String.valueOf(n_category_parent));
                Log.d("n_subcategory_parent", String.valueOf(n_subcategory_parent));

                // 메인 카테고리인 경우 어댑터를 교체한다.
                if (1 <= parent_idx && parent_idx <= n_category_parent) {
                    idx = instance.getidx() - n_category_parent - 1;
                    mAdapter_SubCategory.refreshItems(items_subcategory[idx]);
                    recyclerView.swapAdapter(mAdapter_SubCategory, false);
                }

                // 서브 카테고리인 경우 문제를 로드한다.
                else if (n_category_parent < parent_idx && parent_idx <= n_category_parent + n_subcategory_parent) {
                    // 로딩 다이얼로그 생성
                    mView = new CatLoadingView();
                    mView.setCancelable(true);
                    mView.show(getSupportFragmentManager(), "");

                    // 문제 로딩
                    Intent intent = new Intent(ActivityChooseCategory.this, ActivityChooseQuestion.class);
                    intent.putExtra("category_idx", instance.getidx());
                    startActivity(intent);
                }
            }
        }
    }
}

