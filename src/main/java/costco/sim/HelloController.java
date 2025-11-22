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

import java.util.*;
import java.util.function.IntToDoubleFunction;

public class HelloController {

    @FXML private Label lblTiempo;
    @FXML private RadioButton rbFilaUnica;
    @FXML private RadioButton rbMultiple;
    @FXML private Button btnIniciar;
    @FXML private Button btnPausar;
    @FXML private Button btnDetener;
    @FXML private ComboBox<String> cbVelocidad;
    @FXML private ProgressIndicator progreso;
    @FXML private ImageView imagenFondo;
    @FXML private Pane panelAnimaciones;
    @FXML private Label lblClientesAtendidos;
    @FXML private Label lblEsperaPromedio;
    @FXML private Label lblCajasAbiertas;

    private Simulacion simulacion;
    private List<CajaGrafica> cajasGraficas;
    private Timer timer;
    private boolean pausado = false;
    private final int[] VELOCIDADES = {1000, 500, 200, 100};
    private int velocidadActual = 1000;

    private static final double ENTRADA_CLIENTES_X = 1150;
    private static final double ENTRADA_CLIENTES_Y = 350;
    private static final double FILA_INICIO_X = 600;
    private static final double FILA_Y = 350;
    private static final int ESPACIO_ENTRE_CLIENTES = 45;
    private Cola<ClienteGrafico> clientesGraficosLibres;
    private Cola<ClienteGrafico> clientesGraficosEnUso;
    private Cola<ClienteGrafico> clientesGraficosTemp;
    private final int MAX_CLIENTES_GRAFICOS = 50;

    @FXML
    public void initialize() {
        inicializarControles();
        inicializarEstados();
    }

    private void inicializarControles() {
        cbVelocidad.getItems().addAll("1x", "2x", "5x", "10x");
        cbVelocidad.setValue("1x");
    }

    private void inicializarEstados() {
        lblTiempo.setText("Tiempo: 0/600 min");
        lblClientesAtendidos.setText("Clientes: 0");
        lblEsperaPromedio.setText("Espera Prom: 0.0 min");
        lblCajasAbiertas.setText("Cajas: 0/12");
        progreso.setProgress(0.0);
        inicializarPoolClientes();
    }

    private void inicializarPoolClientes() {
        clientesGraficosLibres = new Cola<>(MAX_CLIENTES_GRAFICOS);
        clientesGraficosEnUso = new Cola<>(MAX_CLIENTES_GRAFICOS);
        clientesGraficosTemp = new Cola<>(MAX_CLIENTES_GRAFICOS);

        for (int i = 0; i < MAX_CLIENTES_GRAFICOS; i++) {
            ClienteGrafico clienteGrafico = new ClienteGrafico(null, ENTRADA_CLIENTES_X, ENTRADA_CLIENTES_Y);
            clienteGrafico.setVisible(false);
            clientesGraficosLibres.insertar(clienteGrafico);
            panelAnimaciones.getChildren().add(clienteGrafico);
        }
    }

