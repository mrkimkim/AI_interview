import hashlib
import extFinder
import os

def hexToLong(str_hex):
    ret = 0
    for i in range(len(str_hex)):
        ret *= 256
        ret += int(str_hex[i])
    return ret

def longToHex(Num):
    return bytes.fromhex("{:016x}".format(Num))

class mVideoReceiver(object):
    def __init__(self, conn, sql, user_idx):
        self.conn = conn
        self.sql = sql
        self.user_idx = user_idx
        self.user_id = hashlib.sha256("Guest".encode('utf-8')).hexdigest()
        
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
        query = """insert into InterviewData(user_idx, video_hash, video_size, video_format)
                values (%s, %s, %s, %s)"""
        print (self.user_idx, self.video_hash, self.video_size, self.video_format)
        data = (self.user_idx, self.video_hash, self.video_size, self.video_format)
        curs = self.sql.cursor()
        curs.execute(query, data)
    
        # Save to TaskQueue DB
        query = """select idx from InterviewData where `video_hash` = %s"""
        curs.execute(query, (self.video_hash))
        rows = curs.fetchall()
        interviewdata_idx = rows[0][0]
        
        query = """insert into TaskQueue(user_idx, interviewdata_idx) values (%s, %s)"""
        curs.execute(query, (self.user_idx, interviewdata_idx))

        # Send Client Task_idx
        query = """select idx from TaskQueue where `interviewdata_idx` = %s"""
        curs.execute(query, (interviewdata_idx))
        rows = curs.fetchall()
        task_idx = rows[0][0]
        self.conn.send(longToHex(task_idx))

    def SaveVideo(self):
        from google.cloud import storage
        
        self.video_hash = hashlib.sha256(self.data).hexdigest()

        storage_client = storage.Client()
        bucket = storage_client.get_bucket("ai_interview")
        
        blob = bucket.blob(str(self.user_idx) + '/' + self.video_hash + '/' + 'Video')
        blob.upload_from_string(self.data)
        
        ext = 'mp4'
        self.video_format = extFinder.findExt("Video", ext)

    def receive(self, length):
        try:
            print ("start receiving")
            received = 0
            percentile = 0.1
            
            while received < length:
                data = self.conn.recv(min(2048, length - received))
                if received == 0: ret = data
                else: ret += data
                received += len(data)

                if received > length * percentile:
                    print (str(percentile * 100) + "percent received")
                    percentile += 0.1
                    
        except Exception as e:
            self.conn.close()
            self.stop()
            raise KeyboardInterrupt
        return ret

    def run(self):
        try:
            print ("Getting Question idx...")
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
        

        
