package costco.sim.grafica;

import costco.sim.logica.Cliente;
import costco.sim.logica.Estado;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class ClienteGrafico extends ImageView {

    private final Cliente cliente;
    private Image imagenEsperando;
    private Image imagenPagando;

    private TranslateTransition transicionActiva;


    private static final ConcurrentHashMap<String, Image> cacheImagenes = new ConcurrentHashMap<>();

    public ClienteGrafico(Cliente cliente, double entradaX, double entradaY) {
        super();
        this.cliente = cliente;
        asignarImagenesCliente();

        setFitWidth(40);
        setFitHeight(50);
        setPreserveRatio(true);
        setLayoutX(entradaX);
        setLayoutY(entradaY);

        actualizarImagen();
    }


    public void actualizarImagen() {
        Image nueva = (cliente.getEstado() == Estado.PAGANDO)
                ? imagenPagando
                : imagenEsperando;

        if (getImage() != nueva) {
            Platform.runLater(() -> setImage(nueva));
        }
    }

    public void moverA(double destinoX, double destinoY, double duracionMs) {
        // Detiene cualquier animación previa
        if (transicionActiva != null) {
            transicionActiva.stop();
        }

        double deltaX = destinoX - getLayoutX();
        double deltaY = destinoY - getLayoutY();

        transicionActiva = new TranslateTransition(Duration.millis(duracionMs), this);
        transicionActiva.setToX(deltaX);
        transicionActiva.setToY(deltaY);

        transicionActiva.setOnFinished(e -> {
            setLayoutX(destinoX);
            setLayoutY(destinoY);
            setTranslateX(0);
            setTranslateY(0);
            transicionActiva = null;
        });

        transicionActiva.play();
    }



    private void asignarImagenesCliente() {
        int numero = new Random().nextInt(1, 5);
        imagenEsperando = cargarImagen("/imagenes/cliente" + numero + ".png");
        imagenPagando   = cargarImagen("/imagenes/cliente" + numero + "_pagando.png");
    }

    private Image cargarImagen(String ruta) {
        return cacheImagenes.computeIfAbsent(ruta, r -> {
            try {
                return new Image(Objects.requireNonNull(
                        ClienteGrafico.class.getResourceAsStream(r)
                ));
            } catch (Exception e) {
                return null;
            }
        });
    }


    public Cliente getCliente() {
        return cliente;
    }

    public double getPosX() {
        return getLayoutX();
    }

    public double getPosY() {
        return getLayoutY();
    }
}
