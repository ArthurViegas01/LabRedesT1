import java.net.InetAddress;
import java.util.Objects;

public class Endereco {
    private InetAddress enderecoCliente;
    private int portaCliente;

    public Endereco(InetAddress enderecoCliente, int portaCliente) {
        this.enderecoCliente = enderecoCliente;
        this.portaCliente = portaCliente;
    }

    public InetAddress getEnderecoCliente() {
        return enderecoCliente;
    }

    public int getPortaCliente() {
        return portaCliente;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Endereco endereco = (Endereco) o;
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
