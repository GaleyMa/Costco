package costco.sim.Simulaciones;

import costco.sim.logica.Caja;
import costco.sim.logica.Cliente;

public class SimulacionMultiplesFilas extends Simulacion {

    private static final int CAJAS_INICIALES = 2;  // Empezar con 2 cajas

    public SimulacionMultiplesFilas() {
        super();
        for (int i = 0; i < CAJAS_INICIALES && i < cajas.size(); i++) {
            cajas.get(i).abrir(0);
        }
    }

    @Override
    public void reiniciar() {
        super.reiniciar();
        for (int i = 0; i < CAJAS_INICIALES && i < cajas.size(); i++) {
            cajas.get(i).abrir(0);
        }
    }
    @Override
    protected void procesarLlegadaCliente(Cliente cliente) {
        int indiceCajaMenosOcupada = encontrarCajaMenosOcupada();

        if (indiceCajaMenosOcupada != -1) {
            Caja caja = cajas.get(indiceCajaMenosOcupada);
            caja.agregarCliente(cliente);
        } else {
            if (abrirSiguienteCaja()) {
                // Asignar a la primera caja abierta
                for (int i = 0; i < cajas.size(); i++) {
                    if (cajas.get(i).estaAbierta()) {
                        cajas.get(i).agregarCliente(cliente);
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

    private void evaluarAperturaCaja() {
        // Verificar si alguna caja tiene muchos clientes
        boolean necesitaAbrirCaja = false;
        int maxClientesEnCaja = 0;

        for (int i = 0; i < cajas.size(); i++) {
            Caja caja = cajas.get(i);
            if (caja.estaAbierta()) {
                int clientes = caja.cantidadClientes();
                if (clientes > maxClientesEnCaja) {
                    maxClientesEnCaja = clientes;
                }
                if (clientes >= UMBRAL_ABRIR_CAJA) {
                    necesitaAbrirCaja = true;
                }
            }
        }
        if (necesitaAbrirCaja) {
            boolean seAbrio = abrirSiguienteCaja();
        }
    }

    private void evaluarCierreCaja() {
        int cajasAbiertas = getCajasAbiertas();

        if (cajasAbiertas <= 2) {
            return;
        }

        int totalClientes = getClientesEsperandoTotal();
        double promedio = (double) totalClientes / cajasAbiertas;

        if (promedio <= UMBRAL_CERRAR_CAJA) {
            cerrarCajaVacia();
        }
    }

    private String getEstadisticasDistribucion() {
        int[] clientesPorCaja = new int[NUM_CAJAS];
        int totalCajasAbiertas = 0;

        for (int i = 0; i < cajas.size(); i++) {
            Caja caja = cajas.get(i);
            if (caja.estaAbierta()) {
                clientesPorCaja[i] = caja.cantidadClientes();
                totalCajasAbiertas++;
            }
        }

        // Calcular desviación (qué tan desigual está la distribución)
        if (totalCajasAbiertas == 0) {
            return "No hay cajas abiertas";
        }

        double promedio = (double) getClientesEsperandoTotal() / totalCajasAbiertas;

        return String.format("Promedio: %.1f clientes/caja", promedio);
    }

    public String getEstadoVisual() {
        StringBuilder sb = new StringBuilder();

        sb.append("=== MÚLTIPLES FILAS ===\n");
        sb.append(String.format("Tiempo: %s\n", formatearTiempo(tiempoActual)));
        sb.append(String.format("Cajas abiertas: %d/%d\n", getCajasAbiertas(), NUM_CAJAS));
        sb.append(String.format("Total esperando: %d\n", getClientesEsperandoTotal()));
        sb.append(String.format("%s\n\n", getEstadisticasDistribucion()));

        // Mostrar todas las cajas (abiertas y cerradas)
        for (int i = 0; i < cajas.size(); i++) {
            Caja caja = cajas.get(i);
            if (caja.estaAbierta()) {
                sb.append(String.format("Caja %d: %s (%d clientes)\n",
                        caja.getNumeroCaja(),
                        caja.getEstadoVisual(),
                        caja.cantidadClientes()));
            }
        }

        return sb.toString();
    }

    public int[] getDistribucionClientes() {
        int[] distribucion = new int[NUM_CAJAS];

        for (int i = 0; i < cajas.size(); i++) {
            Caja caja = cajas.get(i);
            distribucion[i] = caja.estaAbierta() ? caja.cantidadClientes() : 0;
        }

        return distribucion;
    }


    public double calcularDesviacionEstandar() {
        int cajasAbiertas = getCajasAbiertas();
        if (cajasAbiertas == 0) {
            return 0;
        }

        double promedio = (double) getClientesEsperandoTotal() / cajasAbiertas;

        double sumaDiferencias = 0;
        for (int i = 0; i < cajas.size(); i++) {
            Caja caja = cajas.get(i);
            if (caja.estaAbierta()) {
                double diferencia = caja.cantidadClientes() - promedio;
                sumaDiferencias += diferencia * diferencia;
            }
        }

        return Math.sqrt(sumaDiferencias / cajasAbiertas);
    }

    @Override
    public String toString() {
        return String.format("SimulacionMultiplesFilas[tiempo=%s, cajas=%d/%d, esperando=%d, atendidos=%d, desv=%.2f]",
                formatearTiempo(tiempoActual),
                getCajasAbiertas(),
                NUM_CAJAS,
                getClientesEsperandoTotal(),
                getClientesAtendidosActual(),
                calcularDesviacionEstandar());
    }
}