    @FXML
    private void iniciarSimulacion() {
        try {

            simulacion = rbFilaUnica.isSelected() ?
                    new SimulacionFilaUnica() : new SimulacionMultiplesFilas();
            simulacion.iniciar();

            panelAnimaciones.getChildren().clear();
            reinicializarPoolClientes();
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
                    if (!pausado) avanzarSimulacion();
                }
            }, 0, velocidadActual);

        } catch (Exception e) {
            manejarError("Error al iniciar simulación", e);
        }
    }

    private void reinicializarPoolClientes() {
        // Limpiar todas las colas
        while (!clientesGraficosEnUso.estaVacia()) {
            ClienteGrafico cg = clientesGraficosEnUso.eliminar();
            cg.setCliente(null);
            cg.setVisible(false);
            clientesGraficosLibres.insertar(cg);
        }

        while (!clientesGraficosTemp.estaVacia()) {
            ClienteGrafico cg = clientesGraficosTemp.eliminar();
            cg.setCliente(null);
            cg.setVisible(false);
            clientesGraficosLibres.insertar(cg);
        }

        // Asegurarse de que todos los clientes gráficos estén en libres
        Cola<ClienteGrafico> todosClientes = new Cola<>(MAX_CLIENTES_GRAFICOS);
        while (!clientesGraficosLibres.estaVacia()) {
            ClienteGrafico cg = clientesGraficosLibres.eliminar();
            cg.setCliente(null);
            cg.setVisible(false);
            todosClientes.insertar(cg);
        }

        // Re-agregar todos al panel
        Cola<ClienteGrafico> temp = new Cola<>(MAX_CLIENTES_GRAFICOS);
        while (!todosClientes.estaVacia()) {
            ClienteGrafico cg = todosClientes.eliminar();
            if (!panelAnimaciones.getChildren().contains(cg)) {
                panelAnimaciones.getChildren().add(cg);
            }
            temp.insertar(cg);
        }

        clientesGraficosLibres = temp;
    }

    private void crearCajasGraficas() {
        cajasGraficas = new ArrayList<>();
        ArrayList<Caja> cajas = simulacion.getCajas();

        int inicioCajasX = 157, inicioCajasY = 205;
        int espacioEntreCajasX = 300, espacioEntreCajasY = 45;
        int desplazamientoX = -40;

        for (int i = 0; i < cajas.size(); i++) {
            int fila = i / 2, columna = i % 2;
            int posX = inicioCajasX + (columna * espacioEntreCajasX) + (fila * desplazamientoX);
            int posY = inicioCajasY + (fila * espacioEntreCajasY);

            CajaGrafica cajaGrafica = new CajaGrafica(cajas.get(i), posX, posY);
            cajasGraficas.add(cajaGrafica);
            panelAnimaciones.getChildren().add(cajaGrafica);
            cajaGrafica.actualizar();
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
        btnPausar.setText(pausado ? "REANUDAR" : "PAUSAR");
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
        Map<String, Integer> velocidadMap = Map.of(
                "1x", VELOCIDADES[0],
                "2x", VELOCIDADES[1],
                "5x", VELOCIDADES[2],
                "10x", VELOCIDADES[3]
        );

        velocidadActual = velocidadMap.get(cbVelocidad.getValue());

        if (timer != null) {
            reiniciarTimer();
        }
    }

    private void reiniciarTimer() {
        boolean estabaPausado = pausado;
        timer.cancel();
        timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!pausado) avanzarSimulacion();
            }
        }, 0, velocidadActual);
        pausado = estabaPausado;
    }

    private void actualizarInterfaz() {
        int tiempo = simulacion.getTiempoActual();
        lblTiempo.setText(String.format("Tiempo: %d/600 min (%.1f%%)", tiempo, (tiempo * 100.0 / 600)));
        progreso.setProgress(tiempo / 600.0);

        actualizarClientes();

        for (CajaGrafica cajaGrafica : cajasGraficas) {
            cajaGrafica.actualizar();
        }

        Estadistica stats = simulacion.getEstadisticas();
        lblClientesAtendidos.setText(String.format("Clientes: %d", stats.getTotalClientesAtendidos()));
        lblEsperaPromedio.setText(String.format("Espera Prom: %.2f min", stats.getTiempoPromedioEspera()));

        int cajasAbiertas = simulacion.getCajasAbiertas();
        lblCajasAbiertas.setText(String.format("Cajas: %d/12", cajasAbiertas));
    }

    private void actualizarClientes() {
        Set<Cliente> clientesActuales = new HashSet<>();

        if (simulacion instanceof SimulacionFilaUnica) {
            actualizarClientesFilaUnica(clientesActuales);
        } else {
            actualizarClientesFilasMultiples(clientesActuales);
        }

        limpiarClientesTerminados(clientesActuales);
    }

    private void actualizarClientesFilaUnica(Set<Cliente> clientesActuales) {
        SimulacionFilaUnica simFilaUnica = (SimulacionFilaUnica) simulacion;

        // Fila global
        Cola<Cliente> colaGlobal = simFilaUnica.getFilaGeneral();
        procesarColaClientes(colaGlobal, clientesActuales,
                pos -> FILA_INICIO_X + (pos * ESPACIO_ENTRE_CLIENTES),
                pos -> FILA_Y);

        // Cajas individuales
        actualizarClientesEnCajas(clientesActuales);
    }

    private void actualizarClientesFilasMultiples(Set<Cliente> clientesActuales) {
        actualizarClientesEnCajas(clientesActuales);
    }

    private void actualizarClientesEnCajas(Set<Cliente> clientesActuales) {
        for (int i = 0; i < Math.min(simulacion.getCajas().size(), cajasGraficas.size()); i++) {
            Caja caja = simulacion.getCajas().get(i);
            CajaGrafica cajaGrafica = cajasGraficas.get(i);

            if (!caja.estaAbierta()) continue;

            // Clientes en cola
            Cola<Cliente> colaCaja = caja.getColaClientes();
            procesarColaClientes(colaCaja, clientesActuales,
                    pos -> cajaGrafica.getLayoutX() + 120 + (pos * ESPACIO_ENTRE_CLIENTES),
                    pos -> cajaGrafica.getCentroY());

            // Cliente siendo atendido
            Cliente clienteAtendiendo = caja.getClienteActualPagando();
            if (clienteAtendiendo != null) {
                clientesActuales.add(clienteAtendiendo);
                double offsetX = (simulacion instanceof SimulacionFilaUnica) ? 40 : 50;
                actualizarOCrearCliente(clienteAtendiendo,
                        cajaGrafica.getCentroX() - offsetX,
                        cajaGrafica.getCentroY());
            }
        }
    }

    private void procesarColaClientes(Cola<Cliente> cola, Set<Cliente> clientesActuales,
                                      IntToDoubleFunction posXFunction,
                                      IntToDoubleFunction posYFunction) {
        Cliente[] clientesEnCola = getElementosCola(cola);
        for (int i = 0; i < clientesEnCola.length; i++) {
            Cliente cliente = clientesEnCola[i];
            clientesActuales.add(cliente);
            actualizarOCrearCliente(cliente, posXFunction.applyAsDouble(i), posYFunction.applyAsDouble(i));
        }
    }

    private void actualizarOCrearCliente(Cliente cliente, double posX, double posY) {
        ClienteGrafico clienteGrafico = buscarClienteGraficoEnUso(cliente);

        if (clienteGrafico != null) {
            actualizarPosicionCliente(clienteGrafico, posX, posY);
        } else {
            asignarNuevoClienteGrafico(cliente, posX, posY);
        }
    }

    private ClienteGrafico buscarClienteGraficoEnUso(Cliente cliente) {
        if (clientesGraficosEnUso.estaVacia()) return null;

        ClienteGrafico encontrado = null;

        // Usar cola temporal para búsqueda
        while (!clientesGraficosEnUso.estaVacia()) {
            ClienteGrafico cg = clientesGraficosEnUso.eliminar();

            if (encontrado == null && cliente.equals(cg.getCliente())) {
                encontrado = cg;
                // No lo insertamos en temp, lo manejamos aparte
            } else {
                clientesGraficosTemp.insertar(cg);
            }
        }

        // Restaurar cola de en uso
        while (!clientesGraficosTemp.estaVacia()) {
            clientesGraficosEnUso.insertar(clientesGraficosTemp.eliminar());
        }

        // Si encontramos, lo insertamos al final
        if (encontrado != null) {
            clientesGraficosEnUso.insertar(encontrado);
        }

        return encontrado;
    }

    private void actualizarPosicionCliente(ClienteGrafico clienteGrafico, double posX, double posY) {
        if (Math.abs(clienteGrafico.getPosX() - posX) > 5 || Math.abs(clienteGrafico.getPosY() - posY) > 5) {
            double duracion = 0.3 * velocidadActual;
            clienteGrafico.moverA(posX, posY, duracion);
        }
        clienteGrafico.toFront();
        clienteGrafico.actualizarImagen();
    }

    private void asignarNuevoClienteGrafico(Cliente cliente, double posX, double posY) {
        if (!clientesGraficosLibres.estaVacia()) {
            ClienteGrafico nuevoClienteGrafico = clientesGraficosLibres.eliminar();
            nuevoClienteGrafico.setCliente(cliente);
            nuevoClienteGrafico.setVisible(true);
            nuevoClienteGrafico.moverA(posX, posY, 0.5 * velocidadActual);
            nuevoClienteGrafico.toFront();
            nuevoClienteGrafico.actualizarImagen();
            clientesGraficosEnUso.insertar(nuevoClienteGrafico);
        }
    }

    private void limpiarClientesTerminados(Set<Cliente> clientesActuales) {
        Cola<ClienteGrafico> nuevosEnUso = new Cola<>(MAX_CLIENTES_GRAFICOS);

        while (!clientesGraficosEnUso.estaVacia()) {
            ClienteGrafico clienteGrafico = clientesGraficosEnUso.eliminar();
            Cliente cliente = clienteGrafico.getCliente();

            if (cliente != null && clientesActuales.contains(cliente)) {
                nuevosEnUso.insertar(clienteGrafico);
            } else {
                clienteGrafico.setCliente(null);
                clienteGrafico.setVisible(false);
                clientesGraficosLibres.insertar(clienteGrafico);
            }
        }

        clientesGraficosEnUso = nuevosEnUso;
    }

    private Cliente[] getElementosCola(Cola<Cliente> cola) {
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

    private void manejarError(String mensaje, Exception e) {
        System.err.println("ERROR: " + mensaje + ": " + e.getMessage());
        e.printStackTrace();

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(mensaje);
        alert.setContentText(e.getMessage());
        alert.showAndWait();
    }
}