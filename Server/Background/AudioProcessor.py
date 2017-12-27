class mAudioProcessor(object):
    def __init__(self, UserData):
        self.idx = 0
        self.UserData = UserData
        self.STT_path = self.UserData.folder_path + self.UserData.audio_hash + ".stt"
        
    def run(self):
        from google.cloud import storage
        from google.cloud import speech
        from google.cloud.speech import enums
        from google.cloud.speech import types

        print ("[1] Uploade Audio to GCS")
        storage_client = storage.Client()
        bucket = storage_client.get_bucket("ai_interview")
        blob = bucket.blob(self.UserData.audio_hash)
        blob.upload_from_file(open(self.UserData.folder_path + self.UserData.audio_hash, 'rb'))

        print ("[2] Analyze Audio Data")
        client = speech.SpeechClient()
        gcs_uri = 'gs://ai_interview/' + self.UserData.audio_hash
        audio = types.RecognitionAudio(uri=gcs_uri)
        config = types.RecognitionConfig(
            encoding=enums.RecognitionConfig.AudioEncoding.FLAC,
            sample_rate_hertz=44100,
            language_code='ko-KR',
            enable_word_time_offsets=True)

        operation = client.long_running_recognize(config, audio)

        print('Waiting for operation to complete...')
        result = operation.result(timeout=90)

        print ("[3] Write Result ")
        STT_output = open(self.STT_path, 'w')
        
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

        STT_output.close()

        print ("Successfully Analyze Audio Data")
        return self.STT_path
