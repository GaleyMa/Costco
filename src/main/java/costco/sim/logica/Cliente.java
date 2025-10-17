package costco.sim.logica;
/**
 * Representa un cliente en la simulación.
 */
public class Cliente {

    private int id;
    private int tiempoInicioEspera;
    private int tiempoInicioPago;
    private int tiempoFinPago;
    private Estado estado;
    private int numeroCajaAsignada;

    public Cliente(int id) {
        this.id = id;
        this.estado = Estado.RECIEN_LLEGADO;
        this.numeroCajaAsignada = -1;
        this.tiempoInicioEspera = 0;
        this.tiempoInicioPago = 0;
        this.tiempoFinPago = 0;
    }

    /**
     * Marca cuando el cliente empieza a esperar
     */
    public void iniciarEspera(int tiempoActual) {
        this.tiempoInicioEspera = tiempoActual;
    }

    /**
     * Marca que el cliente entró a la fila general (solo para fila única)
     */
    public void entrarFilaGeneral() {
        this.estado = Estado.EN_FILA_GENERAL;
    }

    /**
     * Asigna el cliente a una caja específica
     */
    public void asignarACaja(int numeroCaja) {
        this.numeroCajaAsignada = numeroCaja;
        this.estado = Estado.EN_CAJA;
    }

    /**
     * Marca que el cliente comenzó a pagar
     */
    public void iniciarPago(int tiempoActual, double duracionPago) {
        this.tiempoInicioPago = tiempoActual;
        this.tiempoFinPago = (int) Math.ceil(tiempoActual + duracionPago);
        this.estado = Estado.PAGANDO;
    }

    /**
     * Marca que el cliente terminó de pagar
     */
    public void terminarPago(int tiempoActual) {
        this.estado = Estado.FINALIZADO;
    }

    /**
     * Verifica si el cliente ya terminó de pagar
     */
    public boolean haTerminadoDePagar(int tiempoActual) {
        if (estado != Estado.PAGANDO) {
            return false;
        }
        return tiempoActual >= tiempoFinPago;
    }

    /**
     * Calcula el tiempo total de espera del cliente
     * (Desde que empieza a esperar hasta que empieza a pagar)
     */
    public double getTiempoEspera() {
        if (tiempoInicioPago == 0) {
            return 0; // Aún no ha empezado a pagar
        }
        return tiempoInicioPago - tiempoInicioEspera;
    }

    /**
     * Calcula el tiempo que tardó pagando
     */
    public double getTiempoPago() {
        if (tiempoFinPago == 0 || tiempoInicioPago == 0) {
            return 0; // Aún no ha terminado de pagar
        }
        return tiempoFinPago - tiempoInicioPago;
    }

    /**
     * Calcula el tiempo total en el sistema
     * (Desde que empieza a esperar hasta que termina de pagar)
     */
    public double getTiempoTotal() {
        if (tiempoFinPago == 0) {
            return 0; // Aún no ha terminado
        }
        return tiempoFinPago - tiempoInicioEspera;
    }

    /**
     * Calcula el tiempo que lleva esperando actualmente
     * (Solo si está en estado de espera)
     */
    public double getTiempoEsperaActual(int tiempoActual) {
        if (estado == Estado.EN_CAJA ||
                estado == Estado.EN_FILA_GENERAL) {
            return tiempoActual - tiempoInicioEspera;
        }
        return 0;
    }

    public int getId() {
        return id;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    public int getNumeroCajaAsignada() {
        return numeroCajaAsignada;
    }

    public int getTiempoInicioEspera() {
        return tiempoInicioEspera;
    }

    public void setTiempoInicioEspera(int tiempoInicioEspera) {
        this.tiempoInicioEspera = tiempoInicioEspera;
    }

    public int getTiempoInicioPago() {
        return tiempoInicioPago;
    }

    public int getTiempoFinPago() {
        return tiempoFinPago;
    }


    public String getIcono() {
        switch (estado) {
            case RECIEN_LLEGADO:
            case EN_FILA_GENERAL:
            case EN_CAJA:
                return "[cliente]";
            case PAGANDO:
                return "[pagando]";
            case FINALIZADO:
                return "✓";
            default:
                return "?";
        }
    }

    @Override
    public String toString() {
        return String.format("Cliente #%d [Estado: %s, Caja: %d, Espera: %.1f min, Pago: %.1f min]",
                id, estado, numeroCajaAsignada, getTiempoEspera(), getTiempoPago());
    }
}