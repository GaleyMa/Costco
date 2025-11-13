package costco.sim;




import costco.sim.Simulaciones.*;
import costco.sim.logica.*;
import costco.sim.grafica.CajaGrafica;
import costco.sim.grafica.ClienteGrafico;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.image.ImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

public class HelloController {

    @FXML
    private Label lblTiempo;

    @FXML
    private RadioButton rbFilaUnica;

    @FXML
    private RadioButton rbMultiple;

    @FXML
    private ToggleGroup grupoMetodo;

    @FXML
    private Button btnIniciar;

    @FXML
    private Button btnPausar;

    @FXML
    private Button btnDetener;

    @FXML
    private ComboBox<String> cbVelocidad;

    @FXML
    private ProgressIndicator progreso;

    // Área de juego
    @FXML
    private ImageView imagenFondo;

    @FXML
    private Pane panelAnimaciones;

    // Estadísticas inferiores
    @FXML
    private Label lblClientesAtendidos;

    @FXML
    private Label lblEsperaPromedio;

    @FXML
    private Label lblCajasAbiertas;

    // Lógica
    private Simulacion simulacion;
    private List<CajaGrafica> cajasGraficas;
    private List<ClienteGrafico> clientesGraficos = new ArrayList<>();
    private Timer timer;
    private boolean pausado = false;
    private final int[] VELOCIDADES = {1000, 500, 200, 100};
    private int velocidadActual = 1000;


    private static final double ENTRADA_CLIENTES_X = 1150;
    private static final double ENTRADA_CLIENTES_Y = 350;
    private static final double FILA_INICIO_X = 600;
    private static final double FILA_Y = 350;
    private static final int ESPACIO_ENTRE_CLIENTES = 45;

    private Map<Cliente, ClienteGrafico> mapaClientesGraficos = new HashMap<>();


    @FXML
    public void initialize() {

        cbVelocidad.getItems().addAll("1x", "2x", "5x", "10x");
        cbVelocidad.setValue("1x");

        lblTiempo.setText("Tiempo: 0/600 min");
        lblClientesAtendidos.setText("Clientes: 0");
        lblEsperaPromedio.setText("Espera Prom: 0.0 min");
        lblCajasAbiertas.setText("Cajas: 0/12");
        mapaClientesGraficos = new HashMap<>();
        progreso.setProgress(0.0);
    }

