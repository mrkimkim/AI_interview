import pymysql
import time
import random
import sys
import Scheme

import preProcessor
import VideoProcessor
import AudioProcessor
import extFinder


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
                    cur.execute("select idx, user_idx, userdata_idx from TaskQueue where (`lock` = " + str(lock_seed) + ") limit 1")
                    rows = cur.fetchall()
                    
                    """ [3] Get Userdata from Database """
                    if len(rows) > 0:
                        task_idx, user_idx, userdata_idx = rows[0][0], rows[0][1], rows[0][2]
                        
                        mUserData = Scheme.UserData(user_idx)
                        mResultInfo = Scheme.ResultInfo(user_idx, userdata_idx)
                        
                        
                        cur.execute("select file_name, folder_path, video_hash, video_format from UserData where `idx` = %s", (userdata_idx))
                        rows = cur.fetchall()

                        mUserData.idx, mUserData.user_idx = userdata_idx, user_idx
                        mUserData.file_name, mUserData.folder_path, mUserData.video_hash, mUserData.video_format = rows[0][0], rows[0][1], rows[0][2], rows[0][3]

                        """ [4] PreProcess Data """
                        mPreProcessor = preProcessor.mPreProcessor(mUserData)
                        mUserData = mPreProcessor.run()
                        mPreProcessoor = None
                        
                        ''' [5-1] Process Video Data '''
                        mVideoProcessor = VideoProcessor.mVideoProcessor(mUserData)
                        mResultInfo.emotion_path, mUserData = mVideoProcessor.run()
                        mVideoProcessor = None
                        
                        ''' [5-2] Process Audio Data '''
                        mAudioProcessor = AudioProcessor.mAudioProcessor(mUserData)
                        mResultInfo.stt_path = mAudioProcessor.run()
                        mAudioProcessor = None

                        ''' [6] Update Result DB '''
                        cur.execute("insert into ResultInfo (user_idx, userdata_idx, question_idx, emotion_path, stt_path) values(%s, %s, %s, %s, %s)",
                                    (mUserData.user_idx, mUserData.idx, mUserData.question_idx, mResultInfo.emotion_path, mResultInfo.stt_path))
                        
                        ''' [7] Patch TaskQueue '''
                        cur.execute("update TaskQueue SET `isProcessed`=%s where idx=%s", (1, task_idx))

                        print ("Finish Analyze")
                        
                break
        except KeyboardInterrupt:
            print ("TaskQueue is killed by keyboardInterrupt")

mb = mBackgroundProcessor()
mb.run()
        
