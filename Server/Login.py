import requests
import socket
import threading
import ast
import hashlib
import pymysql
import time

def hexToLong(str_hex):
    ret = 0
    for i in range(len(str_hex)):
        ret *= 256
        ret += int(str_hex[i])
    return str(ret)

def hexToString(bytes_token):
    return bytes_token.decode('ascii')

def req(path, query, method, user_token, data={}):
    API_HOST = 'https://kapi.kakao.com'
    url = API_HOST + path
    headers = {'Authorization': 'Bearer ' + user_token}
    if method == 'GET':
        return requests.get(url, headers=headers)
    else:
        return requests.post(url, headers=headers, data=data)


class LoginSession(threading.Thread):
    def __init__(self, conn, sql):
        threading.Thread.__init__(self)
        self.conn = conn
        self.sql = sql
        self.curs = self.sql.cursor()
        self._stop_event = threading.Event()
        self.user_id = -1
        self.user_token = ""
        
    def stop(self):
        self.conn.close()
        self._stop_event.set()

    def run(self):
        """
        Receive Packet from Client
        Structure : User_ID(8byte), User_Token (54byte)
        """
        cmd = self.conn.recv(64)
        try:
            self.user_id = hexToLong(cmd[:8])
            self.user_token = hexToString(cmd[8:])
        except Exception as e:
            print ("Error : Invalid packet format")
            return -1, "Error : Invalid packet format"


        """
        Verify Kakao_Token by Kakao REST API
        """
        try:
            resp = req('/v1/user/me', '', 'GET', self.user_token)
            resp = ast.literal_eval(resp.text.replace(':true',':True'))
            if str(resp['id']) != self.user_id:
                raise Exception("not a valid user_token!!!")
                
        except Exception as e:
            print (e)
            print ("Error : Fail to verify user")
            return -1, "Error : Fail to verify user"


        """
        Check if user info is in DB and generate hash
        """
        try:
            login_query = """select idx from UserInfo where `idx` = %s"""
            self.curs.execute(login_query, (self.user_id))
            rows = self.curs.fetchall()

            if len(rows) < 1: raise Exception("user isn't signed up")
        except Exception as e:
            id_hash = hashlib.sha256(self.user_id.encode('utf-8')).hexdigest()
            
            insert_query = """insert into UserInfo(idx, user_id, id_hash, credit)
                            values (%s, %s, %s, %s)"""
            data = (self.user_id, self.user_id, id_hash, 0)
            self.curs.execute(insert_query, data)
            
        """
        Generate app_token
        """
        try:
            token_hash = self.user_id.encode('utf-8') + self.user_token + str(time.time())
            app_token = str(hashlib.sha256(token_hash).hexdigest())
            update_query = """Update UserInfo SET `token` = %s where `idx` = %s"""
            self.curs.execute(update_query, (app_token, self.user_id))

            app_token = app_token.encode('utf-8')
            self.conn.send(app_token)
            print (app_token)
        except Exception as e:
            print ("Token Update Failed.")
            print (e)

        
        self.conn.close()
    

class LoginServer(object):
    def __init__(self, hostname, port):
        self.hostname = hostname
        self.port = port
        self.sql = pymysql.connect(host='localhost', user='root', password='lfamesk5uf!@#',
                                   db='AI', charset='utf8', autocommit=True)
        
    def start(self):
        self.socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.socket.bind((self.hostname, self.port))
        self.socket.listen(24)

        while True:
            print ("Server Start")
            conn, address = self.socket.accept()
            print ("Connected to Server")
            t = LoginSession(conn, self.sql)
            t.start()
            t.join()
            break

if __name__ == "__main__":
    login_server = LoginServer('', 8426)
    try:
        login_server.start()
    except Exception as e:
        print (e)
