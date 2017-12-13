import socket
import sys

import threading

class Session(threading.Thread):
    def __init__(self, conn, addr):
        threading.Thread.__init__(self)
        self.conn = conn
        self.addr = addr
        self._stop_event = threading.Event()

    def stop(self):
        self._stop_event.set()

    def run(self):
        # if Log-in is failed
            # Throw failed msg and quit

        # waiting message

        # quit
        conn.close()
        self.stop()
