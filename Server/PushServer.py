# -*- coding: cp949 -*-
import random
import pymysql
from pyfcm import FCMNotification
push_service = FCMNotification(api_key="AAAAuP9RJiM:APA91bEdTsyNT-zMrUf3nTR1suqSEVLKVOMBKdipBO9kl3Gb1FG9UebsXM08SkuK-elJgPggW0SvveXDOXcUpbZqzEeEK_MpADIEG77m61we50QdvMwmTz1V86zg2xx2l4Rplh8G-y4m")
pushToken = "ctaLKhZTXYg:APA91bH15MX4_1PMx_c6MA8A96mKreawEMBGwhVraZqRuqfEepgtMTwwysoMCZZ0qWEzLsmSvhQTesCS_i3wDgLTCy_QNIFtmGdy_daQ_qFRFNmoHGztisOAqJx50Oq1spdRriOobyIN"

# Connect to DB Server
class PushServer(object):
    def __init__(self):
        self.sql = pymysql.connect(host='localhost',
                                   user='root',
                                   password='lfamesk5uf!@#',
                                   db='AI',
                                   charset='utf8',
                                   autocommit=True)
        self.curs = self.sql.cursor()

    def generate_seed(self):
        return random.randrange(1, 2 ** 31)
        
    def start(self):
        while True:
            # get Completed Task From DB
            query = """select idx, user_idx from TaskQueue where (`isProcessed` == True AND `lock` == 0)"""
            curs.execute(query)
            rows = curs.fetchall()
            idx, user_idx = rows[0][0], rows[0][1]

            if len(rows) > 0:
                # lock the row
                lock_seed = self.generate_seed()
                query = """UPDATE TaskQueue SET `lock` = %s where `idx` = %s"""
                curs.execute(query, (lock_seed, idx))

                # reselect the row
                query = """select idx, user_idx from TaskQueue where (`lock` = %s AND `idx` = %s)"""
                curs.execute(query, (lock_seed, idx))

                if len(rows) > 0:
                    # delete row
                    query = """delete * from UserData where (`idx` == %s)"""
                    curs.execute(query, idx)

                    # get Token
                    query = """select Token from UserInfo where (`user_idx` == %s)"""
                    curs.execute(query, user_idx)
                    rows = curs.fetchall()
                    Token = rows[0][0]

                    # send push request to Firebase
                    result = push_service.notify_single_device(registration_id=pushToken,
                                                   message_title="Notice",
                                                   message_body="인터뷰 분석이 완료되었습니다.")

                    # print result
                    print (result)
            
        
