package costco.sim.Simulaciones;

import costco.sim.logica.Caja;
import costco.sim.logica.Cliente;
import costco.sim.logica.Estadistica;

import java.util.ArrayList;
import java.util.Random;

public abstract class Simulacion {

    private static final double TIEMPO_LLEGADA_MIN = 0.5;
    private static final double TIEMPO_LLEGADA_MAX = 1.0;
    protected static final int TIEMPO_SIMULACION = 600;  // 10 horas en minutos
    protected static final int NUM_CAJAS = 12;
    protected static final int UMBRAL_ABRIR_CAJA = 4;    // Clientes para abrir caja
    protected static final int UMBRAL_CERRAR_CAJA = 3;   // Clientes para considerar cerrar

    protected Random random;
    protected int tiempoActual;
    protected double tiempoProximaLlegada;
    protected int contadorClientes;

    protected Estadistica estadisticas;
    protected ArrayList<Caja> cajas;

    protected boolean enEjecucion;
    protected boolean terminada;

    public Simulacion() {
        this.random = new Random();
        this.tiempoActual = 0;
        this.contadorClientes = 1;
        this.enEjecucion = false;
        this.terminada = false;

        this.estadisticas = new Estadistica();

        this.cajas = new ArrayList<>();
        for (int i = 1; i <= NUM_CAJAS; i++) {
            cajas.add(new Caja(i));
        }


        this.tiempoProximaLlegada = generarTiempoLlegada();
    }

    public void iniciar() {
        this.enEjecucion = true;
        this.terminada = false;
    }

    public void pausar() {
        this.enEjecucion = false;
    }

    public void reiniciar() {
        this.tiempoActual = 0;
        this.contadorClientes = 1;
        this.enEjecucion = false;
        this.terminada = false;

        this.estadisticas = new Estadistica();

        this.cajas.clear();
        for (int i = 1; i <= NUM_CAJAS; i++) {
            cajas.add(new Caja(i));
        }

        this.tiempoProximaLlegada = generarTiempoLlegada();
    }


    public void avanzarTiempo() {
         if(!enEjecucion || terminada) {
            return;
        }

        tiempoActual++;

        // ✅ SOLUCIÓN: Solo generar clientes ANTES de los 600 minutos
        if (tiempoActual < TIEMPO_SIMULACION && tiempoActual >= tiempoProximaLlegada) {
            Cliente nuevoCliente = generarNuevoCliente();
            procesarLlegadaCliente(nuevoCliente);
            tiempoProximaLlegada = tiempoActual + generarTiempoLlegada();
        }

        procesarPagosEnCajas();
        gestionarCajas();

        // Verificar si terminó
        if (tiempoActual >= TIEMPO_SIMULACION && todasCajasVacias()) {
            terminada = true;
            enEjecucion = false;
        }
    }

    public int getTiempoActual() {
        return tiempoActual;
    }

    public int getClientesAtendidosActual() {
        return estadisticas.getTotalClientesAtendidos();
    }


    public int getClientesEsperandoTotal() {
        int total = 0;
        for (int i = 0; i < cajas.size(); i++) {
            Caja caja = cajas.get(i);
            if (caja.estaAbierta()) {
                total += caja.cantidadClientes();
            }
        }
        return total;
    }

    public int getCajasAbiertas() {
        int abiertas = 0;
        for (int i = 0; i < cajas.size(); i++) {
            if (cajas.get(i).estaAbierta()) {
                abiertas++;
            }
        }
        return abiertas;
    }

    public Caja getCaja(int numeroCaja) {
        if (numeroCaja >= 1 && numeroCaja <= cajas.size()) {
            return cajas.get(numeroCaja - 1);
        }
        return null;
    }


    public ArrayList<Caja> getCajas() {
        return cajas;
    }

    public Caja[] getCajasArray() {
        Caja[] array = new Caja[cajas.size()];
        for (int i = 0; i < cajas.size(); i++) {
            array[i] = cajas.get(i);
        }
        return array;
    }


    public Estadistica getEstadisticas() {
        return estadisticas;
    }

    public boolean estaEnEjecucion() {
        return enEjecucion;
    }

    public boolean haTerminado() {
        return terminada;
    }

    protected Cliente generarNuevoCliente() {
        Cliente cliente = new Cliente(contadorClientes);
        cliente.iniciarEspera(tiempoActual);
        contadorClientes++;
        return cliente;
    }

    protected double generarTiempoLlegada() {
        return TIEMPO_LLEGADA_MIN +
                (random.nextDouble() * (TIEMPO_LLEGADA_MAX - TIEMPO_LLEGADA_MIN));
    }

    protected void procesarPagosEnCajas() {
        for (int i = 0; i < cajas.size(); i++) {
            Caja caja = cajas.get(i);
            if (caja.estaAbierta()) {
                // ✅ SOLUCIÓN: procesarPago() ahora retorna el cliente terminado
                Cliente clienteTerminado = caja.procesarPago(tiempoActual, random);

                if (clienteTerminado != null) {
                    estadisticas.registrarCliente(clienteTerminado);
                }
            }
        }
    }

    protected boolean todasCajasVacias() {
        for (int i = 0; i < cajas.size(); i++) {
            if (!cajas.get(i).estaVacia()) {
                return false;
            }
        }
        return true;
    }

    protected boolean abrirSiguienteCaja() {
        for (int i = 0; i < cajas.size(); i++) {
            Caja caja = cajas.get(i);
            if (!caja.estaAbierta()) {
                caja.abrir(tiempoActual);
                return true;
            }
        }
        return false;  // No hay cajas disponibles para abrir
    }

    protected boolean cerrarCajaVacia() {
        // Buscar cajas vacías (de atrás hacia adelante para cerrar las últimas primero)
        for (int i = cajas.size() - 1; i >= 0; i--) {
            Caja caja = cajas.get(i);
            if (caja.estaAbierta() && caja.estaVacia()) {
                // Solo cerrar si hay más de 2 cajas abiertas
                if (getCajasAbiertas() > 2) {
                    caja.cerrar(tiempoActual);
                    return true;
                }
            }
        }
        return false;
    }

    protected int encontrarCajaMenosOcupada() {
        int indiceMejorCaja = -1;
        int menorCantidad = Integer.MAX_VALUE;

        for (int i = 0; i < cajas.size(); i++) {
            Caja caja = cajas.get(i);
            if (caja.estaAbierta() && !caja.colaLlena()) {
                int cantidad = caja.cantidadClientes();
                if (cantidad < menorCantidad) {
                    menorCantidad = cantidad;
                    indiceMejorCaja = i;
                }
            }
        }

        return indiceMejorCaja;
    }

    protected abstract void procesarLlegadaCliente(Cliente cliente);

    protected abstract void gestionarCajas();

    public String formatearTiempo(int minutos) {
        int horas = minutos / 60;
        int mins = minutos % 60;
        return String.format("%d:%02d", horas, mins);
    }

    @Override
    public String toString() {
        return String.format("%s[tiempo=%s, clientes=%d, cajas=%d/%d, estado=%s]",
                getClass().getSimpleName(),
                formatearTiempo(tiempoActual),
                getClientesAtendidosActual(),
                getCajasAbiertas(),
                NUM_CAJAS,
                terminada ? "TERMINADA" : (enEjecucion ? "EJECUTANDO" : "PAUSADA"));
    }
}