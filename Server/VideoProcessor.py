import ffmpy
import os
import httplib, urllib, base64

headers = {
    # Request headers
    'Content-Type': 'application/octet-stream',
    'Ocp-Apim-Subscription-Key': 'e2b5e793ad29432594bbbbf6b94a20ed',
}
params = urllib.urlencode({})

class mVideoProcessor(object):
    def __init__(self, sql, file_path):
        print ("Start Video Processor")
        self.sql = sql
        self.file_path = file_path
        self.Audio_path = ""
        self.Frame_path = ""
        self.Video_result = ""
        
        self.preProcess()
        self.Process()
        self.Result()


    def preProcess(self):
        print ("[1] Breakdown Video to Frame image")

        folder_path = self.file_path[:self.file_path.rfind('.')]
        if not os.path.isdir(folder_path): os.makedirs(folder_path)      
        ff = ffmpy.FFmpeg(inputs={self.file_path:None},
                          outputs = {folder_path + '/output_%04d.png': '-r 5 '})
        ff.run()

        print ("[2] Extract Audio from Video")
        ff = ffmpy.FFmpeg(inputs={self.file_path:None},
                          outputs = {folder_path[:folder_path.rfind('/')] + '/output.flac': '-ar 44100 -ac 1 -c:a flac'})
        ff.run()

        print ("[3] End")
        self.Frame_path = folder_path

        
    def Process(self):
        print ("[1] Connecting to MS Azure Server")
        conn = httplib.HTTPSConnection('westus.api.cognitive.microsoft.com')

        print ("[2] Analyzing Frame")
        targets = os.listdir(self.Frame_path)
        for target in targets:
            filename = self.Frame_path + '/' + target
            frame = open(filename, 'rb').read()
            conn.request("POST", "/emotion/v1.0/recognize?%s" % params, data, headers)
            response = conn.getresponse()
            data = response.read()
            print (data)

        print ("[3] Successfully Analyzed Frame")
        conn.close()
            
        
mv = mVideoProcessor(0, './Video/TestUser/sample.mp4')
mv.preProcess()
