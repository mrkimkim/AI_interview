class mResultFinder(object):
    def __init__(self, conn, sql, UserInfo):
        self.conn = conn
        self.sql = sql
        self.UserInfo = UserInfo

    def run():
        curs = self.sql.cursor()
        query = """select * from ResultInfo where `user_idx` = %s"""
        curs.execute(query, (self.UserInfo.idx))

        
