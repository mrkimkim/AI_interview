########### Python 2.7 #############
import httplib, urllib, base64

headers = {
    # Request headers
    'Content-Type': 'multipart/form-data',
    'Ocp-Apim-Subscription-Key': 'ddaa40a3639d4f06a9f9fe1cc34bb837',
}

params = urllib.urlencode({
    # Request parameters
    'name': 'hyeon',
    'privacy': 'Private',
    'videoUrl': 'https://storage.googleapis.com/test-hyeongyujang/sample.mp4'
})

try:
    conn = httplib.HTTPSConnection('videobreakdown.azure-api.net')
    conn.request("POST", "/Breakdowns/Api/Partner/Breakdowns?%s" % params, "{body}", headers)
    response = conn.getresponse()
    data = response.read()
    print(data)
    conn.close()
except Exception as e:
    print("[Errno {0}] {1}".format(e.errno, e.strerror))

####################################
