import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.Arrays;

public class ClienteGUI {
    private DatagramSocket socket;
    private InetAddress enderecoServidor;
    private int portaServidor;
    private int portaCliente;
    private String username;
    private String[] usuariosLista;

    private JTextArea areaMensagens;
    private JTextField campoEnviar;
    private JList<String> listaUsuarios;

    public ClienteGUI(String enderecoServidor, int portaServidor, int portaCliente, String username) {
        try {
            this.enderecoServidor = InetAddress.getByName(enderecoServidor);
            this.portaServidor = portaServidor;
            this.portaCliente = portaCliente;
            this.username = username;
            socket = new DatagramSocket(portaCliente);
        } catch (IOException e) {
            e.printStackTrace();
        }

        JFrame frame = new JFrame("Chat UDP - " + username);
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JPanel panelUsuarios = new JPanel(new BorderLayout());

        JButton btnListar = new JButton("Listar");
        btnListar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enviarMensagemListar();
            }
        });
        panelUsuarios.add(btnListar, BorderLayout.NORTH);

        listaUsuarios = new JList<>();
        listaUsuarios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panelUsuarios.add(new JScrollPane(listaUsuarios), BorderLayout.CENTER);
        frame.add(panelUsuarios, BorderLayout.WEST);

        areaMensagens = new JTextArea();
        areaMensagens.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(areaMensagens);
        frame.add(scrollPane, BorderLayout.CENTER);

        campoEnviar = new JTextField();
        campoEnviar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String mensagem = campoEnviar.getText();
                String destinatario = listaUsuarios.getSelectedValue();
                if (destinatario != null && !destinatario.isEmpty()) {
                    enviarMensagem(mensagem, destinatario);
                    campoEnviar.setText("");
                } else {
                    JOptionPane.showMessageDialog(frame, "Selecione um destinatário na lista.");
                }
            }
        });
        frame.add(campoEnviar, BorderLayout.SOUTH);

        frame.setVisible(true);

        Thread threadReceberMensagens = new Thread(() -> {
            while (true) {
                String mensagemRecebida = receberMensagem();
                if (mensagemRecebida != null) {
                    processarMensagemRecebida(mensagemRecebida);
                }
            }
        });
        threadReceberMensagens.start();
    }

    private void enviarMensagem(String mensagem, String destinatario) {
        Protocolo mensagemEnviar = new Protocolo('M', username, destinatario, mensagem);
        enviarMensagem(mensagemEnviar.toString());
    }

    private void enviarMensagem(String mensagem) {
        try {
            byte[] buffer = mensagem.getBytes();
            Protocolo protocolo = new Protocolo(mensagem);
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, enderecoServidor, portaServidor);
            socket.send(packet);
            if(protocolo.getTipo() == 'M'){
                atualizarAreaMensagens("Mensagem enviada: " + protocolo.getMensagem());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void enviarMensagemListar() {
        Protocolo mensagemListar = new Protocolo('L', username, "servidor", "");
        enviarMensagem(mensagemListar.toString());
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

    private void processarMensagemRecebida(String mensagemRecebida) {
        Protocolo protocolo = new Protocolo(mensagemRecebida);
        if (protocolo.getTipo() == 'L') {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    exibirListaUsuarios(protocolo.getMensagem());
                }
            });
        } else if (protocolo.getTipo() == 'M') {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    String mensagemFormatada = "Mensagem recebida de " + protocolo.getUsernameOrigem() + ": " + protocolo.getMensagem();
                    atualizarAreaMensagens(mensagemFormatada);
                }
            });
        } else {
            atualizarAreaMensagens("Mensagem recebida do servidor: " + mensagemRecebida);
        }
    }

    private void exibirListaUsuarios(String mensagem) {
        usuariosLista = mensagem.split(",");
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                listaUsuarios.setListData(usuariosLista);
            }
        });
    }

    private void atualizarAreaMensagens(String mensagem) {
        areaMensagens.append(mensagem + "\n");
    }

    public void fecharConexao() {
        socket.close();
    }
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Uso: java ClienteGUI <porta> <username>");
            return;
        }

        String enderecoServidor = "localhost";
        int portaServidor = 12345;
        int portaCliente = Integer.parseInt(args[0]);
        String username = args[1];

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Protocolo mensagemInicial = new Protocolo('R', username, "servidor", "");
                ClienteGUI cliente = new ClienteGUI(enderecoServidor, portaServidor, portaCliente, username);
                cliente.enviarMensagem(mensagemInicial.toString());
            }
        });
    }
}