    @FXML
    private void iniciarSimulacion() {
        try {
            System.out.println("DEBUG: Iniciando simulación...");

            if (rbFilaUnica.isSelected()) {
                System.out.println("DEBUG: Creando SimulacionFilaUnica");
                simulacion = new SimulacionFilaUnica();
            } else {
                System.out.println("DEBUG: Creando SimulacionMultiplesFilas");
                simulacion = new SimulacionMultiplesFilas();
            }

            simulacion.iniciar();

            panelAnimaciones.getChildren().clear();
            clientesGraficos.clear();
            mapaClientesGraficos.clear();
            crearCajasGraficas();

            rbFilaUnica.setDisable(true);
            rbMultiple.setDisable(true);
            btnIniciar.setDisable(true);
            btnPausar.setDisable(false);
            btnDetener.setDisable(false);

            pausado = false;
            progreso.setProgress(0.0);

            timer = new Timer(true);
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (!pausado) {
                        avanzarSimulacion();
                    }
                }
            }, 0, velocidadActual);

        } catch (Exception e) {
            System.err.println("ERROR al iniciar simulación: " + e.getMessage());
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Error al iniciar simulación");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private void crearCajasGraficas() {
        try {
            cajasGraficas = new ArrayList<>();
            ArrayList<Caja> cajas = simulacion.getCajas();

            int inicioCajasX = 157;
            int inicioCajasY = 205;
            int espacioEntreCajasX = 300;
            int espacioEntreCajasY = 45;
            int desplazamientoX = -40;

            int indiceCaja = 0;

            for (int fila = 0; fila < 6; fila++) {
                for (int columna = 0; columna < 2; columna++) {
                    if (indiceCaja >= cajas.size()) break;

                    int posX = inicioCajasX + (columna * espacioEntreCajasX) + (fila * desplazamientoX);
                    int posY = inicioCajasY + (fila * espacioEntreCajasY);

                    CajaGrafica cajaGrafica = new CajaGrafica(cajas.get(indiceCaja), posX, posY);
                    cajasGraficas.add(cajaGrafica);
                    panelAnimaciones.getChildren().add(cajaGrafica);
                    cajaGrafica.actualizar();

                    indiceCaja++;
                }
            }

        } catch (Exception e) {
            System.err.println("ERROR en crearCajasGraficas(): " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    private void avanzarSimulacion() {
        if (simulacion.haTerminado()) {
            Platform.runLater(() -> {
                detenerSimulacion();
                mostrarResultadosFinales();
            });
            return;
        }

        simulacion.avanzarTiempo();

        Platform.runLater(() -> actualizarInterfaz());
    }

    @FXML
    private void pausarReanudar() {
        pausado = !pausado;
        btnPausar.setText(pausado ? "▶ REANUDAR" : "⏸ PAUSAR");
    }

    @FXML
    private void detenerSimulacion() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        rbFilaUnica.setDisable(false);
        rbMultiple.setDisable(false);
        btnIniciar.setDisable(false);
        btnPausar.setDisable(true);
        btnDetener.setDisable(true);
        pausado = false;
        progreso.setProgress(0.0);
    }

    @FXML
    private void cambiarVelocidad() {
        String vel = cbVelocidad.getValue();
        switch (vel) {
            case "1x": velocidadActual = VELOCIDADES[0]; break;
            case "2x": velocidadActual = VELOCIDADES[1]; break;
            case "5x": velocidadActual = VELOCIDADES[2]; break;
            case "10x": velocidadActual = VELOCIDADES[3]; break;
        }

        if (timer != null) {
            boolean estabaPausado = pausado;
            timer.cancel();
            timer = new Timer(true);
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (!pausado) {
                        avanzarSimulacion();
                    }
                }
            }, 0, velocidadActual);
            pausado = estabaPausado;
        }
    }


    private void actualizarInterfaz() {

        int tiempo = simulacion.getTiempoActual();
        lblTiempo.setText(String.format("Tiempo: %d/600 min (%.1f%%)",
                tiempo, (tiempo * 100.0 / 600)));
        progreso.setProgress(tiempo / 600.0);


        if (simulacion instanceof SimulacionFilaUnica) {
            actualizarClientesFilaUnica();
        } else {
            actualizarClientesFilasMultiples();
        }

        for (CajaGrafica cajaGrafica : cajasGraficas) {
            cajaGrafica.actualizar();
        }

        Estadistica stats = simulacion.getEstadisticas();
        lblClientesAtendidos.setText(String.format("Clientes: %d",
                stats.getTotalClientesAtendidos()));
        lblEsperaPromedio.setText(String.format("Espera Prom: %.2f min",
                stats.getTiempoPromedioEspera()));

        int cajasAbiertas = simulacion.getCajasAbiertas();
        lblCajasAbiertas.setText(String.format("Cajas: %d/12", cajasAbiertas));
    }


    private void actualizarClientesFilaUnica() {
        SimulacionFilaUnica simFilaUnica = (SimulacionFilaUnica) simulacion;
        Set<Cliente> clientesActuales = new HashSet<>();

        int posicionEnFila = 0;


        Cola<Cliente> colaGlobal = simFilaUnica.getFilaGeneral();
        Cliente[] clientesEnFilaGlobal = getElementosCola(colaGlobal);

        for (int i = 0; i < clientesEnFilaGlobal.length; i++) {
            Cliente cliente = clientesEnFilaGlobal[i];
            clientesActuales.add(cliente);

            double posX = FILA_INICIO_X + (posicionEnFila * ESPACIO_ENTRE_CLIENTES);
            double posY = FILA_Y;

            actualizarOCrearCliente(cliente, posX, posY);
            posicionEnFila++;
        }

        ArrayList<Caja> cajas = simulacion.getCajas();
        for (int i = 0; i < cajas.size() && i < cajasGraficas.size(); i++) {
            Caja caja = cajas.get(i);
            CajaGrafica cajaGrafica = cajasGraficas.get(i);

            if (!caja.estaAbierta()) continue;

            Cola<Cliente> colaCaja = caja.getColaClientes();
            Cliente[] clientesEnColaCaja = getElementosCola(colaCaja);

            for (int j = 0; j < clientesEnColaCaja.length; j++) {
                Cliente cliente = clientesEnColaCaja[j];
                clientesActuales.add(cliente);

                double posX = cajaGrafica.getLayoutX() + 80 + (j * ESPACIO_ENTRE_CLIENTES);
                double posY = cajaGrafica.getCentroY();

                actualizarOCrearCliente(cliente, posX, posY);
            }

            Cliente clienteAtendiendo = caja.getClienteActualPagando();
            if (clienteAtendiendo != null) {
                clientesActuales.add(clienteAtendiendo);

                double posX = cajaGrafica.getCentroX() - 40;
                double posY = cajaGrafica.getCentroY();

                actualizarOCrearCliente(clienteAtendiendo, posX, posY);
            }
        }

        limpiarClientesTerminados(clientesActuales);
    }

    private void actualizarOCrearCliente(Cliente cliente, double posX, double posY) {
        ClienteGrafico clienteGrafico = mapaClientesGraficos.get(cliente);

        if (clienteGrafico == null) {


            clienteGrafico = new ClienteGrafico(cliente, ENTRADA_CLIENTES_X, ENTRADA_CLIENTES_Y);
            mapaClientesGraficos.put(cliente, clienteGrafico);
            panelAnimaciones.getChildren().add(clienteGrafico);
            clienteGrafico.toFront();
            clienteGrafico.moverA(posX, posY, 0.5*velocidadActual);
        } else {

            if (Math.abs(clienteGrafico.getPosX() - posX) > 5 || Math.abs(clienteGrafico.getPosY() - posY) > 5) {
                double duracion= 0.3*velocidadActual;
                clienteGrafico.moverA(posX, posY, duracion);
            }
            clienteGrafico.toFront();
        }

        clienteGrafico.actualizarImagen();
    }

    private void actualizarClientesFilasMultiples() {
        ArrayList<Caja> cajas = simulacion.getCajas();
        Set<Cliente> clientesActuales = new HashSet<>();

        for (int i = 0; i < cajas.size() && i < cajasGraficas.size(); i++) {
            Caja caja = cajas.get(i);
            CajaGrafica cajaGrafica = cajasGraficas.get(i);

            if (!caja.estaAbierta()) continue;

            Cola<Cliente> colaCaja = caja.getColaClientes();
            Cliente[] clientesEnCola = getElementosCola(colaCaja);


            for (int j = 0; j < clientesEnCola.length; j++) {
                Cliente cliente = clientesEnCola[j];
                clientesActuales.add(cliente);

                double posX = cajaGrafica.getLayoutX() + 120 + (j * ESPACIO_ENTRE_CLIENTES);
                double posY = cajaGrafica.getCentroY();

                actualizarOCrearCliente(cliente, posX, posY);
            }

            Cliente clienteAtendiendo = caja.getClienteActualPagando();
            if (clienteAtendiendo != null) {
                clientesActuales.add(clienteAtendiendo);

                double posX = cajaGrafica.getCentroX() - 50;
                double posY = cajaGrafica.getCentroY();

                actualizarOCrearCliente(clienteAtendiendo, posX, posY);
            }
        }

        limpiarClientesTerminados(clientesActuales);
    }

    private void limpiarClientesTerminados(Set<Cliente> clientesActuales) {

        List<Cliente> clientesAEliminar = new ArrayList<>();

        for (Map.Entry<Cliente, ClienteGrafico> entry : mapaClientesGraficos.entrySet()) {
            if (!clientesActuales.contains(entry.getKey())) {
                clientesAEliminar.add(entry.getKey());
            }
        }


        for (Cliente cliente : clientesAEliminar) {
            ClienteGrafico clienteGrafico = mapaClientesGraficos.remove(cliente);
            if (clienteGrafico != null) {
                panelAnimaciones.getChildren().remove(clienteGrafico);
            }
        }
    }

    public Cliente[] getElementosCola(Cola<Cliente> cola) {

        int size = cola.tamanio();
        if (size == 0) {
            return new Cliente[0];
        }

        Cliente[] arreglo = new Cliente[size];
        Cola<Cliente> temporal = new Cola<>(100);

        for (int i = 0; i < size; i++) {
            Cliente cliente = cola.eliminar();
            arreglo[i] = cliente;
            temporal.insertar(cliente);
        }

        for (int i = 0; i < size; i++) {
            cola.insertar(temporal.eliminar());
        }

        return arreglo;
    }
    private void mostrarResultadosFinales() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Simulación Completada");
        alert.setHeaderText("La simulación ha terminado");

        Estadistica stats = simulacion.getEstadisticas();
        String contenido = String.format(
                "Clientes atendidos: %d\n" +
                        "Tiempo espera promedio: %.2f min\n" +
                        "Tiempo pago promedio: %.2f min\n" +
                        "Tiempo total promedio: %.2f min",
                stats.getTotalClientesAtendidos(),
                stats.getTiempoPromedioEspera(),
                stats.getTiempoPromedioPago(),
                stats.getTiempoPromedioTotal()
        );

        alert.setContentText(contenido);
        alert.showAndWait();
    }
}