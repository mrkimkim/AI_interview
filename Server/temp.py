import socket
import os

sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
server_address = ('13.75.93.81', 8426)
sock.connect(server_address)

file_name = 'sample.mp4'

# Start Video Transformation
sock.sendall('sendVideo ')

# send file_name length
data = sock.recv(5)
if data == 'ready':
    sock.sendall('0' * (16 - len(str(len(file_name)))) + str(len(file_name)))

# send file_name
data = sock.recv(5)
if data == 'ready':
    sock.sendall(file_name)

# send file_size
f = open(file_name, 'rb')
f.seek(0, os.SEEK_END)
size = f.tell()
f.seek(0, 0)
print size

data = sock.recv(5)
if data == 'ready':
    sock.sendall('0' * (16 - len(str(size))) + str(size))

# send file
data = sock.recv(5)
if data == 'ready':
    data = f.read()
    sock.sendall(data)


# close connection
data = sock.recv(5)
if data == 'ready':
    sock.sendall('quit      ')

sock.close()

