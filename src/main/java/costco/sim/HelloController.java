package costco.sim;

import costco.sim.grafica.ClienteGrafico;
import costco.sim.logica.*;
import costco.sim.Simulaciones.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.ImageView;
import java.util.*;

/**
 * Controlador principal - Maneja la lógica y las animaciones
 */
public class HelloController {

    // ==================== COMPONENTES FXML ====================

    @FXML private Label lblTiempo;
    @FXML private RadioButton rbFilaUnica;
    @FXML private RadioButton rbMultiple;
    @FXML private Button btnIniciar;
    @FXML private Button btnPausar;
    @FXML private Button btnDetener;
    @FXML private ComboBox<String> cbVelocidad;
    @FXML private StackPane areaJuego;
    @FXML private ImageView imagenFondo;
    @FXML private Pane panelAnimaciones;
    @FXML private Label lblClientesAtendidos;
    @FXML private Label lblEsperaPromedio;
    @FXML private Label lblCajasAbiertas;

    // ==================== LÓGICA ====================

    private Simulacion simulacion;
    private Timer timer;
    private boolean pausado = false;
    private final int[] VELOCIDADES = {1000, 500, 200, 100};
    private int velocidadActual = 1000;

    // Clientes gráficos en pantalla
    private Map<Integer, ClienteGrafico> clientesEnPantalla;

    // Posiciones de las cajas en pantalla
    private static final int CAJAS_POR_FILA = 4;
    private static final double INICIO_CAJAS_X = 100;
    private static final double INICIO_CAJAS_Y = 300;
    private static final double ESPACIO_ENTRE_CAJAS_X = 250;
    private static final double ESPACIO_ENTRE_CAJAS_Y = 150;

    // Posiciones de la fila general
    private static final double FILA_GENERAL_X = 100;
    private static final double FILA_GENERAL_Y = 100;
    private static final double ESPACIO_ENTRE_CLIENTES = 50;

    /**
     * Se ejecuta al cargar el FXML
     */
    @FXML
    private void initialize() {
        clientesEnPantalla = new HashMap<>();

        // Ajustar el tamaño del panel de animaciones al área de juego
        panelAnimaciones.prefWidthProperty().bind(areaJuego.widthProperty());
        panelAnimaciones.prefHeightProperty().bind(areaJuego.heightProperty());

        System.out.println("Controlador inicializado correctamente");
    }

    // ==================== EVENTOS ====================

