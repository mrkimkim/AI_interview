import datetime
import urllib
from bs4 import BeautifulSoup as bs

"""
Naver News Crawler
"""

# Variable Setting
urlRoot = 'http://news.naver.com/main/history/mainnews/index.nhn?'

start_date = datetime.date(2014, 05, 01)
end_date = datetime.date(2018, 02, 21)
day_count = (end_date - start_date).days + 1

filePath = ""
length = 0

# def getPage()

# def getArticle()

# def parseArticle()

# Do Crawling range (start_date, end_date)
for day in [d for d in (start_date + datetime.timedelta(n) for n in range(day_count)) if d < end_date]:

    # Set post params
    post_params = {
        'date' : day.strftime("%Y-%m-%d"),
        'time' : '00:00'
        }
    post_args = urllib.urlencode(post_params)
    html = urllib.urlopen(urlRoot, post_args)

    # Read Page and Make News List
    soup = bs(html, "html.parser")
    result = soup.find_all("a", {"class":["nclicks(his_pan.text,,2)",
                                          "nclicks(his_pan.text,,3)",
                                          "nclicks(his_pan.text,,4)",
                                          "nclicks(his_pan.text,,5)",
                                          "nclicks(his_pan.text,,6)"]})

    # Read Article and Extract News Body
    try:
        print (day.strftime("%Y-%m-%d"), day, length)
        for r in result:
            title, subUrl = (r.get_text()), (r['href'])
            length += len(title) + len(subUrl)

            html = urllib.urlopen(subUrl)
            soup = bs(html, "html.parser")

            subResult = soup.find_all("div", {"id":"articleBodyContents"})
            for sub_r in subResult:
                print (sub_r.get_text())
                break

            # Write News Data in CSV Format
    
    except:
        print ("Error", day.strftime("%Y-%m-%d"))

    break
