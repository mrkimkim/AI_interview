"""
=== Voice Pitch Analyzer ===

This module uses aubio as core engine

- Input : Audio Flac
- Output : Hz

"""

from subprocess import Popen, PIPE

class mPitchAnalyzer(object):
    def __init__(self, mTmpInfo, bucket):
        self.mTmpInfo = mTmpInfo
        self.mTmpInfo.pitch_blob = self.mTmpInfo.storage_blob + 'pitch'
        self.bucket = bucket
        return
        
    def run(self):
        from google.cloud import storage
        p = Popen(["sudo", "python", "demo.py", self.mTmpInfo.tmp_audio, "44100"], stdout=PIPE, stderr=PIPE)
        output, err = p.communicate()
        output = output.decode('ascii')
        
        blob = self.bucket.blob(self.mTmpInfo.pitch_blob)
        blob.upload_from_string(output)

        print ("Successfully Analyze Audio Pitch")
        return self.mTmpInfo
