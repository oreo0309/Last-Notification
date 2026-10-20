package estructuras;

public class TablaHash<K, V> {

    private static class Entrada<K, V> {
        final K clave;
        V valor;
        Entrada<K, V> siguiente;

        Entrada(K clave, V valor, Entrada<K, V> siguiente) {
            this.clave = clave;
            this.valor = valor;
            this.siguiente = siguiente;
        }
    }

    private static final double FACTOR_CARGA_MAXIMO = 0.75;

    private Entrada<K, V>[] cubetas;
    private int tamano;

    public TablaHash() {
        this(16);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public TablaHash(int capacidadInicial) {
        int capacidad = 1;
        while (capacidad < Math.max(4, capacidadInicial)) {
            capacidad <<= 1;
        }
        cubetas = (Entrada<K, V>[]) new Entrada[capacidad];
    }

    public void poner(K clave, V valor) {
        if (clave == null) {
            throw new IllegalArgumentException("La clave no puede ser nula");
        }
        int indice = indice(clave, cubetas.length);
        for (Entrada<K, V> e = cubetas[indice]; e != null; e = e.siguiente) {
            if (e.clave.equals(clave)) {
                e.valor = valor;
                return;
            }
        }
        cubetas[indice] = new Entrada<>(clave, valor, cubetas[indice]);
        tamano++;
        if ((double) tamano / cubetas.length > FACTOR_CARGA_MAXIMO) {
            redimensionar();
        }
    }

    public V obtener(K clave) {
        if (clave == null) {
            return null;
        }
        for (Entrada<K, V> e = cubetas[indice(clave, cubetas.length)]; e != null; e = e.siguiente) {
            if (e.clave.equals(clave)) {
                return e.valor;
            }
        }
        return null;
    }

    public V obtenerOPorDefecto(K clave, V porDefecto) {
        V valor = obtener(clave);
        return valor != null ? valor : porDefecto;
    }

    public boolean contieneClave(K clave) {
        return obtener(clave) != null;
    }

    public V eliminar(K clave) {
        int indice = indice(clave, cubetas.length);
        Entrada<K, V> anterior = null;
        for (Entrada<K, V> e = cubetas[indice]; e != null; e = e.siguiente) {
            if (e.clave.equals(clave)) {
                if (anterior == null) {
                    cubetas[indice] = e.siguiente;
                } else {
                    anterior.siguiente = e.siguiente;
                }
                tamano--;
                return e.valor;
            }
            anterior = e;
        }
        return null;
    }

    public ListaEnlazada<K> claves() {
        ListaEnlazada<K> claves = new ListaEnlazada<>();
        for (Entrada<K, V> cubeta : cubetas) {
            for (Entrada<K, V> e = cubeta; e != null; e = e.siguiente) {
                claves.agregar(e.clave);
            }
        }
        return claves;
    }

    public int tamano() {
        return tamano;
    }

    public boolean estaVacia() {
        return tamano == 0;
    }

    public int capacidad() {
        return cubetas.length;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void redimensionar() {
        Entrada<K, V>[] nuevas = (Entrada<K, V>[]) new Entrada[cubetas.length * 2];
        for (Entrada<K, V> cubeta : cubetas) {
            Entrada<K, V> e = cubeta;
            while (e != null) {
                Entrada<K, V> siguiente = e.siguiente;
                int indice = indice(e.clave, nuevas.length);
                e.siguiente = nuevas[indice];
                nuevas[indice] = e;
                e = siguiente;
            }
        }
        cubetas = nuevas;
    }

    private int indice(Object clave, int capacidad) {
        int h = clave.hashCode();
        h ^= (h >>> 16);
        return h & (capacidad - 1);
    }
}
