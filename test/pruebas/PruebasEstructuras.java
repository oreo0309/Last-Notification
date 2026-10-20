package pruebas;

import estructuras.ArbolBinarioBusqueda;
import estructuras.Cola;
import estructuras.ColaPrioridad;
import estructuras.Grafo;
import estructuras.ListaEnlazada;
import estructuras.Pila;
import estructuras.TablaHash;
import java.util.NoSuchElementException;
import java.util.Random;

public final class PruebasEstructuras {

    private PruebasEstructuras() {
    }

    public static void ejecutar() {
        lista();
        cola();
        pila();
        colaPrioridad();
        tablaHash();
        grafo();
        arbol();
    }

    private static void lista() {
        Prueba.grupo("ListaEnlazada");
        ListaEnlazada<Integer> l = new ListaEnlazada<>();
        Prueba.verdadero("empieza vacía", l.estaVacia());
        for (int i = 1; i <= 5; i++) {
            l.agregar(i);
        }
        l.agregarAlInicio(0);
        Prueba.igual("tamaño tras 6 inserciones", 6, l.tamano());
        Prueba.igual("primero", 0, l.primero());
        Prueba.igual("último", 5, l.ultimo());
        Prueba.igual("obtener(3)", 3, l.obtener(3));
        Prueba.verdadero("eliminar valor existente", l.eliminar(3));
        Prueba.verdadero("ya no contiene el 3", !l.contiene(3));
        Prueba.igual("eliminarEn(0) devuelve 0", 0, l.eliminarEn(0));
        Prueba.igual("eliminarSi pares", 2, l.eliminarSi(x -> x % 2 == 0));
        Prueba.igual("contenido final", "[1, 5]", l.toString());
        Prueba.lanza("índice fuera de rango", () -> l.obtener(10), IndexOutOfBoundsException.class);
        ListaEnlazada<Integer> desordenada = new ListaEnlazada<>();
        Random r = new Random(7);
        for (int i = 0; i < 200; i++) {
            desordenada.agregar(r.nextInt(1000));
        }
        desordenada.ordenar(Integer::compare);
        boolean ordenada = true;
        int anterior = Integer.MIN_VALUE;
        for (int v : desordenada) {
            ordenada &= v >= anterior;
            anterior = v;
        }
        Prueba.verdadero("ordenar por mezcla deja 200 elementos en orden", ordenada && desordenada.tamano() == 200);
        Prueba.igual("último tras ordenar es el máximo", desordenada.obtener(199), desordenada.ultimo());
    }

    private static void cola() {
        Prueba.grupo("Cola");
        Cola<String> c = new Cola<>();
        c.encolar("a");
        c.encolar("b");
        c.encolar("c");
        Prueba.igual("FIFO: sale primero 'a'", "a", c.desencolar());
        Prueba.igual("frente es 'b'", "b", c.frente());
        Prueba.igual("tamaño 2", 2, c.tamano());
        c.desencolar();
        c.desencolar();
        Prueba.verdadero("queda vacía", c.estaVacia());
        Prueba.lanza("desencolar vacía lanza excepción", c::desencolar, NoSuchElementException.class);
    }

    private static void pila() {
        Prueba.grupo("Pila");
        Pila<Integer> p = new Pila<>();
        p.apilar(1);
        p.apilar(2);
        p.apilar(3);
        Prueba.igual("LIFO: sale primero 3", 3, p.desapilar());
        Prueba.igual("cima es 2", 2, p.cima());
        Prueba.igual("tamaño 2", 2, p.tamano());
        p.limpiar();
        Prueba.lanza("desapilar vacía lanza excepción", p::desapilar, NoSuchElementException.class);
    }

