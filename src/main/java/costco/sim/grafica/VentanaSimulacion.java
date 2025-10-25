package costco.sim.grafica;

import costco.sim.logica.*;
import costco.sim.Simulaciones.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Ventana principal con visualización gráfica
 */
public class VentanaSimulacion extends Application {

    // Control
    private RadioButton rbFilaUnica;
    private RadioButton rbMultiple;
    private Button btnIniciar;
    private Button btnPausar;
    private Button btnDetener;
    private ComboBox<String> cbVelocidad;
    private Label lblTiempo;

    // Visualización
    private FlowPane panelCajas;
    private List<CajaGrafica> cajasGraficas;
    private HBox filaGeneral;
    private Label lblFilaGeneral;

    // Estadísticas
    private TextArea txtEstadisticas;

    // Lógica
    private Simulacion simulacion;
    private Timer timer;
    private boolean pausado = false;
    private final int[] VELOCIDADES = {1000, 500, 200, 100};
    private int velocidadActual = 1000;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Simulador de Filas - Costco Mexicali");

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        root.setTop(crearPanelSuperior());
        root.setCenter(crearPanelCentral());
        root.setBottom(crearPanelInferior());

        Scene scene = new Scene(root, 1000, 750);
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> detenerSimulacion());
        primaryStage.show();
    }

    private VBox crearPanelSuperior() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: #3498db; -fx-background-radius: 5;");

        Label titulo = new Label("SIMULADOR DE FILAS - COSTCO MEXICALI");
        titulo.setFont(Font.font("System", FontWeight.BOLD, 18));
        titulo.setStyle("-fx-text-fill: white;");

        // Método
        HBox boxMetodo = new HBox(15);
        boxMetodo.setAlignment(Pos.CENTER);
        ToggleGroup grupo = new ToggleGroup();

        rbFilaUnica = new RadioButton("Fila Única");
        rbFilaUnica.setToggleGroup(grupo);
        rbFilaUnica.setSelected(true);
        rbFilaUnica.setStyle("-fx-text-fill: white;");

        rbMultiple = new RadioButton("Múltiples Filas");
        rbMultiple.setToggleGroup(grupo);
        rbMultiple.setStyle("-fx-text-fill: white;");

        boxMetodo.getChildren().addAll(rbFilaUnica, rbMultiple);

        // Botones
        HBox boxBotones = new HBox(10);
        boxBotones.setAlignment(Pos.CENTER);

        btnIniciar = new Button("▶ INICIAR");
        btnIniciar.setPrefWidth(100);
        btnIniciar.setOnAction(e -> iniciarSimulacion());

        btnPausar = new Button("⏸ PAUSAR");
        btnPausar.setPrefWidth(100);
        btnPausar.setDisable(true);
        btnPausar.setOnAction(e -> pausarReanudar());

        btnDetener = new Button("⏹ DETENER");
        btnDetener.setPrefWidth(100);
        btnDetener.setDisable(true);
        btnDetener.setOnAction(e -> detenerSimulacion());

        boxBotones.getChildren().addAll(btnIniciar, btnPausar, btnDetener);

        // Velocidad
        HBox boxVelocidad = new HBox(10);
        boxVelocidad.setAlignment(Pos.CENTER);

        Label lblVel = new Label("Velocidad:");
        lblVel.setStyle("-fx-text-fill: white;");

        cbVelocidad = new ComboBox<>();
        cbVelocidad.getItems().addAll("1x", "2x", "5x", "10x");
        cbVelocidad.setValue("1x");
        cbVelocidad.setOnAction(e -> cambiarVelocidad());

        boxVelocidad.getChildren().addAll(lblVel, cbVelocidad);

        // Tiempo
        lblTiempo = new Label("Tiempo: 0/600 min");
        lblTiempo.setFont(Font.font("System", FontWeight.BOLD, 14));
        lblTiempo.setStyle("-fx-text-fill: white;");

        panel.getChildren().addAll(titulo, boxMetodo, boxBotones, boxVelocidad, lblTiempo);
        panel.setAlignment(Pos.CENTER);

        return panel;
    }

    private VBox crearPanelCentral() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));

        // Fila general
        VBox contenedorFilaGeneral = new VBox(5);
        lblFilaGeneral = new Label("FILA GENERAL");
        lblFilaGeneral.setFont(Font.font("System", FontWeight.BOLD, 12));

        filaGeneral = new HBox(5);
        filaGeneral.setAlignment(Pos.CENTER_LEFT);
        filaGeneral.setPrefHeight(50);
        filaGeneral.setStyle("-fx-background-color: #ecf0f1; -fx-border-color: #3498db; " +
                "-fx-border-width: 2; -fx-border-radius: 5; " +
                "-fx-background-radius: 5; -fx-padding: 5;");

        contenedorFilaGeneral.getChildren().addAll(lblFilaGeneral, filaGeneral);
        contenedorFilaGeneral.setVisible(false);

        // Panel de cajas
        Label lblCajas = new Label("CAJAS");
        lblCajas.setFont(Font.font("System", FontWeight.BOLD, 12));

        panelCajas = new FlowPane(10, 10);
        panelCajas.setAlignment(Pos.CENTER);
        panelCajas.setPrefWrapLength(850);

        ScrollPane scrollCajas = new ScrollPane(panelCajas);
        scrollCajas.setFitToWidth(true);
        scrollCajas.setPrefHeight(450);

        panel.getChildren().addAll(contenedorFilaGeneral, lblCajas, scrollCajas);

        return panel;
    }

    private VBox crearPanelInferior() {
        VBox panel = new VBox(5);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 5;");

        Label titulo = new Label("ESTADÍSTICAS EN TIEMPO REAL");
        titulo.setFont(Font.font("System", FontWeight.BOLD, 12));

        txtEstadisticas = new TextArea();
        txtEstadisticas.setEditable(false);
        txtEstadisticas.setPrefHeight(80);
        txtEstadisticas.setFont(Font.font("Monospaced", 10));

        panel.getChildren().addAll(titulo, txtEstadisticas);

        return panel;
    }

    // ==================== LÓGICA ====================

    private void iniciarSimulacion() {
        if (rbFilaUnica.isSelected()) {
            simulacion = new SimulacionFilaUnica();
        } else {
            simulacion = new SimulacionMultiplesFilas();
        }
        simulacion.iniciar();
        crearCajasGraficas();

        rbFilaUnica.setDisable(true);
        rbMultiple.setDisable(true);
        btnIniciar.setDisable(true);
        btnPausar.setDisable(false);
        btnDetener.setDisable(false);

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

    private void crearCajasGraficas() {
        panelCajas.getChildren().clear();
        cajasGraficas = new ArrayList<>();

        for (Caja caja : simulacion.getCajas()) {
            CajaGrafica cajaGrafica = new CajaGrafica(caja);
            cajasGraficas.add(cajaGrafica);
            panelCajas.getChildren().add(cajaGrafica);
        }

        // Mostrar fila general solo para fila única
        if (simulacion instanceof SimulacionFilaUnica) {
            lblFilaGeneral.getParent().setVisible(true);
        } else {
            lblFilaGeneral.getParent().setVisible(false);
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

    private void pausarReanudar() {
        pausado = !pausado;
        btnPausar.setText(pausado ? "▶ REANUDAR" : "⏸ PAUSAR");
    }

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
        // Actualizar tiempo
        int tiempo = simulacion.getTiempoActual();
        lblTiempo.setText(String.format("Tiempo: %d/600 min (%.1f%%)",
                tiempo, (tiempo * 100.0 / 600)));

        // Actualizar fila general
        if (simulacion instanceof SimulacionFilaUnica) {
            actualizarFilaGeneral();
        }

        // Actualizar cajas
        for (CajaGrafica cajaGrafica : cajasGraficas) {
            cajaGrafica.actualizar();
        }

        // Actualizar estadísticas
        actualizarEstadisticas();
    }

    private void actualizarFilaGeneral() {
        SimulacionFilaUnica simFila = (SimulacionFilaUnica) simulacion;
        int clientes = simFila.getClientesEnFilaGeneral();

        filaGeneral.getChildren().clear();

        if (clientes == 0) {
            Label vacia = new Label("(vacía)");
            vacia.setFont(Font.font("System", FontPosture.ITALIC, 11));
            filaGeneral.getChildren().add(vacia);
        } else {
            int maxMostrar = Math.min(clientes, 15);

            for (int i = 0; i < maxMostrar; i++) {
                // Crear un cliente genérico para la fila
                Cliente c = new Cliente(i);
                c.setEstado(Estado.EN_FILA_GENERAL);

                ClienteGrafico grafico = new ClienteGrafico(c);
                grafico.setFitWidth(25);
                grafico.setFitHeight(35);
                filaGeneral.getChildren().add(grafico);
            }

            if (clientes > maxMostrar) {
                Label mas = new Label("+ " + (clientes - maxMostrar) + " más");
                mas.setFont(Font.font("System", FontWeight.BOLD, 11));
                filaGeneral.getChildren().add(mas);
            }
        }

        lblFilaGeneral.setText("FILA GENERAL (" + clientes + " clientes)");
    }

    private void actualizarEstadisticas() {
        Estadistica stats = simulacion.getEstadisticas();

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Clientes atendidos: %d  |  ",
                stats.getTotalClientesAtendidos()));
        sb.append(String.format("Espera promedio: %.2f min  |  ",
                stats.getTiempoPromedioEspera()));

        long cajasAbiertas = simulacion.getCajas().stream()
                .filter(Caja::estaAbierta)
                .count();
        sb.append(String.format("Cajas abiertas: %d/12", cajasAbiertas));

        txtEstadisticas.setText(sb.toString());
    }

    private void mostrarResultadosFinales() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Simulación Completada");
        alert.setHeaderText("La simulación ha terminado");
        alert.setContentText(simulacion.getEstadisticas().generarReporte());
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}