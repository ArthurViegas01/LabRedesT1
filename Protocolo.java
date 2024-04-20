import java.io.Serializable;

public class Protocolo implements Serializable {
    private static final long serialVersionUID = 1L;

    private char tipo;
    private String usernameOrigem;
    private String usernameDestino;
    private String mensagem;

    public Protocolo(String protocolo) {
        tipo = protocolo.charAt(0);
        usernameOrigem = protocolo.substring(1, protocolo.indexOf('|'));
        usernameDestino = protocolo.substring(protocolo.indexOf('|') + 1, protocolo.indexOf('|', protocolo.indexOf('|') + 1));
        mensagem = protocolo.substring(protocolo.indexOf('|', protocolo.indexOf('|') + 1) + 1);
    }

    public Protocolo(char tipo, String username, String usernameDestino, String mensagem) {
        this.tipo = tipo;
        this.usernameOrigem = username;
        this.usernameDestino = usernameDestino;
        this.mensagem = mensagem;
    }

    public char getTipo() {
        return tipo;
    }

    public void setTipo(char tipo) {
        this.tipo = tipo;
    }

    public String getUsernameOrigem() {
        return usernameOrigem;
    }

    public void setUsernameOrigem(String username) {
        this.usernameOrigem = username;
    }

    public String getUsernameDestino() {
        return usernameDestino;
    }

    public void setUsernameDestino(String usernameDestino) {
        this.usernameDestino = usernameDestino;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    @Override
    public String toString() {
        return tipo + usernameOrigem +"|"+ usernameDestino +"|" + mensagem;
    }
}
