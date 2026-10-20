package estructuras;

import java.util.NoSuchElementException;

public class Pila<T> {

    private static class Nodo<T> {
        final T dato;
        final Nodo<T> debajo;

        Nodo(T dato, Nodo<T> debajo) {
            this.dato = dato;
            this.debajo = debajo;
        }
    }

    private Nodo<T> cima;
    private int tamano;

    public void apilar(T dato) {
        cima = new Nodo<>(dato, cima);
        tamano++;
    }

    public T desapilar() {
        if (cima == null) {
            throw new NoSuchElementException("La pila está vacía");
        }
        T dato = cima.dato;
        cima = cima.debajo;
        tamano--;
        return dato;
    }

    public T cima() {
        if (cima == null) {
            throw new NoSuchElementException("La pila está vacía");
        }
        return cima.dato;
    }

    public boolean estaVacia() {
        return tamano == 0;
    }

    public int tamano() {
        return tamano;
    }

    public void limpiar() {
        cima = null;
        tamano = 0;
    }
}
