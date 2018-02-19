package portfolio.projects.mrkimkim.ai_interview;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.roger.catloadinglibrary.CatLoadingView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

import portfolio.projects.mrkimkim.ai_interview.DBHelper.DBHelper;
import portfolio.projects.mrkimkim.ai_interview.InterviewModule.UploadVideo;
import portfolio.projects.mrkimkim.ai_interview.NetworkModule.NetworkService;
import portfolio.projects.mrkimkim.ai_interview.Utils.Functions;
import portfolio.projects.mrkimkim.ai_interview.Utils.item_result;

public class A_InterviewResult extends AppCompatActivity {
    Context mContext;
    RecyclerView recyclerView;
    ResultAdapter Adapter;
    RecyclerView.LayoutManager layoutManager;
    android.support.v4.app.FragmentManager fragmentManager;
    CatLoadingView catLoadingView;
    LinearLayout btn_back, btn_refresh;

    ArrayList<item_result> items;
    ArrayList<ServerData> serverData;
    boolean isDialogshow = false;

    /**
     * 서버 데이터를 다루는 클래스
     */
    class ServerData{
        Long task_idx, result_idx;
        String emotion, subtitle, pitch;

        public ServerData(Long task_idx, Long result_idx, String emotion, String subtitle, String pitch) {
            this.task_idx = task_idx;
            this.result_idx = result_idx;
            this.emotion = emotion;
            this.subtitle = subtitle;
            this.pitch = pitch;
        }
        public Long getTask_idx() { return this.task_idx; }
        public Long getResult_idx() { return this.result_idx; }
        public String getEmotion() { return this.emotion; }
        public String getSubtitle() { return this.subtitle; }
        public String getPitch() { return this.pitch; }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interview_result);

        items = new ArrayList<item_result>();
        setUiElement(); // UI 요소 설정

