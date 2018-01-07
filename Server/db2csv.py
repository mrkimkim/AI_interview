import csv
import pymysql

sql = pymysql.connect(host='localhost', user='root', password='lfamesk5uf!@#',
                        db='AI', charset='utf8', autocommit=True)
curs = sql.cursor()

with open('DB.csv', 'w', newline='', encoding='utf-8') as csv_file:
    writer = csv.writer(csv_file, delimiter=',')
    
    query = """select * from Category"""
    curs.execute(query)
    rows = curs.fetchall()

    for i in range(len(rows)):
        writer.writerow(rows[i])

    query = """select * from Problems"""
    curs.execute(query)
    rows = curs.fetchall()

    for i in range(len(rows)):
        writer.writerow(rows[i])
