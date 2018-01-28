import pymysql
import time
import random
import sys
import Scheme

import preProcessor
import VideoProcessor
import AudioProcessor
import PushServer
import extFinder

def get_storage_bucket():
    from google.cloud import storage
    from google.cloud import speech
    from google.cloud.speech import enums
    from google.cloud.speech import types

    storage_client = storage.Client()
    bucket = storage_client.get_bucket("ai_interview")
    return bucket

# Single Thread Processing Task Queue
class mBackgroundProcessor(object):
    def __init__(self):
        self.sql = pymysql.connect(host='localhost',
                                   user='root',
                                   password='lfamesk5uf!@#',
                                   db = 'AI',
                                   charset='utf8',
                                   autocommit=True
                                   )

    def generate_seed(self):
        return random.randrange(1, 2 ** 31)
        
    def run(self):
        cur = self.sql.cursor()
        try:
            while True:
                ''' [1] Pick Task From Queue '''
                cur.execute("select idx from TaskQueue where (`isProcessed` = 0 AND `isError` = 0 AND `lock` = 0) limit 1")
                rows = cur.fetchall()
                if len(rows) > 0:
                    ''' [2] Lock the Row '''
                    idx = int(rows[0][0])
                    lock_seed = self.generate_seed()
                    cur.execute("UPDATE TaskQueue SET `lock` = %s, `reg_date` = now() where `idx` = %s", (lock_seed, idx))

                    time.sleep(1)
                    cur.execute("select idx, user_idx, interviewdata_idx from TaskQueue where (`lock` = " + str(lock_seed) + ") limit 1")
                    rows = cur.fetchall()
                    
                    """ [3] Get InterviewData from Database """
                    if len(rows) > 0:
                        task_idx, user_idx, interviewdata_idx = rows[0][0], rows[0][1], rows[0][2]
                        
                        mInterviewData = Scheme.InterviewData(user_idx)
                        mResultInfo = Scheme.ResultInfo(user_idx, interviewdata_idx)
                        
                        cur.execute("select video_hash, video_format from InterviewData where `idx` = %s", (interviewdata_idx))
                        rows = cur.fetchall()

                        mInterviewData.idx, mInterviewData.user_idx = interviewdata_idx, user_idx
                        mInterviewData.video_hash, mInterviewData.video_format = rows[0][0], rows[0][1]


                        """ [4] PreProcess Data """
                        mPreProcessor = preProcessor.mPreProcessor(mInterviewData, get_storage_bucket())
                        mInterviewData, mTmpInfo = mPreProcessor.run()
                        mPreProcessoor = None

                        
                        ''' [5-1] Process Video Data '''
                        mVideoProcessor = VideoProcessor.mVideoProcessor(mTmpInfo)
                        mResultInfo.emotion_path, mTmpInfo = mVideoProcessor.run()
                        mVideoProcessor = None

                        
                        ''' [5-2] Process Audio Data '''
                        mAudioProcessor = AudioProcessor.mAudioProcessor(mInterviewData, mTmpInfo)
                        mResultInfo.stt_path, mTmpInfo = mAudioProcessor.run()
                        mAudioProcessor = None

                        ''' [6] Update Result DB '''
                        cur.execute("insert into ResultInfo (user_idx, interviewdata_idx, question_idx, emotion_path, stt_path) values(%s, %s, %s, %s, %s)",
                                    (mInterviewData.user_idx, mInterviewData.idx, mInterviewData.question_idx, mResultInfo.emotion_path, mResultInfo.stt_path))

                        
                        ''' [7] Patch TaskQueue '''
                        cur.execute("update TaskQueue SET `isProcessed`=%s where idx=%s", (1, task_idx))

                        ''' [8] Push Message '''
                        print ("Make Push")
                        cur.execute("select push_token from UserInfo where `idx` = %s", (mInterviewData.user_idx))
                        rows = cur.fetchall()

                        push_token = [rows[0][0]]
                        mPushServer = PushServer.mPushServer()
                        mPushServer.run(push_token, "Finish", "Analyze Success")
                        print ("Finish Analyze")
                        
                break
        except KeyboardInterrupt:
            print ("TaskQueue is killed by keyboardInterrupt")

mb = mBackgroundProcessor()
mb.run()
        
