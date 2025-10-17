package costco.sim.logica;

public class Cola<T> {
    private T[] cola;
    private int inicio;
    private int fin;
    private int MAX;

    public Cola() {
        this.MAX = 10;
        this.inicio = -1;
        this.fin = -1;
        this.cola = (T[]) new Object[MAX];
    }

    public Cola(int max) {
        this.MAX = max;
        this.inicio = -1;
        this.fin = -1;
        this.cola = (T[]) new Object[MAX];
    }


    public boolean insertar(T elemento) {
        boolean done = false;
        if (fin < MAX - 1) {
            fin++;
            cola[fin] = elemento;
            done = true;
            if (fin == 0) {
                inicio = 0;
            }
        }
        return done;
    }

    public T eliminar() {
        T dato = null;
        if (inicio != -1) {
            dato = cola[inicio];
            if (inicio == fin) {
                inicio = -1;
                fin = -1;
            } else {
                inicio++;
            }
        }
        return dato;
    }

    public T peek() {
        if (inicio != -1) {
            return cola[inicio];
        }
        return null;
    }

    public boolean estaVacia() {
        return inicio == -1;
    }


    public boolean estaLlena() {
        return fin == MAX - 1;
    }


    public int tamanio() {
        if (inicio == -1) {
            return 0;
        }
        return fin - inicio + 1;
    }


    public T[] obtenerElementos() {
        if (inicio == -1) {
            return (T[]) new Object[0];
        }

        int cantidad = tamanio();
        T[] elementos = (T[]) new Object[cantidad];

        for (int i = 0; i < cantidad; i++) {
            elementos[i] = cola[inicio + i];
        }

        return elementos;
    }

    public T obtenerEnPosicion(int posicion) {
        if (inicio == -1 || posicion < 0 || posicion >= tamanio()) {
            return null;
        }
        return cola[inicio + posicion];
    }

    public void limpiar() {
        inicio = -1;
        fin = -1;
        // Limpiar referencias para el garbage collector
        for (int i = 0; i < MAX; i++) {
            cola[i] = null;
        }
    }

    public int getCapacidadMaxima() {
        return MAX;
    }

    @Override
    public String toString() {
        if (inicio == -1) {
            return "Cola: []";
        }

        StringBuilder sb = new StringBuilder("Cola: [");
        for (int i = inicio; i <= fin; i++) {
            sb.append(cola[i]);
            if (i < fin) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}