    private static void colaPrioridad() {
        Prueba.grupo("ColaPrioridad (montículo)");
        ColaPrioridad<Integer> cp = new ColaPrioridad<>(Integer::compare);
        int[] valores = {5, 1, 9, 3, 7, 9, 2, 8, 4, 6, 0};
        for (int v : valores) {
            cp.insertar(v);
        }
        Prueba.igual("ver() da el mayor", 9, cp.ver());
        boolean descendente = true;
        int anterior = Integer.MAX_VALUE;
        int extraidos = 0;
        while (!cp.estaVacia()) {
            int v = cp.extraer();
            descendente &= v <= anterior;
            anterior = v;
            extraidos++;
        }
        Prueba.verdadero("extrae en orden de prioridad", descendente);
        Prueba.igual("extrae todos los elementos", valores.length, extraidos);
        ColaPrioridad<Integer> grande = new ColaPrioridad<>((a, b) -> Integer.compare(b, a));
        for (int i = 1000; i > 0; i--) {
            grande.insertar(i);
        }
        Prueba.igual("con comparador invertido sale el menor", 1, grande.extraer());
        Prueba.igual("crece más allá de su capacidad inicial", 999, grande.tamano());
    }

    private static void tablaHash() {
        Prueba.grupo("TablaHash");
        TablaHash<String, Integer> t = new TablaHash<>(4);
        for (int i = 0; i < 100; i++) {
            t.poner("clave" + i, i);
        }
        Prueba.igual("guarda 100 pares", 100, t.tamano());
        Prueba.igual("obtiene clave57", 57, t.obtener("clave57"));
        Prueba.verdadero("redimensiona (capacidad > 100)", t.capacidad() > 100);
        t.poner("clave57", 570);
        Prueba.igual("reemplaza valor existente", 570, t.obtener("clave57"));
        Prueba.igual("no duplica la clave", 100, t.tamano());
        Prueba.igual("eliminar devuelve el valor", 570, t.eliminar("clave57"));
        Prueba.verdadero("ya no contiene clave57", !t.contieneClave("clave57"));
        Prueba.igual("valor por defecto", -1, t.obtenerOPorDefecto("nada", -1));
        Prueba.igual("claves() lista todas", 99, t.claves().tamano());
        Prueba.lanza("clave nula no permitida", () -> t.poner(null, 1), IllegalArgumentException.class);
    }

    private static void grafo() {
        Prueba.grupo("Grafo (BFS)");
        Grafo g = new Grafo(6);
        g.agregarArista(0, 1);
        g.agregarArista(1, 2);
        g.agregarArista(2, 3);
        g.agregarArista(0, 4);
        g.agregarArista(4, 3);
        Prueba.igual("aristas sin duplicados", 5, g.aristas());
        g.agregarArista(0, 1);
        Prueba.igual("agregar arista repetida no cambia el total", 5, g.aristas());
        Prueba.igual("camino más corto 0→3 tiene 3 vértices", 3, g.caminoMasCorto(0, 3).tamano());
        Prueba.igual("distancia BFS de 0 a 3", 2, g.distancias(0)[3]);
        Prueba.verdadero("vértice 5 aislado", !g.estanConectados(0, 5));
        Prueba.verdadero("camino a vértice aislado está vacío", g.caminoMasCorto(0, 5).estaVacia());
    }

    private static void arbol() {
        Prueba.grupo("ArbolBinarioBusqueda");
        ArbolBinarioBusqueda<Integer, String> a = new ArbolBinarioBusqueda<>();
        int[] puntajes = {500, 300, 800, 200, 400, 700, 900, 400};
        for (int p : puntajes) {
            a.insertar(p, "j" + p);
        }
        Prueba.igual("tamaño con duplicados", 8, a.tamano());
        Prueba.igual("buscar 700", "j700", a.buscar(700));
        Prueba.verdadero("buscar inexistente da null", a.buscar(123) == null);
        StringBuilder enOrden = new StringBuilder();
        for (ArbolBinarioBusqueda.Par<Integer, String> par : a.enOrden()) {
            enOrden.append(par.clave()).append(' ');
        }
        Prueba.igual("recorrido en orden", "200 300 400 400 500 700 800 900 ", enOrden.toString());
        StringBuilder mejores = new StringBuilder();
        for (ArbolBinarioBusqueda.Par<Integer, String> par : a.mayoresPrimero(3)) {
            mejores.append(par.clave()).append(' ');
        }
        Prueba.igual("top 3 de mayor a menor", "900 800 700 ", mejores.toString());
        Prueba.igual("altura del árbol", 4, a.altura());
    }
}
