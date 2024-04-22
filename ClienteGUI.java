import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.Arrays;

public class ClienteGUI {
    // Declarar uma constante para o tamanho máximo do arquivo
    private static final int TAMANHO_MAX_ARQUIVO = 1024 * 1024; // 1MB

    private DatagramSocket socket;
    private InetAddress enderecoServidor;
    private int portaServidor;
    private int portaCliente;
    private String username;
    private String[] usuariosLista;

    private JTextArea areaMensagens;
    private JTextField campoEnviar;
    private JList<String> listaUsuarios;

    // Novos atributos para lidar com a seleção de arquivos
    private JButton btnSelecionarArquivo;
    private JButton btnEnviarArquivo;
    private File arquivoSelecionado;
    private JFrame frame;

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

        frame = new JFrame("Chat UDP - " + username);
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

        // Adicionar botão para selecionar arquivo
        btnSelecionarArquivo = new JButton("Selecionar Arquivo");
        btnSelecionarArquivo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selecionarArquivo();
            }
        });

        // Adicionar botão para enviar arquivo
        btnEnviarArquivo = new JButton("Enviar Arquivo");
        btnEnviarArquivo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String destinatario = listaUsuarios.getSelectedValue();
                enviarArquivo(destinatario);
            }
        });

        JPanel panelBotoes = new JPanel(new GridLayout(2, 1));
        panelBotoes.add(btnSelecionarArquivo);
        panelBotoes.add(btnEnviarArquivo);
        frame.add(panelBotoes, BorderLayout.NORTH);

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

    private void selecionarArquivo() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            arquivoSelecionado = fileChooser.getSelectedFile();
        }
    }

    private void enviarArquivo(String destinatario) {
        if (arquivoSelecionado == null) {
            JOptionPane.showMessageDialog(frame, "Nenhum arquivo selecionado.");
            return;
        }

        try {
            // Verificar o tamanho do arquivo
            if (arquivoSelecionado.length() > TAMANHO_MAX_ARQUIVO) {
                JOptionPane.showMessageDialog(frame, "O arquivo selecionado excede o tamanho máximo permitido.");
                return;
            }

            // Ler o conteúdo do arquivo
            byte[] conteudoArquivo = Files.readAllBytes(arquivoSelecionado.toPath());

            // Criar e enviar mensagem contendo o arquivo
            Protocolo mensagemEnviar = new Protocolo('F', username, destinatario, conteudoArquivo);
            enviarMensagem(mensagemEnviar.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        } else if (protocolo.getTipo() == 'F') {
            // Receber arquivo
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    String mensagemFormatada = "Arquivo recebido de " + protocolo.getUsernameOrigem() + ".";
                    atualizarAreaMensagens(mensagemFormatada);
                    salvarArquivo(protocolo.getUsernameOrigem(), protocolo.getConteudoArquivo());
                }
            });
        } else {
            atualizarAreaMensagens("Mensagem recebida do servidor: " + mensagemRecebida);
        }
    }

    private void salvarArquivo(String origem, byte[] conteudoArquivo) {
        String nomeArquivo = "arquivo_" + System.currentTimeMillis() + ".txt"; // Nome do arquivo com timestamp para evitar sobrescrição
        String diretorioDestino = System.getProperty("user.dir"); // Diretório atual do usuário
    
        File arquivo = new File(diretorioDestino, nomeArquivo);
        try (FileOutputStream fos = new FileOutputStream(arquivo)) {
            fos.write(conteudoArquivo);
            atualizarAreaMensagens("Arquivo salvo como: " + arquivo.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
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
