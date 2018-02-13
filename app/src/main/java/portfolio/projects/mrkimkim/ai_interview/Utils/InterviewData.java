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
        ArrayList<Float> timestamp;
        ArrayList<Float> happy;
        ArrayList<Float> neutral;
        ArrayList<Float> sad;
        ArrayList<Float> nervous;
        Float avg_happy = 0.0f, avg_neutral = 0.0f, avg_sad = 0.0f, avg_nervous = 0.0f;

        public EmotionData(String data) {
            timestamp = new ArrayList<Float>();
            happy = new ArrayList<Float>();
            neutral = new ArrayList<Float>();
            sad = new ArrayList<Float>();
            nervous = new ArrayList<Float>();
            this.loadData(data);
        }


        public void loadData(String data) {

            if (data == null) return;

            // Emotion 데이터를 로드
            JsonParser parser = new JsonParser();
            JsonElement element;
            data = data.replace("'[", "");

            Float Timeoffset = 0.0f;
            for (String s : data.split("\\]'")) {
                s = s.replace("]'", "");

                // Json 파싱
                element = parser.parse(s);
                JsonObject o = element.getAsJsonObject().get("scores").getAsJsonObject();

                Float happy, neutral, sad, nervous;

                // 스코어 로드
                try {
                    happy = Float.parseFloat(String.format("%.1f", o.get("happiness").getAsDouble() * 100.0));
                    avg_happy += happy;

                    neutral = Float.parseFloat(String.format("%.1f", o.get("neutral").getAsDouble() * 100.0 + o.get("surprise").getAsDouble() * 100.0));
                    avg_neutral += neutral;

                    sad = Float.parseFloat(String.format("%.1f", o.get("sadness").getAsDouble() * 100.0));
                    avg_sad += sad;

                    nervous = Float.parseFloat(String.format("%.1f", o.get("fear").getAsDouble() * 100.0 + o.get("contempt").getAsDouble() * 100.0 + o.get("disgust").getAsDouble() * 100.0));
                    avg_nervous += nervous;
                } catch (Exception e) {
                    happy = 0.0f;
                    neutral = 0.0f;
                    sad = 0.0f;
                    nervous = 0.0f;
                }

                // 스코어 저장
                this.addItem(Timeoffset, happy, neutral, sad, nervous);
                Timeoffset += 0.2f;
            }

            // 평균 값
            if (Timeoffset > 0) {
                avg_happy /= (Timeoffset * 5);
                avg_neutral /= (Timeoffset * 5);
                avg_sad /= (Timeoffset * 5);
                avg_nervous /= (Timeoffset * 5);
            }
        }

        public void addItem(Float timestamp, Float happy, Float neutral, Float sad, Float nervous) {
            this.timestamp.add(timestamp);
            this.happy.add(happy);
            this.neutral.add(neutral);
            this.sad.add(sad);
            this.nervous.add(nervous);
        }

        public Float getAvg_happy() { return this.avg_happy; }
        public Float getAvg_neutral() { return this.avg_neutral; }
        public Float getAvg_sad() { return this.avg_sad; }
        public Float getAvg_nervous() { return this.avg_nervous; }

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

        public ArrayList<Float> getTimestamp() { return this.timestamp; }
        public ArrayList<Float> getHappy() { return  this.happy; }
        public ArrayList<Float> getNeutral() { return  this.neutral; }
        public ArrayList<Float> getSad() { return  this.sad; }
        public ArrayList<Float> getNervous() { return  this.nervous; }
    }



    public class SubtitleData {
        String text;
        ArrayList<Float> timestamp;
        ArrayList<String> word;
        ArrayList<Float> wps;
        Float total_wps, avg_wps, tick;

        public SubtitleData(String data) {
            timestamp = new ArrayList<Float>();
            word = new ArrayList<String>();
            wps = new ArrayList<Float>();
            total_wps = 0.0f;
            tick = 0.0f;
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
            Float firstSpeakTime = 0.0f;
            Float wordTime = 0.0f;
            Float subtitleTime = 0.0f;
            Float new_timestamp = 0.0f;
            Float word_cnt = 0.0f;

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
                new_timestamp = Float.valueOf(rows[i].substring(o1, o2));

                Log.d("Word : ", new_word + "/" + String.valueOf(new_timestamp));
                word_cnt += 1;

                // === 첫 단어가 등장하기 전까지의 데이터를 세팅한다. ===
                if (subtitleTime <= 0.0 || subtitleTime - firstSpeakTime < 0.2) {
                    while (subtitleTime < new_timestamp) {
                        word.add("");
                        timestamp.add(subtitleTime);
                        wps.add(0.0f);
                        subtitleTime += 0.2f;
                    }
                    wordTime = subtitleTime;
                    if (firstSpeakTime == 0.0f) {
                        firstSpeakTime = new_timestamp;
                    }
                } else {
                    // ===== WPS 계산부 =======
                    while (wordTime < new_timestamp) {
                        wps.add(Float.parseFloat(String.format("%.1f", word_cnt / (wordTime - firstSpeakTime))));
                        total_wps += Float.parseFloat(String.format("%.1f", word_cnt / (wordTime - firstSpeakTime)));
                        tick += 1;
                        wordTime += 0.2f;
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
                        subtitleTime += 0.2f;
                    }
                    prevword = new_word;
                }
            }

            while(wordTime < new_timestamp + 1.0f) {
                wps.add(Float.parseFloat(String.format("%.1f",word_cnt / (wordTime - firstSpeakTime))));
                wordTime += 0.2f;
                total_wps += Float.parseFloat(String.format("%.1f", word_cnt / (wordTime - firstSpeakTime)));
                tick += 1;
            }

            while(subtitleTime < new_timestamp + 1.0f) {
                word.add(prevword);
                timestamp.add(subtitleTime);
                subtitleTime += 0.2f;
            }

            if (tick > 0) avg_wps = total_wps / tick;
            else avg_wps = 0.0f;
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
        public ArrayList<Float> getTimestamp() { return this.timestamp; }
        public ArrayList<Float> getWps() { return this.wps; }
        public Float getAvg_wps() { return this.avg_wps; }
    }


    public class PitchData {
        ArrayList<Float> timestamp;
        ArrayList<Float> pitch;
        ArrayList<Float> confidence;
        Float sum_pitch, avg_pitch;

        public PitchData(String data) {
            timestamp = new ArrayList<Float>();
            pitch = new ArrayList<Float>();
            confidence = new ArrayList<Float>();
            sum_pitch = 0.0f;
            avg_pitch = 0.0f;
            this.loadData(data);
        }

        public void loadData(String data) {
            if (data == null) return;

            String[] rows = data.split("\\r?\\n");

            Float timestamp = 0.0f;
            Float total_pitch = 0.0f;
            Float total_confidence = 0.0f;
            Float tick = 0.0f;

            for(int i = 0; i < rows.length; ++i) {
                int o1 = 0;
                int o2 = rows[i].indexOf(' ');

                // 발음 시작시간 파싱
                Float currentTime = Float.valueOf(rows[i].substring(o1, o2));

                // Pitch 파싱
                o1 = o2 + 1;
                rows[i] = rows[i].substring(o1);
                o2 = rows[i].indexOf(' ');
                Float pitch = Float.valueOf(rows[i].substring(0, o2));

                // Confidence 파싱
                o1 = o2 + 1;
                Float confidence = 0.0f;
                if (!rows[i].substring(o1).equals("nan")) confidence = Float.valueOf(rows[i].substring(o1));

                // 0.2초의 간격으로 Pitch를 수집함
                if (currentTime < timestamp + 0.2) {
                    // 가청 주파수이며 Confidence 0.5 이상인 것만 수집
                    if (pitch >= 55.0 && pitch <= 300.0 && confidence >= 0.5) {
                        total_pitch += pitch * confidence;
                        total_confidence += confidence;
                    }
                } else {
                    this.timestamp.add(timestamp);
                    timestamp += 0.2f;
                    if (total_confidence <= 0.0) this.pitch.add(0.0f);
                    else {
                        this.pitch.add(total_pitch / total_confidence);
                        avg_pitch += total_pitch / total_confidence;
                        tick += 1;
                    }
                    total_pitch = 0.0f;
                    total_confidence = 0.0f;
                }
            }

            // 남는 시간처리
            this.timestamp.add(timestamp);
            timestamp += 0.2f;
            if (total_confidence <= 0.0) this.pitch.add(0.0f);
            else {
                this.pitch.add(total_pitch / total_confidence);
                avg_pitch += total_pitch / total_confidence;
                tick += 1;
            }

            if (tick > 0) avg_pitch /= tick;
        }

        public String getSinglePitch(int timestamp) {
            int index = timestamp / 2;
            if (index >= this.pitch.size()) return "0.0";
            else return String.format("%.1f",this.pitch.get(index));
        }

        public Float getAvg_pitch() { return this.avg_pitch; }
        public ArrayList<Float> getTimestamp() { return this.timestamp; }
        public ArrayList<Float> getPitch() { return this.pitch; };

    }
}
