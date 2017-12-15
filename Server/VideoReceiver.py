class mVideoReceiver(object):
    def __init__(self, conn, sql):
        self.conn = conn
        self.sql = sql

        self.idx = 0
        self.owner_idx = 0
        self.file_name = ""
        self.file_path = ""
        self.is_processed = False
        self.file_size = 0
        self.question_idx = 0
        self.data = ""

        self.receive_msg = "ready"
        
    def receive(self, length):
        try:
            if length <= 1000:
                data = self.conn.recv(length)
                if data: return data.strip()
            else:
                print "start receiving"
                ret = ""
                received = 0
                percentile = 0.1
                while received < length:
                    data = self.conn.recv(2048)
                    received += len(data)
                    ret += data

                    if received > length * percentile:
                        print str(percentile * 100) + "percent received"
                        percentile += 0.1
        except KeyboardInterrupt:
            self.conn.close()
            self.stop()
            
        return ret

    def get_file_name(self):
        self.signal()
        file_name_length = int(self.receive(16))
        self.signal()
        return self.receive(file_name_length)

    def get_file_path(self):
        # Use Cloud Storage
        # path = "gs://AI/Storage/Video/" + "id_hash/" + "file_name"
        return "./Video/TestUser/"
    
    def signal(self):
        self.conn.sendall(self.receive_msg)

    def get_file_size(self):
        self.signal()
        return int(self.receive(16))
    
    def getOwnerIdx(self, user_id):
        return 0

    def get_file(self, length):
        self.signal()
        return self.receive(length)
    
    def SaveDB(self):
        query = """insert into VideoInfo(idx, owner_idx, file_name, file_path, is_processed, file_size, question_idx)
                values (%s, %s, %s, %s, %s, %s, %s)"""

        print self.idx, self.owner_idx, self.file_name, self.file_path, int(self.is_processed), self.file_size, self.question_idx
        
        data = (self.idx,
                 self.owner_idx,
                 self.file_name,
                 self.file_path,
                 int(self.is_processed),
                 self.file_size,
                 self.question_idx)
        curs = self.sql.cursor()
        curs.execute(query, data)

    def SaveVideo(self):
        
        f = open(self.file_path + self.file_name, 'wb')
        f.write(self.data)
        f.close()

        self.signal()

    def Receive(self):
        try:
            print "Gettting File Name..."
            self.file_name = self.get_file_name()
            print "Setting File Path..."
            self.file_path = self.get_file_path()
            print "Getting File Size..."
            self.file_size = self.get_file_size()
            print "Receiving File..."
            self.data = self.get_file(self.file_size)
            print "Saving to DB..."
            self.SaveDB()
            print "Saving Video"
            self.SaveVideo()
            print "Successfully Receive Video"
            return self.file_path
            
        except Exception as e:
            print e
            print "!"
            self.conn.close()
            raise KeyboardInterrupt
            self.stop()
        

        
