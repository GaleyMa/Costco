package costco.sim.Simulaciones;

import costco.sim.logica.Caja;
import costco.sim.logica.Cliente;
import costco.sim.logica.Cola;

/**
 * Simulación de modo fila unica
 */
public class SimulacionFilaUnica extends Simulacion {

    private static final int MAX_CLIENTES_POR_CAJA = 3;
    private static final int CAPACIDAD_FILA_GENERAL = 100;
    private static final int CAJAS_INICIALES = 2;
    private Cola<Cliente> filaGeneral;


    public SimulacionFilaUnica() {
        super();
        this.filaGeneral = new Cola<>(CAPACIDAD_FILA_GENERAL);


        for (int i = 0; i < CAJAS_INICIALES && i < cajas.size(); i++) {
            cajas.get(i).abrir(0);
        }
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
        if (!filaGeneral.estaLlena()) {
            cliente.entrarFilaGeneral();
            filaGeneral.insertar(cliente);
        }
    }

    @Override
    protected void gestionarCajas() {
        evaluarAperturaCaja();

        asignarClientesACajas();

        evaluarCierreCaja();
    }
    private void evaluarAperturaCaja() {
        int totalClientes = filaGeneral.tamanio() + getClientesEsperandoTotal();

        if (totalClientes >= 8 && getCajasAbiertas() < 4) {
            abrirSiguienteCaja();
        } else if (totalClientes >= 16 && getCajasAbiertas() < 6) {
            abrirSiguienteCaja();
        } else if (totalClientes >= 24 && getCajasAbiertas() < 8) {
            abrirSiguienteCaja();
        } else if (totalClientes >= 32 && getCajasAbiertas() < 10) {
            abrirSiguienteCaja();
        } else if (totalClientes >= 40 && getCajasAbiertas() < 12) {
            abrirSiguienteCaja();
        }

        // También abrir si alguna caja está llena y hay fila general
        if (filaGeneral.tamanio() > 0) {
            for (Caja caja : cajas) {
                if (caja.estaAbierta() && caja.cantidadClientes() >= MAX_CLIENTES_POR_CAJA) {
                    if (abrirSiguienteCaja()) {
                        //System.out.println("Caja abierta porque caja " + caja.getNumeroCaja() + " está llena");
                    }
                    break;
                }
            }
        }
    }


    private void asignarClientesACajas() {
        for (Caja caja : cajas) {
            // Mientras la caja tenga espacio Y haya clientes en fila general
            while (caja.estaAbierta() &&  caja.cantidadClientes() < MAX_CLIENTES_POR_CAJA && !caja.colaLlena() && !filaGeneral.estaVacia()) {
                Cliente cliente = filaGeneral.eliminar();
                caja.agregarCliente(cliente);
            }

        }
    }

    @Override
    protected boolean todasCajasVacias() {
        return super.todasCajasVacias() && filaGeneral.estaVacia();
    }

    public Cola<Cliente> getFilaGeneral() {
        return filaGeneral;
    }

    public Cliente[] getClientesFilaGeneralArray() {
        Cola<Cliente> copia = new Cola<>(filaGeneral);
        int cantidad = copia.tamanio();
        Cliente[] arreglo = new Cliente[cantidad];

        for (int i = 0; i < cantidad; i++) {
            arreglo[i] = copia.eliminar();
        }

        return arreglo;
    }


    @Override
    public String getEstadoVisual() {
        StringBuilder sb = new StringBuilder();

        sb.append("========== FILA ÚNICA ==========\n");
        sb.append(String.format("Tiempo: %d min (%d horas %d min)\n",
                tiempoActual, tiempoActual / 60, tiempoActual % 60));
        sb.append(String.format("Clientes en fila general: %d\n", filaGeneral.tamanio()));
        sb.append(String.format("Cajas abiertas: %d/%d\n\n", getCajasAbiertas(), NUM_CAJAS));

        // Mostrar cajas abiertas
        for (Caja caja : cajas) {
            if (caja.estaAbierta()) {
                sb.append(String.format("Caja %d: %s\n",
                        caja.getNumeroCaja(),
                        caja.getEstadoVisual()));
            }
        }

        return sb.toString();
    }

    @Override
    public boolean esFilaUnica() {
        return true;
    }

    @Override
    public String toString() {
        return String.format("SimulacionFilaUnica[tiempo=%d min, filaGeneral=%d, cajas=%d/%d, atendidos=%d]",
                tiempoActual,
                filaGeneral.tamanio(),
                getCajasAbiertas(),
                NUM_CAJAS,
                getClientesAtendidosActual());
    }
}