import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.nio.file.Files;

public class ClienteGUI {
    private static final int TAMANHO_MAX_ARQUIVO = 1024 * 1024; 

    private Socket socket;
    private ObjectOutputStream saida;
    private ObjectInputStream entrada;
    private String username;
    private String[] usuariosLista;

    private JTextArea areaMensagens;
    private JTextField campoEnviar;
    private JList<String> listaUsuarios;

    private JButton btnSelecionarArquivo;
    private JButton btnEnviarArquivo;
    private File arquivoSelecionado;
    private JFrame frame;

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
        frame = new JFrame("Chat TCP - " + username);
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

        btnSelecionarArquivo = new JButton("Selecionar Arquivo");
        btnSelecionarArquivo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selecionarArquivo();
            }
        });

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
            if (arquivoSelecionado.length() > TAMANHO_MAX_ARQUIVO) {
                JOptionPane.showMessageDialog(frame, "O arquivo selecionado excede o tamanho máximo permitido.");
                return;
            }

            byte[] conteudoArquivo = Files.readAllBytes(arquivoSelecionado.toPath());

            Protocolo mensagemEnviar = new Protocolo('F', username, destinatario, conteudoArquivo);
            enviarMensagem(mensagemEnviar, destinatario);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private void enviarMensagem(Protocolo protocolo, String destinatario) {
        try {
            if(protocolo.getTipo() == 'F'){
                atualizarAreaMensagens("Arquivo enviado. ");
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
        } else if (protocolo.getTipo() == 'F') {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    String mensagemFormatada = "Arquivo recebido de " + protocolo.getUsernameOrigem() + ".";
                    atualizarAreaMensagens(mensagemFormatada);
                    salvarArquivo(protocolo.getUsernameOrigem(), protocolo.getConteudoArquivo());
                }
            });
        }
        
        else {
            SwingUtilities.invokeLater(() -> atualizarAreaMensagens("Mensagem recebida do servidor: " + protocolo.toString()));
        }
    }

    private void salvarArquivo(String origem, byte[] conteudoArquivo) {
        String nomeArquivo = "arquivo_" + System.currentTimeMillis() + ".txt";
        String diretorioDestino = System.getProperty("user.dir");
    
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
        if (args.length != 2) {
            System.out.println("Uso: java ClienteGUI <porta> <username>");
            return;
        }

        String enderecoServidor = "localhost";
        int portaServidor = 12345;
        int portaCliente = Integer.parseInt(args[0]);
        String username = args[1];

        SwingUtilities.invokeLater(() -> new ClienteGUI(enderecoServidor, portaServidor, portaCliente, username));
    }
}
