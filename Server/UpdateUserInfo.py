class mUpdateUserInfo(object):
    def __init__(self, conn, sql, user_idx):
        self.conn = conn
        self.sql = sql
        self.user_idx = user_idx
        self.buf_size = 1024

    def hexToInt(self, b):
        ret = 0
        for i in range(len(b)):
            print (int(b[i]))
            ret *= 256
            ret += int(b[i])
        return ret        

    def receive(self, size):
        recv = 0
        try:
            print (size)
            while recv < size:
                buf = self.conn.recv(min(self.buf_size, size - recv))
                if recv == 0: data = buf
                else: data += buf
                recv += len(buf)
                print (buf)
            return data
        except Exception as e:
            print (e)
            return None

    # Receive UserMsg and Update
    def run(self):
        print ("Hello")
        packet_size = self.receive(4)
        if packet_size is not None:
            try:
                packet = self.receive(self.hexToInt(packet_size))
                print (len(packet))
                print (packet.decode('utf-8'))
                print (packet)
                cur = self.sql.cursor()
                query = """Update UserInfo SET `userMsg`= %s where `user_id` = %s"""
                cur.execute(query, (packet, self.user_idx))
            except Exception as e:
                print (e)

        return
