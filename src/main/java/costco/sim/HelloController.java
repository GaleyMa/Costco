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

public class HelloController {

    // Controles superiores
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

    // Posiciones para la fila general
    private static final int FILA_GENERAL_X = 50;
    private static final int FILA_GENERAL_Y = 150;
    private static final int ESPACIO_ENTRE_CLIENTES = 45;

    @FXML
    public void initialize() {
        // Configurar ComboBox de velocidad
        cbVelocidad.getItems().addAll("1x", "2x", "5x", "10x");
        cbVelocidad.setValue("1x");

        // Inicializar labels
        lblTiempo.setText("Tiempo: 0/600 min");
        lblClientesAtendidos.setText("Clientes: 0");
        lblEsperaPromedio.setText("Espera Prom: 0.0 min");
        lblCajasAbiertas.setText("Cajas: 0/12");

        progreso.setProgress(0.0);
    }

    @FXML
    private void iniciarSimulacion() {
        // Crear simulación según el tipo seleccionado
        if (rbFilaUnica.isSelected()) {
            simulacion = new SimulacionFilaUnica();
        } else {
            simulacion = new SimulacionMultiplesFilas();
        }

        // Iniciar la simulación
        simulacion.iniciar();

        // Limpiar panel y crear cajas gráficas
        panelAnimaciones.getChildren().clear();
        clientesGraficos.clear();
        crearCajasGraficas();

        // Deshabilitar controles
        rbFilaUnica.setDisable(true);
        rbMultiple.setDisable(true);
        btnIniciar.setDisable(true);
        btnPausar.setDisable(false);
        btnDetener.setDisable(false);

        // Resetear estado
        pausado = false;
        progreso.setProgress(0.0);

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

    private void crearCajasGraficas() {
        cajasGraficas = new ArrayList<>();
        ArrayList<Caja> cajas = simulacion.getCajas();

        // Posiciones de las cajas en pantalla (2 columnas, 6 filas)
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

                CajaGrafica cajaGrafica = new CajaGrafica(cajas.get(indiceCaja),posX,posY);
                //cajaGrafica.setLayoutX(posX);
                //cajaGrafica.setLayoutY(posY);

                cajasGraficas.add(cajaGrafica);
                panelAnimaciones.getChildren().add(cajaGrafica);

                indiceCaja++;
            }
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

        // Avanzar un minuto en la simulación
        simulacion.avanzarTiempo();

        // Actualizar interfaz en el hilo de JavaFX
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
        // Actualizar tiempo y progreso
        int tiempo = simulacion.getTiempoActual();
        lblTiempo.setText(String.format("Tiempo: %d/600 min (%.1f%%)",
                tiempo, (tiempo * 100.0 / 600)));
        progreso.setProgress(tiempo / 600.0);

        // LIMPIAR todos los clientes gráficos
        limpiarClientes();

        // Recrear clientes según el tipo de simulación
        if (simulacion instanceof SimulacionFilaUnica) {
            actualizarClientesFilaUnica();
        } else {
            actualizarClientesFilasMultiples();
        }

        // Actualizar cajas gráficas
        for (CajaGrafica cajaGrafica : cajasGraficas) {
            cajaGrafica.actualizar();
        }

        // Actualizar estadísticas
        Estadistica stats = simulacion.getEstadisticas();
        lblClientesAtendidos.setText(String.format("Clientes: %d",
                stats.getTotalClientesAtendidos()));
        lblEsperaPromedio.setText(String.format("Espera Prom: %.2f min",
                stats.getTiempoPromedioEspera()));

        int cajasAbiertas = simulacion.getCajasAbiertas();
        lblCajasAbiertas.setText(String.format("Cajas: %d/12", cajasAbiertas));
    }

    private void limpiarClientes() {
        // Remover todos los clientes gráficos del panel
        for (ClienteGrafico clienteGrafico : clientesGraficos) {
            panelAnimaciones.getChildren().remove(clienteGrafico);
        }
        clientesGraficos.clear();
    }

