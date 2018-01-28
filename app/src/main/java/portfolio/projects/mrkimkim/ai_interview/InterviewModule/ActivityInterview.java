package portfolio.projects.mrkimkim.ai_interview.InterviewModule;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.File;

import at.markushi.ui.CircleButton;
import portfolio.projects.mrkimkim.ai_interview.DBHelper.DBHelper;
import portfolio.projects.mrkimkim.ai_interview.GlobalApplication;
import portfolio.projects.mrkimkim.ai_interview.R;

public class ActivityInterview extends AppCompatActivity {
    // Camera.CameraInfo.CAMERA_FACING_FRONT or Camera.CameraInfo.CAMERA_FACING_BACK
    private final static int CAMERA_FACING = Camera.CameraInfo.CAMERA_FACING_FRONT;
    private static final String TAG = "ActivityInterview";
    private AppCompatActivity mActivity;

    int question_idx;
    String tempFilePath = "";
    boolean isRecording = false;

    Context ctx;
    Preview preview;
    Camera camera;
    MediaRecorder mediaRecorder;
    CircleButton btn_record;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = this;
        mActivity = this;

        Intent intent = getIntent();
        question_idx = intent.getIntExtra("question_idx", 0);

        clearTitleBar();
        setContentView(R.layout.activity_interview);

        btn_record = (CircleButton)findViewById(R.id.btnPower);
        btn_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isRecording) startRecord(view);
                else stopRecord(view);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        startCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Surface will be destroyed when we return, so stop the preview.
        if(camera != null) {
            // Call stopPreview() to stop updating the preview surface
            camera.stopPreview();
            preview.setCamera(null);
            camera.release();
            camera = null;
        }
        ((FrameLayout) findViewById(R.id.layout)).removeView(preview);
        preview = null;
    }

    public void startCamera() {
        if ( preview == null ) {
            preview = new Preview(this, (SurfaceView) findViewById(R.id.surfaceView));
            preview.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT));
            ((FrameLayout) findViewById(R.id.layout)).addView(preview);
            preview.setKeepScreenOn(true);
        }

        preview.setCamera(null);
        if (camera != null) {
            camera.release();
            camera = null;
        }

        int numCams = Camera.getNumberOfCameras();
        if (numCams > 0) {
            try {
                camera = Camera.open(CAMERA_FACING);
                // camera orientation
                camera.setDisplayOrientation(setCameraDisplayOrientation(this, CAMERA_FACING,
                        camera));
                // get Camera parameters
                Camera.Parameters params = camera.getParameters();
                // picture image orientation
                params.setRotation(setCameraDisplayOrientation(this, CAMERA_FACING, camera));
                camera.startPreview();

            } catch (RuntimeException ex) {
                Toast.makeText(ctx, "camera_not_found " + ex.getMessage().toString(),
                        Toast.LENGTH_LONG).show();
                Log.d(TAG, "camera_not_found " + ex.getMessage().toString());
            }
        }
        preview.setCamera(camera);
    }

    public void startRecord(View view) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    isRecording = true;
                    btn_record.setClickable(false);

                    // 파일 준비
                    File sdCard = Environment.getExternalStorageDirectory();
                    File dir = new File (sdCard.getAbsolutePath() + "/interview");
                    dir.mkdirs();

                    tempFilePath = dir + String.format("%d.mp4", System.currentTimeMillis());

                    // 카메라 준비
                    camera.unlock();

                    mediaRecorder = new MediaRecorder();
                    mediaRecorder.setCamera(camera);
                    mediaRecorder.setOrientationHint(270);

                    mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

                    mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_480P));
                    mediaRecorder.setOutputFile(tempFilePath);

                    mediaRecorder.prepare();
                    mediaRecorder.start();
                    btn_record.setClickable(true);
                } catch (Exception e) {
                    e.printStackTrace();
                    HandleCameraException();
                }
            }
        });
    }

    public void stopRecord(View view) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isRecording) {
                    try {
                        // 카메라 해제
                        btn_record.setClickable(false);
                        camera.lock();
                        mediaRecorder.release();
                        btn_record.setClickable(true);
                        isRecording = false;

                        refreshGallery();
                        dialogConnectServer();
                    } catch (Exception e) {
                        e.printStackTrace();
                        HandleCameraException();
                    }
                }
            }
        });
    }


    public void dialogConnectServer() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityInterview.this);
        builder.setTitle("면접 종료");
        builder.setMessage("지금 AI 분석을 시작하시겠습니까? (1크레딧)");
        builder.setPositiveButton("예",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        SaveDB();
                        Toast.makeText(getApplicationContext(),"서버와 연결 시작",Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(ActivityInterview.this, UploadVideo.class);
                        intent.putExtra("uri", "file://" + tempFilePath);
                        intent.putExtra("question_idx", question_idx);
                        intent.putExtra("video_path", tempFilePath);
                        startActivity(intent);
                    }
                });
        builder.setNegativeButton("아니오",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialogSaveVideo();
                    }
                });
        builder.show();
    }


    public void dialogSaveVideo() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityInterview.this);
        builder.setTitle("동영상 저장");
        builder.setMessage("면접 영상 및 정보를 기기에 저장하시겠습니까?");
        builder.setPositiveButton("예",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        SaveDB();
                        Toast.makeText(getApplicationContext(), tempFilePath + "경로에 영상이 저장되었습니다. 면접 결과 페이지에서 AI 분석을 요청할 수 있습니다.",Toast.LENGTH_LONG).show();
                    }
                });
        builder.setNegativeButton("아니오",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        delteTempFile();
                    }
                });
        builder.show();
    }


    public void SaveDB() {
        DBHelper mDBHelper = DBHelper.getInstance(getApplicationContext());
        mDBHelper.insert("InterviewData",
                new String[]{"user_idx", "video_path", "task_idx", "result_idx", "question_idx"},
                new String[]{String.valueOf(GlobalApplication.mUserInfoManager.getUserProfile().getId()), tempFilePath, "0", "0", String.valueOf(question_idx)}, null, null);
        Log.d("User ID User ID User ID", String.valueOf(GlobalApplication.mUserInfoManager.getUserProfile().getId()));
    }


    public void HandleCameraException() {
        btn_record.setClickable(false);

        camera.lock();
        mediaRecorder.release();
        isRecording = !isRecording;
        delteTempFile();

        btn_record.setClickable(true);
    }

    public void delteTempFile() {
        File file = new File(tempFilePath);
        if (file.exists()) file.delete();
        tempFilePath = "";
    }

    private void refreshGallery() {
        File file = new File(tempFilePath);
        Intent mediaScanIntent = new Intent( Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(Uri.fromFile(file));
        sendBroadcast(mediaScanIntent);
    }

    private void clearTitleBar() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }



    public static int setCameraDisplayOrientation(Activity activity,
                                                  int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }

}
