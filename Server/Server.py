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
    return ret

def hexToString(bytes_token):
    print (bytes_token)
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
            packet = self.conn.recv(8)
            self.user_id = hexToLong(packet)
            packet = self.conn.recv(64)
            self.user_token = hexToString(packet)
            
            check_query = """select app_token from UserInfo where `idx` = %s"""
            self.curs.execute(check_query, (self.user_id))
            rows = self.curs.fetchall()
            print (len(rows))
            print (self.user_token)
            print (rows[0][0])
            
            if len(rows) < 1 or rows[0][0] != self.user_token: raise Exception
        except Exception as e:
            print (Exception)
            print (e)
            self.curs.close()
            self.sql.close()
            self.conn.close()
            self.stop()
            return

        print ("Waiting Command...")
        cmd = hexToLong(self.conn.recv(4))
        print (cmd)
        print (cmd + 1)
        # interpret command
        if cmd == 1000:
            # receive Video from Client
            mVideoReceiver = VideoReceiver.mVideoReceiver(self.conn, self.sql, self.user_id)
            file_path = mVideoReceiver.run()

            # PreProcess

        self.conn.close()
        self.curs.close()
        self.sql.close()
        self.stop()
