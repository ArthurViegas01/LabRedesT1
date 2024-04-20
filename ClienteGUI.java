import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class ClienteGUI {
    private Socket socket;
    private ObjectOutputStream saida;
    private ObjectInputStream entrada;
    private String username;
    private String[] usuariosLista;

    private JTextArea areaMensagens;
    private JTextField campoEnviar;
    private JList<String> listaUsuarios;

    public ClienteGUI(String enderecoServidor, int portaServidor, int portaCliente, String username) {
        try {
            this.username = username;
            socket = new Socket(enderecoServidor, portaServidor);
            saida = new ObjectOutputStream(socket.getOutputStream());
            entrada = new ObjectInputStream(socket.getInputStream());
            inicializarInterface();
            enviarMensagemInicial();
            Thread threadReceberMensagens = new Thread(this::receberMensagens);
            threadReceberMensagens.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void inicializarInterface() {
        JFrame frame = new JFrame("Chat TCP - " + username);
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
    }

    private void enviarMensagem(String mensagem, String destinatario) {
        try {
            Protocolo protocolo = new Protocolo('M', username, destinatario, mensagem);
            if(protocolo.getTipo() == 'M'){
                atualizarAreaMensagens("Mensagem enviada: " + protocolo.getMensagem());
            }
            saida.writeObject(protocolo);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void enviarMensagemListar() {
        try {
            Protocolo protocolo = new Protocolo('L', username, "servidor", "");
            saida.writeObject(protocolo);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void enviarMensagemInicial() {
        try {
            Protocolo protocolo = new Protocolo('R', username, "servidor", "");
            saida.writeObject(protocolo);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void receberMensagens() {
        try {
            while (true) {
                Protocolo protocolo = (Protocolo) entrada.readObject();
                processarMensagemRecebida(protocolo);
                
            }
        } catch (EOFException e) {
            System.out.println("Conexão encerrada pelo servidor.");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void processarMensagemRecebida(Protocolo protocolo) {
        if (protocolo.getTipo() == 'L') {
            SwingUtilities.invokeLater(() -> exibirListaUsuarios(protocolo.getMensagem()));
        } else if (protocolo.getTipo() == 'M') {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    String mensagemFormatada = "Mensagem recebida de " + protocolo.getUsernameOrigem() + ": " + protocolo.getMensagem();
                    atualizarAreaMensagens(mensagemFormatada);
                }
            });
        } else {
            SwingUtilities.invokeLater(() -> atualizarAreaMensagens("Mensagem recebida do servidor: " + protocolo.toString()));
        }
    }

    private void exibirListaUsuarios(String mensagem) {
        usuariosLista = mensagem.split(",");
        SwingUtilities.invokeLater(() -> listaUsuarios.setListData(usuariosLista));
    }

    private void atualizarAreaMensagens(String mensagem) {
        areaMensagens.append(mensagem + "\n");
    }

    public void fecharConexao() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        String enderecoServidor = "localhost";
        int portaServidor = 12345;
        int portaCliente = 36459;
        String username = "Maria";

        SwingUtilities.invokeLater(() -> new ClienteGUI(enderecoServidor, portaServidor, portaCliente, username));
    }
}
