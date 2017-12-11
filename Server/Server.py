import socket
import sys

def get_userinfo(con, len_userinfo):
    userinfo = {}
    received = ""
    len_receive = 0
    
    while len_receive < len_userinfo:
        data = connection.recv(100)
        if data:
            received += data
            len_receive += len(data)

    received = received.strip().split(":")
    for i in range(len(received) / 2): userinfo[received[i * 2]] = received[i * 2 + 1]

    return 1, userinfo


def get_metainfo(con, len_metainfo):
    metainfo = {}
    received = ""
    len_receive = 0
    
    while len_receive < len_metainfo:
        data = connection.recv(100)
        if data:
            received += data
            len_receive += len(data)

    received = received.strip().split(":")
    for i in range(len(received) / 2): metainfo[received[i * 2]] = received[i * 2 + 1]

    return 1, metainfo


def get_video(con, len_video):
    video = ""
    len_receive = 0
    while len_receive < len_video:
        data = connection.recv(1024)
        if data:
            video += data
            len_receive += len(data)

    return 1, video


# Server Info
Bind_IP = '0.0.0.0'
Port = 8426

sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
server_addr = (Bind_IP, Port)
sock.bind(server_addr)
sock.listen(1)

msg = "I am ready to receive msg\n"

len_receive = 0
len_userinfo = 100
len_metainfo = 100
len_video = 200

userinfo = ""
metainfo = ""
video = ""

while True:
    connection, client_addr = sock.accept()
    print "Client Connected"
    try:
        # receive userinfo
        flag, userinfo = get_userinfo(connection, len_userinfo)
        assert flag
        connection.sendall(msg)
        print "Successfully Received UserInfo"
        
        # receive metainfo
        flag, metainfo = get_metainfo(connection, len_metainfo)
        assert flag
        connection.sendall(msg)
        print "Successfully Received MetaInfo"
            
        # receive video
        len_video = int(metainfo["size"])
        flag, video = get_video(connection, len_video)
        assert flag
        print "Successfully Received Video"

        print "Saving Video..."
        f = open(metainfo['fileName'], 'wb')
        f.write(video)
        f.close()
        print "Successfully Saved " + metainfo['fileName'] + "!";
        
    finally:
        connection.close()

    print userinfo
    print metainfo
    print len(video)
