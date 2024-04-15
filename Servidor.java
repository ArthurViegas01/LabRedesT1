import java.io.*;
import java.net.*;
import java.util.*;

public class Servidor {
    private DatagramSocket socket;
    private byte[] buffer = new byte[1024];
    private List<Endereco> clientes = new ArrayList<>();
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
                Endereco endereco = new Endereco(enderecoCliente, portaCliente);

                if (!clientes.contains(endereco)) {
                    clientes.add(endereco);
                }

                String mensagem = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Mensagem recebida de " + enderecoCliente + ":" + portaCliente + ": " + mensagem);

                // Enviar a mensagem recebida para todos os clientes
                enviarMensagemParaTodos(mensagem, endereco);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void enviarMensagemParaTodos(String mensagem, Endereco enderecoOrigem) {
        try {
            String mensagemCompleta = (enderecoOrigem.toString() + ": " + mensagem).substring(1);
            byte[] buffer = mensagemCompleta.getBytes();
            for (Endereco cliente : clientes) {
                
                if (!cliente.equals(enderecoOrigem)) {
                    InetAddress endereco = cliente.getEnderecoCliente();
                    int porta = cliente.getPortaCliente();
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, endereco, porta);
                    socket.send(packet);
                    System.out.println("Mensagem enviada para " + cliente.getEnderecoCliente() + ":" + cliente.getPortaCliente() + ": " + mensagem);
                }
            }
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
