# -*- coding: utf-8 -*-
from bs4 import BeautifulSoup as bs
from striplist import Newsfooter, Javascript
import datetime
import urllib.request, urllib.parse
import re
import codecs


"""
Naver News Crawler
"""

# Global Variables
filePath = ""
length = 0

# Crawler Class
class Crawler():
    def __init__(self, url):
        self.folderpath = "./data/"
        self.url = url
        self.start_date = datetime.date(2014, 5, 1)
        self.end_date = datetime.date(2018, 3, 7)
        self.day_count = (self.end_date - self.start_date).days + 1

        self.article_list_arrtibute = ["a", {"class": ["nclicks(his_pan.text,,2)",
                                               "nclicks(his_pan.text,,3)",
                                               "nclicks(his_pan.text,,4)",
                                               "nclicks(his_pan.text,,5)",
                                               "nclicks(his_pan.text,,6)"]}]
        self.article_attribute = ["div", {"id" : ["articleBodyContents"]}, {"class" : ["article_body font1 size3"]}]
        self.article_link_signature = "//news.naver.com"

    # POST 파라미터를 세팅
    def get_post_params(self, day):
        return urllib.parse.urlencode({
            'date': day.strftime("%Y-%m-%d"),
            'time': '00:00'
        }).encode('ascii')


    # 뉴스 리스트에서 타이틀과 링크만을 추출한다
    def get_article_list(self, day):
        try:
            records = []
            html = urllib.request.urlopen(self.url, self.get_post_params(day))
            soup = bs(html, "html.parser")

            for record in soup.find_all(self.article_list_arrtibute[0], self.article_list_arrtibute[1]):
                title, link = record.get_text(), record['href']
                if self.article_link_signature in link: records.append([title, link])

            return records
        except:
            return []


    # 뉴스 리스트에서 본문을 추출한다
    def parse_article(self, article):
        body = ""
        try:
            html = urllib.request.urlopen(article[1])
            body = bs(html, "html.parser").find_all(self.article_attribute[0], self.article_attribute[1])[0].getText().strip()
        except Exception as e:
            try:
                html = urllib.request.urlopen(article[1])
                body = bs(html, "html.parser").find_all(self.article_attribute[0], self.article_attribute[2])[0].getText().strip()
            except Exception as e:
                print (e)
        return self.huristic_stripper(body)


    # 특수문자와 말줄임표를 제거한다.
    def remove_special_character(self, body):
        hangul = re.compile('[^. 0-9a-zA-Zㄱ-ㅣ가-힣]+')
        body = hangul.sub('', body)
        body = body.replace('...','.')
        body = body.replace('..','.')
        return body


    # 본문을 좀 더 글에 가깝게 정제한다
    def huristic_stripper(self, body):
        # Java Script 제거
        for i in range(len(Javascript)): body = body.replace(Javascript[i], "").strip()

        # 특수 문자 제거
        body = self.remove_special_character(body)

        # News Footer 제거
        for i in range(len(Newsfooter)): body = body.replace(Newsfooter[i], "").strip()

        # 마침표 기준 슬라이스
        body = body.split('.')
        i = len(body) - 1
        while i >= 0:
            if len(body[i].split(' ')) < 5: del body[i]
            i -= 1
        return '.'.join(body[:len(body) - 2])


    # 크롤러 실행
    def run(self):
        for day in [d for d in (self.start_date + datetime.timedelta(n) for n in range(self.day_count)) if d < self.end_date]:
            print (day.strftime("%Y-%m-%d"))
            try:
                article_list = self.get_article_list(day)
                data = ""
                for article in article_list:
                    data += article[0] + "\t" + article[1] + "\t"
                    data += self.parse_article(article) + "\n"
                self.write(day.strftime("%Y-%m-%d"), data)
            except Exception as e:
                print (e)


    # 파일을 쓴다
    def write(self, filename, data):
        file = codecs.open(self.folderpath + filename + '.csv', 'w', 'utf-8')
        file.write(data)
        file.close()


crawler = Crawler('http://news.naver.com/main/history/mainnews/index.nhn?')
crawler.run()