    private void actualizarClientesFilaUnica() {
        SimulacionFilaUnica simFilaUnica = (SimulacionFilaUnica) simulacion;
        Cola<Cliente> colaGlobal = simFilaUnica.getFilaGeneral();

        System.out.println("DEBUG: Tamaño de cola global: " + colaGlobal.tamanio());

        // Obtener y dibujar clientes en la fila general
        Cliente[] clientesEnFila = getElementosCola(colaGlobal);

        System.out.println("DEBUG: Clientes en fila: " + clientesEnFila.length);

        for (int i = 0; i < clientesEnFila.length; i++) {
            Cliente cliente = clientesEnFila[i];
            double posX = FILA_GENERAL_X + (i * ESPACIO_ENTRE_CLIENTES);
            double posY = FILA_GENERAL_Y;

            ClienteGrafico clienteGrafico = new ClienteGrafico(cliente, posX, posY);

            clientesGraficos.add(clienteGrafico);
            panelAnimaciones.getChildren().add(clienteGrafico);
        }

        System.out.println("DEBUG: Clientes en fila general creados: " + clientesEnFila.length);

        // Dibujar clientes siendo atendidos en cajas
        ArrayList<Caja> cajas = simulacion.getCajas();
        for (int i = 0; i < cajas.size() && i < cajasGraficas.size(); i++) {
            Caja caja = cajas.get(i);
            CajaGrafica cajaGrafica = cajasGraficas.get(i);
            Cliente clienteAtendiendo = caja.getClienteActualPagando();

            if (clienteAtendiendo != null) {
                double posX = cajaGrafica.getLayoutX() - 50;
                double posY = cajaGrafica.getLayoutY();

                ClienteGrafico clienteGrafico = new ClienteGrafico(clienteAtendiendo, posX, posY);

                clientesGraficos.add(clienteGrafico);
                panelAnimaciones.getChildren().add(clienteGrafico);

                System.out.println("DEBUG: Cliente atendiendo en caja " + i);
            }
        }

        System.out.println("DEBUG: Total clientes gráficos creados: " + clientesGraficos.size());
        System.out.println("DEBUG: Hijos en panelAnimaciones: " + panelAnimaciones.getChildren().size());
    }

    private void actualizarClientesFilasMultiples() {
        ArrayList<Caja> cajas = simulacion.getCajas();

        System.out.println("DEBUG: Actualizando filas múltiples");

        for (int i = 0; i < cajas.size() && i < cajasGraficas.size(); i++) {
            Caja caja = cajas.get(i);
            CajaGrafica cajaGrafica = cajasGraficas.get(i);
            Cola<Cliente> colaCaja = caja.getColaClientes();

            System.out.println("DEBUG: Caja " + i + " tiene " + colaCaja.tamanio() + " clientes");

            // Dibujar clientes en la cola de la caja
            Cliente[] clientesEnCola = getElementosCola(colaCaja);

            for (int j = 0; j < clientesEnCola.length; j++) {
                Cliente cliente = clientesEnCola[j];
                double posX = cajaGrafica.getLayoutX() - 100 - (j * 45);
                double posY = cajaGrafica.getLayoutY();

                ClienteGrafico clienteGrafico = new ClienteGrafico(cliente, posX, posY);

                clientesGraficos.add(clienteGrafico);
                panelAnimaciones.getChildren().add(clienteGrafico);
            }

            // Dibujar cliente siendo atendido
            Cliente clienteAtendiendo = caja.getClienteActualPagando();
            if (clienteAtendiendo != null) {
                double posX = cajaGrafica.getLayoutX() - 50;
                double posY = cajaGrafica.getLayoutY();

                ClienteGrafico clienteGrafico = new ClienteGrafico(clienteAtendiendo, posX, posY);

                clientesGraficos.add(clienteGrafico);
                panelAnimaciones.getChildren().add(clienteGrafico);
            }
        }

        System.out.println("DEBUG: Total clientes gráficos creados: " + clientesGraficos.size());
    }
    public Cliente[] getElementosCola( Cola<Cliente> cola ) {
        Cola<Cliente> copia= new Cola<>(cola);
        Cliente[] arreglo= new Cliente[cola.tamanio()];
        for (int i = 0; i < cola.tamanio(); i++) {
            arreglo[i]=copia.eliminar();
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