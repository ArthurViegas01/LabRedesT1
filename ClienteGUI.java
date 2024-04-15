import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class ClienteGUI {
    private DatagramSocket socket;
    private InetAddress enderecoServidor;
    private int portaServidor;
    private int portaCliente;

    private JTextArea areaMensagens;
    private JTextField campoEnviar;

    public ClienteGUI(String enderecoServidor, int portaServidor, int portaCliente) {
        try {
            this.enderecoServidor = InetAddress.getByName(enderecoServidor);
            this.portaServidor = portaServidor;
            this.portaCliente = portaCliente;
            socket = new DatagramSocket(portaCliente);
        } catch (IOException e) {
            e.printStackTrace();
        }

        JFrame frame = new JFrame("Cliente Chat");
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        areaMensagens = new JTextArea();
        areaMensagens.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(areaMensagens);
        frame.add(scrollPane, BorderLayout.CENTER);

        campoEnviar = new JTextField();
        campoEnviar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enviarMensagem(campoEnviar.getText());
                campoEnviar.setText("");
            }
        });
        frame.add(campoEnviar, BorderLayout.SOUTH);

        frame.setVisible(true);

        Thread threadReceberMensagens = new Thread(() -> {
            while (true) {
                String mensagemRecebida = receberMensagem();
                if (mensagemRecebida != null) {
                    atualizarAreaMensagens(mensagemRecebida + ":");
                }
            }
        });
        threadReceberMensagens.start();
    }

    private void enviarMensagem(String mensagem) {
        try {
            byte[] buffer = mensagem.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, enderecoServidor, portaServidor);
            socket.send(packet);
            atualizarAreaMensagens("Mensagem enviada para o servidor: " + mensagem);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String receberMensagem() {
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

    private void atualizarAreaMensagens(String mensagem) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                areaMensagens.append(mensagem + "\n");
            }
        });
    }

    public void fecharConexao() {
        socket.close();
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Uso: java ClienteGUI <porta_cliente>");
            return;
        }

        String enderecoServidor = "localhost";
        int portaServidor = 12345;
        int portaCliente = Integer.parseInt(args[0]);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ClienteGUI(enderecoServidor, portaServidor, portaCliente);
            }
        });
    }
}
