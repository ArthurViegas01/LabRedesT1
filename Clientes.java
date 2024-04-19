import java.net.InetAddress;
import java.util.Objects;

public class Clientes {
    private InetAddress enderecoCliente;
    private int portaCliente;
    private String username; // novo atributo

    public Clientes(InetAddress enderecoCliente, int portaCliente, String username) {
        this.enderecoCliente = enderecoCliente;
        this.portaCliente = portaCliente;
        this.username = username; 
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

    @Override
    public int hashCode() {
        return Objects.hash(enderecoCliente, portaCliente);
    }

    @Override
    public String toString() {
        return enderecoCliente + ":" + portaCliente;
    }
}
