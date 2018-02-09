package portfolio.projects.mrkimkim.ai_interview.Utils;

import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Time;
import java.util.ArrayList;

/**
 * Created by JHG on 2018-02-04.
 */

public class InterviewData {
    public EmotionData mEmotionData;
    public SubtitleData mSubtitleData;
    public PitchData mPitchData;

    public InterviewData(String Emotion_path, String STT_path, String Pitch_path) {
        mEmotionData = new EmotionData(this.readFile(Emotion_path));
        mSubtitleData = new SubtitleData(this.readFile(STT_path));
        mPitchData = new PitchData(this.readFile(Pitch_path));
    }

    public EmotionData getmEmotionData() { return this.mEmotionData; }
    public SubtitleData getmSubtitleData() { return this.mSubtitleData; }
    public PitchData getmPitchData() { return this.mPitchData; }

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

    public class EmotionData {
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

            int Timeoffset = 0;
            for (String s : data.split("\\]'")) {
                s = s.replace("]'", "");

                // Json 파싱
                element = parser.parse(s);
                JsonObject o = element.getAsJsonObject().get("scores").getAsJsonObject();

                // 스코어 로드
                Double happy = Double.parseDouble(String.format("%.1f",o.get("happiness").getAsDouble() * 100.0));
                Double neutral = Double.parseDouble(String.format("%.1f",o.get("neutral").getAsDouble() * 100.0 + o.get("surprise").getAsDouble() * 100.0));
                Double sad = Double.parseDouble(String.format("%.1f",o.get("sadness").getAsDouble() * 100.0));
                Double nervous = Double.parseDouble(String.format("%.1f",o.get("fear").getAsDouble() * 100.0 + o.get("contempt").getAsDouble() * 100.0 + o.get("disgust").getAsDouble() * 100.0));

                this.addItem(String.valueOf(Timeoffset), happy, neutral, sad, nervous);
                Timeoffset += 2;
            }
        }

        public void addItem(String timestamp, Double happy, Double neutral, Double sad, Double nervous) {
            this.timestamp.add(timestamp);
            this.happy.add(happy);
            this.neutral.add(neutral);
            this.sad.add(sad);
            this.nervous.add(nervous);
        }

        public String getSingleHappy(int timestamp) {
            int index = timestamp / 2;
            if (index >= happy.size()) return "HAPPY (0%)";
            return "HAPPY (" + String.valueOf(happy.get(index)) + "%)";
        }

        public String getSingleNeutral(int timestamp) {
            int index = timestamp / 2;
            if (index >= neutral.size()) return "NEUTRAL (0%)";
            return "NEUTRAL (" + String.valueOf(neutral.get(index)) + "%)";
        }

        public String getSingleSad(int timestamp) {
            int index = timestamp / 2;
            if (index >= sad.size()) return "SAD (0%)";
            return "SAD (" + String.valueOf(sad.get(index)) + "%)";
        }

        public String getSingleNervous(int timestamp) {
            int index = timestamp / 2;
            if (index >= nervous.size()) return "NERVOUS (0%)";
            return "NERVOUS (" + String.valueOf(nervous.get(index)) + "%)";
        }

