package costco.sim.Simulaciones;
import costco.sim.logica.Caja;
import costco.sim.logica.Cliente;
import costco.sim.logica.Cola;

public class SimulacionFilaUnica extends Simulacion {

    private static final int MAX_CLIENTES_POR_CAJA = 4;
    private static final int CAPACIDAD_FILA_GENERAL = 100;
    private static final int CAJAS_INICIALES = 2;  // Empezar con 2 cajas

    private Cola<Cliente> filaGeneral;

    public SimulacionFilaUnica() {
        super();
        this.filaGeneral = new Cola<>(CAPACIDAD_FILA_GENERAL);
        for (int i = 0; i < CAJAS_INICIALES && i < cajas.size(); i++) {
            cajas.get(i).abrir(0);
        }
    }
    public int getClientesEnFilaGeneral() {
        return filaGeneral.tamanio();
    }
    public Cliente[] getClientesFilaGeneralArray() {
        return filaGeneral.obtenerElementos();
    }
    public Cola<Cliente> getFilaGeneral() {
        return filaGeneral;
    }
    @Override
    public void reiniciar() {
        super.reiniciar();
        this.filaGeneral = new Cola<>(CAPACIDAD_FILA_GENERAL);
        for (int i = 0; i < CAJAS_INICIALES && i < cajas.size(); i++) {
            cajas.get(i).abrir(0);
        }
    }

    @Override
    protected void procesarLlegadaCliente(Cliente cliente) {
        // Agregar a la fila general
        if (!filaGeneral.estaLlena()) {
            cliente.entrarFilaGeneral();
            filaGeneral.insertar(cliente);
        }
    }

    @Override
    protected void gestionarCajas() {
        asignarClientesACajas();
        evaluarAperturaCaja();
        evaluarCierreCaja();
    }

    private void asignarClientesACajas() {
        while (!filaGeneral.estaVacia()) {
            int indiceCaja = encontrarCajaConEspacio();

            if (indiceCaja == -1) {
                break;
            }
            Cliente cliente = filaGeneral.eliminar();
            Caja caja = cajas.get(indiceCaja);
            caja.agregarCliente(cliente);
        }
    }

    private int encontrarCajaConEspacio() {
        for (int i = 0; i < cajas.size(); i++) {
            Caja caja = cajas.get(i);
            if (caja.estaAbierta() && caja.cantidadClientes() < MAX_CLIENTES_POR_CAJA) {
                return i;
            }
        }
        return -1;
    }

    private void evaluarAperturaCaja() {
        int totalClientes = filaGeneral.tamanio() + getClientesEsperandoTotal();

        if (totalClientes >= UMBRAL_ABRIR_CAJA) {
            boolean hayCajaLlena = false;
            for (int i = 0; i < cajas.size(); i++) {
                if (cajas.get(i).estaAbierta() &&
                        cajas.get(i).cantidadClientes() >= MAX_CLIENTES_POR_CAJA) {
                    hayCajaLlena = true;
                    break;
                }
            }
            if (hayCajaLlena || filaGeneral.tamanio() > 3) {
                abrirSiguienteCaja();
            }
        }
    }

    private void evaluarCierreCaja() {
        int totalClientes = filaGeneral.tamanio() + getClientesEsperandoTotal();

        if (totalClientes <= UMBRAL_CERRAR_CAJA && getCajasAbiertas() > 2) {
            cerrarCajaVacia();
        }
    }

    private int getTotalClientesEnSistema() {
        return filaGeneral.tamanio() + getClientesEsperandoTotal();
    }

    @Override
    protected boolean todasCajasVacias() {
        return super.todasCajasVacias() && filaGeneral.estaVacia();
    }

    public String getEstadoVisual() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== FILA ÚNICA ===\n");
        sb.append(String.format("Tiempo: %s\n", formatearTiempo(tiempoActual)));
        sb.append(String.format("Clientes en fila general: %d\n", filaGeneral.tamanio()));
        sb.append(String.format("Cajas abiertas: %d/%d\n\n", getCajasAbiertas(), NUM_CAJAS));

        // Mostrar cajas abiertas
        for (int i = 0; i < cajas.size(); i++) {
            Caja caja = cajas.get(i);
            if (caja.estaAbierta()) {
                sb.append(String.format("Caja %d: %s\n",
                        caja.getNumeroCaja(),
                        caja.getEstadoVisual()));
            }
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return String.format("SimulacionFilaUnica[tiempo=%s, filaGeneral=%d, cajas=%d/%d, atendidos=%d]",
                formatearTiempo(tiempoActual),
                filaGeneral.tamanio(),
                getCajasAbiertas(),
                NUM_CAJAS,
                getClientesAtendidosActual());
    }
}