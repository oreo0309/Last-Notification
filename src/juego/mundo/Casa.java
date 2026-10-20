package juego.mundo;

import estructuras.Cola;
import estructuras.Grafo;
import estructuras.ListaEnlazada;
import estructuras.TablaHash;
import juego.motor.MapaRender;
import juego.motor.Textura;

public final class Casa implements MapaRender {

    public static final String[] PLANO = {
        "###VV###V####E######",
        "#Cn..ee#.#bb.l######",
        "#c.....#.#....######",
        "H......#.D....######",
        "A......D.#....######",
        "A......#.###########",
        "#......#.#..Cn.#####",
        "########.#..c..#####",
        "########.X.....A####",
        "#######R.#.....#####",
        "########.#.....#####",
        "########.###########",
        "####T###a#L#########",
        "#............#.....#",
        "V...t........o.....N",
        "#..sss.......o.....G",
        "#m...........#kkkk.#",
        "######F#########V###"
    };

    private final int ancho;
    private final int alto;
    private final char[][] celdas;
    private final int[] habitacion;
    private final int[] duenoEspecial;
    private final TablaHash<String, Textura> texturas;
    private final TablaHash<Character, Textura> especiales = new TablaHash<>();
    private final TablaHash<Integer, Puerta> puertas = new TablaHash<>();
    private final ListaEnlazada<Interactuable> interactuables = new ListaEnlazada<>();
    private final Textura[] murosHabitacion = new Textura[Habitacion.values().length];
    private final Textura[] pisosHabitacion = new Textura[Habitacion.values().length];
    private final Textura techo;
    private final Textura techoMancha;
    private Grafo navegacion;

    public Casa(TablaHash<String, Textura> texturas) {
        this.texturas = texturas;
        this.alto = PLANO.length;
        this.ancho = PLANO[0].length();
        this.celdas = new char[ancho][alto];
        for (int y = 0; y < alto; y++) {
            for (int x = 0; x < ancho; x++) {
                celdas[x][y] = PLANO[y].charAt(x);
            }
        }
        for (Habitacion h : Habitacion.values()) {
            murosHabitacion[h.ordinal()] = texturas.obtener(h.muro());
            pisosHabitacion[h.ordinal()] = texturas.obtener(h.piso());
        }
        techo = texturas.obtener("techo");
        techoMancha = texturas.obtener("techo_mancha");
        especiales.poner('V', texturas.obtener("ventana"));
        especiales.poner('A', texturas.obtener("armario"));
        especiales.poner('H', texturas.obtener("poster"));
        especiales.poner('L', texturas.obtener("librero"));
        especiales.poner('T', texturas.obtener("tv"));
        especiales.poner('N', texturas.obtener("nevera"));
        especiales.poner('R', texturas.obtener("cuadro"));
        especiales.poner('E', texturas.obtener("espejo"));
        especiales.poner('G', texturas.obtener("gabinete"));
        especiales.poner('F', texturas.obtener("puerta_principal"));
        habitacion = new int[ancho * alto];
        duenoEspecial = new int[ancho * alto];
        asignarHabitaciones();
        crearPuertas();
        crearInteractuables();
        reconstruirNavegacion();
    }

    private void asignarHabitaciones() {
        for (int i = 0; i < habitacion.length; i++) {
            habitacion[i] = -1;
        }
        inundar(3, 3, Habitacion.CUARTO);
        inundar(8, 6, Habitacion.PASILLO);
        inundar(11, 3, Habitacion.BANO);
        inundar(11, 8, Habitacion.PADRES);
        inundar(6, 14, Habitacion.SALA);
        inundar(16, 14, Habitacion.COCINA);
        for (int y = 0; y < alto; y++) {
            for (int x = 0; x < ancho; x++) {
                char c = celdas[x][y];
                if (c == 'a') {
                    habitacion[indice(x, y)] = Habitacion.PASILLO.ordinal();
                } else if (c == 'o') {
                    habitacion[indice(x, y)] = Habitacion.COCINA.ordinal();
                } else if (c == 'D' || c == 'X') {
                    habitacion[indice(x, y)] = Habitacion.PASILLO.ordinal();
                }
            }
        }
        for (int y = 0; y < alto; y++) {
            for (int x = 0; x < ancho; x++) {
                duenoEspecial[indice(x, y)] = -1;
                if (!esEspecial(celdas[x][y])) {
                    continue;
                }
                int[][] vecinos = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
                for (int[] v : vecinos) {
                    int nx = x + v[0];
                    int ny = y + v[1];
                    if (dentro(nx, ny) && habitacion[indice(nx, ny)] >= 0 && !esPuertaChar(celdas[nx][ny])) {
                        duenoEspecial[indice(x, y)] = habitacion[indice(nx, ny)];
                        break;
                    }
                }
            }
        }
    }

