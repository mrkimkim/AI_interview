class mAudioProcessor(object):
    def __init__(self, mInterviewData, mTmpInfo, bucket):
        self.idx = 0
        self.mInterviewData = mInterviewData
        self.mTmpInfo = mTmpInfo
        self.mTmpInfo.tmp_stt = self.mTmpInfo.tmp_folder + "text.stt"
        self.mTmpInfo.subtitle_blob = self.mTmpInfo.storage_blob + 'subtitle'

        self.bucket = bucket
        
    def run(self):
        from google.cloud import storage
        from google.cloud import speech
        from google.cloud.speech import enums
        from google.cloud.speech import types


        print ("[1] Analyze Audio Data")
        client = speech.SpeechClient()
        gcs_uri = 'gs://ai_interview/' + self.mTmpInfo.audio_blob
        audio = types.RecognitionAudio(uri=gcs_uri)
        config = types.RecognitionConfig(
            encoding=enums.RecognitionConfig.AudioEncoding.FLAC,
            sample_rate_hertz=44100,
            language_code='ko-KR',
            enable_word_time_offsets=True)

        operation = client.long_running_recognize(config, audio)

        print('Waiting for operation to complete...')
        result = operation.result()




        print ("[3] Write Result ")
        with open(self.mTmpInfo.tmp_stt, 'w') as STT_output:
            for result in result.results:
                alternative = result.alternatives[0]
                print('Transcript: {}'.format(alternative.transcript))
                print('Confidence: {}'.format(alternative.confidence))
                STT_output.write('Transcript: {}'.format(alternative.transcript) + "\n")
                STT_output.write('Confidence: {}'.format(alternative.confidence) + "\n")

                for word_info in alternative.words:
                    word = word_info.word
                    start_time = word_info.start_time
                    end_time = word_info.end_time
                    print('Word: {}, start_time: {}, end_time: {}'.format(
                        word,
                        start_time.seconds + start_time.nanos * 1e-9,
                        end_time.seconds + end_time.nanos * 1e-9))
                    STT_output.write(word + ',')
                    STT_output.write(str(start_time.seconds + start_time.nanos * 1e-9) + ',')
                    STT_output.write(str(end_time.seconds + end_time.nanos * 1e-9) + "\n")

        with open(self.mTmpInfo.tmp_stt, 'rb') as f:
            data = f.read()
            blob = self.bucket.blob(self.mTmpInfo.subtitle_blob)
            blob.upload_from_string(data)


        print ("Successfully Analyze Audio Data")
        return self.mTmpInfo.tmp_stt, self.mTmpInfo
