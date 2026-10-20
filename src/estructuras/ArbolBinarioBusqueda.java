package estructuras;

public class ArbolBinarioBusqueda<K extends Comparable<K>, V> {

    private static class Nodo<K, V> {
        final K clave;
        final V valor;
        Nodo<K, V> izquierdo;
        Nodo<K, V> derecho;

        Nodo(K clave, V valor) {
            this.clave = clave;
            this.valor = valor;
        }
    }

    public static class Par<K, V> {
        private final K clave;
        private final V valor;

        Par(K clave, V valor) {
            this.clave = clave;
            this.valor = valor;
        }

        public K clave() {
            return clave;
        }

        public V valor() {
            return valor;
        }
    }

    private Nodo<K, V> raiz;
    private int tamano;

    public void insertar(K clave, V valor) {
        if (clave == null) {
            throw new IllegalArgumentException("La clave no puede ser nula");
        }
        Nodo<K, V> nuevo = new Nodo<>(clave, valor);
        if (raiz == null) {
            raiz = nuevo;
        } else {
            Nodo<K, V> actual = raiz;
            while (true) {
                if (clave.compareTo(actual.clave) < 0) {
                    if (actual.izquierdo == null) {
                        actual.izquierdo = nuevo;
                        break;
                    }
                    actual = actual.izquierdo;
                } else {
                    if (actual.derecho == null) {
                        actual.derecho = nuevo;
                        break;
                    }
                    actual = actual.derecho;
                }
            }
        }
        tamano++;
    }

    public V buscar(K clave) {
        Nodo<K, V> actual = raiz;
        while (actual != null) {
            int comparacion = clave.compareTo(actual.clave);
            if (comparacion == 0) {
                return actual.valor;
            }
            actual = comparacion < 0 ? actual.izquierdo : actual.derecho;
        }
        return null;
    }

    public ListaEnlazada<Par<K, V>> enOrden() {
        ListaEnlazada<Par<K, V>> resultado = new ListaEnlazada<>();
        enOrden(raiz, resultado);
        return resultado;
    }

    public ListaEnlazada<Par<K, V>> mayoresPrimero(int limite) {
        ListaEnlazada<Par<K, V>> resultado = new ListaEnlazada<>();
        Pila<Nodo<K, V>> pila = new Pila<>();
        Nodo<K, V> actual = raiz;
        while ((actual != null || !pila.estaVacia()) && resultado.tamano() < limite) {
            while (actual != null) {
                pila.apilar(actual);
                actual = actual.derecho;
            }
            actual = pila.desapilar();
            resultado.agregar(new Par<>(actual.clave, actual.valor));
            actual = actual.izquierdo;
        }
        return resultado;
    }

    public int altura() {
        return altura(raiz);
    }

    public int tamano() {
        return tamano;
    }

    public boolean estaVacio() {
        return tamano == 0;
    }

    private void enOrden(Nodo<K, V> nodo, ListaEnlazada<Par<K, V>> resultado) {
        if (nodo == null) {
            return;
        }
        enOrden(nodo.izquierdo, resultado);
        resultado.agregar(new Par<>(nodo.clave, nodo.valor));
        enOrden(nodo.derecho, resultado);
    }

    private int altura(Nodo<K, V> nodo) {
        if (nodo == null) {
            return 0;
        }
        return 1 + Math.max(altura(nodo.izquierdo), altura(nodo.derecho));
    }
}