    private void inundar(int sx, int sy, Habitacion h) {
        Cola<Integer> pendientes = new Cola<>();
        pendientes.encolar(indice(sx, sy));
        habitacion[indice(sx, sy)] = h.ordinal();
        while (!pendientes.estaVacia()) {
            int i = pendientes.desencolar();
            int x = i % ancho;
            int y = i / ancho;
            int[][] vecinos = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
            for (int[] v : vecinos) {
                int nx = x + v[0];
                int ny = y + v[1];
                if (!dentro(nx, ny)) {
                    continue;
                }
                int j = indice(nx, ny);
                char c = celdas[nx][ny];
                if (habitacion[j] >= 0 || esMuroChar(c) || esPuertaChar(c) || c == 'a' || c == 'o') {
                    continue;
                }
                habitacion[j] = h.ordinal();
                pendientes.encolar(j);
            }
        }
    }

    private void crearPuertas() {
        for (int y = 0; y < alto; y++) {
            for (int x = 0; x < ancho; x++) {
                char c = celdas[x][y];
                if (!esPuertaChar(c)) {
                    continue;
                }
                boolean vertical = dentro(x - 1, y) && dentro(x + 1, y)
                        && !esMuroChar(celdas[x - 1][y]) && !esMuroChar(celdas[x + 1][y]);
                Puerta p = new Puerta(x, y, vertical, c == 'X' ? "puerta_cerrada" : "puerta", c == 'X');
                puertas.poner(indice(x, y), p);
                interactuables.agregar(new Interactuable(Interactuable.Tipo.PUERTA, x + 0.5, y + 0.5, p));
            }
        }
    }

    private void crearInteractuables() {
        agregar(Interactuable.Tipo.CAMA, 1.5, 1.6);
        agregar(Interactuable.Tipo.CAMA, 1.5, 2.5);
        agregar(Interactuable.Tipo.ESCRITORIO, 5.5, 1.5);
        agregar(Interactuable.Tipo.ESCRITORIO, 6.5, 1.5);
        agregar(Interactuable.Tipo.LAMPARA, 2.5, 1.5);
        agregar(Interactuable.Tipo.VENTANA, 4.0, 1.0);
        agregar(Interactuable.Tipo.ARMARIO, 1.0, 5.0);
        agregar(Interactuable.Tipo.VENTANA, 8.5, 1.0);
        agregar(Interactuable.Tipo.CUADRO, 8.0, 9.5);
        agregar(Interactuable.Tipo.ESPEJO, 13.5, 1.6);
        agregar(Interactuable.Tipo.TV, 4.5, 13.0);
        agregar(Interactuable.Tipo.LIBRERO, 10.5, 13.0);
        agregar(Interactuable.Tipo.VENTANA, 1.0, 14.5);
        agregar(Interactuable.Tipo.ROUTER, 1.5, 16.4);
        agregar(Interactuable.Tipo.PUERTA_PRINCIPAL, 6.5, 17.0);
        agregar(Interactuable.Tipo.NEVERA, 19.0, 14.5);
    }

    private void agregar(Interactuable.Tipo tipo, double x, double y) {
        interactuables.agregar(new Interactuable(tipo, x, y));
    }

