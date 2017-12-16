import ffmpy
import os
import httplib, urllib, base64
import extFinder

headers = {
    # Request headers
    'Content-Type': 'application/octet-stream',
    'Ocp-Apim-Subscription-Key': 'e2b5e793ad29432594bbbbf6b94a20ed',
}
params = urllib.urlencode({})

class mVideoProcessor(object):
    def __init__(self, folder_path, video_hash, file_name):
        print ("Start Video Processor")
        self.file_name = file_name

        self.Folder_path = folder_path
        self.Video_path = self.Folder_path + self.file_name
        self.Audio_path = self.Folder_path + video_hash
        self.Frame_path = self.Folder_path + "Frame/"
        self.video_frame_count = 0
        self.video_duration = 0
        self.audio_duration = 0
        self.audio_format = 0
        self.Emotion_path = self.Folder_path + video_hash + ".emo"
        self.emotion = []


    def run(self):
        try:
            self.preProcess()
            self.Process()
            return self.saveResult()
        except:
            return "Fail"


    def preProcess(self):
        print ("---- preProcess Video Data ----")
        
        print ("[1] Breakdown Video to Frame image")
        if not os.path.isdir(self.Frame_path): os.makedirs(self.Frame_path)      
        ff = ffmpy.FFmpeg(inputs={self.Video_path:None},
                          outputs = {self.Frame_path + 'frame_%04d.png': '-r 4 '})
        ff.run()

        try:
            print ("[2] Extract Audio from Video")
            ff = ffmpy.FFmpeg(inputs={self.Video_path:None},
                              outputs = {self.Audio_path: '-ar 44100 -ac 1 -c:a flac'})
            ff.run()
            self.audio_format = extFinder.findExt("Audio", "flac")
            self.audio_sample_rate = 44100
            
            print ("[3] Extract Duration")
            ff = ffmpy.FFprobe(inputs={self.Audio_path: '-show_entries stream=duration'})
            with open(self.Folder_path + 'temp', 'w') as output:
                ff.run(stdout = output)
            data = open(self.Folder_path + 'temp', 'r').readlines()

            self.video_duration = int(float(data[1][data[1].find('=') + 1:].strip()) * 100)
            self.audio_duration = int(float(data[4][data[4].find('=') + 1:].strip()) * 100)
            
        except:
            print "[!] output.flac already exists!"

        print ("---- Successfully preProcess Video Data ----")

        
    def Process(self):
        print ("---- Process Frame Data ----")
        print ("[1] Connecting to MS Azure Server")
        conn = httplib.HTTPSConnection('westus.api.cognitive.microsoft.com')
        
        print ("[2] Analyzing Frame")
        targets = os.listdir(self.Frame_path)

        self.video_frame_count = len(targets)
        Percentile = 0.1
        Processed_cnt = 0
        
        for target in targets:
            filename = self.Frame_path + target
            frame = open(filename, 'rb').read()
            conn.request("POST", "/emotion/v1.0/recognize?%s" % params, frame, headers)
            response = conn.getresponse()
            data = response.read()
            self.emotion.append(data)

            Processed_cnt += 1
            if Processed_cnt >= self.video_frame_count * Percentile:
                print str(Percentile * 100) + "% Complete"
                Percentile += 0.1

        print ("---- Successfully Analyze Frame Data ----")
        conn.close()


    def saveResult(self):
        print ("--- Write Video Result Data ---")

        print ("[1] Writing Emotion Data")
        f = open(self.Emotion_path, 'w')
        for emotion in self.emotion: f.write(emotion + "\n")
        f.close()
        
        self.emotion = []
        print ("--- Successfully Write Video Result Data ---")
        return "Success", self.video_duration, self.video_frame_count, self.audio_duration
        
mv = mVideoProcessor(0, './Video/TestUser/', 'sample.mp4')
mv.preProcess()
