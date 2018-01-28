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
    def __init__(self, mTmpInfo):
        print ("Start Video Processor")
        self.mTmpInfo = mTmpInfo

        self.mTmpInfo.tmp_emotion = self.mTmpInfo.tmp_folder + "emotion.emo"
        self.emotion = []
        
    def run(self):
        print ("---- Process Frame Data ----")
        print ("[1] Connecting to MS Azure Server")
        conn = http.client.HTTPSConnection('westus.api.cognitive.microsoft.com')



        
        print ("[2] Analyzing Frame")
        targets = os.listdir(self.mTmpInfo.tmp_folder)

        self.video_frame_count = len(targets)
        Percentile = 0.1
        Processed_cnt = 0

        targets.sort()
        for target in targets:
            if 'frame' in target:
                frame = open(self.mTmpInfo.tmp_folder + target, 'rb').read()
                conn.request("POST", "/emotion/v1.0/recognize?%s" % params, frame, headers)
                response = conn.getresponse()
                data = response.read()
    
                self.emotion.append(data)
                os.remove(self.mTmpInfo.tmp_folder + target)
    
                Processed_cnt += 1
                if Processed_cnt >= self.video_frame_count * Percentile:
                    print (str(int(Percentile * 100)) + "% Complete")
                    Percentile += 0.1
        conn.close()



        
        print ("[3] Write Emotion Data")
        with open(self.mTmpInfo.tmp_emotion, 'w') as f:
            for emotion in self.emotion:
                f.write(str(emotion).replace('b',''))
        self.emotion = []



        
        print ("--- Successfully Write Video Result Data ---")
        return self.mTmpInfo.tmp_emotion, self.mTmpInfo
