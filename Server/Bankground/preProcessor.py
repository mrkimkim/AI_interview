import os
import hashlib
import extFinder
import ffmpy

# Single Thread Processing Task Queue
class mPreProcessor(object):
    def __init__(self, UserData):
        self.UserData = UserData

        self.Folder_path = self.UserData.folder_path
        self.Video_path = self.Folder_path + self.UserData.video_hash + '.' + extFinder.findExt("Video", self.UserData.video_format)
        self.Audio_path = self.Folder_path + "temp.flac"
        self.Frame_path = self.Folder_path + "Frame/"
        
    def run(self):
        print ("---- preProcess Video Data ----")
        print ("[1] Breakdown Video to Frame image")

        if not os.path.isdir(self.Frame_path): os.makedirs(self.Frame_path)      

        """ Generate 4 frame per second """
        ff = ffmpy.FFmpeg(inputs={self.Video_path:None},
                          outputs = {self.Frame_path + 'frame_%04d.png': '-r 4 '})
        ff.run()
        _, _, frames = os.walk(self.Frame_path).__next__()
        self.UserData.video_frame_count = len(frames)



        try:
            print ("[2] Extract Audio from Video")
            ''' Generate flac audio from video '''
            
            ff = ffmpy.FFmpeg(inputs={self.Video_path:None},
                              outputs = {self.Audio_path: '-ar 44100 -ac 1 -c:a flac'})
            ff.run()

            self.UserData.audio_format = extFinder.findExt("Audio", "flac")
            self.UserData.audio_sample_rate = 44100
        except:
            print ("Audio Already Exists")
 
        ''' reload audio and generate hash and save '''
        audio_data = open(self.Audio_path, 'rb').read()
        self.UserData.audio_hash = hashlib.sha256(audio_data).hexdigest()
        os.remove(self.Audio_path)

        self.Audio_path = self.Folder_path + self.UserData.audio_hash
        audio_output = open(self.Audio_path, 'wb')
        audio_output.write(audio_data)
        audio_output.close()        


            
        print ("[3] Extract Duration")
        """ extract duration [0] : video / [1] : audio """
        ff = ffmpy.FFprobe(inputs={self.Video_path: '-show_entries stream=duration'})
        with open(self.Folder_path + 'temp', 'w') as output:
            ff.run(stdout = output)
        data = open(self.Folder_path + 'temp', 'r').readlines()
        
        ''' save duration info '''
        self.UserData.video_duration = int(float(data[1][data[1].find('=') + 1:].strip()) * 100)
        self.UserData.audio_duration = int(float(data[4][data[4].find('=') + 1:].strip()) * 100)
            
        os.remove(self.Folder_path + 'temp')


        print ("[4] Set Size")
        f = open(self.Video_path, 'rb')
        f.seek(0, os.SEEK_END)
        self.UserData.video_size = f.tell()
        f.close()

        f = open(self.Audio_path, 'rb')
        f.seek(0, os.SEEK_END)
        self.UserData.audio_size = f.tell()
        f.close()

        return self.UserData
        
        
