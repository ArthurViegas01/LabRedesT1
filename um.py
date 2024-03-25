import socket

server_ip = 'localhost'
server_port = 12000

server_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM, 0)

server_socket.bind((server_ip, server_port))

print("O servidor está pronto para receber mensagens.")

while True:
    message, client_address = server_socket.recvfrom(2048)
    print(f"Mensagem recebida de {client_address}: {message.decode()}")
    
    modified_message = message.decode().upper()
    server_socket.sendto(modified_message.encode(), client_address)
