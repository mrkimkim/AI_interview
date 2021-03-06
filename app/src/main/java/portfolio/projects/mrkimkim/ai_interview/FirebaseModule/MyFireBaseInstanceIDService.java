package portfolio.projects.mrkimkim.ai_interview.NetworkModule;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by 킹조 on 2018-01-03.
 */

public class MyFireBaseInstanceIDService extends FirebaseInstanceIdService {
    private static final String TAG = "MyFireBaseIIDService";

    // [Start refresh_token]
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String token = FirebaseInstanceId.getInstance().getToken();
                Log.d(TAG, "Refreshed token : " + token);
    }
}
