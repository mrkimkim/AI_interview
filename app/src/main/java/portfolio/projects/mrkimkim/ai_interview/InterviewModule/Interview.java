package portfolio.projects.mrkimkim.ai_interview.InterviewModule;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.widget.FrameLayout;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import ng.max.slideview.SlideView;
import portfolio.projects.mrkimkim.ai_interview.R;

public class Interview extends AppCompatActivity {
    private String FileName;
    private boolean isrecording;
    private MediaRecorder mediaRecorder;
    private CameraPreview mPreview;
    private Camera mCamera;

    public static void setCameraDisplayOrientation(Activity activity, int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360; // compensate the mirror
        } else { // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    private Camera openFrontCamera() {
        int cameraCount = 0;
        Camera cam = null;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();
        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                try {
                    cam = Camera.open(camIdx);

                } catch (RuntimeException e) {
                    Log.e("Show : ", "Camera failed to open: " + e.getLocalizedMessage());
                }
            }
        }
        return cam;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCamera.lock();
        mediaRecorder.release();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interview);

        // set layout preview
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);

        // Create an instance of Camera
        mCamera = openFrontCamera();
        setCameraDisplayOrientation(this, Camera.CameraInfo.CAMERA_FACING_FRONT, mCamera);

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        preview.addView(mPreview);

        SlideView slideView = (SlideView)findViewById(R.id.slideView);
        slideView.setOnSlideCompleteListener(new SlideView.OnSlideCompleteListener() {
            @Override
            public void onSlideComplete(SlideView slideView) {
                // vibrate the device
                if (isrecording) {
                    mediaRecorder.stop();
                    mediaRecorder.release();
                    mCamera.lock();
                    isrecording = false;

                    // update device gallary
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                            Uri.parse("file://" + Environment.getExternalStorageDirectory() + "/interview/" + FileName)));

                    // give uri information to UploadVideo
                    Intent intent = new Intent(Interview.this, UploadVideo.class);
                    intent.putExtra("uri","file://" + Environment.getExternalStorageDirectory() + "/interview/" + FileName);
                    startActivity(intent);
                    finish();

                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                // Set File Path
                                String timeStamp = new SimpleDateFormat("yyyy-MM-dd-mm-ss").format(new Date());
                                FileName = timeStamp + ".mp4";

                                // Folder Check
                                File f = new File("/sdcard/interview");
                                if(!f.isDirectory()) f.mkdir();

                                // unlock camera
                                mCamera.unlock();

                                // mediaResorder configuration
                                mediaRecorder = new MediaRecorder();

                                mediaRecorder.setCamera(mCamera);
                                mediaRecorder.setOrientationHint(270);

                                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                                mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

                                mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_480P));
                                mediaRecorder.setOutputFile("/sdcard/interview/" + FileName);

                                mediaRecorder.prepare();
                                mediaRecorder.start();
                                isrecording = true;
                            } catch (final Exception e) {
                                e.printStackTrace();
                                mCamera.lock();
                                mediaRecorder.release();
                                return;
                            }
                        }
                    });
                }
            }
        });
    }
}

