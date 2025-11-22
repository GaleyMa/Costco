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

    private Cliente cliente;
    private Image imagenEsperando;
    private Image imagenPagando;
    private TranslateTransition transicionActiva;

    private static final ConcurrentHashMap<String, Image> cacheImagenes = new ConcurrentHashMap<>();

    public ClienteGrafico(Cliente cliente, double entradaX, double entradaY) {
        super();
        this.cliente = cliente;

        // Configurar propiedades básicas
        setFitWidth(40);
        setFitHeight(50);
        setPreserveRatio(true);
        setLayoutX(entradaX);
        setLayoutY(entradaY);

        // Solo asignar imágenes y actualizar si el cliente no es null
        if (cliente != null) {
            asignarImagenesCliente();
            actualizarImagen();
        } else {
            // Para cliente null, cargar una imagen por defecto y ocultar
            cargarImagenPorDefecto();
            setVisible(false);
        }
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;

        if (cliente != null) {
            // Si es un nuevo cliente, asignar imágenes y hacer visible
            asignarImagenesCliente();
            setVisible(true);
            actualizarImagen();
        } else {
            // Si se establece null, ocultar y usar imagen por defecto
            setVisible(false);
            cargarImagenPorDefecto();
        }
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void actualizarImagen() {
        // Verificar si cliente es null
        if (cliente == null) {
            setVisible(false);
            return;
        }

        // Verificar si el estado del cliente es null
        Estado estado = cliente.getEstado();
        if (estado == null) {
            setImage(imagenEsperando); // Usar imagen por defecto
            setVisible(true);
            return;
        }

        // Determinar la imagen basada en el estado
        Image nuevaImagen = (estado == Estado.PAGANDO) ? imagenPagando : imagenEsperando;

        // Actualizar la imagen en el hilo de JavaFX
        if (getImage() != nuevaImagen) {
            Platform.runLater(() -> {
                setImage(nuevaImagen);
                setVisible(true);
            });
        }
    }

    public void moverA(double destinoX, double destinoY, double duracionMs) {
        // Detener cualquier animación previa
        if (transicionActiva != null) {
            transicionActiva.stop();
        }

        // Calcular deltas para la transición
        double deltaX = destinoX - getLayoutX();
        double deltaY = destinoY - getLayoutY();

        // Crear y configurar la transición
        transicionActiva = new TranslateTransition(Duration.millis(duracionMs), this);
        transicionActiva.setToX(deltaX);
        transicionActiva.setToY(deltaY);

        // Configurar lo que pasa cuando termina la animación
        transicionActiva.setOnFinished(e -> {
            // Actualizar la posición real
            setLayoutX(destinoX);
            setLayoutY(destinoY);
            // Resetear las traducciones
            setTranslateX(0);
            setTranslateY(0);
            transicionActiva = null;
        });

        // Iniciar la animación
        transicionActiva.play();
    }

    private void asignarImagenesCliente() {
        try {
            int numero = new Random().nextInt(1, 5);
            imagenEsperando = cargarImagen("/imagenes/cliente" + numero + ".png");
            imagenPagando = cargarImagen("/imagenes/cliente" + numero + "_pagando.png");

            // Verificar que las imágenes se cargaron correctamente
            if (imagenEsperando == null || imagenPagando == null) {
                cargarImagenPorDefecto();
            }
        } catch (Exception e) {
            System.err.println("Error asignando imágenes del cliente: " + e.getMessage());
            cargarImagenPorDefecto();
        }
    }

    private void cargarImagenPorDefecto() {
        try {
            imagenEsperando = cargarImagen("/imagenes/cliente1.png"); // Imagen por defecto
            imagenPagando = cargarImagen("/imagenes/cliente1_pagando.png"); // Imagen por defecto
        } catch (Exception e) {
            System.err.println("Error cargando imágenes por defecto: " + e.getMessage());

            imagenEsperando = null;
            imagenPagando = null;
        }
    }

    private Image cargarImagen(String ruta) {
        return cacheImagenes.computeIfAbsent(ruta, r -> {
            try {
                return new Image(Objects.requireNonNull(
                        ClienteGrafico.class.getResourceAsStream(r)
                ));
            } catch (Exception e) {
                System.err.println("Error cargando imagen: " + ruta);
                return null;
            }
        });
    }

    public double getPosX() {
        return getLayoutX() + getTranslateX();
    }

    public double getPosY() {
        return getLayoutY() + getTranslateY();
    }

    public double getCentroX() {
        return getPosX() + getFitWidth() / 2;
    }

    public double getCentroY() {
        return getPosY() + getFitHeight() / 2;
    }

    public boolean tieneCliente() {
        return cliente != null;
    }

    public void limpiar() {
        this.cliente = null;
        setVisible(false);
    }


    @Override
    public String toString() {
        return String.format("ClienteGrafico{cliente=%s, posX=%.1f, posY=%.1f, visible=%s}",
                cliente, getPosX(), getPosY(), isVisible());
    }
}