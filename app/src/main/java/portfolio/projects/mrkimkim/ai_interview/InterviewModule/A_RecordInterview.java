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

public class A_RecordInterview extends AppCompatActivity {
    // Camera.CameraInfo.CAMERA_FACING_FRONT or Camera.CameraInfo.CAMERA_FACING_BACK
    private final static int CAMERA_FACING = Camera.CameraInfo.CAMERA_FACING_FRONT;
    private static final String TAG = "A_RecordInterview";

    String tempFilePath = "";
    int questionIdx;
    boolean isRecording = false;

    Context context;
    Preview preview;
    Camera camera;
    MediaRecorder mediaRecorder;
    CircleButton btnRecord;

    @Override
    protected void onResume() {
        super.onResume();
        startCamera();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;

        /* 풀 스크린 모드 */
        clearTitleBar();
        setContentView(R.layout.activity_record_interview);

        /* 녹화 버튼 클릭 이벤트 등록 */
        btnRecord = (CircleButton)findViewById(R.id.btnPower);
        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isRecording) startRecord(view);
                else stopRecord(view);
            }
        });

        /* 문제 정보를 받아옴 */
        Intent intent = getIntent();
        questionIdx = intent.getIntExtra("questionIdx", 0);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Surface will be destroyed when we return, so stop the preview.
        if(camera != null) {
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
                Toast.makeText(context, "camera_not_found " + ex.getMessage().toString(),
                        Toast.LENGTH_LONG).show();
            }
        }
        preview.setCamera(camera);
    }

    /* 녹화 시작 */
    public void startRecord(View view) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    isRecording = true;
                    btnRecord.setClickable(false);

                    // 파일 준비
                    File sdCard = Environment.getExternalStorageDirectory();
                    File dir = new File (sdCard.getAbsolutePath() + "/interview/");
                    dir.mkdirs();

                    tempFilePath = dir + String.format("%d.mp4", System.currentTimeMillis());

                    /* 카메라 준비 */
                    camera.unlock();

                    /* MediaRecorder 초기화 */
                    mediaRecorder = new MediaRecorder();
                    mediaRecorder.setCamera(camera);
                    mediaRecorder.setOrientationHint(270);
                    mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
                    mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_480P));
                    mediaRecorder.setOutputFile(tempFilePath);
                    mediaRecorder.prepare();

                    /* 녹화 시작 */
                    mediaRecorder.start();

                    /* 버튼 해제 */
                    btnRecord.setClickable(true);
                } catch (Exception e) {
                    e.printStackTrace();
                    HandleCameraException();
                }
            }
        });
    }

    /* 녹화 종료 */
    public void stopRecord(View view) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isRecording) {
                    try {
                        // 카메라 해제
                        btnRecord.setClickable(false);
                        camera.lock();
                        mediaRecorder.release();
                        btnRecord.setClickable(true);
                        isRecording = false;

                        refreshGallery();
                        dialogFinishInterview();
                    } catch (Exception e) {
                        e.printStackTrace();
                        HandleCameraException();
                    }
                }
            }
        });
    }

    /* 카메라 예외 처리 */
    public void HandleCameraException() {
        btnRecord.setClickable(false);

        // 카메라 자원 할당을 해제 한다
        camera.lock();
        mediaRecorder.release();
        isRecording = !isRecording;
        delteTempFile();

        btnRecord.setClickable(true);
    }

    /* 인터뷰 종료 다이얼로그 */
    public void dialogFinishInterview() {
        AlertDialog.Builder builder = new AlertDialog.Builder(A_RecordInterview.this);
        builder.setTitle(getString(R.string.finishInterviewTitle));
        builder.setMessage(getString(R.string.finishInterviewBody));
        builder.setPositiveButton(getString(R.string.yes),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        saveInterviewDB();
                            Toast.makeText(getApplicationContext(),getString(R.string.connecting),Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(A_RecordInterview.this, A_UploadInterview.class);
                        intent.putExtra("uri", "file://" + tempFilePath);
                        intent.putExtra("questionIdx", Long.valueOf(String.valueOf(questionIdx)));
                        intent.putExtra("video_path", tempFilePath);
                        startActivity(intent);
                    }
                });
        builder.setNegativeButton(getString(R.string.no),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialogSaveInterview();
                    }
                });
        builder.show();
    }

    /* 인터뷰 저장 다이얼로그 */
    public void dialogSaveInterview() {
        AlertDialog.Builder builder = new AlertDialog.Builder(A_RecordInterview.this);
        builder.setTitle(getString(R.string.saveInterviewTitle));
        builder.setMessage(getString(R.string.saveInterviewBody));
        builder.setPositiveButton(getString(R.string.yes),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        saveInterviewDB();
                        Toast.makeText(getApplicationContext(), tempFilePath + "경로에 영상이 저장되었습니다. 면접 결과 페이지에서 AI 분석을 요청할 수 있습니다.",Toast.LENGTH_LONG).show();
                    }
                });
        builder.setNegativeButton(getString(R.string.no),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        delteTempFile();
                    }
                });
        builder.show();
    }

    /* DB에 데이터를 저장 */
    public void saveInterviewDB() {
        DBHelper mDBHelper = DBHelper.getInstance(getApplicationContext());
        mDBHelper.insert("InterviewData",
                new String[]{"user_idx", "video_path", "task_idx", "result_idx", "questionIdx"},
                new String[]{String.valueOf(GlobalApplication.mUserInfoManager.getUserProfile().getId()), tempFilePath, "0", "0", String.valueOf(questionIdx)}, null, null);
    }

    /* 임시 영상 파일 제거 */
    public void delteTempFile() {
        File file = new File(tempFilePath);
        if (file.exists()) file.delete();
        tempFilePath = "";
    }

    /* 핸드폰 갤러리 업데이트 */
    private void refreshGallery() {
        File file = new File(tempFilePath);
        Intent mediaScanIntent = new Intent( Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(Uri.fromFile(file));
        sendBroadcast(mediaScanIntent);
    }

    /* 타이틀 바 제거 */
    private void clearTitleBar() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }


    /* 스마트 폰 회전 상태에 따른 카메라 촬영 각도를 설정한다 */
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
