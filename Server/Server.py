import socket
import sys
import threading
import VideoReceiver
import pymysql

class Session(threading.Thread):
    def __init__(self, conn, sql):
        threading.Thread.__init__(self)
        self.conn = conn
        self.sql = sql
        self._stop_event = threading.Event()

    def stop(self):
        self._stop_event.set()

    def run(self):
        # Login to Server
        print "Login to Server"

        # waiting message
        while True:
            try:
                print "Waiting Command..."
                cmd = self.conn.recv(10)
                if cmd:
                    cmd = cmd.strip()
                    if "quit" in cmd:
                        print "Close Program"
                        self.conn.close()
                        self.stop()
                        break
                    elif "sendVideo" in cmd:
                        print "Start Receving Video"
                        mVideoReceiver = VideoReceiver.mVideoReceiver(self.conn, self.sql)
                        mVideoReceiver.Receive()
                        print "Successfuly register Video Data"
                else: raise KeyboardInterrupt
            except KeyboardInterrupt:
                print "Force Down Server"
                self.conn.close()
                self.stop()
                break
