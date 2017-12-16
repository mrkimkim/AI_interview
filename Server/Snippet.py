import ffmpy

result = ""
ff = ffmpy.FFprobe(inputs = {'./Data/5ed8944a85a9763fd315852f448cb7de36c5e928e13b3be427f98f7dc455f141/33e6cfc7ff0831e9a8f3a26491472bdcaea139f3163e07b1daf2c70c9d9fe0ba/33e6cfc7ff0831e9a8f3a26491472bdcaea139f3163e07b1daf2c70c9d9fe0ba.mp4':'-show_entries stream=duration'})
with open('./Data/5ed8944a85a9763fd315852f448cb7de36c5e928e13b3be427f98f7dc455f141/33e6cfc7ff0831e9a8f3a26491472bdcaea139f3163e07b1daf2c70c9d9fe0ba/temp','w') as output_file:
    ff.run(stdout=output_file)
    
f = open('./Data/5ed8944a85a9763fd315852f448cb7de36c5e928e13b3be427f98f7dc455f141/33e6cfc7ff0831e9a8f3a26491472bdcaea139f3163e07b1daf2c70c9d9fe0ba/temp','r').readlines()
print "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@"
print (f[1], f[4])
