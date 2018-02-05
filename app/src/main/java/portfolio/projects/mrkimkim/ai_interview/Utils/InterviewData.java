package portfolio.projects.mrkimkim.ai_interview.Utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

/**
 * Created by JHG on 2018-02-04.
 */

public class InterviewData {
    public EmotionData mEmotionData;
    public SubtitleData mSubtitleData;

    public InterviewData(String Emotion_path, String STT_path) {
        mEmotionData = new EmotionData(this.readFile(Emotion_path));
        mSubtitleData = new SubtitleData(this.readFile(STT_path));
    }

    public EmotionData getmEmotionData() { return this.mEmotionData; }
    public SubtitleData getmSubtitleData() { return this.mSubtitleData; }

    // UTF-8 형태의 파일을 읽음
    public static String readFile(String path) {
        int readcount=0;
        File file = new File(path);
        if(file!=null&&file.exists()){
            try {
                FileInputStream fis = new FileInputStream(file);
                readcount = (int)file.length();
                byte[] buffer = new byte[readcount];
                fis.read(buffer);
                fis.close();
                return new String(buffer, "utf-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    class EmotionData {
        ArrayList<String> timestamp;
        ArrayList<Double> happy;
        ArrayList<Double> neutral;
        ArrayList<Double> sad;
        ArrayList<Double> nervous;

        public EmotionData(String data) {
            timestamp = new ArrayList<String>();
            happy = new ArrayList<Double>();
            neutral = new ArrayList<Double>();
            sad = new ArrayList<Double>();
            nervous = new ArrayList<Double>();
            this.loadData(data);
        }


        public void loadData(String data) {

            if (data == null) return;

            // Emotion 데이터를 로드
            JsonParser parser = new JsonParser();
            JsonElement element;
            data = data.replace("'[", "");

            for (String s : data.split("\\]'")) {
                s = s.replace("]'", "");

                // Json 파싱
                element = parser.parse(s);
                JsonObject o = element.getAsJsonObject().get("scores").getAsJsonObject();

                // 스코어 로드
                Double happy = o.get("happiness").getAsDouble();
                Double neutral = o.get("neutral").getAsDouble() + o.get("surprise").getAsDouble();
                Double sad = o.get("sadness").getAsDouble();
                Double nervous = o.get("fear").getAsDouble() + o.get("contempt").getAsDouble() + o.get("disgust").getAsDouble();

                this.addItem("00:05", happy, neutral, sad, nervous);
            }
        }

        public void addItem(String timestamp, Double happy, Double neutral, Double sad, Double nervous) {
            this.timestamp.add(timestamp);
            this.happy.add(happy);
            this.neutral.add(neutral);
            this.sad.add(sad);
            this.nervous.add(nervous);
        }

        public ArrayList<String> getTimestamp() { return this.timestamp; }
        public ArrayList<Double> getHappy() { return  this.happy; }
        public ArrayList<Double> getNeutral() { return  this.neutral; }
        public ArrayList<Double> getSad() { return  this.sad; }
        public ArrayList<Double> getNervous() { return  this.nervous; }
    }



    class SubtitleData {
        String text;
        ArrayList<Double> timestamp;
        ArrayList<String> word;


        public SubtitleData(String data) {
            timestamp = new ArrayList<Double>();
            word = new ArrayList<String>();
            this.loadData(data);
        }

        public void loadData(String data) {

            if (data == null) return;

            // 내용 전문 저장
            String[] rows = data.split("\\r?\\n");
            this.text = rows[0].replace("Transcript: ","");

            // 시간대별 단어 저장
            for(int i = 2; i < rows.length; ++i) {
                int o1 = rows[i].indexOf(" ");
                int o2 = rows[i].indexOf(",");
                word.add(rows[i].substring(o1 + 1, o2));

                rows[i] = rows[i].substring(o2 + 2);
                o1 = rows[i].indexOf(" ");
                o2 = rows[i].indexOf(",");
                timestamp.add(Double.valueOf(rows[i].substring(o1 + 1, o2)));
            }
        }

        public String getText() { return this.text; }
        public ArrayList<Double> getTimestamp() { return this.timestamp; }
        public ArrayList<String> getWord() { return this.word; }
    }
}
