import hashlib

f = open('./sample.mp4', 'rb').read()
print hashlib.sha256("Guest").hexdigest()
print len(f)
print hashlib.sha256(f).hexdigest()
