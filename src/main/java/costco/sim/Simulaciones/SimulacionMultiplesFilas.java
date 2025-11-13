package costco.sim.Simulaciones;

import costco.sim.logica.Caja;
import costco.sim.logica.Cliente;

/**
 * Simulación con estrategia de MÚLTIPLES FILAS
 * Cada caja tiene su propia fila
 * Clientes van a la caja más corta
 */
public class SimulacionMultiplesFilas extends Simulacion {

    private static final int CAJAS_INICIALES = 2;

    public SimulacionMultiplesFilas() {
        super();

        // Abrir cajas iniciales
        for (int i = 0; i < CAJAS_INICIALES && i < cajas.size(); i++) {
            cajas.get(i).abrir(0);
        }
    }

    @Override
    public void reiniciar() {
        super.reiniciar();

        // Abrir cajas iniciales
        for (int i = 0; i < CAJAS_INICIALES && i < cajas.size(); i++) {
            cajas.get(i).abrir(0);
        }
    }

    @Override
    protected void procesarLlegadaCliente(Cliente cliente) {
        int indiceCajaMenosOcupada = encontrarCajaMenosOcupada();

        if (indiceCajaMenosOcupada != -1) {
            // Asignar a la caja menos ocupada
            Caja caja = cajas.get(indiceCajaMenosOcupada);
            caja.agregarCliente(cliente);
        } else {
            // No hay cajas disponibles, abrir una nueva
            if (abrirSiguienteCaja()) {
                // Asignar a la primera caja abierta disponible
                for (Caja caja : cajas) {
                    if (caja.estaAbierta()) {
                        caja.agregarCliente(cliente);
                        break;
                    }
                }
            }
        }
    }

    @Override
    protected void gestionarCajas() {
        evaluarAperturaCaja();
        evaluarCierreCaja();
    }
    public void evaluarAperturaCaja() {
        // Verificar si alguna caja tiene muchos clientes
        boolean necesitaAbrirCaja = false;

        for (Caja caja : cajas) {
            if (caja.estaAbierta() && caja.cantidadClientes() > UMBRAL_ABRIR_CAJA) {
                necesitaAbrirCaja = true;
                break;
            }
        }

        if (necesitaAbrirCaja) {
            abrirSiguienteCaja();
        }
    }

    @Override
    public String getEstadoVisual() {
        StringBuilder sb = new StringBuilder();

        sb.append("========== MÚLTIPLES FILAS ==========\n");
        sb.append(String.format("Tiempo: %d min (%d horas %d min)\n",
                tiempoActual, tiempoActual / 60, tiempoActual % 60));
        sb.append(String.format("Cajas abiertas: %d/%d\n", getCajasAbiertas(), NUM_CAJAS));
        sb.append(String.format("Total esperando: %d\n\n", getClientesEsperandoTotal()));

        // Mostrar todas las cajas abiertas
        for (Caja caja : cajas) {
            if (caja.estaAbierta()) {
                sb.append(String.format("Caja %d: %s (%d clientes)\n",
                        caja.getNumeroCaja(),
                        caja.getEstadoVisual(),
                        caja.cantidadClientes()));
            }
        }

        return sb.toString();
    }

    @Override
    public boolean esFilaUnica() {
        return false;
    }

    @Override
    public String toString() {
        return String.format("SimulacionMultiplesFilas[tiempo=%d min, cajas=%d/%d, esperando=%d, atendidos=%d]",
                tiempoActual,
                getCajasAbiertas(),
                NUM_CAJAS,
                getClientesEsperandoTotal(),
                getClientesAtendidosActual());
    }
}