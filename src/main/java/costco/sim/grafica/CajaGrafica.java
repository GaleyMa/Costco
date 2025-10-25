package costco.sim.grafica;

import costco.sim.logica.Caja;
import costco.sim.logica.Cliente;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.ArrayList;
import java.util.List;


    public class CajaGrafica extends VBox {

        private Caja caja;
        private ImageView imagenCaja;
        private Label lblNumero;
        private Label lblAtendidos;
        private HBox contenedorClientes;
        private List<ClienteGrafico> clientesGraficos;

        // Imágenes estáticas
        private static Image imagenAbierta;
        private static Image imagenCerrada;

        // Cargar imágenes
        static {
            try {
                imagenAbierta = new Image(
                        CajaGrafica.class.getResourceAsStream("/imagenes/caja_abierta.png")
                );
                imagenCerrada = new Image(
                        CajaGrafica.class.getResourceAsStream("/imagenes/caja_cerrada.png")
                );
            } catch (Exception e) {
                System.err.println("Error cargando imágenes de caja: " + e.getMessage());
            }
        }

        /**
         * Constructor
         */
        public CajaGrafica(Caja caja) {
            super(5);
            this.caja = caja;
            this.clientesGraficos = new ArrayList<>();

            configurarEstilo();
            crearComponentes();
            actualizar();
        }

        private void configurarEstilo() {
            this.setAlignment(Pos.TOP_CENTER);
            this.setPrefWidth(120);
            this.setStyle("-fx-border-color: #95a5a6; -fx-border-width: 2; " +
                    "-fx-border-radius: 5; -fx-background-radius: 5; " +
                    "-fx-background-color: white; -fx-padding: 5;");
        }

        private void crearComponentes() {
            // Número de caja
            lblNumero = new Label("CAJA " + caja.getNumeroCaja());
            lblNumero.setFont(Font.font("System", FontWeight.BOLD, 12));

            // Imagen de la caja
            imagenCaja = new ImageView();
            imagenCaja.setFitWidth(100);
            imagenCaja.setFitHeight(80);
            imagenCaja.setPreserveRatio(true);

            // Contenedor para los clientes
            contenedorClientes = new HBox(5);
            contenedorClientes.setAlignment(Pos.CENTER);
            contenedorClientes.setPrefHeight(50);

            // Clientes atendidos
            lblAtendidos = new Label("Atendidos: 0");
            lblAtendidos.setFont(Font.font("System", 10));

            this.getChildren().addAll(lblNumero, imagenCaja, contenedorClientes, lblAtendidos);
        }

        /**
         * Actualiza la caja completa
         */
        public void actualizar() {
            // Actualizar imagen de caja
            if (caja.estaAbierta()) {
                imagenCaja.setImage(imagenAbierta);
                this.setStyle("-fx-border-color: #27ae60; -fx-border-width: 2; " +
                        "-fx-border-radius: 5; -fx-background-radius: 5; " +
                        "-fx-background-color: #ecf9f2; -fx-padding: 5;");
            } else {
                imagenCaja.setImage(imagenCerrada);
                this.setStyle("-fx-border-color: #95a5a6; -fx-border-width: 2; " +
                        "-fx-border-radius: 5; -fx-background-radius: 5; " +
                        "-fx-background-color: #f5f5f5; -fx-padding: 5;");
            }

            // Actualizar clientes atendidos
            lblAtendidos.setText("Atendidos: " + caja.getClientesAtendidos());

            // Actualizar clientes en la cola
            actualizarClientes();
        }

        /**
         * Actualiza los clientes visualmente
         */
        private void actualizarClientes() {
            // Limpiar contenedor
            contenedorClientes.getChildren().clear();
            clientesGraficos.clear();

            if (!caja.estaAbierta()) {
                return;
            }

            // Cliente pagando
            if (caja.tieneClientePagando()) {
                Cliente clientePagando = caja.getClienteActualPagando();
                ClienteGrafico grafico = new ClienteGrafico(clientePagando);
                grafico.setFitWidth(35);
                grafico.setFitHeight(45);
                clientesGraficos.add(grafico);
                contenedorClientes.getChildren().add(grafico);

                // Flecha
                Label flecha = new Label("→");
                flecha.setFont(Font.font("System", FontWeight.BOLD, 16));
                contenedorClientes.getChildren().add(flecha);
            }

            // Clientes esperando
            Object[] clientesEsperando = caja.getClientesEsperando();
            int maxMostrar = Math.min(clientesEsperando.length, 2); // Mostrar máximo 2

            for (int i = 0; i < maxMostrar; i++) {
                Cliente c = (Cliente) clientesEsperando[i];
                ClienteGrafico grafico = new ClienteGrafico(c);
                grafico.setFitWidth(25);
                grafico.setFitHeight(35);
                clientesGraficos.add(grafico);
                contenedorClientes.getChildren().add(grafico);
            }

            // Indicador de más clientes
            if (clientesEsperando.length > maxMostrar) {
                Label mas = new Label("+" + (clientesEsperando.length - maxMostrar));
                mas.setFont(Font.font("System", FontWeight.BOLD, 10));
                contenedorClientes.getChildren().add(mas);
            }
        }

        public Caja getCaja() {
            return caja;
        }
    }

