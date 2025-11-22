package costco.sim.grafica;

import costco.sim.logica.Caja;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.paint.Color;

import java.util.Objects;

/**
 * Representación gráfica de una caja
 * Muestra imagen diferente según esté abierta o cerrada
 */
public class CajaGrafica extends StackPane {

    private Caja caja;
    private ImageView imagenCaja;
    private Label lblNumero;
    private Label lblAtendidos;

    private static Image imagenAbierta;
    private static Image imagenCerrada;


    static {
        try {
            imagenAbierta = new Image(
                    Objects.requireNonNull(CajaGrafica.class.getResourceAsStream("/imagenes/caja_abierta.png"))
            );
            imagenCerrada = new Image(
                    Objects.requireNonNull(CajaGrafica.class.getResourceAsStream("/imagenes/caja_cerrada.png"))
            );
            System.out.println("Imágenes de caja cargadas correctamente");
        } catch (Exception e) {
            System.err.println("Error cargando imágenes de caja: " + e.getMessage());
        }
    }


    public CajaGrafica(Caja caja, int x, int y) {
        super();
        this.caja = caja;
        this.setLayoutX(x);
        this.setLayoutY(y);
        crearComponentes();
    }

    private void crearComponentes() {
        imagenCaja = new ImageView();
        imagenCaja.setFitWidth(120);
        imagenCaja.setFitHeight(100);
        imagenCaja.setPreserveRatio(true);
        imagenCaja.setImage(imagenCerrada);

        // Label con número de caja (encima de la imagen)
        lblNumero = new Label("CAJA " + caja.getNumeroCaja());
        lblNumero.setFont(Font.font("System", FontWeight.BOLD, 14));
        lblNumero.setTextFill(Color.WHITE);
        lblNumero.setStyle("-fx-background-color: rgba(0,0,0,0.7); " +
                "-fx-padding: 5px; " +
                "-fx-background-radius: 5;");
        StackPane.setAlignment(lblNumero, Pos.TOP_CENTER);

        // Label con clientes atendidos (abajo de la imagen)
        lblAtendidos = new Label("Atendidos: 0");
        lblAtendidos.setFont(Font.font("System", FontWeight.NORMAL, 11));
        lblAtendidos.setTextFill(Color.WHITE);
        lblAtendidos.setStyle("-fx-background-color: rgba(0,0,0,0.7); " +
                "-fx-padding: 3px; " +
                "-fx-background-radius: 5;");
        StackPane.setAlignment(lblAtendidos, Pos.BOTTOM_CENTER);
        this.getChildren().addAll(imagenCaja, lblNumero, lblAtendidos);
    }

    public void actualizar() {

        if (caja.estaAbierta()) {
            imagenCaja.setImage(imagenAbierta);
            lblNumero.setTextFill(Color.LIGHTGREEN);
        } else {
            imagenCaja.setImage(imagenCerrada);
            lblNumero.setTextFill(Color.GRAY);
        }

        lblAtendidos.setText("Atendidos: " + caja.getClientesAtendidos());
        lblAtendidos.setVisible(caja.estaAbierta());
    }


    public Caja getCaja() {
        return caja;
    }


    public double getPosX() {
        return this.getLayoutX();
    }


    public double getPosY() {
        return this.getLayoutY();
    }

    public double getCentroX() {
        return this.getLayoutX() + imagenCaja.getFitWidth() / 2;
    }


    public double getCentroY() {
        return this.getLayoutY() + imagenCaja.getFitHeight() / 2;
    }
}