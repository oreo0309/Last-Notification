package estructuras;

import java.util.NoSuchElementException;

public class Cola<T> {

    private static class Nodo<T> {
        final T dato;
        Nodo<T> siguiente;

        Nodo(T dato) {
            this.dato = dato;
        }
    }

    private Nodo<T> frente;
    private Nodo<T> fin;
    private int tamano;

    public void encolar(T dato) {
        Nodo<T> nuevo = new Nodo<>(dato);
        if (fin == null) {
            frente = nuevo;
        } else {
            fin.siguiente = nuevo;
        }
        fin = nuevo;
        tamano++;
    }

    public T desencolar() {
        if (frente == null) {
            throw new NoSuchElementException("La cola está vacía");
        }
        T dato = frente.dato;
        frente = frente.siguiente;
        if (frente == null) {
            fin = null;
        }
        tamano--;
        return dato;
    }

    public T frente() {
        if (frente == null) {
            throw new NoSuchElementException("La cola está vacía");
        }
        return frente.dato;
    }

    public boolean estaVacia() {
        return tamano == 0;
    }

    public int tamano() {
        return tamano;
    }

    public void limpiar() {
        frente = null;
        fin = null;
        tamano = 0;
    }
}
