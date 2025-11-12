package costco.sim.logica;
import java.util.Random;

/**
 * Representa una caja registradora en Costco.
 * Gestiona su propia cola de clientes y procesa pagos.
 */
public class Caja {

    private static final int CAPACIDAD_COLA = 50;
    private static final double TIEMPO_PAGO_MIN = 3.0;
    private static final double TIEMPO_PAGO_MAX = 5.0;

    private int numeroCaja;
    private boolean abierta;
    private Cola<Cliente> colaClientes;
    private Cliente clienteActualPagando;
    private int clientesAtendidos;
    private int tiempoAbiertaAcumulado;
    private int tiempoApertura;
    private int tiempoCierre;


    public Caja(int numeroCaja) {
        this.numeroCaja = numeroCaja;
        this.abierta = false;
        this.colaClientes = new Cola<>(CAPACIDAD_COLA);
        this.clienteActualPagando = null;
        this.clientesAtendidos = 0;
        this.tiempoAbiertaAcumulado = 0;
        this.tiempoApertura = 0;
        this.tiempoCierre = 0;
    }

    public void abrir() {
        this.abierta = true;
    }

    /**
     * Abre la caja registrando el tiempo de apertura
     */
    public void abrir(int tiempoActual) {
        this.abierta = true;
        this.tiempoApertura = tiempoActual;
    }


    public void cerrar() {
        this.abierta = false;
    }

    public boolean cerrar(int tiempoActual) {
        if (!estaVacia()) {
            return false; // No se puede cerrar con clientes
        }
        this.abierta = false;
        this.tiempoCierre = tiempoActual;

        // Acumular el tiempo que estuvo abierta
        if (tiempoApertura > 0) {
            this.tiempoAbiertaAcumulado += (tiempoActual - tiempoApertura);
        }

        return true;
    }


    public boolean agregarCliente(Cliente cliente) {
        boolean agregado = colaClientes.insertar(cliente);
        if (agregado) {
            cliente.asignarACaja(this.numeroCaja);
        }
        return agregado;
    }

    public Cliente procesarPago(int tiempoActual, Random random) {
        Cliente clienteTerminado = null;

        // Si hay un cliente pagando, verificar si ya terminó
        if (clienteActualPagando != null) {
            if (clienteActualPagando.haTerminadoDePagar(tiempoActual)) {
                clienteActualPagando.terminarPago(tiempoActual);
                clientesAtendidos++;

                clienteTerminado = clienteActualPagando;  // ✅ Guardar antes de limpiar
                clienteActualPagando = null;  // Liberar la caja
            }
        }

        // Si la caja está libre y hay clientes esperando, atender al siguiente
        if (clienteActualPagando == null && !colaClientes.estaVacia()) {
            clienteActualPagando = colaClientes.eliminar();
            double duracionPago = generarTiempoPago(random);
            clienteActualPagando.iniciarPago(tiempoActual, duracionPago);
        }

        return clienteTerminado;  // ✅ Retornar cliente terminado
    }

    private double generarTiempoPago(Random random) {
        return TIEMPO_PAGO_MIN + (random.nextDouble() * (TIEMPO_PAGO_MAX - TIEMPO_PAGO_MIN));
    }

    public boolean estaVacia() {
        return colaClientes.estaVacia() && clienteActualPagando == null;
    }

    public int cantidadClientes() {
        int total = colaClientes.tamanio();
        if (clienteActualPagando != null) {
            total++;
        }
        return total;
    }


    public int cantidadClientesEsperando() {
        return colaClientes.tamanio();
    }

    public boolean tieneClientePagando() {
        return clienteActualPagando != null;
    }


    public boolean colaLlena() {
        return colaClientes.estaLlena();
    }


    public double getTiempoRestantePago(int tiempoActual) {
        if (clienteActualPagando == null) {
            return 0;
        }
        double tiempoRestante = clienteActualPagando.getTiempoFinPago() - tiempoActual;
        return Math.max(0, tiempoRestante);
    }

    public int calcularTiempoTotalAbierta(int tiempoActual) {
        if (abierta && tiempoApertura > 0) {
            return tiempoAbiertaAcumulado + (tiempoActual - tiempoApertura);
        }
        return tiempoAbiertaAcumulado;
    }

    public Cliente getSiguienteCliente() {
        return colaClientes.peek();
    }

    public Cliente[] getClientesEsperando() {
        return colaClientes.getElementosCola();
    }

    /**
     * Obtiene un cliente específico de la cola sin eliminarlo
     */
    public Cliente getClienteEnPosicion(int posicion) {
        return colaClientes.obtenerEnPosicion(posicion);
    }

    public String getEstadoVisual() {
        if (!abierta) {
            return "CERRADA";
        }

        StringBuilder sb = new StringBuilder();

        // Mostrar cliente pagando
        if (clienteActualPagando != null) {
            sb.append("[pagando]");
        } else {
            sb.append("[ ]");
        }

        // Mostrar clientes esperando
        if (!colaClientes.estaVacia()) {
            sb.append(" → ");
            int cantidad = colaClientes.tamanio();

            // Mostrar hasta 5 clientes con íconos
            for (int i = 0; i < Math.min(cantidad, 5); i++) {
                sb.append("[cliente]");
            }

            // Si hay más de 5, mostrar contador
            if (cantidad > 5) {
                sb.append("...(+").append(cantidad - 5).append(")");
            }
        }

        return sb.toString();
    }


    public int getNumeroCaja() {
        return numeroCaja;
    }

    public boolean estaAbierta() {
        return abierta;
    }

    public int getClientesAtendidos() {
        return clientesAtendidos;
    }

    public int getTiempoAbierta() {
        return tiempoAbiertaAcumulado;
    }

    public Cliente getClienteActualPagando() {
        return clienteActualPagando;
    }

    public Cola<Cliente> getColaClientes() {
        return colaClientes;
    }

    @Override
    public String toString() {
        return String.format("Caja #%d [%s, Clientes: %d (esperando: %d), Atendidos: %d, Tiempo abierta: %d min]",
                numeroCaja,
                abierta ? "ABIERTA" : "CERRADA",
                cantidadClientes(),
                cantidadClientesEsperando(),
                clientesAtendidos,
                tiempoAbiertaAcumulado);
    }


}