package costco.sim.grafica;
import costco.sim.logica.Cliente;
import costco.sim.logica.Estado;
import javafx.animation.TranslateTransition;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

import java.util.Objects;
import java.util.Random;


public class ClienteGrafico extends ImageView {

    private Cliente cliente;
    private static Image imagenEsperando;
    private static Image imagenPagando;



    public ClienteGrafico(Cliente cliente, double x, double y) {
        super();
        this.cliente = cliente;

        asignacionDeCliente();
        this.setFitWidth(40);
        this.setFitHeight(50);
        this.setPreserveRatio(true);

        this.setX(x);
        this.setY(y);

        actualizarImagen();
    }

    public ClienteGrafico(Cliente clienteAtendiendo) {
        this.cliente = clienteAtendiendo;
        asignacionDeCliente();
        asignacionDeCliente();
        this.setFitWidth(40);
        this.setFitHeight(50);
        this.setPreserveRatio(true);
    }


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
    private void asignacionDeCliente(){
        Random rand = new Random();
        int numero = rand.nextInt(1,4);

        switch (numero){
            case 1:
                imagenEsperando = new Image(Objects.requireNonNull(ClienteGrafico.class.getResourceAsStream("/imagenes/cliente1.png")));
                imagenPagando = new Image(Objects.requireNonNull(ClienteGrafico.class.getResourceAsStream("/imagenes/cliente1_pagando.png")));
                break;
            case 2:
                imagenEsperando = new Image(Objects.requireNonNull(ClienteGrafico.class.getResourceAsStream("/imagenes/cliente2.png")));
                imagenPagando = new Image(Objects.requireNonNull(ClienteGrafico.class.getResourceAsStream("/imagenes/cliente2_pagando.png")));
                break;
            case 3:
                imagenEsperando = new Image(Objects.requireNonNull(ClienteGrafico.class.getResourceAsStream("/imagenes/cliente3.png")));
                imagenPagando = new Image(Objects.requireNonNull(ClienteGrafico.class.getResourceAsStream("/imagenes/cliente3_pagando.png")));
                break;
            case 4:
                imagenEsperando = new Image(Objects.requireNonNull(ClienteGrafico.class.getResourceAsStream("/imagenes/cliente4.png")));
                imagenPagando = new Image(Objects.requireNonNull(ClienteGrafico.class.getResourceAsStream("/imagenes/cliente4_pagando.png")));
                break;
            default:
                imagenEsperando = null;
                imagenPagando = null;
                System.out.println("Error al cargar imagen de cliente");
        }
    }

    public Cliente getCliente() {
        return cliente;
    }
}