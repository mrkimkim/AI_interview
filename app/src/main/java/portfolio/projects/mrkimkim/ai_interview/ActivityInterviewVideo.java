package portfolio.projects.mrkimkim.ai_interview;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


public class ActivityInterviewVideo extends AppCompatActivity {
    int MAX_PAGE = 2;
    android.support.v4.app.Fragment cur_fragment = new android.support.v4.app.Fragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_interview_video);
    }
}
