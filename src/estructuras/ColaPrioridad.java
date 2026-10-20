package estructuras;

import java.util.Comparator;
import java.util.NoSuchElementException;

public class ColaPrioridad<T> {

    private Object[] monticulo;
    private int tamano;
    private final Comparator<? super T> comparador;

    public ColaPrioridad(Comparator<? super T> comparador) {
        this.comparador = comparador;
        this.monticulo = new Object[16];
    }

    public void insertar(T dato) {
        if (tamano == monticulo.length) {
            Object[] mayor = new Object[monticulo.length * 2];
            System.arraycopy(monticulo, 0, mayor, 0, tamano);
            monticulo = mayor;
        }
        monticulo[tamano] = dato;
        subir(tamano);
        tamano++;
    }

    public T extraer() {
        if (tamano == 0) {
            throw new NoSuchElementException("La cola de prioridad está vacía");
        }
        T raiz = elemento(0);
        tamano--;
        monticulo[0] = monticulo[tamano];
        monticulo[tamano] = null;
        if (tamano > 0) {
            bajar(0);
        }
        return raiz;
    }

    public T ver() {
        if (tamano == 0) {
            throw new NoSuchElementException("La cola de prioridad está vacía");
        }
        return elemento(0);
    }

    public boolean estaVacia() {
        return tamano == 0;
    }

    public int tamano() {
        return tamano;
    }

    public void limpiar() {
        for (int i = 0; i < tamano; i++) {
            monticulo[i] = null;
        }
        tamano = 0;
    }

    private void subir(int indice) {
        while (indice > 0) {
            int padre = (indice - 1) / 2;
            if (comparador.compare(elemento(indice), elemento(padre)) <= 0) {
                return;
            }
            intercambiar(indice, padre);
            indice = padre;
        }
    }

    private void bajar(int indice) {
        while (true) {
            int izquierdo = 2 * indice + 1;
            int derecho = izquierdo + 1;
            int mayor = indice;
            if (izquierdo < tamano && comparador.compare(elemento(izquierdo), elemento(mayor)) > 0) {
                mayor = izquierdo;
            }
            if (derecho < tamano && comparador.compare(elemento(derecho), elemento(mayor)) > 0) {
                mayor = derecho;
            }
            if (mayor == indice) {
                return;
            }
            intercambiar(indice, mayor);
            indice = mayor;
        }
    }

    private void intercambiar(int a, int b) {
        Object temporal = monticulo[a];
        monticulo[a] = monticulo[b];
        monticulo[b] = temporal;
    }

    @SuppressWarnings("unchecked")
    private T elemento(int indice) {
        return (T) monticulo[indice];
    }
}
