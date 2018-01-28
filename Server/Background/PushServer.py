# -*- coding: cp949 -*-
from pyfcm import FCMNotification
push_service = FCMNotification(api_key="AAAAarNyU_8:APA91bGCJ_ow6CRtfHK84bzEQG1_tOwSNart9H0RlsgxHp-GjUIViFqYOc3HEJJaTU1NAtlxhUBch38AtecEvc2r9Cw3hc6ceZIGZ9nvG-30nxkN_tW3PtJX4Ge74Q6B61Prv3O8dAGM")

# Connect to DB Server
class mPushServer(object):
    def __init__(self):
        return
        
    def run(self, Tokens, title, msg):
        for Token in Tokens:
            # send push request to Firebase
            result = push_service.notify_single_device(registration_id=Token,
                                                       message_title=title,
                                                       message_body=msg)
        return
