import java.io.Serializable;

public class Protocolo implements Serializable{
    private char tipo;
    private String usernameOrigem;
    private String usernameDestino;
    private byte[] conteudoArquivo;

    public Protocolo(String protocolo) {
        tipo = protocolo.charAt(0);
        usernameOrigem = protocolo.substring(1, protocolo.indexOf('|'));
        usernameDestino = protocolo.substring(protocolo.indexOf('|') + 1, protocolo.indexOf('|', protocolo.indexOf('|') + 1));
        conteudoArquivo = protocolo.substring(protocolo.indexOf('|', protocolo.indexOf('|') + 1) + 1).getBytes();
    }

    public Protocolo(char tipo, String usernameOrigem, String usernameDestino, String mensagem) {
        this.tipo = tipo;
        this.usernameOrigem = usernameOrigem;
        this.usernameDestino = usernameDestino;
        this.conteudoArquivo = mensagem.getBytes();
    }

    public Protocolo(char tipo, String usernameOrigem, String usernameDestino, byte[] conteudoArquivo) {
        this.tipo = tipo;
        this.usernameOrigem = usernameOrigem;
        this.usernameDestino = usernameDestino;
        this.conteudoArquivo = conteudoArquivo;
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

    public void setUsernameOrigem(String usernameOrigem) {
        this.usernameOrigem = usernameOrigem;
    }

    public String getMensagem() {
        return new String(conteudoArquivo);
    }

    public String getUsernameDestino() {
        return usernameDestino;
    }

    public void setUsernameDestino(String usernameDestino) {
        this.usernameDestino = usernameDestino;
    }

    public byte[] getConteudoArquivo() {
        return conteudoArquivo;
    }

    public void setConteudoArquivo(byte[] conteudoArquivo) {
        this.conteudoArquivo = conteudoArquivo;
    }

    @Override
    public String toString() {

            return tipo + usernameOrigem +"|"+ usernameDestino +"|" + new String(conteudoArquivo);
    }
}
