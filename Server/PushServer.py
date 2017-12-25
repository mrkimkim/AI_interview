# -*- coding: cp949 -*-
import time
import random
import pymysql
from pyfcm import FCMNotification
push_service = FCMNotification(api_key="AAAAuP9RJiM:APA91bEdTsyNT-zMrUf3nTR1suqSEVLKVOMBKdipBO9kl3Gb1FG9UebsXM08SkuK-elJgPggW0SvveXDOXcUpbZqzEeEK_MpADIEG77m61we50QdvMwmTz1V86zg2xx2l4Rplh8G-y4m")

# Connect to DB Server
class PushServer(object):
    def __init__(self):
        self.sql = pymysql.connect(host='localhost',
                                   user='root',
                                   password='lfamesk5uf!@#',
                                   db='AI',
                                   charset='utf8',
                                   autocommit=True)
        
    def generate_seed(self):
        return random.randrange(1, 2 ** 31)
        
    def start(self):
        curs = self.sql.cursor()
        
        while True:
            # get Completed Task From DB
            query = """select idx, user_idx from TaskQueue where (`isProcessed` = '1' AND `lock` = '0')"""
            curs.execute(query)
            rows = curs.fetchall()
        
            if len(rows) > 0:
                idx, user_idx = rows[0][0], rows[0][1]
                
                # lock the row
                lock_seed = self.generate_seed()
                query = """UPDATE TaskQueue SET `lock` = %s where (`idx` = %s AND `lock` = 0)"""
                curs.execute(query, (lock_seed, idx))

                # reselect the row
                time.sleep(1)
                query = """select idx, user_idx from TaskQueue where (`lock` = %s AND `idx` = %s)"""
                curs.execute(query, (lock_seed, idx))

                if len(rows) > 0:
                    # get Token
                    query = """select token from UserInfo where `idx` = %s"""
                    curs.execute(query, user_idx)
                    rows = curs.fetchall()
                    Token = rows[0][0]

                    # send push request to Firebase
                    result = push_service.notify_single_device(registration_id=Token,
                                                   message_title="Notice",
                                                   message_body="인터뷰 분석이 완료되었습니다.")

                    # print result
                    print (result)
            
mPushServer = PushServer()
mPushServer.start()
