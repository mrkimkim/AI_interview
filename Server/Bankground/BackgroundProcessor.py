import pymysql
import time
import random
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

    def Process(self):
        

    def checkTaskQueue(self):
        cur = self.sql.cursor()
        try:
        while True:
            # [1] Pick Task From Queue
                cur.execute("select idx from TaskQueue where (`isProcessed` = 0 AND `isError` = 0 AND `lock` = 0) limit 1")
                rows = cur.fetchall()
                if len(rows) > 0:
                    # [2] Lock the Row
                    idx = int(rows[0][0])
                    lock_seed = random.randrange(1, 10000000)
                    cur.execute("UPDATE TaskQueue SET `lock` = %s, `reg_date` = now() where `idx` = %s", (lock_seed, idx))

                    time.sleep(1)
                    cur.execute("select idx, owner_idx, userdata_idx from TaskQueue where (`lock` = " + str(lock_seed) + ") limit 1")
                    rows = cur.fetchall()
                    
                    # [3] Get Userdata from Database                
                    if len(rows) > 0:
                        idx, owner_idx, userdata_idx = rows[0][0], rows[0][1], rows[0][2]

                        cur.execute("select folder_path, video_hash, video_format from UserData where `idx` = %s", (userdata_idx))
                        rows = cur.fetchall()
                        folder_path, video_hash, video_format = rows[0][0], rows[0][1], rows[0][2]

                        # [4] Process Data
                        # [4-1] Process Video Data
                        mVideoProcessor = VideoProcessor.mVideoProcessor(folder_path,
                                                                         video_hash,
                                                                         video_hash + "." + extFinder.findExt("Video", int(video_format)))
                        video_duration, video_frame_count, audio_duration = mVideoProcessor.run()
                        
                        # [4-2] Process Audio Data
                        mAudioProcessor = AudioProcessor.mAudioProcessor(folder_path,
                                                                         
                                                                         
                        # [4] Move Task From Queue
                        
                    
            break
        except KeyboardInterrupt:
            print ("TaskQueue is killed by keyboardInterrupt")

mb = mBackgroundProcessor()
mb.run()
        
