import java.io.*;
import java.net.*;
import java.util.*;

public class Servidor {
    private ServerSocket serverSocket;
    private List<Clientes> clientes = new ArrayList<>();
    private int portaServidor;

    public Servidor(int portaServidor) {
        this.portaServidor = portaServidor;
        try {
            serverSocket = new ServerSocket(portaServidor);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void iniciar() {
        System.out.println("Servidor TCP iniciado. Aguardando conexões...");
        while (true) {
            try {
                Socket clienteSocket = serverSocket.accept();
                Thread clienteThread = new Thread(new ClienteHandler(clienteSocket));
                clienteThread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class ClienteHandler implements Runnable {
        private Socket clienteSocket;

        public ClienteHandler(Socket clienteSocket) {
            this.clienteSocket = clienteSocket;
        }

        @Override
        public void run() {
            try (ObjectInputStream entrada = new ObjectInputStream(clienteSocket.getInputStream());
                 ObjectOutputStream saida = new ObjectOutputStream(clienteSocket.getOutputStream())) {

                InetAddress enderecoCliente = clienteSocket.getInetAddress();
                int portaCliente = clienteSocket.getPort();
                Protocolo protocolo = (Protocolo) entrada.readObject();

                Clientes cliente = new Clientes(enderecoCliente, portaCliente, protocolo.getUsernameOrigem(), saida);

                if (!clientes.contains(cliente)) {
                    clientes.add(cliente);
                    System.out.println("Cliente registrado: " + cliente.getUsername());
                }

                while (protocolo != null) {
                    Protocolo resposta = null;

                    switch (protocolo.getTipo()) {
                        case 'M':
                        for (Clientes c : clientes) {
                            if (c.getUsername().equals(protocolo.getUsernameDestino())) {
                                ObjectOutputStream saidaDestino = c.getSaida();
                                resposta = new Protocolo('M', protocolo.getUsernameOrigem(), protocolo.getUsernameDestino(), protocolo.getMensagem());
                                saidaDestino.writeObject(resposta);
                                System.out.println("Mensagem enviada para " + resposta.getUsernameDestino());
                                break;
                            }
                        }
                            break;

                        case 'F':
                        for (Clientes c : clientes) {
                            if (c.getUsername().equals(protocolo.getUsernameDestino())) {
                                 ObjectOutputStream saidaDestino = c.getSaida();
                                 resposta = new Protocolo('F', protocolo.getUsernameOrigem(), protocolo.getUsernameDestino(), protocolo.getConteudoArquivo());
                                 saidaDestino.writeObject(resposta);
                                  System.out.println("Arquivo enviado para " + resposta.getUsernameDestino());
                                break;
                            }
                        }
                             break;
                        case 'L':
                            StringBuilder usernames = new StringBuilder();
                            for (Clientes c : clientes) {
                                if (!c.getUsername().equals(protocolo.getUsernameOrigem())) {
                                    usernames.append(c.getUsername()).append(",");
                                }
                            }
                            resposta = new Protocolo('L', "servidor", protocolo.getUsernameOrigem(), usernames.toString());
                            saida.writeObject(resposta);
                            System.out.println("Lista de usuários enviada para " + resposta.getUsernameDestino());
                            break;

                        case 'R':
                        break;

                        default:
                            break;
                    }
                    protocolo = (Protocolo) entrada.readObject();
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    clienteSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        int portaServidor = 12345;

        Servidor servidor = new Servidor(portaServidor);
        servidor.iniciar();
    }
}
