package estructuras;

public class Grafo {

    private final Object[] adyacencia;
    private int aristas;

    public Grafo(int vertices) {
        adyacencia = new Object[vertices];
        for (int i = 0; i < vertices; i++) {
            adyacencia[i] = new ListaEnlazada<Integer>();
        }
    }

    @SuppressWarnings("unchecked")
    private ListaEnlazada<Integer> lista(int vertice) {
        return (ListaEnlazada<Integer>) adyacencia[vertice];
    }

    public void agregarArista(int a, int b) {
        validar(a);
        validar(b);
        if (a == b || lista(a).contiene(b)) {
            return;
        }
        lista(a).agregar(b);
        lista(b).agregar(a);
        aristas++;
    }

    public ListaEnlazada<Integer> vecinos(int vertice) {
        validar(vertice);
        return lista(vertice);
    }

    public int[] distancias(int origen) {
        validar(origen);
        int[] distancia = new int[adyacencia.length];
        llenar(distancia, -1);
        Cola<Integer> pendientes = new Cola<>();
        distancia[origen] = 0;
        pendientes.encolar(origen);
        while (!pendientes.estaVacia()) {
            int actual = pendientes.desencolar();
            for (int vecino : lista(actual)) {
                if (distancia[vecino] < 0) {
                    distancia[vecino] = distancia[actual] + 1;
                    pendientes.encolar(vecino);
                }
            }
        }
        return distancia;
    }

    public ListaEnlazada<Integer> caminoMasCorto(int origen, int destino) {
        validar(origen);
        validar(destino);
        ListaEnlazada<Integer> camino = new ListaEnlazada<>();
        int[] previo = new int[adyacencia.length];
        llenar(previo, -2);
        Cola<Integer> pendientes = new Cola<>();
        previo[origen] = -1;
        pendientes.encolar(origen);
        while (!pendientes.estaVacia()) {
            int actual = pendientes.desencolar();
            if (actual == destino) {
                break;
            }
            for (int vecino : lista(actual)) {
                if (previo[vecino] == -2) {
                    previo[vecino] = actual;
                    pendientes.encolar(vecino);
                }
            }
        }
        if (previo[destino] == -2) {
            return camino;
        }
        for (int v = destino; v != -1; v = previo[v]) {
            camino.agregarAlInicio(v);
        }
        return camino;
    }

    public boolean estanConectados(int a, int b) {
        return distancias(a)[b] >= 0;
    }

    public int vertices() {
        return adyacencia.length;
    }

    public int aristas() {
        return aristas;
    }

    private static void llenar(int[] arreglo, int valor) {
        for (int i = 0; i < arreglo.length; i++) {
            arreglo[i] = valor;
        }
    }

    private void validar(int vertice) {
        if (vertice < 0 || vertice >= adyacencia.length) {
            throw new IndexOutOfBoundsException("Vértice inválido: " + vertice);
        }
    }
}
