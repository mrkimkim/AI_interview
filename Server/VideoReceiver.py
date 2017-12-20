import hashlib
import extFinder
import os

class mVideoReceiver(object):
    def __init__(self, conn, sql):
        self.conn = conn
        self.sql = sql
        
        self.user_idx = 0
        self.user_id = hashlib.sha256("Guest").hexdigest()
        
        self.folder_path = "./Data/" + self.user_id + "/"
        self.file_name = ""
        self.video_hash = ""
        self.video_size = 0
        self.video_format = 0
        
        self.is_processed = False
        self.question_idx = 0
        self.data = ""

        self.receive_msg = "ready"

    def get_file_name(self):
        self.signal()
        file_name_length = int(self.receive(16))
        return self.receive(file_name_length)

    def get_file_path(self):
        # Use Cloud Storage
        # path = "gs://AI/Storage/Video/" + "id_hash/" + "file_name"
        return "./Video/TestUser/"
    
    def signal(self):
        self.conn.sendall(self.receive_msg)

    def get_file_size(self):
        return int(self.receive(16))
    
    def getOwnerIdx(self, user_id):
        return 0

    def get_file(self, length):
        return self.receive(length)

    
    def SaveDB(self):
        # Save to UserData DB
        query = """insert into UserData(user_idx, folder_path, file_name, video_hash, video_size, video_format)
                values (%s, %s, %s, %s, %s, %s)"""
        print (self.user_idx, self.folder_path, self.file_name, self.video_hash, self.video_size, self.video_format)
        data = (self.user_idx, self.folder_path, self.file_name, self.video_hash, self.video_size, self.video_format)
        curs = self.sql.cursor()
        curs.execute(query, data)
        print (1)
        # Save to TaskQueue DB
        query = """select idx from UserData where (`video_hash` = %s AND `file_name` = %s)"""
        curs.execute(query, (self.video_hash, self.file_name))
        rows = curs.fetchall()
        userdata_idx = rows[0][0]
        print (2)
        query = """insert into TaskQueue(user_idx, userdata_idx) values (%s, %s)"""
        curs.execute(query, (self.user_idx, userdata_idx))
        print (3)

    def SaveVideo(self):
        self.video_hash = hashlib.sha256(self.data).hexdigest()
        self.folder_path = self.folder_path + self.video_hash + "/"
        if not os.path.isdir(self.folder_path): os.makedirs(self.folder_path)
        
        ext = self.file_name[self.file_name.rfind('.') + 1:]
        print (ext)
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

    def Receive(self):
        try:
            print ("Gettting File Name...")
            self.file_name = self.get_file_name()
            print ("Getting File Size...")
            self.video_size = self.get_file_size()
            print ("Receiving File...")
            self.data = self.get_file(self.video_size)
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
        

        
