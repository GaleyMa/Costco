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

    /**
     * Constructor de la caja
     * @param numeroCaja Número identificador de la caja (1-12)
     */
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

    /**
     * Abre la caja para empezar a atender clientes
     */
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

    /**
     * Cierra la caja
     */
    public void cerrar() {
        this.abierta = false;
    }

    /**
     * Cierra la caja registrando el tiempo de cierre
     * Solo se puede cerrar si está vacía
     */
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

    /**
     * Agrega un cliente a la cola de esta caja
     */
    public boolean agregarCliente(Cliente cliente) {
        boolean agregado = colaClientes.insertar(cliente);
        if (agregado) {
            cliente.asignarACaja(this.numeroCaja);
        }
        return agregado;
    }

    /**
     * Procesa el pago del cliente actual o inicia el pago del siguiente
     * @param tiempoActual Tiempo actual de la simulación
     * @param random Generador de números aleatorios para el tiempo de pago
     */
    public void procesarPago(int tiempoActual, Random random) {
        // Si hay un cliente pagando, verificar si ya terminó
        if (clienteActualPagando != null) {
            if (clienteActualPagando.haTerminadoDePagar(tiempoActual)) {
                clienteActualPagando.terminarPago(tiempoActual);
                clientesAtendidos++;
                clienteActualPagando = null; // Liberar la caja
            }
        }

        // Si la caja está libre y hay clientes esperando, atender al siguiente
        if (clienteActualPagando == null && !colaClientes.estaVacia()) {
            clienteActualPagando = colaClientes.eliminar();
            double duracionPago = generarTiempoPago(random);
            clienteActualPagando.iniciarPago(tiempoActual, duracionPago);
        }
    }

    /**
     * Obtiene el cliente que acaba de terminar de pagar
     */
    public Cliente obtenerClienteTerminado() {
        if (clienteActualPagando != null &&
                clienteActualPagando.getEstado() == Estado.FINALIZADO) {
            Cliente terminado = clienteActualPagando;
            clienteActualPagando = null;
            return terminado;
        }
        return null;
    }



    /**
     * Genera un tiempo de pago aleatorio dentro del rango permitido
     * @param random
     * @return Tiempo de pago en minutos (3.0 - 5.0)
     */
    private double generarTiempoPago(Random random) {
        return TIEMPO_PAGO_MIN + (random.nextDouble() * (TIEMPO_PAGO_MAX - TIEMPO_PAGO_MIN));
    }

    /**
     * Verifica si la caja está vacía (sin clientes esperando ni pagando)
     */
    public boolean estaVacia() {
        return colaClientes.estaVacia() && clienteActualPagando == null;
    }

    /**
     * Obtiene la cantidad total de clientes (esperando + pagando)
     */
    public int cantidadClientes() {
        int total = colaClientes.tamanio();
        if (clienteActualPagando != null) {
            total++;
        }
        return total;
    }

    /**
     * Obtiene solo los clientes en espera (sin contar el que está pagando)
     */
    public int cantidadClientesEsperando() {
        return colaClientes.tamanio();
    }

    /**
     * Verifica si hay un cliente pagando actualmente
     */
    public boolean tieneClientePagando() {
        return clienteActualPagando != null;
    }

    /**
     * Verifica si la cola está llena
     */
    public boolean colaLlena() {
        return colaClientes.estaLlena();
    }

    /**
     * Obtiene el tiempo restante de pago del cliente actual
     */
    public double getTiempoRestantePago(int tiempoActual) {
        if (clienteActualPagando == null) {
            return 0;
        }
        double tiempoRestante = clienteActualPagando.getTiempoFinPago() - tiempoActual;
        return Math.max(0, tiempoRestante);
    }

    /**
     * Calcula el tiempo total que la caja ha estado abierta
     */
    public int calcularTiempoTotalAbierta(int tiempoActual) {
        if (abierta && tiempoApertura > 0) {
            return tiempoAbiertaAcumulado + (tiempoActual - tiempoApertura);
        }
        return tiempoAbiertaAcumulado;
    }


    /**
     * Obtiene el siguiente cliente que será atendido (sin sacarlo de la cola)
     */
    public Cliente getSiguienteCliente() {
        return colaClientes.peek();
    }

    /**
     * Obtiene todos los clientes en espera como arreglo
     */
    public Cliente[] getClientesEsperando() {
        return colaClientes.obtenerElementos();
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