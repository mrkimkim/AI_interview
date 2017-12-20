import ffmpy
import os
import http.client, urllib, base64
import extFinder
import shutil

headers = {
    # Request headers
    'Content-Type': 'application/octet-stream',
    'Ocp-Apim-Subscription-Key': 'e2b5e793ad29432594bbbbf6b94a20ed',
}
params = urllib.parse.urlencode({})

class mVideoProcessor(object):
    def __init__(self, UserData):
        print ("Start Video Processor")
        self.UserData = UserData
        self.file_name = self.UserData.file_name

        self.Folder_path = self.UserData.folder_path
        self.Frame_path = self.Folder_path + "Frame/"

        self.Emotion_path = self.Folder_path + self.UserData.video_hash + ".emo"
        self.emotion = []
        
    def run(self):
        print ("---- Process Frame Data ----")
        print ("[1] Connecting to MS Azure Server")
        conn = http.client.HTTPSConnection('westus.api.cognitive.microsoft.com')
        
        print ("[2] Analyzing Frame")
        targets = os.listdir(self.Frame_path)

        self.video_frame_count = len(targets)
        Percentile = 0.1
        Processed_cnt = 0

        targets.sort()
        for target in targets:
            filename = self.Frame_path + target
            frame = open(filename, 'rb').read()
            conn.request("POST", "/emotion/v1.0/recognize?%s" % params, frame, headers)
            response = conn.getresponse()
            data = response.read()

            self.emotion.append(data)
            os.remove(filename)

            Processed_cnt += 1
            if Processed_cnt >= self.video_frame_count * Percentile:
                print (str(int(Percentile * 100)) + "% Complete")
                Percentile += 0.1

        conn.close()
        shutil.rmtree(self.Frame_path)
        
        print ("[3] Write Emotion Data")
        f = open(self.Emotion_path, 'w')
        for emotion in self.emotion:
            emotion = str(emotion).replace('b','')
            f.write(emotion)
        f.close()
        
        self.emotion = []
        print ("--- Successfully Write Video Result Data ---")
        return self.Emotion_path, self.UserData
