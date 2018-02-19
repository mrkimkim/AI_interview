package portfolio.projects.mrkimkim.ai_interview;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import portfolio.projects.mrkimkim.ai_interview.NetworkModule.LoginActivity;

public class A_CheckPermission extends AppCompatActivity {
    String[] permissions = new String[] {
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.INTERNET,
            android.Manifest.permission.ACCESS_NETWORK_STATE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_permission);

        // 기기 버전 체크 후 퍼미션 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkPermissions()) {
                if (showRequestPermission()) {
                } else {
                }
                ActivityCompat.requestPermissions(this, permissions, 12345);
            }
        }

        while (true) {
            if (!checkPermissions()) {
                break;
            }
        }
        Intent intent = new Intent(A_CheckPermission.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    /* 퍼미션 체크 */
    private boolean checkPermissions() {
        for (int i = 0; i < permissions.length; ++i) {
            if (ContextCompat.checkSelfPermission(this, permissions[i]) == PackageManager.PERMISSION_DENIED) return true;
        }
        return false;
    }

    /* 퍼미션 요구 이유 체크 */
    private boolean showRequestPermission() {
        for (int i = 0; i < permissions.length; ++i) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i])) return true;
        }
        return false;
    }
}
