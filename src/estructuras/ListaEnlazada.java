package estructuras;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

public class ListaEnlazada<T> implements Iterable<T> {

    private static class Nodo<T> {
        T dato;
        Nodo<T> anterior;
        Nodo<T> siguiente;

        Nodo(T dato) {
            this.dato = dato;
        }
    }

    private Nodo<T> cabeza;
    private Nodo<T> cola;
    private int tamano;

    public void agregar(T dato) {
        Nodo<T> nuevo = new Nodo<>(dato);
        if (cola == null) {
            cabeza = nuevo;
            cola = nuevo;
        } else {
            cola.siguiente = nuevo;
            nuevo.anterior = cola;
            cola = nuevo;
        }
        tamano++;
    }

    public void agregarAlInicio(T dato) {
        Nodo<T> nuevo = new Nodo<>(dato);
        if (cabeza == null) {
            cabeza = nuevo;
            cola = nuevo;
        } else {
            nuevo.siguiente = cabeza;
            cabeza.anterior = nuevo;
            cabeza = nuevo;
        }
        tamano++;
    }

    public T obtener(int indice) {
        return nodoEn(indice).dato;
    }

    public T primero() {
        if (cabeza == null) {
            throw new NoSuchElementException("La lista está vacía");
        }
        return cabeza.dato;
    }

    public T ultimo() {
        if (cola == null) {
            throw new NoSuchElementException("La lista está vacía");
        }
        return cola.dato;
    }

    public T eliminarEn(int indice) {
        Nodo<T> nodo = nodoEn(indice);
        desenlazar(nodo);
        return nodo.dato;
    }

    public T eliminarPrimero() {
        if (cabeza == null) {
            throw new NoSuchElementException("La lista está vacía");
        }
        T dato = cabeza.dato;
        desenlazar(cabeza);
        return dato;
    }

    public boolean eliminar(T dato) {
        for (Nodo<T> actual = cabeza; actual != null; actual = actual.siguiente) {
            if (iguales(actual.dato, dato)) {
                desenlazar(actual);
                return true;
            }
        }
        return false;
    }

    public int eliminarSi(Predicate<T> condicion) {
        int eliminados = 0;
        Nodo<T> actual = cabeza;
        while (actual != null) {
            Nodo<T> siguiente = actual.siguiente;
            if (condicion.test(actual.dato)) {
                desenlazar(actual);
                eliminados++;
            }
            actual = siguiente;
        }
        return eliminados;
    }

    public boolean contiene(T dato) {
        return indiceDe(dato) >= 0;
    }

    public int indiceDe(T dato) {
        int indice = 0;
        for (Nodo<T> actual = cabeza; actual != null; actual = actual.siguiente) {
            if (iguales(actual.dato, dato)) {
                return indice;
            }
            indice++;
        }
        return -1;
    }

    public T buscar(Predicate<T> condicion) {
        for (Nodo<T> actual = cabeza; actual != null; actual = actual.siguiente) {
            if (condicion.test(actual.dato)) {
                return actual.dato;
            }
        }
        return null;
    }

    public int contar(Predicate<T> condicion) {
        int total = 0;
        for (Nodo<T> actual = cabeza; actual != null; actual = actual.siguiente) {
            if (condicion.test(actual.dato)) {
                total++;
            }
        }
        return total;
    }

    public void ordenar(Comparator<? super T> comparador) {
        cabeza = ordenarPorMezcla(cabeza, comparador);
        Nodo<T> anterior = null;
        Nodo<T> actual = cabeza;
        while (actual != null) {
            actual.anterior = anterior;
            anterior = actual;
            actual = actual.siguiente;
        }
        cola = anterior;
    }

    private Nodo<T> ordenarPorMezcla(Nodo<T> inicio, Comparator<? super T> comparador) {
        if (inicio == null || inicio.siguiente == null) {
            return inicio;
        }
        Nodo<T> lento = inicio;
        Nodo<T> rapido = inicio.siguiente;
        while (rapido != null && rapido.siguiente != null) {
            lento = lento.siguiente;
            rapido = rapido.siguiente.siguiente;
        }
        Nodo<T> mitad = lento.siguiente;
        lento.siguiente = null;
        Nodo<T> izquierda = ordenarPorMezcla(inicio, comparador);
        Nodo<T> derecha = ordenarPorMezcla(mitad, comparador);
        return mezclar(izquierda, derecha, comparador);
    }

    private Nodo<T> mezclar(Nodo<T> a, Nodo<T> b, Comparator<? super T> comparador) {
        Nodo<T> centinela = new Nodo<>(null);
        Nodo<T> fin = centinela;
        while (a != null && b != null) {
            if (comparador.compare(a.dato, b.dato) <= 0) {
                fin.siguiente = a;
                a = a.siguiente;
            } else {
                fin.siguiente = b;
                b = b.siguiente;
            }
            fin = fin.siguiente;
        }
        fin.siguiente = (a != null) ? a : b;
        return centinela.siguiente;
    }

    public void limpiar() {
        cabeza = null;
        cola = null;
        tamano = 0;
    }

    public int tamano() {
        return tamano;
    }

    public boolean estaVacia() {
        return tamano == 0;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            private Nodo<T> actual = cabeza;
            private Nodo<T> ultimoDevuelto;

            @Override
            public boolean hasNext() {
                return actual != null;
            }

            @Override
            public T next() {
                if (actual == null) {
                    throw new NoSuchElementException();
                }
                ultimoDevuelto = actual;
                actual = actual.siguiente;
                return ultimoDevuelto.dato;
            }

            @Override
            public void remove() {
                if (ultimoDevuelto == null) {
                    throw new IllegalStateException();
                }
                desenlazar(ultimoDevuelto);
                ultimoDevuelto = null;
            }
        };
    }

    private Nodo<T> nodoEn(int indice) {
        if (indice < 0 || indice >= tamano) {
            throw new IndexOutOfBoundsException("Índice " + indice + " fuera de rango (tamaño " + tamano + ")");
        }
        Nodo<T> actual;
        if (indice < tamano / 2) {
            actual = cabeza;
            for (int i = 0; i < indice; i++) {
                actual = actual.siguiente;
            }
        } else {
            actual = cola;
            for (int i = tamano - 1; i > indice; i--) {
                actual = actual.anterior;
            }
        }
        return actual;
    }

    private void desenlazar(Nodo<T> nodo) {
        if (nodo.anterior == null) {
            cabeza = nodo.siguiente;
        } else {
            nodo.anterior.siguiente = nodo.siguiente;
        }
        if (nodo.siguiente == null) {
            cola = nodo.anterior;
        } else {
            nodo.siguiente.anterior = nodo.anterior;
        }
        nodo.anterior = null;
        nodo.siguiente = null;
        tamano--;
    }

    private boolean iguales(T a, T b) {
        return a == null ? b == null : a.equals(b);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (Nodo<T> actual = cabeza; actual != null; actual = actual.siguiente) {
            sb.append(actual.dato);
            if (actual.siguiente != null) {
                sb.append(", ");
            }
        }
        return sb.append("]").toString();
    }
}
