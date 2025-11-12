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
    public Cola(Cola a){
        this.cola = (T[]) a.cola;
        this.inicio=a.inicio;
        this.fin=a.fin;
        this.MAX=a.MAX;
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
    public T[] getElementosCola( ) {
        Cola<T> copia= new Cola<>();
        T[] arreglo= (T[]) new Object[tamanio()];
        for (int i = 0; i < tamanio(); i++) {
            arreglo[i]=copia.eliminar();
        }
        return arreglo;
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

    public T obtenerEnPosicion(int posicion) {
        return cola[posicion];
    }
}