import threading
import socket
from Server import Session

class Server(object):
    def __init__(self, hostname, port):
        self.hostname = hostname
        self.port = port

    def start(self):
        self.socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.socket.bind((self.hostname, self.port))
        self.socket.listen(1)

        while True:
            conn, address = self.socket.accept()
            t = threading.Thread(target = Session, args=(conn, address))
            t.daemon = True
            t.start()
            t.join()

if __name__ == "__main__":
    print "Server Start"
    
    server = Server("0.0.0.0", 8426)
    try:
        server.start()
    except Exception as e:
        print (e)
