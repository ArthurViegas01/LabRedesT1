import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Cliente {
    private DatagramSocket socket;
    private InetAddress enderecoServidor;
    private int portaServidor;
    private int portaCliente;

    public Cliente(String enderecoServidor, int portaServidor, int portaCliente) {
        try {
            this.enderecoServidor = InetAddress.getByName(enderecoServidor);
            this.portaServidor = portaServidor;
            this.portaCliente = portaCliente;
            socket = new DatagramSocket(portaCliente);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void enviarMensagem(String mensagem) {
        try {
            byte[] buffer = mensagem.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, enderecoServidor, portaServidor);
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String receberMensagem() {
        try {
            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);
            return new String(packet.getData(), 0, packet.getLength());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void fecharConexao() {
        socket.close();
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Uso: java Cliente <porta_cliente>");
            return;
        }

        String enderecoServidor = "localhost";
        int portaServidor = 12345;
        int portaCliente = Integer.parseInt(args[0]);
        //int portaCliente = Integer.parseInt(args[0]);

        Cliente cliente = new Cliente(enderecoServidor, portaServidor, portaCliente);

        // Thread para receber mensagens
        Thread threadReceberMensagens = new Thread(() -> {
            while (true) {
                String mensagemRecebida = cliente.receberMensagem();
                if (mensagemRecebida != null) {
                    System.out.println("Mensagem recebida do servidor: " + mensagemRecebida);
                }
            }
        });
        threadReceberMensagens.start();

        // Lógica para enviar mensagens
        Scanner scanner = new Scanner(System.in);
        System.out.println("Conexão estabelecida com o servidor. Digite 'sair' para encerrar.");
        while (true) {
            System.out.print("Digite a mensagem: ");
            String mensagemParaEnviar = scanner.nextLine();
            if (mensagemParaEnviar.equalsIgnoreCase("sair")) {
                cliente.fecharConexao();
                break;
            }
            cliente.enviarMensagem(mensagemParaEnviar);
        }
        scanner.close();
    }
}
