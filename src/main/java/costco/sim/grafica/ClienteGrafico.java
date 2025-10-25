package costco.sim.grafica;
import costco.sim.logica.Cliente;
import costco.sim.logica.Estado;
import javafx.animation.TranslateTransition;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

/**
 * Sprite animado de un cliente que se mueve por la tienda
 */
public class ClienteGrafico extends ImageView {

    private Cliente cliente;
    private static Image imagenEsperando;
    private static Image imagenPagando;

    // Cargar imágenes una sola vez
    static {
        try {
            imagenEsperando = new Image(
                    ClienteGrafico.class.getResourceAsStream("/imagenes/cliente_esperando.png")
            );
            imagenPagando = new Image(
                    ClienteGrafico.class.getResourceAsStream("/imagenes/cliente_pagando.png")
            );
        } catch (Exception e) {
            System.err.println("Error cargando imágenes: " + e.getMessage());
        }
    }

    public ClienteGrafico(Cliente cliente, double x, double y) {
        super();
        this.cliente = cliente;

        // Configurar tamaño
        this.setFitWidth(40);
        this.setFitHeight(50);
        this.setPreserveRatio(true);

        // Posición inicial
        this.setX(x);
        this.setY(y);

        // Imagen inicial
        actualizarImagen();
    }

    /**
     * Actualiza la imagen según el estado
     */
    public void actualizarImagen() {
        if (cliente.getEstado() == Estado.PAGANDO) {
            this.setImage(imagenPagando);
        } else {
            this.setImage(imagenEsperando);
        }
    }

    /**
     * Mueve el cliente a una posición con animación
     * @param destinoX Posición X de destino
     * @param destinoY Posición Y de destino
     * @param duracion Duración de la animación en milisegundos
     */
    public void moverA(double destinoX, double destinoY, double duracion) {
        TranslateTransition transicion = new TranslateTransition(
                Duration.millis(duracion),
                this
        );

        // Calcular desplazamiento desde posición actual
        transicion.setToX(destinoX - this.getX());
        transicion.setToY(destinoY - this.getY());

        transicion.setOnFinished(e -> {
            // Actualizar posición real después de la animación
            this.setX(destinoX);
            this.setY(destinoY);
            this.setTranslateX(0);
            this.setTranslateY(0);
        });

        transicion.play();
    }

    /**
     * Hace que el cliente "rebote" ligeramente (para cuando está esperando)
     */
    public void animarEspera() {
        TranslateTransition rebote = new TranslateTransition(
                Duration.millis(500),
                this
        );
        rebote.setByY(-5);
        rebote.setCycleCount(2);
        rebote.setAutoReverse(true);
        rebote.play();
    }

    public Cliente getCliente() {
        return cliente;
    }
}