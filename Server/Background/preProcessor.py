import os
import hashlib
import extFinder
import ffmpy
from Scheme import TmpInfo

# Single Thread Processing Task Queue
class mPreProcessor(object):
    def __init__(self, InterviewData, bucket):
        self.mInterviewData = InterviewData
        self.bucket = bucket

        self.storage_blob = str(self.mInterviewData.user_idx) + '/' + self.mInterviewData.video_hash + '/'
        self.video_blob = self.storage_blob + 'Video'
        self.audio_blob = self.storage_blob + 'Audio'
        
        self.tmp_folder = '/tmp/' + str(self.mInterviewData.user_idx) + '_' + self.mInterviewData.video_hash + '/'
        self.tmp_video = self.tmp_folder + 'video.mp4'
        self.tmp_audio = self.tmp_folder + 'audio.flac'
        
    def run(self):
        print ("---- preProcess Video Data ----")
        
        print ("[0] Get Video from GCS")
        if not os.path.exists(self.tmp_folder): os.makedirs(self.tmp_folder)
        
        blob = self.bucket.blob(self.video_blob)
        with open(self.tmp_video,'wb') as tmp_file:
            blob.download_to_file(tmp_file)


            
        print ("[1] Breakdown Video to Frame image")
        """ Generate 4 frame per second """
        ff = ffmpy.FFmpeg(inputs={self.tmp_video:None},
                          outputs = {self.tmp_folder + 'frame_%04d.png': '-r 5 '})
        ff.run()
        _, _, frames = os.walk(self.tmp_folder).__next__()
        self.mInterviewData.video_frame_count = len(frames) - 1


        try:
            print ("[2] Extract Audio from Video")
            ''' Generate flac audio from video '''
            
            ff = ffmpy.FFmpeg(inputs={self.tmp_video:None},
                              outputs = {self.tmp_audio: '-ar 44100 -ac 1 -c:a flac'})
            ff.run()

            self.mInterviewData.audio_format = extFinder.findExt("Audio", "flac")
            self.mInterviewData.audio_sample_rate = 44100
        except:
            print ("Audio Already Exists")



        print ("[3] Extract Duration")
        """ extract duration [0] : video / [1] : audio """
        ff = ffmpy.FFprobe(inputs={self.tmp_video: '-show_entries stream=duration'})
        with open(self.tmp_folder + 'temp', 'w') as output:
            ff.run(stdout = output)
        data = open(self.tmp_folder + 'temp', 'r').readlines()

        """ Save Duration Info """
        self.mInterviewData.video_duration = int(float(data[1][data[1].find('=') + 1:].strip()) * 100)
        self.mInterviewData.audio_duration = self.mInterviewData.video_duration
            
        os.remove(self.tmp_folder + 'temp')



        print ("[4] Set Size and generate hash")
        f = open(self.tmp_video, 'rb')
        f.seek(0, os.SEEK_END)
        self.mInterviewData.video_size = f.tell()
        f.close()

        audio_data = open(self.tmp_audio, 'rb').read()
        self.mInterviewData.audio_size = len(audio_data)



        print ("[5] Save Audio to GCS")
        self.mInterviewData.audio_hash = hashlib.sha256(audio_data).hexdigest()

        blob = self.bucket.blob(self.audio_blob)
        blob.upload_from_string(audio_data)

        mTmpInfo = TmpInfo(self.storage_blob, self.video_blob, self.audio_blob, self.tmp_folder, self.tmp_video, self.tmp_audio)

        return self.mInterviewData, mTmpInfo
        
        