    public final void reconstruirNavegacion() {
        navegacion = new Grafo(ancho * alto);
        for (int y = 0; y < alto; y++) {
            for (int x = 0; x < ancho; x++) {
                if (!navegable(x, y)) {
                    continue;
                }
                if (navegable(x + 1, y)) {
                    navegacion.agregarArista(indice(x, y), indice(x + 1, y));
                }
                if (navegable(x, y + 1)) {
                    navegacion.agregarArista(indice(x, y), indice(x, y + 1));
                }
            }
        }
    }

    private boolean navegable(int x, int y) {
        if (!dentro(x, y)) {
            return false;
        }
        char c = celdas[x][y];
        if (esPuertaChar(c)) {
            Puerta p = puertas.obtener(indice(x, y));
            return p != null && !p.bloqueada();
        }
        return alturaChar(c) == 0;
    }

    public Grafo navegacion() {
        return navegacion;
    }

    public int indice(int x, int y) {
        return y * ancho + x;
    }

    public boolean dentro(int x, int y) {
        return x >= 0 && y >= 0 && x < ancho && y < alto;
    }

    private static boolean esPuertaChar(char c) {
        return c == 'D' || c == 'X';
    }

    private static boolean esEspecial(char c) {
        return "VAHLTNREGF".indexOf(c) >= 0;
    }

    private static boolean esMuroChar(char c) {
        return c == '#' || esEspecial(c);
    }

    private static double alturaChar(char c) {
        switch (c) {
            case '.':
            case 'a':
            case 'o':
                return 0;
            case 'C':
            case 'c':
                return 0.22;
            case 'n':
                return 0.24;
            case 'e':
                return 0.30;
            case 's':
                return 0.18;
            case 't':
                return 0.16;
            case 'm':
                return 0.28;
            case 'k':
                return 0.36;
            case 'b':
                return 0.22;
            case 'l':
                return 0.34;
            default:
                return 1.0;
        }
    }

    public char celda(int x, int y) {
        return dentro(x, y) ? celdas[x][y] : '#';
    }

    public Puerta puerta(int x, int y) {
        return dentro(x, y) ? puertas.obtener(indice(x, y)) : null;
    }

    public Puerta puertaEn(char tipo) {
        for (int y = 0; y < alto; y++) {
            for (int x = 0; x < ancho; x++) {
                if (celdas[x][y] == tipo) {
                    return puerta(x, y);
                }
            }
        }
        return null;
    }

    public ListaEnlazada<Puerta> puertas() {
        ListaEnlazada<Puerta> lista = new ListaEnlazada<>();
        for (Integer clave : puertas.claves()) {
            lista.agregar(puertas.obtener(clave));
        }
        return lista;
    }

    public ListaEnlazada<Interactuable> interactuables() {
        return interactuables;
    }

    public Habitacion habitacionEn(double x, double y) {
        int cx = (int) Math.floor(x);
        int cy = (int) Math.floor(y);
        if (!dentro(cx, cy)) {
            return null;
        }
        int h = habitacion[indice(cx, cy)];
        return h < 0 ? null : Habitacion.values()[h];
    }

    public void cambiarEspecial(char tipo, String claveTextura) {
        Textura t = texturas.obtener(claveTextura);
        if (t != null) {
            especiales.poner(tipo, t);
        }
    }

    public Textura especial(char tipo) {
        return especiales.obtener(tipo);
    }

    public boolean solido(int x, int y) {
        if (!dentro(x, y)) {
            return true;
        }
        char c = celdas[x][y];
        if (esPuertaChar(c)) {
            Puerta p = puertas.obtener(indice(x, y));
            return p == null || !p.transitable();
        }
        return alturaChar(c) > 0;
    }

