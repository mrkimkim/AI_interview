package portfolio.projects.mrkimkim.ai_interview.Utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.Window;
import android.widget.ImageView;

import portfolio.projects.mrkimkim.ai_interview.R;

/**
 * Created by 킹조 on 2018-01-04.
 */

public class LoadingDialog extends Dialog {
    public LoadingDialog(Context context) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.progress_loading);

        ImageView iv = (ImageView) findViewById(R.id.iv_frame_loading);
        AnimationDrawable drawable = (AnimationDrawable) iv.getBackground();
        drawable.start();
    }
}
