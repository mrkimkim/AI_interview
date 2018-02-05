package portfolio.projects.mrkimkim.ai_interview;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
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

public class ActivityInterviewResult extends AppCompatActivity {
    Context mContext;
    RecyclerView recyclerView;
    ResultAdapter Adapter;
    RecyclerView.LayoutManager layoutManager;

    CatLoadingView catLoadingView;
    LinearLayout btn_back, btn_refresh;

    ArrayList<item_result> items;
    ArrayList<ServerData> mServerData;

    class ServerData{
        Long task_idx, result_idx;
        String emotion, subtitle;

        public ServerData(Long task_idx, Long result_idx, String emotion, String subtitle) {
            this.task_idx = task_idx;
            this.result_idx = result_idx;
            this.emotion = emotion;
            this.subtitle = subtitle;
        }

        public Long getTask_idx() { return this.task_idx; }
        public Long getResult_idx() { return this.result_idx; }
        public String getEmotion() { return this.emotion; }
        public String getSubtitle() { return this.subtitle; }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // RecyclerView
        mContext = getApplicationContext();
        recyclerView = (RecyclerView)findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);

        items = new ArrayList<item_result>();

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

        // DB를 업데이트 함
        refreshDB(getCurrentFocus());
    }

    public void refreshDB(View v) {
        catLoadingView = new CatLoadingView();
        catLoadingView.setCancelable(false);
        catLoadingView.show(getSupportFragmentManager(), "Loading DB");
        _downloadServerDB();
    }

    private void _downloadServerDB() {
        class t_downloadServerDB implements Runnable {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket();
                    socket.connect(new InetSocketAddress(getString(R.string.server_ip), Integer.parseInt(getString(R.string.cmd_server_port))), 1000);
                    InputStream networkReader = socket.getInputStream();
                    OutputStream networkWriter = socket.getOutputStream();

                    // 유저 인증 정보 전송
                    NetworkService.Auth_User(networkReader, networkWriter);

                    // 명령 코드 전송
                    networkWriter.write(Functions.intToBytes(2000));

                    // 결과 데이터 크기 수신
                    int offset = 0;
                    byte[] packet_size = new byte[8];
                    byte[] buffer = new byte[1024];
                    networkReader.read(packet_size, 0, 8);
                    byte[] packet = new byte[Functions.bytesToInt(packet_size)];
                    mServerData = new ArrayList<ServerData>();

                    if (Functions.bytesToInt(packet_size) > 0) {
                        // 전체 데이터 수집
                        while (offset < Functions.bytesToInt(packet)) {
                            int len = networkReader.read(buffer);
                            if (len > 0) {
                                System.arraycopy(buffer, 0, packet, offset, len);
                                offset += len;
                            }
                        }

                        offset = 0;
                        // 청크 사이즈
                        byte[] chunkCnt = new byte[8];
                        System.arraycopy(packet, 0, chunkCnt, 0, 8);
                        offset += 8;

                        // 각각의 청크를 탐색함
                        for(int i = 0; i < Functions.bytesToInt(chunkCnt); ++i) {
                            // TaskIdx와 ResultIdx
                            byte[] taskIdx = new byte[8];
                            byte[] resultIdx = new byte[8];
                            System.arraycopy(packet, offset, taskIdx, 0, 8);
                            System.arraycopy(packet, offset + 8, resultIdx, 0, 8);
                            offset += 16;

                            // Emotion
                            byte[] emotion_size = new byte[8];
                            System.arraycopy(packet, offset, emotion_size, 0, 8);
                            byte[] emotion = new byte[Functions.bytesToInt(emotion_size)];
                            System.arraycopy(packet, offset + 8, emotion, 0, Functions.bytesToInt(emotion_size));
                            offset += 8 + Functions.bytesToInt(emotion_size);

                            // Subtitle
                            byte[] subtitle_size = new byte[8];
                            System.arraycopy(packet, offset, subtitle_size, 0, 8);
                            byte[] subtitle = new byte[Functions.bytesToInt(subtitle_size)];
                            System.arraycopy(packet, offset + 8, subtitle, 0, Functions.bytesToInt(subtitle_size));
                            offset += 8 + Functions.bytesToInt(subtitle_size);

                            mServerData.add(new ServerData(Functions.bytesToLong(taskIdx),
                                    Functions.bytesToLong(resultIdx),
                                    new String(emotion, "utf-8"),
                                    new String(subtitle, "utf-8")));
                        }

                    }

                    _updateLocalDB();
                    _LoadInterviewData();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        Runnable t = new t_downloadServerDB();
        t.run();
    }

    public void _updateLocalDB() {
        DBHelper mDBHelper = DBHelper.getInstance(getApplicationContext());
        for(int i = 0; i < mServerData.size(); i++) {
            long task_idx = mServerData.get(i).getTask_idx();
            long result_idx = mServerData.get(i).getResult_idx();

            try {
                // 기기에 파일을 씀
                File emotion = new File(GlobalApplication.ApplicationDataPath + String.valueOf(result_idx) + "_emo");
                BufferedWriter out_emotion = new BufferedWriter(new FileWriter(emotion));
                out_emotion.write(mServerData.get(i).getEmotion());
                out_emotion.flush();
                out_emotion.close();

                File subtitle = new File(GlobalApplication.ApplicationDataPath + String.valueOf(result_idx) + "_stt");
                BufferedWriter out_subtitle = new BufferedWriter(new FileWriter(subtitle));
                out_subtitle.write(mServerData.get(i).getSubtitle());
                out_subtitle.flush();
                out_subtitle.close();

                // DB에 정보를 저장
                mDBHelper.update("InterviewData",
                        new String[]{"result_idx", "emotion_path", "stt_path"},
                        new String[]{String.valueOf(result_idx), emotion.getAbsolutePath(), subtitle.getAbsolutePath()},
                        "task_idx=?",
                        new String[]{String.valueOf(task_idx)});
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private void _LoadInterviewData() {
        class t_LoadInterviewData implements Runnable {
            @Override
            public void run() {
                DBHelper mDBHelper = DBHelper.getInstance(getApplicationContext());
                ContentValues[] values = mDBHelper.select("InterviewData", DBHelper.column_interviewdata, null, null, null, null, null);
                if (values == null) return;

                for (int i = 0; i < values.length; ++i) {
                    // InterviewData를 로드.
                    long idx = values[i].getAsLong("idx");
                    long user_idx = 0;
                    //long user_idx = values[i].getAsLong("user_idx");
                    String video_path = values[i].getAsString("video_path");
                    String emotion_path = values[i].getAsString("emotion_path");
                    String stt_path = values[i].getAsString("stt_path");
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
                        items.add(new item_result(idx, user_idx, video_path, emotion_path, stt_path, task_idx, result_idx, question_idx, title, history, duration, src_lang, dest_lang, view_cnt, like_cnt, markdown_uri));
                    }
                }

                // 어댑터 설정
                _setAdapter();

                // 다이얼로그 제거
                catLoadingView.dismiss();
            }
        }
        Runnable t = new t_LoadInterviewData();
        t.run();
    }

    private void _setAdapter() {
        Adapter = new ResultAdapter(items, mContext);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(Adapter);
    }

    class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.ViewHolder> {
        private Context context;
        private ArrayList<item_result> mItems;
        private int lastPosition = -1;

        public ResultAdapter(ArrayList items, Context mContext) {
            mItems = items;
            context = mContext;
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
                item_result instance = mItems.get(getAdapterPosition());

                if (v.getId() == ib_download.getId()) {
                    Log.d("Button Click Event", "Download " + String.valueOf(getAdapterPosition()));

                    // Case 1 온라인에 업로드 되지 않은 파일인 경우
                    if (instance.getTask_idx() == 0) {
                        showDialog_uploadVideo(getAdapterPosition(), instance.getIdx(), instance.getQuestion_idx(), instance.getVideo_path());
                    }

                    // Case 2 온라인에 업로드 되어 아직 처리되지 않은 파일
                    else if (instance.getResult_idx() == 0) {
                        showDialog_isprocessing();
                    }

                    // Case 3 온라인에 업로드 되어 처리된 파일
                    else {
                        showDialog_showResult(instance.getVideo_path(), instance.getEmotion_path(), instance.getStt_path());
                    }

                    notifyDataSetChanged();
                }

                else if (v.getId() == ib_share.getId()) {
                    Toast.makeText(getApplicationContext(), "준비중인 기능이에요!", Toast.LENGTH_LONG).show();
                }

                else if (v.getId() == ib_delete.getId()) {
                    showDialog_deleteDB(getAdapterPosition(), instance.getIdx());
                }
            }
        }
    }


    public AlertDialog.Builder showDialog_showResult(final String Video_path, final String Emotion_path, final String Subtitle_path) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityInterviewResult.this);
        builder.setTitle("결과 확인");
        builder.setMessage("면접 결과를 확인하시겠습니까?");
        builder.setPositiveButton("예",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // STT와 Emotion을 처리한다.
                        Intent intent = new Intent(ActivityInterviewResult.this, ActivityInterviewVideo.class);
                        intent.putExtra("Video_path", Video_path);
                        intent.putExtra("Emotion_path", Emotion_path);
                        intent.putExtra("Subtitle_path", Subtitle_path);
                        startActivity(intent);
                    }
                });
        builder.setNegativeButton("아니오", null);
        return builder;
    }

    public void showDialog_isprocessing() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityInterviewResult.this);
        builder.setTitle("분석 중");
        builder.setMessage("서버에서 분석 중이에요. 결과가 나오면 푸시로 알려드릴게요.");
        builder.setPositiveButton("확인", null);
        builder.show();
    }


    public void showDialog_uploadVideo(final int position, final long idx, final long question_idx, final String video_path) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityInterviewResult.this);
        builder.setTitle("분석 의뢰");
        builder.setMessage("영상을 서버로 업로드하여 분석을 진행할까요?");
        builder.setPositiveButton("예",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // 파일이 존재하는지 확인
                        File file = new File(video_path);
                        if(file.exists() != true) {
                            showDialog_fileNotexist();
                            Adapter.removeItem(position);
                            delete_DB(idx);
                        }
                        else {
                            // 영상을 서버로 전송함
                            upload_video(idx, question_idx, video_path);
                        }
                    }
                });
        builder.setNegativeButton("아니오", null);
        builder.show();
    }

    public void showDialog_fileNotexist() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityInterviewResult.this);
        builder.setTitle("데이터 에러");
        builder.setMessage("촬영된 면접 영상을 기기에서 찾을 수 없어요.");
        builder.setPositiveButton("확인", null);
        builder.show();
    }

    public void showDialog_deleteDB(final int position, final long idx) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityInterviewResult.this);
        builder.setTitle("데이터 삭제");
        builder.setMessage("인터뷰 기록을 삭제할까요?");
        builder.setPositiveButton("예",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        delete_DB(idx);
                        Adapter.removeItem(position);
                    }
                });
        builder.setNegativeButton("아니오", null);
        builder.show();
    }


    public void delete_DB(long idx) {
        DBHelper mDBHelper = DBHelper.getInstance(getApplicationContext());
        // 로컬 DB에서 해당 InterviewData를 삭제함
        mDBHelper.delete("InterviewData", "idx=?", new String[]{String.valueOf(idx)});
    }


    public void upload_video(final long idx, final long question_idx, final String video_path) {
        Intent intent = new Intent(ActivityInterviewResult.this, UploadVideo.class);
        intent.putExtra("question_idx", question_idx);
        intent.putExtra("video_path", video_path);
        intent.putExtra("uri", "file://" + video_path);
        startActivity(intent);
    }
}