    public boolean lineaDeVision(double x0, double y0, double x1, double y1) {
        double dx = x1 - x0;
        double dy = y1 - y0;
        double d = Math.sqrt(dx * dx + dy * dy);
        int pasos = (int) Math.ceil(d / 0.1);
        for (int i = 1; i < pasos; i++) {
            double t = (double) i / pasos;
            int cx = (int) Math.floor(x0 + dx * t);
            int cy = (int) Math.floor(y0 + dy * t);
            if (bloqueaLuz(cx, cy)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int ancho() {
        return ancho;
    }

    @Override
    public int alto() {
        return alto;
    }

    @Override
    public double altura(int x, int y) {
        if (!dentro(x, y)) {
            return 1.0;
        }
        return alturaChar(celdas[x][y]);
    }

    @Override
    public boolean esPuerta(int x, int y) {
        return dentro(x, y) && esPuertaChar(celdas[x][y]);
    }

    @Override
    public double aperturaPuerta(int x, int y) {
        Puerta p = puerta(x, y);
        return p == null ? 0 : p.apertura();
    }

    @Override
    public boolean puertaVertical(int x, int y) {
        Puerta p = puerta(x, y);
        return p != null && p.vertical();
    }

    @Override
    public Textura texturaPuerta(int x, int y) {
        Puerta p = puerta(x, y);
        return texturas.obtener(p == null ? "puerta" : p.textura());
    }

    @Override
    public Textura texturaMuro(int x, int y, int desdeX, int desdeY) {
        int hab = dentro(desdeX, desdeY) ? habitacion[indice(desdeX, desdeY)] : -1;
        if (hab < 0) {
            hab = Habitacion.PASILLO.ordinal();
        }
        if (dentro(x, y)) {
            char c = celdas[x][y];
            if (esEspecial(c) && duenoEspecial[indice(x, y)] == hab) {
                Textura t = especiales.obtener(c);
                if (t != null) {
                    return t;
                }
            }
        }
        return murosHabitacion[hab];
    }

    @Override
    public Textura texturaLado(int x, int y) {
        switch (celda(x, y)) {
            case 'C':
            case 'c':
                return texturas.obtener("cama_lado");
            case 'n':
                return texturas.obtener("mesanoche_lado");
            case 'e':
                return texturas.obtener("escritorio_lado");
            case 's':
                return texturas.obtener("sofa_lado");
            case 't':
                return texturas.obtener("mesa_lado");
            case 'm':
                return texturas.obtener("mueble_lado");
            case 'k':
                return texturas.obtener("meson_lado");
            case 'b':
                return texturas.obtener("banera_lado");
            case 'l':
                return texturas.obtener("lavamanos_lado");
            default:
                return texturas.obtener("mesa_lado");
        }
    }

    @Override
    public Textura texturaTope(int x, int y) {
        switch (celda(x, y)) {
            case 'C':
                return texturas.obtener("cama_almohada");
            case 'c':
                return texturas.obtener("cama_tope");
            case 'n':
                return texturas.obtener("mesanoche_tope");
            case 'e':
                return texturas.obtener("escritorio_tope");
            case 's':
                return texturas.obtener("sofa_tope");
            case 't':
                return texturas.obtener("mesa_tope");
            case 'm':
                return texturas.obtener("mueble_tope");
            case 'k':
                return texturas.obtener("meson_tope");
            case 'b':
                return texturas.obtener("banera_tope");
            case 'l':
                return texturas.obtener("lavamanos_tope");
            default:
                return null;
        }
    }

    @Override
    public Textura texturaPiso(int x, int y) {
        if (!dentro(x, y)) {
            return null;
        }
        int h = habitacion[indice(x, y)];
        return pisosHabitacion[h < 0 ? Habitacion.PASILLO.ordinal() : h];
    }

    @Override
    public Textura texturaTecho(int x, int y) {
        int hash = (x * 73856093) ^ (y * 19349663);
        return Math.floorMod(hash, 7) == 0 ? techoMancha : techo;
    }

    @Override
    public boolean bloqueaLuz(int x, int y) {
        if (!dentro(x, y)) {
            return true;
        }
        char c = celdas[x][y];
        if (esPuertaChar(c)) {
            Puerta p = puertas.obtener(indice(x, y));
            return p == null || p.apertura() < 0.3;
        }
        return alturaChar(c) >= 1.0;
    }
}
