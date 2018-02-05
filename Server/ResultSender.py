
def longToHex(Num):
    return (bytes.fromhex("{:016x}".format(Num)))

class mResultSender(object):
    def __init__(self, conn, sql, user_idx):
        self.conn = conn
        self.sql = sql
        self.curs = self.sql.cursor()
        self.user_idx = user_idx

    def run(self):
        from google.cloud import storage
        storage_client = storage.Client()
        bucket = storage_client.get_bucket("ai_interview")
        
        """ Search Valid ResultInfo """
        query = """select idx, interviewdata_idx from ResultInfo where `interviewdata_idx` in (select idx from InterviewData where `user_idx` = %s and `is_deleted` = %s)"""
        self.curs.execute(query, (self.user_idx, 0))
        rows = self.curs.fetchall()

        interview_idxs = []
        task_dics = {}
        for row in rows:
            task_dics[row[1]] = row[0]
            interview_idxs.append(row[1])

        print (tuple(interview_idxs))
        """ Search InterviewData Idx """
        format_strings = ','.join(['%s'] * len(interview_idxs))
        query = """select idx, interviewdata_idx, emotion_blob, subtitle_blob from ResultInfo where `interviewdata_idx` IN (%s)""" % format_strings
        self.curs.execute(query, tuple(interview_idxs))
        rows = self.curs.fetchall()

        data = []
        for row in rows:
            if row[1] in task_dics:
                emotion_blob = bucket.blob(row[2])
                emotion_data = emotion_blob.download_as_string()
                
                subtitle_blob = bucket.blob(row[3])
                subtitle_data = subtitle_blob.download_as_string()

                data.append([task_dics[row[1]], row[0], emotion_data, subtitle_data])

    
        """ Packet Structure
            Size - Length of Chunk
            Chunk_Count
            TaskIdx - 8byte
            ResultIdx - 8byte
            EmotionSize - 8byte
            SubtitleSize - 8byte
            
        """

        packet = longToHex(len(data))
        for row in data:
            packet += longToHex(row[0]) + longToHex(row[1])
            packet += longToHex(len(row[2]))
            packet += row[2]
            packet += longToHex(len(row[3]))
            packet += row[3]

        packet_size = longToHex(len(packet))    
        self.conn.send(packet_size + packet)
