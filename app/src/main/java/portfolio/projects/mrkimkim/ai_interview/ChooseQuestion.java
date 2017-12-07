package portfolio.projects.mrkimkim.ai_interview;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * Created by mrkimkim on 2017-12-06.
 */

public class ChooseQuestion extends AppCompatActivity{
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choosequestion);
        Intent intent = new Intent(this.getIntent());
    }

    public void startInterview (View v) {
        Intent intent = new Intent(ChooseQuestion.this, Interview.class);
        startActivity(intent);
        finish();
    }
}
