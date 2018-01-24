import socket
import sys
import threading
import VideoReceiver
import pymysql
from Background.Scheme import UserInfo

def hexToLong(str_hex):
    ret = 0
    for i in range(len(str_hex)):
        ret *= 256
        ret += int(str_hex[i])
    return str(ret)

def hexToString(bytes_token):
    return bytes_token.decode('ascii')

class Session(threading.Thread):
    def __init__(self, conn, sql):
        threading.Thread.__init__(self)
        self.conn = conn
        self.sql = sql
        self.curs = self.sql.cursor()
        self._stop_event = threading.Event()

    def stop(self):
        self._stop_event.set()

    def run(self):
        # Login to Server
        print ("Login to Server")

        """ Verify userToken
            8byte - UserIdx
            64byte - UserToken
        """
        try:
            packet = self.conn.recv(72)
            self.user_id = hexToLong(packet[:8])
            self.user_token = hexToString(packet[:])

            check_query = """select app_token from UserInfo where `idx` = %s"""
            self.curs.execute(check_query, (self.user_id))

            rows = self.curs.fetchall()
            if len(rows) < 1 or rows[0][0] != self.user_token: raise Exception
        except Exception as e:
            print (e)
            self.curs.close()
            self.sql.close()
            self.conn.close()
            self.stop()
            return
        
        # waiting message
        while True:
            try:
                print ("Waiting Command...")
                cmd = hexToLong(self.conn.recv(8))
                if cmd == 4444:
                    print ("Close Program")
                    self.conn.close()
                    self.stop()
                    break
                elif cmd == 1000:
                    print ("Start Receving Video")
                    mVideoReceiver = VideoReceiver.mVideoReceiver(self.conn, self.sql)
                    file_path = mVideoReceiver.run()
                    print ("Successfuly register Video Data")
                elif "resultList" in cmd:
                    print ("Result List")
                    mResultSender = ResultSender.mResultSender(self.conn, self.sql, mUserInfo)
                    mResultSender.run()
                else: raise KeyboardInterrupt
                raise KeyboardInterrupt
            except KeyboardInterrupt:
                print ("Force Down Server")
                self.conn.close()
                self.curs.close()
                self.sql.close()
                self.stop()
                break