        public ArrayList<String> getTimestamp() { return this.timestamp; }
        public ArrayList<Double> getHappy() { return  this.happy; }
        public ArrayList<Double> getNeutral() { return  this.neutral; }
        public ArrayList<Double> getSad() { return  this.sad; }
        public ArrayList<Double> getNervous() { return  this.nervous; }
    }



    public class SubtitleData {
        String text;
        ArrayList<Double> timestamp;
        ArrayList<String> word;
        ArrayList<Double> wps;


        public SubtitleData(String data) {
            timestamp = new ArrayList<Double>();
            word = new ArrayList<String>();
            wps = new ArrayList<Double>();
            this.loadData(data);
        }

        public void loadData(String data) {

            if (data == null) return;

            // 내용 전문 저장
            String[] rows = data.split("\\r?\\n");
            this.text = rows[0].replace("Transcript: ","");

            // 시간대별 단어 저장

            // 0.2초 단위로 시간 계속 증가

            String prevword = "";
            Double firstSpeakTime = 0.0;
            Double wordTime = 0.0;
            Double subtitleTime = 0.0;
            Double new_timestamp = 0.0;
            Double word_cnt = 0.0;

            for(int i = 2; i < rows.length; ++i) {
                int o1 = rows[i].indexOf(" ");
                int o2 = rows[i].indexOf(",");
                // 단어 추출
                String new_word = rows[i].substring(o1 + 1, o2);
                Log.d("RAW :", rows[i]);

                rows[i] = rows[i].substring(o2 + 1);
                Log.d("RAW2 : ", rows[i]);
                o1 = 0;
                o2 = rows[i].indexOf(",");

                // 시작 오프셋 추출
                new_timestamp = Double.valueOf(rows[i].substring(o1, o2));

                Log.d("Word : ", new_word + "/" + String.valueOf(new_timestamp));
                word_cnt += 1;

                // 첫 단어가 등장하기 전까지의 데이터를 세팅한다.
                if (subtitleTime <= 0.0 || subtitleTime - firstSpeakTime < 0.2) {
                    while (subtitleTime < new_timestamp) {
                        word.add("");
                        timestamp.add(subtitleTime);
                        wps.add(0.0);
                        subtitleTime += 0.2;
                    }
                    wordTime = subtitleTime;
                    if (firstSpeakTime == 0.0) {
                        firstSpeakTime = new_timestamp;
                    }
                } else {
                    // ===== WPS 계산부 =======
                    while (wordTime < new_timestamp) {
                        wps.add(Double.parseDouble(String.format("%.1f", word_cnt / (wordTime - firstSpeakTime))));
                        wordTime += 0.2;
                    }
                }

                // ===== 자막 생성부 =====
                // 누적 길이가 25에 도달할때까지 쌓으며
                if (prevword.length() + new_word.length() <= 20) {
                    prevword += " " + new_word;
                } else {
                    // 도달한다면 그 길이까지 0.2 단위로 데이터를 씌운다.
                    while(subtitleTime < new_timestamp) {
                        word.add(prevword);
                        timestamp.add(subtitleTime);
                        subtitleTime += 0.2;
                    }
                    prevword = new_word;
                }
            }

            while(wordTime < new_timestamp + 1.0) {
                wps.add(Double.parseDouble(String.format("%.1f",word_cnt / (wordTime - firstSpeakTime))));
                wordTime += 0.2;
            }

            while(subtitleTime < new_timestamp + 1.0) {
                word.add(prevword);
                timestamp.add(subtitleTime);
                subtitleTime += 0.2;
            }
        }

        public String getSingleWPS(int timestamp) {
            int index = timestamp / 2;
            if (index >= wps.size()) {
                return "0.0";
            }
            return String.valueOf(wps.get(index));
        }

        public String getSingleWord(int timestamp) {
            int index = timestamp / 2;
            if (index >= word.size()) {
                return "";
            }
            return word.get(index);
        }

        public String getText() { return this.text; }
        public ArrayList<Double> getTimestamp() { return this.timestamp; }
    }


    public class PitchData {
        ArrayList<Double> timestamp;
        ArrayList<Double> pitch;
        ArrayList<Double> confidence;

        public PitchData(String data) {
            timestamp = new ArrayList<Double>();
            pitch = new ArrayList<Double>();
            confidence = new ArrayList<Double>();
            this.loadData(data);
        }

        public void loadData(String data) {
            if (data == null) return;

            String[] rows = data.split("\\r?\\n");

            Double timestamp = 0.0;
            Double total_pitch = 0.0;
            Double total_confidence = 0.0;

            for(int i = 0; i < rows.length; ++i) {
                int o1 = 0;
                int o2 = rows[i].indexOf(' ');

                Double currentTime = Double.valueOf(rows[i].substring(o1, o2));

                o1 = o2 + 1;
                rows[i] = rows[i].substring(o1);
                o2 = rows[i].indexOf(' ');
                Double pitch = Double.valueOf(rows[i].substring(0, o2));

                o1 = o2 + 1;
                Double confidence = 0.0;
                if (!rows[i].substring(o1).equals("nan")) confidence = Double.valueOf(rows[i].substring(o1));

                if (currentTime < timestamp + 0.2) {
                    if (pitch >= 55.0 && pitch <= 300.0 && confidence >= 0.5) {
                        total_pitch += pitch * confidence;
                        total_confidence += confidence;
                    }
                } else {
                    this.timestamp.add(timestamp);
                    if (total_confidence <= 0.0) this.pitch.add(0.0);
                    else this.pitch.add(total_pitch / total_confidence);
                    total_pitch = 0.0;
                    total_confidence = 0.0;
                    timestamp += 0.2;
                }
            }
            this.timestamp.add(timestamp);
            if (total_confidence <= 0.0) this.pitch.add(0.0);
            else this.pitch.add(total_pitch / total_confidence);
        }

        public String getSinglePitch(int timestamp) {
            int index = timestamp / 2;
            if (index >= this.pitch.size()) return "0.0";
            else return String.format("%.1f",this.pitch.get(index));
        }
    }
}
