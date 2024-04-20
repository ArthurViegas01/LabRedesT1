import java.net.InetAddress;
import java.util.Objects;
import java.io.*;

public class Clientes {
    private InetAddress enderecoCliente;
    private int portaCliente;
    private String username;
    private ObjectOutputStream saida; // novo atributo

    public Clientes(InetAddress enderecoCliente, int portaCliente, String username, ObjectOutputStream saida) {
        this.enderecoCliente = enderecoCliente;
        this.portaCliente = portaCliente;
        this.username = username; 
        this.saida = saida;
    }

    public InetAddress getEnderecoCliente() {
        return enderecoCliente;
    }

    public int getPortaCliente() {
        return portaCliente;
    }

    public String getUsername() { 
        return username;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Clientes endereco = (Clientes) o;
        return portaCliente == endereco.portaCliente &&
                Objects.equals(enderecoCliente, endereco.enderecoCliente);
    }

    public void setSaida(ObjectOutputStream saida) {
        this.saida = saida;
    }

    public ObjectOutputStream getSaida() {
        return saida;
    }

    @Override
    public int hashCode() {
        return Objects.hash(enderecoCliente, portaCliente);
    }

    @Override
    public String toString() {
        return enderecoCliente + ":" + portaCliente;
    }
}
