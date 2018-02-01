package portfolio.projects.mrkimkim.ai_interview;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


public class InterviewResultActivity extends AppCompatActivity {
    int MAX_PAGE = 2;
    android.support.v4.app.Fragment cur_fragment = new android.support.v4.app.Fragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_interview_result);

        ViewPager viewPager = (ViewPager) findViewById(R.id.show_interview_result_viewpager);


    }

    private class adapter extends FragmentPagerAdapter {
        public adapter(android.support.v4.app.FragmentManager fm) {
            super(fm);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            if(position<0 || MAX_PAGE<=position)
                return null;
            switch (position){
                case 0:
                    cur_fragment=new page_1();
                    break;
                case 1:
                    cur_fragment=new page_2();
                    break;
            }
            return cur_fragment;
        }

        @Override
        public int getCount() {
            return MAX_PAGE;
        }
    }
    }
}
