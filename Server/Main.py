import threading
import socket
import pymysql

from Server import Session

class Server(object):
    def __init__(self, hostname, port):
        self.hostname = hostname
        self.port = port
        self.sql = pymysql.connect(host='localhost', user='root', password='lfamesk5uf!@#',
                                   db='AI', charset='utf8', autocommit=True)
    def start(self):
        self.socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.socket.bind((self.hostname, self.port))
        self.socket.listen(1)
        
        while True:
            print ("Server Start") 
            conn, address = self.socket.accept()
            print ("Client is Connected")
            t = Session(conn, self.sql)
            t.daemon = True
            t.start()
            t.join()

if __name__ == "__main__":
    server = Server("0.0.0.0", 8426)
    try:
        server.start()
    except Exception as e:
        print (e)
