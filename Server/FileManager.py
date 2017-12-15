class mFileManager(object):
    def mkdir(self, path):
        import os
        try:
            if os.path.isdir(path): return 0
            else: os.makedirs(path)
            return True
        except:
            print "Fail to Make direction"
            return False
        
