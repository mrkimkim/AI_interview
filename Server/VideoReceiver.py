import hashlib
import extFinder
import os

def hexToLong(str_hex):
    ret = 0
    for i in range(len(str_hex)):
        ret *= 256
        ret += int(str_hex[i])
    return str(ret)

class mVideoReceiver(object):
    def __init__(self, conn, sql):
        self.conn = conn
        self.sql = sql
        self.user_idx = 0
        self.user_id = hashlib.sha256("Guest").hexdigest()
        
        self.video_hash = ""
        self.video_size = 0
        self.video_format = 0
        
        self.is_processed = False
        self.question_idx = 0
        self.data = ""
        self.question_idx = -1
        
        self.receive_msg = "ready"

    def get_question_idx(self):
        self.question_idx = hexToLong(self.receive(8))
        return

    def get_file_path(self):
        # Use Cloud Storage
        # path = "gs://AI/Storage/Video/" + "id_hash/" + "file_name"
        return "./Video/TestUser/"
    
    def get_file_size(self):
        self.video_size = hexToLong(self.receive(8))
        return
    
    def signal(self):
        self.conn.sendall(self.receive_msg)

    def getOwnerIdx(self, user_id):
        return 0

    def get_file(self, length):
        return self.receive(length)

    def SaveDB(self):
        # Save to UserData DB
        query = """insert into UserData(user_idx, video_hash, video_size, video_format)
                values (%s, %s, %s, %s)"""
        print (self.user_idx, self.video_hash, self.video_size, self.video_format)
        data = (self.user_idx, self.video_hash, self.video_size, self.video_format)
        curs = self.sql.cursor()
        curs.execute(query, data)
    
        # Save to TaskQueue DB
        query = """select idx from UserData where `video_hash` = %s"""
        curs.execute(query, (self.video_hash))
        rows = curs.fetchall()
        userdata_idx = rows[0][0]
        
        query = """insert into TaskQueue(user_idx, userdata_idx) values (%s, %s)"""
        curs.execute(query, (self.user_idx, userdata_idx))

    def SaveVideo(self):
        from google.cloud import storage
        from google.cloud import speech
        from google.cloud.speech import enums
        from google.cloud.speech import types

        self.video_hash = hashlib.sha256(self.data).hexdigest()
        
        storage_client = storage.Client()
        bucket = storage_client.get_bucket("ai_interview")
        blob = bucket.blob(str(self.user_idx) + self.video_hash)
        blob.upload_from_file(self.data)
        
        ext = 'mp4'
        self.video_format = extFinder.findExt("Video", ext)
        
        f = open(self.folder_path + self.video_hash + '.' + ext, 'wb')
        f.write(self.data)
        f.close()

    def receive(self, length):
        try:
            if length <= 1000:
                data = self.conn.recv(length)
                if data:
                    self.signal()
                    return data.strip()
            else:
                print ("start receiving")
                ret = ""
                received = 0
                percentile = 0.1
                while received < length:
                    data = self.conn.recv(2048)
                    received += len(data)
                    ret += data

                    if received > length * percentile:
                        print (str(percentile * 100) + "percent received")
                        percentile += 0.1
        except Exception as e:
            self.conn.close()
            self.stop()
            raise KeyboardInterrupt

        self.signal()
        return ret

    def run(self):
        try:
            print ("Gettting Question idx...")
            self.get_question_idx()
            
            print ("Getting File Size...")
            self.get_file_size()
            
            print ("Receiving File...")
            self.data = self.receive(self.video_size)
            
            print ("Saving Video")
            self.SaveVideo()
            
            print ("Saving to DB...")
            self.SaveDB()
            
            print ("Successfully Receive Video")
            return
            
        except Exception as e:
            print (e)
            self.conn.close()
            raise KeyboardInterrupt
            self.stop()
        

        
