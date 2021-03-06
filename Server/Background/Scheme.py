import hashlib

class UserInfo():
    def __init__(self):
        self.idx = 1
        self.user_id = "Guest"
        self.id_hash = hashlib.sha1(self.user_id).hexdigest()
        self.pwd_hash = hashlib.sha1("1234").hexdigest()
        self.last_login = ""
        self.credit = 100
        self.icon_path = "./"
        self.token = "dogNLt6SCqc:APA91bErS_0Lg-_3-sqEKWw4GW53loBrm4awjpSnVOi6OnMyP0sE96S6CBdIZk1kIbQgeOv-FIxrApMPUw1Q2VA5vm6DekkXt3070hNgUelF54uv8H6RzPB1eZaLQWvK5CX4KY1Wg4Vf"
        self.Session = ""
        self.last_login_ip = "127.0.0.1"
        
class InterviewData():
    def __init__(self, user_idx):
        self.idx = ""
        self.user_idx = user_idx

        """ Video Info """
        self.video_hash = "" #
        self.video_size = "" 
        self.video_format = "0" #
        self.video_duration = "" #
        self.video_frame_count = ""

        """ Audio Info """
        self.audio_hash = "" #
        self.audio_size = "0"
        self.audio_format = "-1" #
        self.audio_duration = "0" #
        self.audio_sample_rate = "" #

        """ Extra Info """
        self.isProcessed = 0
        self.result_idx = "-1"
        self.question_idx = "0"

class ResultInfo():
    def __init__(self, user_idx, userdata_idx):
        self.idx = ""
        self.user_idx = user_idx
        self.userdata_idx = userdata_idx
        self.question_idx = "0"

        ''' Result Info '''
        self.emotion_path = ""
        self.stt_path = ""

class TmpInfo():
    def __init__(self, storage_blob, video_blob, audio_blob, tmp_folder, tmp_video, tmp_audio):
        self.storage_blob = storage_blob
        self.video_blob = video_blob
        self.audio_blob = audio_blob
        self.emotion_blob = ''
        self.subtitle_blob = ''
        self.pitch_blob = ''
        
        self.tmp_folder = tmp_folder
        self.tmp_video = tmp_video
        self.tmp_audio = tmp_audio
        self.tmp_emotion = ''
        self.tmp_stt = ''
        
