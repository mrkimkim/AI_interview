import requests
import socket
import threading
import ast
import hashlib
import pymysql
import time
import os

def intToHex(num):
    return bytes.fromhex("{:08x}".format(num))

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
        Structure : user_id(8byte), kakao_token (54byte)
        """
        cmd = self.conn.recv(64)
        try:
            self.user_id = hexToLong(cmd[:8])
            self.user_token = hexToString(cmd[8:])
            print (self.user_id, self.user_token)
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
                self.conn.send(" " * 64)
                raise Exception("not a valid kakao_token!!!")
                
        except Exception as e:
            print (e)
            print ("Error : Fail to verify user")
            return -1, "Error : Fail to verify user"




        """
        Check if user info is in DB and generate hash
        """
        try:
            login_query = """select userMsg, userNumtry, userUpvote, userCredit from UserInfo where `user_id` = %s"""
            self.curs.execute(login_query, (self.user_id))
            rows = self.curs.fetchall()
            # If User isn't signed up
            if len(rows) < 1: raise Exception("user isn't signed up")
        except Exception as e:
            # Create New User
            id_hash = hashlib.sha256(self.user_id.encode('utf-8')).hexdigest()
            insert_query = """insert into UserInfo(idx, user_id, id_hash, credit)
                            values (%s, %s, %s, %s)"""
            self.curs.execute(insert_query, (self.user_id, self.user_id, id_hash, 0))
            # Default DataSet
            rows = ['write your message', 0, 0, 0]


        """
        Send UserInfo and Update UserInfo
        """
        try:
            update_query = """update UserInfo SET `last_login` = now()"""
            self.curs.execute(update_query)
            userMsg, userNumtry, userUpvote, userCredit = rows[0][0], rows[0][1], rows[0][2], rows[0][3]

            packet = intToHex(int(userNumtry)) + intToHex(int(userUpvote)) + intToHex(int(userCredit))
            self.conn.send(packet)
            packet = intToHex(len(userMsg.encode('utf-8'))) + userMsg.encode('utf-8')
            self.conn.send(packet)
            
        except Exception as e:
            print (e)


            
        """
        Generate app_token
        """
        try:
            app_token_seed = str(self.user_id) + str(self.user_token) + str(time.time())
            app_token = str(hashlib.sha256(app_token_seed.encode('utf-8')).hexdigest())
            update_query = """Update UserInfo SET `app_token` = %s where `idx` = %s"""
            self.curs.execute(update_query, (app_token, self.user_id))
            self.conn.send(app_token.encode('ascii'))
        except Exception as e:
            print ("App Token Update Failed.")
            print (e)



        """
        Receive & Register FCM Token
        """
        try:
            push_token = self.conn.recv(152)
            self.push_token = hexToString(push_token)
            update_query = """Update UserInfo SET `push_token` = %s where `idx` = %s"""
            self.curs.execute(update_query, (self.push_token, self.user_id))
        except Exception as e:
            print ("Push Token Update Failed.")
            print (e)

            

        """ Send DB File """
        try:
            db_version = self.conn.recv(4)
            data = open("DB.csv","rb").read()
            data_size = len(data)
            print ("Total Byte is " + str(data_size))
            self.conn.send(bytes.fromhex("{:08x}".format(data_size)))
            
            buf = 1024
            offset = 0
            while offset < data_size:
                if offset + buf < data_size:
                    self.conn.sendall(data[offset:offset + buf])
                else:
                    self.conn.sendall(data[offset:])
                offset += buf
        except Exception as e:
            print ("Send DB Failed")
            print (e)
            self.curs.close()
        self.conn.close()
        print ("Connection Closed")
    

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

    def __del__(self):
        print ("Program finished")
        self.socket.close()
        self.socket = ""

if __name__ == "__main__":
    login_server = LoginServer('', 8425)
    try:
        login_server.start()
    except Exception as e:
        print (e)
