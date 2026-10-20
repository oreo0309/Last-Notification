package estructuras;

public class Grafo {

    private final ListaEnlazada<Integer>[] adyacencia;
    private int aristas;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public Grafo(int vertices) {
        adyacencia = (ListaEnlazada<Integer>[]) new ListaEnlazada[vertices];
        for (int i = 0; i < vertices; i++) {
            adyacencia[i] = new ListaEnlazada<>();
        }
    }

    public void agregarArista(int a, int b) {
        validar(a);
        validar(b);
        if (a == b || adyacencia[a].contiene(b)) {
            return;
        }
        adyacencia[a].agregar(b);
        adyacencia[b].agregar(a);
        aristas++;
    }

    public ListaEnlazada<Integer> vecinos(int vertice) {
        validar(vertice);
        return adyacencia[vertice];
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
            for (int vecino : adyacencia[actual]) {
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
            for (int vecino : adyacencia[actual]) {
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
