package portfolio.projects.mrkimkim.ai_interview;

import android.content.ClipData;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import portfolio.projects.mrkimkim.ai_interview.Utils.item_category;

public class SearchActivity extends AppCompatActivity {
    Context mContext;

    RecyclerView recyclerView;
    RecyclerView.Adapter Adapter;
    RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        recyclerView = (RecyclerView)findViewById(R.id.search_recycler_view);
        recyclerView.setHasFixedSize(true);

        ArrayList items = new ArrayList<>();

        items.add(new item_category());
        items.add(new item_category());
        items.add(new item_category());
        items.add(new item_category());
        items.add(new item_category());

        layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        Adapter = new Adapter_search(items, mContext);
        recyclerView.setAdapter(Adapter);
    }

    class Adapter_search extends RecyclerView.Adapter<Adapter_search.ViewHolder_search> {
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

        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

        public class ViewHolder_search extends RecyclerView.ViewHolder {
            public ViewHolder_search(View v) {
                super(v);
            }
        }

        private void setAnimation(View viewToAnimate, int position) {
            if (position > lastPosition) {
                Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
                viewToAnimate.startAnimation(animation);
                lastPosition = position; }
        }
    }
}

