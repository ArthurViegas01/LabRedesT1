import java.io.*;
import java.net.*;
import java.util.*;

public class Servidor {
    private DatagramSocket socket;
    private byte[] buffer = new byte[1024];
    private List<Clientes> clientes = new ArrayList<>();
    private int portaServidor;

    public Servidor(int portaServidor) {
        this.portaServidor = portaServidor;
        try {
            socket = new DatagramSocket(portaServidor);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void iniciar() {
        System.out.println("Servidor UDP iniciado. Aguardando mensagens...");
        while (true) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                InetAddress enderecoCliente = packet.getAddress();
                int portaCliente = packet.getPort();
                String mensagem = new String(packet.getData(), 0, packet.getLength());
                Protocolo protocolo = new Protocolo(mensagem);
                Protocolo resposta = null;

                Clientes cliente = new Clientes(enderecoCliente, portaCliente,protocolo.getUsernameOrigem());

                switch (protocolo.getTipo()) {
                    case 'R':
                        if (!clientes.contains(cliente)) {
                            clientes.add(cliente);
                        }
                        System.out.println("Cliente registrado: " + cliente.getUsername());
                        break;

                    case 'M':
                        resposta = new Protocolo('M', protocolo.getUsernameOrigem(), protocolo.getUsernameDestino(), protocolo.getMensagem());
                        enviarMensagem(resposta);
                        System.out.println("Mensagem enviada para " + resposta.getUsernameDestino());
                        break;

                    case 'L':
                        StringBuilder usernames = new StringBuilder();
                        for (Clientes c : clientes){
                            if(!c.getUsername().equals(protocolo.getUsernameOrigem())){
                                usernames.append(c.getUsername()).append(",");
                            }
                        }
                        resposta = new Protocolo('L', "servidor", protocolo.getUsernameOrigem(), usernames.toString());
                        enviarMensagem(resposta);
                        System.out.println("Lista de usuários enviada para " + resposta.getUsernameDestino());
                        break;

                    case 'F':
                        resposta = new Protocolo('F', protocolo.getUsernameOrigem(), protocolo.getUsernameDestino(), protocolo.getConteudoArquivo());
                        enviarMensagem(resposta);
                        System.out.println("Arquivo enviado para " + resposta.getUsernameDestino());
                        break;

                    default:
                        break;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void enviarMensagem(Protocolo protocoloDestino) {
        try {
            byte[] buffer = protocoloDestino.toString().getBytes();
            Clientes cliente = null;
            for (Clientes c : clientes) {
                if (c.getUsername().equals(protocoloDestino.getUsernameDestino())) {
                    cliente = c;
                    break;
                }
            }
            InetAddress enderecoClienteDestino = cliente.getEnderecoCliente();
            int portaClienteDestino = cliente.getPortaCliente();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, enderecoClienteDestino, portaClienteDestino);
            socket.send(packet);
            System.out.println("Mensagem enviada para " + cliente.getEnderecoCliente().toString().substring(1) + ":" + cliente.getPortaCliente());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Uso: java Servidor <porta_servidor>");
            return;
        }

        int portaServidor = Integer.parseInt(args[0]);

        Servidor servidor = new Servidor(portaServidor);
        servidor.iniciar();
    }
}
