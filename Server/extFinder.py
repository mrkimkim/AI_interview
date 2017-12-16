
def findExt(flag, ext):
    video_ext = {'mp4' : 0, 0: 'mp4'}
    audio_ext = {'flac' : 0, 0: 'flac'}
    try:
        if flag == "Video": return video_ext[ext]
        else: return audio_ext[ext]
    except:
        return -1
    
    
