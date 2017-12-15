class mAudioProcessor(object):
    def __init__(self):
        self.idx = 0
        
    def Process(self):
        from google.cloud import speech
        from google.cloud.speech import enums
        from google.cloud.speech import types
        client = speech.SpeechClient()
        gcs_uri = 'gs://ai_interview/output.flac'
        audio = types.RecognitionAudio(uri=gcs_uri)
        config = types.RecognitionConfig(
            encoding=enums.RecognitionConfig.AudioEncoding.FLAC,
            sample_rate_hertz=44100,
            language_code='ko-KR',
            enable_word_time_offsets=True)

        operation = client.long_running_recognize(config, audio)

        print('Waiting for operation to complete...')
        result = operation.result(timeout=90)

        # Each result is for a consecutive portion of the audio. Iterate through
        # them to get the transcripts for the entire audio file.
        for result in result.results:
            alternative = result.alternatives[0]
            print('Transcript: {}'.format(alternative.transcript))
            print('Confidence: {}'.format(alternative.confidence))

            for word_info in alternative.words:
                word = word_info.word
                start_time = word_info.start_time
                end_time = word_info.end_time
                print('Word: {}, start_time: {}, end_time: {}'.format(
                    word,
                    start_time.seconds + start_time.nanos * 1e-9,
                    end_time.seconds + end_time.nanos * 1e-9))
ma = mAudioProcessor()
ma.Process()
