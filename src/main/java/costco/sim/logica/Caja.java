package costco.sim.logica;

import java.util.Random;

/**
 * Representa una caja registradora en Costco
 * Gestiona su propia cola de clientes y procesa pagos
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


    public Caja(int numeroCaja) {
        this.numeroCaja = numeroCaja;
        this.abierta = false;
        this.colaClientes = new Cola<>(CAPACIDAD_COLA);
        this.clienteActualPagando = null;
        this.clientesAtendidos = 0;
        this.tiempoAbiertaAcumulado = 0;
        this.tiempoApertura = 0;
    }


    public void abrir(int tiempoActual) {
        this.abierta = true;
        this.tiempoApertura = tiempoActual;
    }


    public boolean cerrar(int tiempoActual) {
        if (!estaVacia()) {
            return false;
        }

        this.abierta = false;

        // Acumular tiempo que estuvo abierta
        if (tiempoApertura > 0) {
            this.tiempoAbiertaAcumulado += (tiempoActual - tiempoApertura);
        }

        return true;
    }

    public void agregarCliente(Cliente cliente) {
        if (colaClientes.insertar(cliente)) {
            cliente.asignarACaja(this.numeroCaja);
        }
    }

    public Cliente procesarPago(int tiempoActual, Random random) {

        if (clienteActualPagando != null) {
            if (clienteActualPagando.haTerminadoDePagar(tiempoActual)) {
                clienteActualPagando.terminarPago(tiempoActual);
                clientesAtendidos++;

                Cliente clienteTerminado = clienteActualPagando;
                clienteActualPagando = null;

                return clienteTerminado;
            }
            return null; // Aún está pagando
        }

        if (!colaClientes.estaVacia()) {
            clienteActualPagando = colaClientes.eliminar();
            double tiempoPago = generarTiempoPago(random);
            clienteActualPagando.iniciarPago(tiempoActual, tiempoPago);
        }

        return null;
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

    public int calcularTiempoTotalAbierta(int tiempoActual) {
        if (abierta && tiempoApertura > 0) {
            return tiempoAbiertaAcumulado + (tiempoActual - tiempoApertura);
        }
        return tiempoAbiertaAcumulado;
    }

    public String getEstadoVisual() {
        if (!abierta) {
            return "CERRADA";
        }

        StringBuilder sb = new StringBuilder();


        sb.append(clienteActualPagando != null ? "[$]" : "[ ]");


        if (!colaClientes.estaVacia()) {
            sb.append(" → ");
            int cantidad = colaClientes.tamanio();

            for (int i = 0; i < Math.min(cantidad, 5); i++) {
                sb.append("👤");
            }

            if (cantidad > 5) {
                sb.append("...(+").append(cantidad - 5).append(")");
            }
        }

        return sb.toString();
    }

    // ========== GETTERS ==========

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
        return String.format("Caja #%d [%s, Clientes: %d, Atendidos: %d]",
                numeroCaja,
                abierta ? "ABIERTA" : "CERRADA",
                cantidadClientes(),
                clientesAtendidos);
    }
}