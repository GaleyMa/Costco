package costco.sim.logica;

public class Cola<T> {
    private T[] cola;
    private int inicio;
    private int fin;
    private int MAX;

    public Cola() {
        this(10);
    }

    public Cola(int max) {
        this.MAX = max;
        this.inicio = -1;
        this.fin = -1;
        this.cola = (T[]) new Object[MAX];
    }

    public Cola(Cola<T> original) {
        this.MAX = original.MAX;
        this.cola = (T[]) new Object[MAX];
        this.inicio = -1;
        this.fin = -1;

        if (!original.estaVacia()) {
            for (int i = original.inicio; i <= original.fin; i++) {
                this.insertar(original.cola[i]);
            }
        }
    }

    public boolean insertar(T elemento) {
        if (fin >= MAX - 1) {
            return false;
        }

        fin++;
        cola[fin] = elemento;

        if (inicio == -1) {
            inicio = 0;
        }

        return true;
    }


    public T eliminar() {
        if (inicio == -1) {
            return null;
        }

        T dato = cola[inicio];
        cola[inicio] = null;

        if (inicio == fin) {
            // Cola quedó vacía
            inicio = -1;
            fin = -1;
        } else {
            inicio++;
        }

        return dato;
    }


    public T peek() {
        return (inicio != -1) ? cola[inicio] : null;
    }


    public boolean estaVacia() {
        return inicio == -1;
    }

    public boolean estaLlena() {
        return fin == MAX - 1;
    }

    public int tamanio() {
        return (inicio == -1) ? 0 : (fin - inicio + 1);
    }


    @Override
    public String toString() {
        if (estaVacia()) {
            return "Cola vacía";
        }

        StringBuilder sb = new StringBuilder("Cola [");
        for (int i = inicio; i <= fin; i++) {
            sb.append(cola[i]);
            if (i < fin) sb.append(", ");
        }
        sb.append("]");

        return sb.toString();
    }
}