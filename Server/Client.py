import socket
import sys

sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
server_addr = ('localhost', 8426)
sock.connect(server_addr)

try:
    sock.sendall("userinfois" * 10)
    amount_received = 0
    amount_expected = len("I am ready to receive msg")

    while amount_received < amount_expected:
        data = sock.recv(amount_expected)
        if data:
            amount_received += len(data)
            print data
            
    sock.sendall("metainfois" * 10)

    amount_received = 0
    amount_expected = len("I am ready to receive msg")

    while amount_received < amount_expected:
        data = sock.recv(amount_expected)
        if data:
            amount_received += len(data)
            print data

    sock.sendall("video" * 40)
    
finally:
    sock.close()