    /**
     * Inicia la simulación
     */
    @FXML
    private void iniciarSimulacion() {
        // Limpiar pantalla
        panelAnimaciones.getChildren().clear();
        clientesEnPantalla.clear();

        // Crear simulación
        if (rbFilaUnica.isSelected()) {
            simulacion = new SimulacionFilaUnica();
        } else {
            simulacion = new SimulacionMultiplesFilas();
        }

        // Dibujar cajas
        dibujarCajas();

        // Deshabilitar controles
        rbFilaUnica.setDisable(true);
        rbMultiple.setDisable(true);
        btnIniciar.setDisable(true);
        btnPausar.setDisable(false);
        btnDetener.setDisable(false);

        // Iniciar timer
        timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!pausado) {
                    avanzarSimulacion();
                }
            }
        }, 0, velocidadActual);
    }

    /**
     * Pausa o reanuda
     */
    @FXML
    private void pausarReanudar() {
        pausado = !pausado;
        btnPausar.setText(pausado ? "▶ REANUDAR" : "⏸ PAUSAR");
    }

    /**
     * Detiene la simulación
     */
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
    }

    /**
     * Cambia la velocidad
     */
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

    // ==================== LÓGICA DE SIMULACIÓN ====================

    /**
     * Avanza un paso de la simulación
     */
    private void avanzarSimulacion() {
        if (simulacion.haTerminado()) {
            Platform.runLater(() -> {
                detenerSimulacion();
                mostrarResultadosFinales();
            });
            return;
        }

        simulacion.avanzarTiempo();
        Platform.runLater(() -> actualizarVisualizacion());
    }

    /**
     * Actualiza toda la visualización
     */
    private void actualizarVisualizacion() {
        actualizarTiempo();
        actualizarClientesEnPantalla();
        actualizarEstadisticas();
    }

    /**
     * Actualiza el label de tiempo
     */
    private void actualizarTiempo() {
        int tiempo = simulacion.getTiempoActual();
        lblTiempo.setText(String.format("Tiempo: %d/600 min (%.1f%%)",
                tiempo, (tiempo * 100.0 / 600)));
    }

    /**
     * Actualiza los clientes en pantalla con animaciones
     */
    private void actualizarClientesEnPantalla() {
        if (simulacion instanceof SimulacionFilaUnica) {
            actualizarFilaUnica();
        } else {
            actualizarMultiplesFilas();
        }
    }

    /**
     * Actualiza visualización para fila única
     */
    private void actualizarFilaUnica() {
        SimulacionFilaUnica simFila = (SimulacionFilaUnica) simulacion;

        // TODO: Obtener clientes de la fila general y animarlos
        // Por ahora solo actualizamos las cajas
        actualizarCajas();
    }

    /**
     * Actualiza visualización para múltiples filas
     */
    private void actualizarMultiplesFilas() {
        actualizarCajas();
    }

    /**
     * Actualiza los clientes en las cajas
     */
    private void actualizarCajas() {
        List<Caja> cajas = simulacion.getCajas();

        for (int i = 0; i < cajas.size(); i++) {
            Caja caja = cajas.get(i);

            if (!caja.estaAbierta()) {
                continue;
            }

            // Calcular posición de la caja
            int fila = i / CAJAS_POR_FILA;
            int columna = i % CAJAS_POR_FILA;

            double cajaX = INICIO_CAJAS_X + columna * ESPACIO_ENTRE_CAJAS_X;
            double cajaY = INICIO_CAJAS_Y + fila * ESPACIO_ENTRE_CAJAS_Y;

            // Cliente pagando
            if (caja.tieneClientePagando()) {
                Cliente clientePagando = caja.getClienteActualPagando();
                mostrarClienteEnCaja(clientePagando, cajaX, cajaY);
            }

            // Clientes esperando
            Object[] clientesEsperando = caja.getClientesEsperando();
            for (int j = 0; j < Math.min(clientesEsperando.length, 3); j++) {
                Cliente c = (Cliente) clientesEsperando[j];
                mostrarClienteEsperando(c, cajaX - 60 - j * 50, cajaY);
            }
        }
    }

    /**
     * Muestra un cliente en una caja
     */
    private void mostrarClienteEnCaja(Cliente cliente, double x, double y) {
        if (!clientesEnPantalla.containsKey(cliente.getId())) {
            ClienteGrafico grafico = new ClienteGrafico(cliente, x, y);
            clientesEnPantalla.put(cliente.getId(), grafico);
            panelAnimaciones.getChildren().add(grafico);
        } else {
            ClienteGrafico grafico = clientesEnPantalla.get(cliente.getId());
            grafico.actualizarImagen();
            grafico.moverA(x, y, 500);
        }
    }

    /**
     * Muestra un cliente esperando
     */
    private void mostrarClienteEsperando(Cliente cliente, double x, double y) {
        if (!clientesEnPantalla.containsKey(cliente.getId())) {
            ClienteGrafico grafico = new ClienteGrafico(cliente, x, y);
            clientesEnPantalla.put(cliente.getId(), grafico);
            panelAnimaciones.getChildren().add(grafico);
        }
    }

    /**
     * Dibuja las cajas en pantalla
     */
    private void dibujarCajas() {
        // Aquí puedes dibujar imágenes de cajas o usar formas geométricas
        // Por ahora las dejamos invisibles, los clientes se posicionarán en sus coordenadas
    }

    /**
     * Actualiza las estadísticas
     */
    private void actualizarEstadisticas() {
        Estadistica stats = simulacion.getEstadisticas();

        lblClientesAtendidos.setText("Clientes: " + stats.getTotalClientesAtendidos());
        lblEsperaPromedio.setText(String.format("Espera Prom: %.2f min",
                stats.getTiempoPromedioEspera()));

        long cajasAbiertas = simulacion.getCajas().stream()
                .filter(Caja::estaAbierta)
                .count();
        lblCajasAbiertas.setText("Cajas: " + cajasAbiertas + "/12");
    }

    /**
     * Muestra resultados finales
     */
    private void mostrarResultadosFinales() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Simulación Completada");
        alert.setHeaderText("¡La simulación ha terminado!");
        alert.setContentText(simulacion.getEstadisticas().generarReporte());
        alert.showAndWait();
    }
}