        /* 로컬 DB를 불러옴 */
        Runnable r = new LoadInterviewData();
        Thread t = new Thread(r);
        t.start();
    }

    /* UI 요소를 설정함 */
    public void setUiElement() {
        // RecyclerView
        mContext = getApplicationContext();
        recyclerView = (RecyclerView)findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);

        // 뒤로가기 버튼
        btn_back = (LinearLayout) findViewById(R.id.result_btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // 리프레시 버튼 등록
        btn_refresh = (LinearLayout)findViewById(R.id.result_btn_refresh);
        catLoadingView = new CatLoadingView();
        catLoadingView.setCancelable(false);
        fragmentManager = getSupportFragmentManager();
    }

    /* DB를 업데이트 함 */
    public void onClickRefreshDB(View v) {
        catLoadingView.show(fragmentManager, "TAG");
        isDialogshow = true;

        Runnable r = new DownloadServerDB();
        Thread t = new Thread(r);
        t.start();
    }

    /* 서버로부터 DB를 받음 */
    private class DownloadServerDB implements Runnable {
        @Override
        public void run() {
            try {
                /* 소켓 설정 */
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(getString(R.string.server_ip), Integer.parseInt(getString(R.string.cmd_server_port))), 1000);
                InputStream networkReader = socket.getInputStream();
                OutputStream networkWriter = socket.getOutputStream();

                /* 유저 인증 정보 전송 */
                NetworkService.Auth_User(networkReader, networkWriter);

                /* 명령 코드 전송 */
                networkWriter.write(Functions.intToBytes(2000));

                /* 데이터 수신 */
                byte[] packet_size;
                byte[] packet;
                serverData = new ArrayList<ServerData>();
                packet_size = NetworkService.receive(networkReader, 8);
                packet = NetworkService.receive(networkReader, Integer.valueOf(String.valueOf(Functions.bytesToLong(packet_size))));

                /* 데이터 파싱 */
                if (Integer.valueOf(String.valueOf(Functions.bytesToLong(packet_size))) > 0) {
                    int offset = 0;
                    int chunkCnt = 0;
                    byte[] bufferChunkCnt = new byte[8];

                    // 청크 사이즈
                    System.arraycopy(packet, 0, bufferChunkCnt, 0, 8);
                    chunkCnt = Integer.valueOf(String.valueOf(Functions.bytesToLong(bufferChunkCnt)));
                    offset += 8;

                    // 청크 탐색
                    for(int i = 0; i < chunkCnt; ++i) {
                        // TaskIdx 와 ResultIdx를 받음
                        byte[] taskIdx = new byte[8];
                        byte[] resultIdx = new byte[8];
                        System.arraycopy(packet, offset, taskIdx, 0, 8);
                        System.arraycopy(packet, offset + 8, resultIdx, 0, 8);
                        offset += 16;

                        // Emotion 데이터 파싱
                        int emotion_size = 0;
                        byte[] buffer_emotion_size = new byte[8];

                        System.arraycopy(packet, offset, buffer_emotion_size, 0, 8);
                        emotion_size = Integer.valueOf(String.valueOf(Functions.bytesToLong(buffer_emotion_size)));

                        byte[] emotion = new byte[emotion_size];
                        System.arraycopy(packet, offset + 8, emotion, 0, emotion_size);
                        offset += 8 + emotion_size;


                        // Subtitle 데이터 파싱
                        int subtitle_size = 0;
                        byte[] buffer_subtitle_size = new byte[8];
                        System.arraycopy(packet, offset, buffer_subtitle_size, 0, 8);

                        subtitle_size = Integer.valueOf(String.valueOf(Functions.bytesToLong(buffer_subtitle_size)));

                        byte[] subtitle = new byte[subtitle_size];
                        System.arraycopy(packet, offset + 8, subtitle, 0, subtitle_size);
                        offset += 8 + subtitle_size;


                        // Pitch 데이터 파싱
                        int pitch_size = 0;
                        byte[] buffer_pitch_size = new byte[8];
                        System.arraycopy(packet, offset, buffer_pitch_size, 0, 8);

                        pitch_size = Integer.valueOf(String.valueOf(Functions.bytesToLong(buffer_pitch_size)));

                        byte[] pitch = new byte[pitch_size];
                        System.arraycopy(packet, offset + 8, pitch, 0, pitch_size);
                        offset += 8 + pitch_size;

                        // 파싱된 데이터 추가
                        serverData.add(new ServerData(Functions.bytesToLong(taskIdx),
                                Functions.bytesToLong(resultIdx),
                                new String(emotion, "utf-8"),
                                new String(subtitle, "utf-8"),
                                new String(pitch, "utf-8")));

                    }
                }

                Runnable r = new UpdateLocalDB();
                Thread t = new Thread(r);
                t.start();
                t.join();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /* 로컬 DB를 업데이트 함 */
    class UpdateLocalDB implements Runnable {
        @Override
        public void run() {
            DBHelper mDBHelper = DBHelper.getInstance(getApplicationContext());
            for(int i = 0; i < serverData.size(); i++) {
                long task_idx = serverData.get(i).getTask_idx();
                long result_idx = serverData.get(i).getResult_idx();

                try {
                    // 기기에 파일을 씀
                    File emotion = new File(GlobalApplication.ApplicationDataPath + String.valueOf(result_idx) + "_emo");
                    BufferedWriter out_emotion = new BufferedWriter(new FileWriter(emotion));
                    out_emotion.write(serverData.get(i).getEmotion());
                    out_emotion.flush();
                    out_emotion.close();

                    File subtitle = new File(GlobalApplication.ApplicationDataPath + String.valueOf(result_idx) + "_stt");
                    BufferedWriter out_subtitle = new BufferedWriter(new FileWriter(subtitle));
                    out_subtitle.write(serverData.get(i).getSubtitle());
                    out_subtitle.flush();
                    out_subtitle.close();

                    File pitch = new File(GlobalApplication.ApplicationDataPath + String.valueOf(result_idx) + "_pitch");
                    BufferedWriter out_pitch = new BufferedWriter(new FileWriter(pitch));
                    out_pitch.write(serverData.get(i).getPitch());
                    out_pitch.flush();
                    out_pitch.close();

                    // DB에 정보를 저장
                    mDBHelper.update("InterviewData",
                            new String[]{"result_idx", "emotion_path", "stt_path", "pitch_path"},
                            new String[]{String.valueOf(result_idx), emotion.getAbsolutePath(), subtitle.getAbsolutePath(), pitch.getAbsolutePath()},
                            "task_idx=?",
                            new String[]{String.valueOf(task_idx)});

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            Runnable r = new LoadInterviewData();
            Thread t = new Thread(r);
            t.start();
        }
    }

    /* 로컬 DB 데이터를 로드 함 */
    class LoadInterviewData implements Runnable {
        @Override
        public void run() {
            DBHelper mDBHelper = DBHelper.getInstance(getApplicationContext());
            ContentValues[] values = mDBHelper.select("InterviewData", DBHelper.column_interviewdata, null, null, null, null, null);

            if (values != null) {

                items.clear();
                for (int i = 0; i < values.length; ++i) {
                    // InterviewData를 로드.
                    long idx = values[i].getAsLong("idx");
                    long user_idx = 0;
                    //long user_idx = values[i].getAsLong("user_idx");
                    String video_path = values[i].getAsString("video_path");
                    String emotion_path = values[i].getAsString("emotion_path");
                    String stt_path = values[i].getAsString("stt_path");
                    String pitch_path = values[i].getAsString("pitch_path");
                    long task_idx = values[i].getAsLong("task_idx");
                    long result_idx = values[i].getAsLong("result_idx");
                    long question_idx = values[i].getAsLong("question_idx");

                    // Question 데이터를 로드함
                    ContentValues[] question = mDBHelper.select("Question", DBHelper.column_questioninfo, "idx=?", new String[]{String.valueOf(question_idx)}, null, null, null);
                    if (question != null && question.length > 0) {
                        String title = question[0].getAsString("title");
                        String history = question[0].getAsString("history");
                        String duration = question[0].getAsString("duration");
                        String src_lang = question[0].getAsString("src_lang");
                        String dest_lang = question[0].getAsString("dest_lang");
                        String like_cnt = question[0].getAsString("like_cnt");
                        String view_cnt = question[0].getAsString("view_cnt");
                        String markdown_uri = question[0].getAsString("markdown_uri");
                        items.add(new item_result(idx, user_idx, video_path, emotion_path, stt_path, pitch_path, task_idx, result_idx, question_idx, title, history, duration, src_lang, dest_lang, view_cnt, like_cnt, markdown_uri));
                    }
                }
                handler.sendEmptyMessage(0);
            }
            // 다이얼로그 제거
            if (isDialogshow) {
                catLoadingView.dismiss();
                isDialogshow = false;
            }
        }
    }

    /**
     * 데이터 변경 시 뷰를 업데이트 한다
     */
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (Adapter == null) {
                Adapter = new ResultAdapter(items);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(Adapter);
            } else {
                recyclerView.getRecycledViewPool().clear();
                Adapter.notifyDataSetChanged();
            }
        }
    };

    /* 면접 결과 확인 다이얼로그 */
    public AlertDialog.Builder showDialogShowResult(final String Video_path, final String Emotion_path, final String Subtitle_path, final String Pitch_path) {
        AlertDialog.Builder builder = new AlertDialog.Builder(A_InterviewResult.this);
        builder.setTitle("결과 확인");
        builder.setMessage("면접 결과를 확인하시겠습니까?");
        builder.setPositiveButton("예",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // STT와 Emotion을 처리한다.
                        Intent intent = new Intent(A_InterviewResult.this, A_InterviewVideo.class);
                        intent.putExtra("Video_path", Video_path);
                        intent.putExtra("Emotion_path", Emotion_path);
                        intent.putExtra("Subtitle_path", Subtitle_path);
                        intent.putExtra("Pitch_path", Pitch_path);
                        startActivity(intent);
                    }
                });
        builder.setNegativeButton("아니오", null);
        return builder;
    }

    /* 분석 미완료 다이얼로그 */
    public void showDialogIsProcessing() {
        AlertDialog.Builder builder = new AlertDialog.Builder(A_InterviewResult.this);
        builder.setTitle("분석 중");
        builder.setMessage("서버에서 분석 중이에요. 결과가 나오면 푸시로 알려드릴게요.");
        builder.setPositiveButton("확인", null);
        builder.show();
    }

    /* 미업로드 비디오 다이얼로그 */
    public void showDialogUploadVideo(final int position, final long idx, final long question_idx, final String video_path) {
        AlertDialog.Builder builder = new AlertDialog.Builder(A_InterviewResult.this);
        builder.setTitle("분석 의뢰");
        builder.setMessage("영상을 서버로 업로드하여 분석을 진행할까요?");
        builder.setPositiveButton("예",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // 파일이 존재하는지 확인
                        File file = new File(video_path);
                        if(file.exists() != true) {
                            showDialogFileNotexist();
                            Adapter.removeItem(position);
                            deleteDB(idx);
                        }
                        else {
                            // 영상을 서버로 전송함
                            uploadVideo(idx, question_idx, video_path);
                        }
                    }
                });
        builder.setNegativeButton("아니오", null);
        builder.show();
    }

    /* 파일 미존재 다이얼로그 */
    public void showDialogFileNotexist() {
        AlertDialog.Builder builder = new AlertDialog.Builder(A_InterviewResult.this);
        builder.setTitle("데이터 에러");
        builder.setMessage("촬영된 면접 영상을 기기에서 찾을 수 없어요.");
        builder.setPositiveButton("확인", null);
        builder.show();
    }

    /* 파일 삭제 다이얼로그 */
    public void showDialogDeleteDB(final int position, final long idx) {
        AlertDialog.Builder builder = new AlertDialog.Builder(A_InterviewResult.this);
        builder.setTitle("데이터 삭제");
        builder.setMessage("인터뷰 기록을 삭제할까요?");
        builder.setPositiveButton("예",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        deleteDB(idx);
                        Adapter.removeItem(position);
                    }
                });
        builder.setNegativeButton("아니오", null);
        builder.show();
    }

    /* DB 삭제 */
    public void deleteDB(long idx) {
        DBHelper mDBHelper = DBHelper.getInstance(getApplicationContext());
        mDBHelper.delete("InterviewData", "idx=?", new String[]{String.valueOf(idx)}); // 로컬 DB에서 해당 InterviewData를 삭제함
    }

    /* 비디오를 업로드 한다 */
    public void uploadVideo(final long idx, final long question_idx, final String video_path) {
        Intent intent = new Intent(A_InterviewResult.this, UploadVideo.class);
        intent.putExtra("question_idx", question_idx);
        intent.putExtra("video_path", video_path);
        intent.putExtra("uri", "file://" + video_path);
        startActivity(intent);
    }

    class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.ViewHolder> {
        private ArrayList<item_result> mItems;

        public ResultAdapter(ArrayList items) {
            mItems = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_result, parent, false);
            recyclerView.setMinimumWidth(parent.getMeasuredWidth());
            ViewHolder holder = new ViewHolder(v);
            return holder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.t1.setText(mItems.get(position).getTitle());
            holder.t2.setText(mItems.get(position).getSrc_lang() + " -> " + mItems.get(position).getDest_lang() + "\n"
                    + mItems.get(position).getDuration() + "\n"
                    + mItems.get(position).getHistory());
        }

        public void removeItem(int position) {
            mItems.remove(position);
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            public TextView t1, t2;
            protected ImageButton ib_download, ib_share, ib_delete;

            public ViewHolder(View view) {
                super(view);
                t1 = (TextView) view.findViewById(R.id.text_interview_question);
                t2 = (TextView) view.findViewById(R.id.text_interview_meta);

                ib_download = (ImageButton) view.findViewById(R.id.ib_download);
                ib_share = (ImageButton) view.findViewById(R.id.ib_share);
                ib_delete = (ImageButton) view.findViewById(R.id.ib_delete);

                ib_download.setTag(R.integer.result_ib_download, view);
                ib_share.setTag(R.integer.result_ib_share, view);
                ib_delete.setTag(R.integer.result_ib_delete, view);

                ib_download.setOnClickListener(this);
                ib_share.setOnClickListener(this);
                ib_delete.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                item_result instance = mItems.get(getAdapterPosition()); // 인스턴스를 받아옴

                /* 다운로드 버튼을 클릭했을 때 */
                if (v.getId() == ib_download.getId()) {
                    // Case 1 온라인에 업로드 되지 않은 파일인 경우
                    if (instance.getTask_idx() == 0) {
                        showDialogUploadVideo(getAdapterPosition(), instance.getIdx(), instance.getQuestion_idx(), instance.getVideo_path());
                    }

                    // Case 2 온라인에 업로드 되어 아직 처리되지 않은 파일
                    else if (instance.getResult_idx() == 0) {
                        showDialogIsProcessing();
                    }

                    // Case 3 온라인에 업로드 되어 처리된 파일
                    else {
                        AlertDialog.Builder builder = showDialogShowResult(instance.getVideo_path(), instance.getEmotion_path(), instance.getStt_path(), instance.getPitch_path());
                        builder.show();
                    }
                    notifyDataSetChanged();
                }

                else if (v.getId() == ib_share.getId()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.comingSoonBody), Toast.LENGTH_LONG).show();
                }

                else if (v.getId() == ib_delete.getId()) {
                    showDialogDeleteDB(getAdapterPosition(), instance.getIdx());
                }
            }
        }
    }
}

