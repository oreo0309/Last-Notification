package juego.motor;

import estructuras.TablaHash;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.Random;

public final class GeneradorTexturas {

    private static final int T = 64;

    private final Ruido ruido = new Ruido(1312);
    private final Random azar = new Random(312);

    public TablaHash<String, Textura> generarTodo() {
        TablaHash<String, Textura> banco = new TablaHash<>(128);
        banco.poner("papel_cuarto", papelCuarto());
        banco.poner("papel_pasillo", papelPasillo());
        banco.poner("azulejo_bano", azulejoBano());
        banco.poner("pared_sala", paredSala());
        banco.poner("azulejo_cocina", azulejoCocina());
        banco.poner("papel_padres", papelPadres());
        banco.poner("ventana", ventana(false));
        banco.poner("ventana_lluvia", ventana(true));
        banco.poner("puerta", puerta(new Color(92, 64, 42), false));
        banco.poner("puerta_cerrada", puerta(new Color(70, 48, 34), false));
        banco.poner("puerta_principal", puerta(new Color(52, 36, 28), true));
        banco.poner("librero", librero());
        banco.poner("tv", televisor());
        banco.poner("nevera", nevera());
        banco.poner("armario", armario(false));
        banco.poner("armario_abierto", armario(true));
        banco.poner("cuadro", cuadro(false));
        banco.poner("cuadro_perturbado", cuadro(true));
        banco.poner("poster", poster(false));
        banco.poner("poster_ojo", poster(true));
        banco.poner("espejo", espejo(false));
        banco.poner("espejo_figura", espejo(true));
        banco.poner("gabinete", gabinete());
        banco.poner("alfombra", alfombra());
        banco.poner("madera", madera(new Color(74, 52, 36)));
        banco.poner("piso_sala", madera(new Color(60, 44, 32)));
        banco.poner("baldosa_bano", baldosa(new Color(128, 134, 134), new Color(84, 88, 88), 16));
        banco.poner("baldosa_cocina", ajedrez());
        banco.poner("techo", techo(false));
        banco.poner("techo_mancha", techo(true));
        banco.poner("cama_lado", camaLado());
        banco.poner("cama_tope", camaTope(false));
        banco.poner("cama_almohada", camaTope(true));
        banco.poner("escritorio_lado", cajones(new Color(96, 70, 50), 2));
        banco.poner("escritorio_tope", tablero(new Color(104, 76, 54), true));
        banco.poner("mesanoche_lado", cajones(new Color(84, 60, 44), 2));
        banco.poner("mesanoche_tope", tablero(new Color(90, 64, 46), false));
        banco.poner("sofa_lado", sofaLado());
        banco.poner("sofa_tope", sofaTope());
        banco.poner("meson_lado", cajones(new Color(118, 108, 90), 1));
        banco.poner("meson_tope", granito());
        banco.poner("banera_lado", banera(false));
        banco.poner("banera_tope", banera(true));
        banco.poner("mesa_lado", cajones(new Color(70, 50, 36), 0));
        banco.poner("mesa_tope", tablero(new Color(76, 54, 38), false));
        banco.poner("lavamanos_lado", banera(false));
        banco.poner("lavamanos_tope", lavamanosTope());
        banco.poner("mueble_lado", cajones(new Color(64, 48, 40), 1));
        banco.poner("mueble_tope", tablero(new Color(66, 50, 40), false));
        banco.poner("lampara", lampara());
        banco.poner("lampara_pie", lamparaPie());
        banco.poner("reloj", reloj("23:00", false));
        banco.poner("planta", planta());
        banco.poner("silla", silla());
        banco.poner("router", router(0));
        banco.poner("inodoro", inodoro());
        banco.poner("ropa", ropa());
        banco.poner("peluche", peluche(false));
        banco.poner("peluche_mira", peluche(true));
        banco.poner("seguidor", seguidor());
        banco.poner("jugador", jugadorEspalda());
        banco.poner("telefono_mesa", telefonoMesa());
        return banco;
    }

    static int rgb(int r, int g, int b) {
        return 0xFF000000 | (limitar(r) << 16) | (limitar(g) << 8) | limitar(b);
    }

    static int rgb(Color c) {
        return rgb(c.getRed(), c.getGreen(), c.getBlue());
    }

    static int limitar(int v) {
        return v < 0 ? 0 : (v > 255 ? 255 : v);
    }

    static int variar(int color, int delta) {
        int r = ((color >> 16) & 255) + delta;
        int g = ((color >> 8) & 255) + delta;
        int b = (color & 255) + delta;
        return rgb(r, g, b);
    }

    static int mezclar(int a, int b, double t) {
        t = Math.max(0, Math.min(1, t));
        int r = (int) (((a >> 16) & 255) * (1 - t) + ((b >> 16) & 255) * t);
        int g = (int) (((a >> 8) & 255) * (1 - t) + ((b >> 8) & 255) * t);
        int bl = (int) ((a & 255) * (1 - t) + (b & 255) * t);
        return rgb(r, g, bl);
    }

    static int escalar(int color, double f) {
        return rgb((int) (((color >> 16) & 255) * f), (int) (((color >> 8) & 255) * f), (int) ((color & 255) * f));
    }

    private double n(double x, double y, double escala, int octavas) {
        int periodo = (int) Math.round(T / escala);
        return ruido.fbm(x / escala, y / escala, octavas, Math.max(1, periodo));
    }

    private void zocalo(Textura t, int desde) {
        for (int y = desde; y < T; y++) {
            for (int x = 0; x < T; x++) {
                int c = y == desde ? rgb(84, 64, 46) : rgb(40, 29, 22);
                t.ponerPixel(x, y, variar(c, (int) ((n(x, y * 4, 8, 2) - 0.5) * 14)));
            }
        }
    }

    private void manchaHumedad(Textura t, double umbral, int limiteY) {
        for (int y = 0; y < limiteY; y++) {
            for (int x = 0; x < T; x++) {
                double m = n(x + 17, y + 5, 32, 3);
                if (m > umbral) {
                    double k = (m - umbral) * 3.2 * (1 - (double) y / limiteY * 0.6);
                    t.ponerPixel(x, y, mezclar(t.pixel(x, y), rgb(64, 50, 34), k));
                }
            }
        }
    }

    private Textura papelCuarto() {
        Textura t = new Textura(T, T);
        for (int y = 0; y < T; y++) {
            for (int x = 0; x < T; x++) {
                boolean franja = (x / 8) % 2 == 0;
                int c = franja ? rgb(50, 66, 76) : rgb(42, 56, 66);
                int mx = x % 8;
                int my = y % 16;
                if (franja && Math.abs(mx - 4) + Math.abs(my - 8) <= 2) {
                    c = rgb(72, 90, 98);
                }
                c = variar(c, (int) ((n(x, y, 16, 3) - 0.5) * 20));
                t.ponerPixel(x, y, c);
            }
        }
        manchaHumedad(t, 0.6, 44);
        zocalo(t, 58);
        return t;
    }

    private Textura papelPasillo() {
        Textura t = new Textura(T, T);
        for (int y = 0; y < T; y++) {
            for (int x = 0; x < T; x++) {
                int c = rgb(96, 82, 56);
                int a = Math.floorMod(x + y, 16);
                int b = Math.floorMod(x - y, 16);
                if (a == 0 || b == 0) {
                    c = rgb(78, 66, 44);
                }
                int cx = Math.floorMod(x, 16) - 8;
                int cy = Math.floorMod(y + 8, 16) - 8;
                if (cx * cx + cy * cy <= 3) {
                    c = rgb(120, 104, 70);
                }
                double suciedad = (double) y / T;
                c = escalar(c, 1.0 - suciedad * 0.25);
                c = variar(c, (int) ((n(x, y, 8, 3) - 0.5) * 22));
                t.ponerPixel(x, y, c);
            }
        }
        manchaHumedad(t, 0.58, 50);
        zocalo(t, 58);
        return t;
    }

    private Textura papelPadres() {
        Textura t = new Textura(T, T);
        for (int y = 0; y < T; y++) {
            for (int x = 0; x < T; x++) {
                int c = rgb(88, 58, 62);
                int fx = Math.floorMod(x, 16) - 8;
                int fy = Math.floorMod(y + (x / 16) * 8, 16) - 8;
                int d = fx * fx + fy * fy;
                if (d >= 4 && d <= 9) {
                    c = rgb(112, 76, 80);
                } else if (d <= 1) {
                    c = rgb(130, 108, 70);
                }
                c = variar(c, (int) ((n(x, y, 16, 3) - 0.5) * 18));
                t.ponerPixel(x, y, c);
            }
        }
        zocalo(t, 58);
        return t;
    }

    private Textura azulejoBano() {
        Textura t = baldosa(new Color(148, 164, 158), new Color(86, 92, 88), 16);
        for (int y = 0; y < T; y++) {
            for (int x = 0; x < T; x++) {
                if (x % 16 == 1 && y % 16 > 1 && y % 16 < 6) {
                    t.ponerPixel(x, y, variar(t.pixel(x, y), 24));
                }
            }
        }
        manchaHumedad(t, 0.62, 64);
        return t;
    }

    private Textura azulejoCocina() {
        Textura t = baldosa(new Color(168, 156, 126), new Color(104, 96, 80), 8);
        manchaHumedad(t, 0.64, 64);
        return t;
    }

    private Textura baldosa(Color base, Color junta, int tam) {
        Textura t = new Textura(T, T);
        Random r = new Random(tam * 31L + base.getRGB());
        int[] variacion = new int[(T / tam) * (T / tam)];
        for (int i = 0; i < variacion.length; i++) {
            variacion[i] = r.nextInt(14) - 7;
        }
        for (int y = 0; y < T; y++) {
            for (int x = 0; x < T; x++) {
                boolean esJunta = x % tam == 0 || y % tam == 0;
                int c;
                if (esJunta) {
                    c = variar(rgb(junta), (int) ((n(x, y, 4, 2) - 0.5) * 30));
                } else {
                    int idx = (y / tam) * (T / tam) + (x / tam);
                    c = variar(rgb(base), variacion[idx] + (int) ((n(x, y, 16, 2) - 0.5) * 12));
                }
                t.ponerPixel(x, y, c);
            }
        }
        return t;
    }

    private Textura paredSala() {
        Textura t = new Textura(T, T);
        for (int y = 0; y < T; y++) {
            for (int x = 0; x < T; x++) {
                int c;
                if (y < 36) {
                    c = variar(rgb(80, 76, 72), (int) ((n(x, y, 16, 4) - 0.5) * 26));
                } else if (y < 39) {
                    c = y == 36 ? rgb(100, 78, 56) : rgb(58, 42, 30);
                } else {
                    int ranura = x % 16;
                    c = rgb(62, 44, 32);
                    if (ranura == 0) {
                        c = rgb(36, 26, 20);
                    } else if (ranura == 1) {
                        c = rgb(78, 58, 42);
                    }
                    c = variar(c, (int) ((n(x * 0.3, y * 3, 8, 2) - 0.5) * 16));
                }
                t.ponerPixel(x, y, c);
            }
        }
        manchaHumedad(t, 0.63, 36);
        return t;
    }

    private Textura ventana(boolean lluvia) {
        Textura t = new Textura(T, T);
        Random r = new Random(77);
        for (int y = 0; y < T; y++) {
            for (int x = 0; x < T; x++) {
                double k = (double) y / T;
                int cielo = mezclar(rgb(10, 14, 32), rgb(22, 28, 48), k);
                t.ponerPixel(x, y, cielo);
                t.ponerEmision(x, y, 34);
            }
        }
        for (int i = 0; i < 26; i++) {
            int sx = r.nextInt(T);
            int sy = r.nextInt(T / 2);
            t.ponerPixel(sx, sy, rgb(170, 175, 200));
            t.ponerEmision(sx, sy, 120);
        }
        for (int y = 0; y < T; y++) {
            for (int x = 0; x < T; x++) {
                double dx = x - 46;
                double dy = y - 16;
                double d = Math.sqrt(dx * dx + dy * dy);
                if (d < 7) {
                    double sombra = Math.sqrt((x - 49) * (x - 49) + (y - 14) * (y - 14));
                    if (sombra > 6) {
                        t.ponerPixel(x, y, rgb(196, 200, 186));
                        t.ponerEmision(x, y, 220);
                    }
                } else if (d < 11) {
                    t.ponerPixel(x, y, mezclar(t.pixel(x, y), rgb(60, 64, 80), (11 - d) / 6));
                    t.ponerEmision(x, y, 70);
                }
            }
        }
        dibujarRamas(t, r);
        if (lluvia) {
            for (int i = 0; i < 70; i++) {
                int sx = r.nextInt(T);
                int sy = r.nextInt(T);
                int largo = 2 + r.nextInt(4);
                for (int j = 0; j < largo; j++) {
                    int yy = (sy + j) % T;
                    t.ponerPixel(sx, yy, mezclar(t.pixel(sx, yy), rgb(120, 130, 150), 0.45));
                }
            }
        }
        for (int y = 0; y < T; y++) {
            for (int x = 0; x < T; x++) {
                boolean marco = x < 5 || x > 58 || y < 5 || y > 58;
                boolean parteluz = Math.abs(x - 32) <= 1 || Math.abs(y - 32) <= 1;
                if (marco || parteluz) {
                    int c = variar(rgb(58, 46, 36), (int) ((n(x, y * 3, 8, 2) - 0.5) * 18));
                    if (marco && (x == 4 || y == 4)) {
                        c = rgb(30, 22, 18);
                    }
                    t.ponerPixel(x, y, c);
                    t.ponerEmision(x, y, 0);
                }
            }
        }
        return t;
    }

    private void dibujarRamas(Textura t, Random r) {
        BufferedImage img = new BufferedImage(T, T, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(4, 5, 8));
        rama(g, 2, 60, -0.9, 30, 3.2f, r);
        rama(g, 62, 40, -2.4, 22, 2.4f, r);
        g.dispose();
        for (int y = 0; y < T; y++) {
            for (int x = 0; x < T; x++) {
                if ((img.getRGB(x, y) >>> 24) > 0) {
                    t.ponerPixel(x, y, rgb(6, 7, 10));
                    t.ponerEmision(x, y, 0);
                }
            }
        }
    }

    private void rama(Graphics2D g, double x, double y, double angulo, double largo, float grosor, Random r) {
        if (largo < 4 || grosor < 0.6f) {
            return;
        }
        double x2 = x + Math.cos(angulo) * largo;
        double y2 = y + Math.sin(angulo) * largo;
        g.setStroke(new BasicStroke(grosor, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine((int) x, (int) y, (int) x2, (int) y2);
        rama(g, x2, y2, angulo - 0.5 + r.nextDouble() * 0.3, largo * 0.68, grosor * 0.7f, r);
        rama(g, x2, y2, angulo + 0.4 + r.nextDouble() * 0.3, largo * 0.6, grosor * 0.65f, r);
    }

    private Textura puerta(Color madera, boolean principal) {
        Textura t = new Textura(T, T);
        int base = rgb(madera);
        for (int y = 0; y < T; y++) {
            for (int x = 0; x < T; x++) {
                double veta = n(x * 0.25, y * 2.0, 4, 3);
                int c = variar(base, (int) ((veta - 0.5) * 26));
                t.ponerPixel(x, y, c);
            }
        }
        int[][] paneles = principal
                ? new int[][]{{10, 8, 44, 18}, {10, 32, 44, 26}}
                : new int[][]{{10, 6, 18, 22}, {36, 6, 18, 22}, {10, 34, 18, 24}, {36, 34, 18, 24}};
        for (int[] p : paneles) {
            for (int y = p[1]; y < p[1] + p[3]; y++) {
                for (int x = p[0]; x < p[0] + p[2]; x++) {
                    int c = t.pixel(x, y);
                    if (x == p[0] || y == p[1]) {
                        c = escalar(c, 0.6);
                    } else if (x == p[0] + p[2] - 1 || y == p[1] + p[3] - 1) {
                        c = escalar(c, 1.3);
                    } else {
                        c = escalar(c, 0.88);
                    }
                    t.ponerPixel(x, y, c);
                }
            }
        }
        for (int y = 0; y < T; y++) {
            t.ponerPixel(0, y, rgb(24, 18, 14));
            t.ponerPixel(63, y, rgb(24, 18, 14));
        }
        int perillaX = 56;
        int perillaY = 33;
        for (int y = -2; y <= 2; y++) {
            for (int x = -2; x <= 2; x++) {
                if (x * x + y * y <= 4) {
                    t.ponerPixel(perillaX + x, perillaY + y, x + y < 0 ? rgb(200, 172, 90) : rgb(130, 104, 50));
                }
            }
        }
        if (principal) {
            for (int y = -1; y <= 1; y++) {
                for (int x = -1; x <= 1; x++) {
                    t.ponerPixel(32 + x, 4 + y, rgb(20, 20, 24));
                }
            }
            for (int y = 58; y < 63; y++) {
                for (int x = 2; x < 62; x++) {
                    t.ponerPixel(x, y, variar(rgb(110, 110, 104), (int) ((n(x, y, 4, 2) - 0.5) * 30)));
                }
            }
            for (int y = 28; y < 31; y++) {
                for (int x = 52; x < 60; x++) {
                    t.ponerPixel(x, y, rgb(150, 150, 140));
                }
            }
        }
        return t;
    }

    private Textura librero() {
        Textura t = new Textura(T, T);
        Random r = new Random(5);
        for (int y = 0; y < T; y++) {
            for (int x = 0; x < T; x++) {
                t.ponerPixel(x, y, rgb(16, 12, 10));
            }
        }
        int[] estantes = {2, 17, 32, 47, 62};
        Color[] tonos = {new Color(110, 34, 30), new Color(34, 60, 44), new Color(40, 46, 86), new Color(120, 96, 50),
            new Color(80, 40, 70), new Color(150, 140, 120), new Color(60, 30, 20), new Color(30, 70, 80)};
        for (int e = 0; e < estantes.length - 1; e++) {
            int suelo = estantes[e + 1] - 1;
            int x = 3;
            while (x < 60) {
                int ancho = 2 + r.nextInt(4);
                int alto = 9 + r.nextInt(5);
                if (r.nextInt(9) == 0) {
                    x += ancho + 2;
                    continue;
                }
                int c = rgb(tonos[r.nextInt(tonos.length)]);
                for (int yy = suelo - alto; yy < suelo; yy++) {
                    for (int xx = x; xx < Math.min(60, x + ancho); xx++) {
                        int cc = xx == x ? escalar(c, 0.7) : c;
                        if (yy == suelo - alto + 2) {
                            cc = escalar(c, 1.4);
                        }
                        t.ponerPixel(xx, yy, variar(cc, r.nextInt(8) - 4));
                    }
                }
                x += ancho;
            }
        }
        for (int y = 0; y < T; y++) {
            for (int x = 0; x < T; x++) {
                boolean marco = x < 3 || x > 60;
                boolean estante = false;
                for (int s : estantes) {
                    if (y >= s - 1 && y <= s + 1) {
                        estante = true;
                    }
                }
                if (marco || estante) {
                    t.ponerPixel(x, y, variar(rgb(74, 52, 36), (int) ((n(x * 2, y, 8, 2) - 0.5) * 18)));
                }
            }
        }
        return t;
    }

    private Textura televisor() {
        Textura t = paredSala();
        for (int y = 12; y < 42; y++) {
            for (int x = 8; x < 56; x++) {
                boolean borde = x < 10 || x > 53 || y < 14 || y > 39;
                t.ponerPixel(x, y, borde ? rgb(26, 26, 28) : rgb(8, 9, 10));
            }
        }
        for (int y = 15; y < 22; y++) {
            for (int x = 12; x < 18; x++) {
                t.ponerPixel(x, y, rgb(18, 20, 24));
            }
        }
        for (int y = 42; y < 46; y++) {
            for (int x = 26; x < 38; x++) {
                t.ponerPixel(x, y, rgb(30, 30, 32));
            }
        }
        for (int y = 46; y < 64; y++) {
            for (int x = 4; x < 60; x++) {
                int c = y == 46 ? rgb(90, 70, 52) : rgb(48, 36, 28);
                t.ponerPixel(x, y, variar(c, (int) ((n(x, y * 2, 8, 2) - 0.5) * 14)));
            }
        }
        return t;
    }

    public static void pintarEstatica(Textura tv, Random r, boolean encendida, float intensidad) {
        int[] em = tv.crearEmision();
        for (int y = 15; y < 39; y++) {
            for (int x = 10; x < 54; x++) {
                int idx = y * tv.ancho() + x;
                if (encendida) {
                    int v = (int) ((40 + r.nextInt(200)) * intensidad);
                    if ((y + r.nextInt(3)) % 7 == 0) {
                        v = v / 2;
                    }
                    tv.pixeles()[idx] = rgb(v, v, (int) (v * 1.05));
                    em[idx] = 230;
                } else {
                    tv.pixeles()[idx] = rgb(8, 9, 10);
                    em[idx] = 0;
                }
            }
        }
    }

    private Textura nevera() {
        Textura t = new Textura(T, T);
        for (int y = 0; y < T; y++) {
            for (int x = 0; x < T; x++) {
                int c = rgb(176, 178, 172);
                if (x < 3 || x > 60) {
                    c = rgb(120, 122, 118);
                }
                if (y == 22 || y == 23) {
                    c = rgb(90, 92, 90);
                }
                if (x >= 8 && x <= 10 && ((y > 6 && y < 18) || (y > 28 && y < 50))) {
                    c = rgb(70, 72, 74);
                }
                c = variar(c, (int) ((n(x, y, 16, 2) - 0.5) * 10));
                t.ponerPixel(x, y, c);
            }
        }
        int[][] imanes = {{30, 32, 196, 60, 50}, {44, 30, 60, 120, 190}, {38, 44, 230, 190, 60}};
        for (int[] im : imanes) {
            for (int y = 0; y < 4; y++) {
                for (int x = 0; x < 4; x++) {
                    t.ponerPixel(im[0] + x, im[1] + y, rgb(im[2], im[3], im[4]));
                }
            }
        }
        for (int y = 34; y < 50; y++) {
            for (int x = 30; x < 48; x++) {
                t.ponerPixel(x, y, rgb(222, 220, 206));
            }
        }
        for (int i = 0; i < 4; i++) {
            for (int x = 33; x < 45 - i * 2; x++) {
                t.ponerPixel(x, 38 + i * 3, rgb(60, 70, 140));
            }
        }
        return t;
    }

    private Textura armario(boolean abierto) {
        Textura t = new Textura(T, T);
        for (int y = 0; y < T; y++) {
            for (int x = 0; x < T; x++) {
                int c = variar(rgb(104, 78, 54), (int) ((n(x * 0.3, y * 2, 4, 3) - 0.5) * 24));
                if (x < 2 || x > 61 || y < 2) {
                    c = rgb(50, 36, 26);
                }
                if (x == 31 || x == 32) {
                    c = rgb(34, 24, 18);
                }
                t.ponerPixel(x, y, c);
            }
        }
        for (int y = 30; y < 34; y++) {
            t.ponerPixel(28, y, rgb(190, 170, 110));
            t.ponerPixel(35, y, rgb(190, 170, 110));
        }
        if (abierto) {
            for (int y = 3; y < T; y++) {
                for (int x = 30; x < 37; x++) {
                    t.ponerPixel(x, y, rgb(4, 3, 4));
                }
            }
            t.ponerPixel(32, 20, rgb(240, 240, 240));
            t.ponerPixel(34, 20, rgb(240, 240, 240));
            t.ponerEmision(32, 20, 255);
            t.ponerEmision(34, 20, 255);
        }
        return t;
    }

    private Textura cuadro(boolean perturbado) {
        Textura t = papelPasillo();
        for (int y = 10; y < 44; y++) {
            for (int x = 14; x < 50; x++) {
                boolean marco = x < 17 || x > 46 || y < 13 || y > 40;
                int c = marco ? variar(rgb(150, 116, 52), (x + y) % 3 * 8 - 8) : rgb(176, 170, 150);
                if (!marco) {
                    c = mezclar(c, rgb(120, 110, 90), (double) (y - 13) / 30);
                }
                t.ponerPixel(x, y, c);
            }
        }
        int[][] personas = {{23, 24, 5}, {32, 22, 6}, {41, 25, 4}};
        for (int[] p : personas) {
            int cx = p[0];
            int cy = p[1];
            int rad = p[2] - 2;
            for (int y = -rad; y <= rad; y++) {
                for (int x = -rad; x <= rad; x++) {
                    if (x * x + y * y <= rad * rad) {
                        t.ponerPixel(cx + x, cy + y, perturbado ? rgb(14, 12, 12) : rgb(196, 160, 130));
                    }
                }
            }
            for (int y = cy + rad + 1; y < 41; y++) {
                int a = Math.min(p[2], (y - cy - rad) / 2 + 2);
                for (int x = -a; x <= a; x++) {
                    t.ponerPixel(cx + x, y, rgb(70, 60, 80));
                }
            }
            if (perturbado) {
                t.ponerPixel(cx - 1, cy, rgb(250, 250, 250));
                t.ponerPixel(cx + 1, cy, rgb(250, 250, 250));
                t.ponerEmision(cx - 1, cy, 200);
                t.ponerEmision(cx + 1, cy, 200);
            } else {
                t.ponerPixel(cx - 1, cy - 1, rgb(40, 30, 30));
                t.ponerPixel(cx + 1, cy - 1, rgb(40, 30, 30));
            }
        }
        if (perturbado) {
            for (int i = 0; i < 40; i++) {
                int x = 18 + azar.nextInt(28);
                int y = 14 + azar.nextInt(26);
                t.ponerPixel(x, y, rgb(30, 20, 20));
            }
        }
        return t;
    }

    private Textura poster(boolean ojo) {
        Textura t = papelCuarto();
        for (int y = 8; y < 52; y++) {
            for (int x = 14; x < 50; x++) {
                int c = mezclar(rgb(16, 20, 46), rgb(46, 20, 50), (double) (y - 8) / 44);
                t.ponerPixel(x, y, c);
            }
        }
        for (int y = 0; y < T; y++) {
            for (int x = 0; x < T; x++) {
                double dx = x - 32;
                double dy = y - 24;
                double d = Math.sqrt(dx * dx + dy * dy);
                if (d < 10) {
                    if (ojo) {
                        int c = rgb(220, 214, 200);
                        if (d < 6) {
                            c = rgb(150, 30, 34);
                        }
                        if (d < 3) {
                            c = rgb(4, 4, 6);
                        }
                        if (Math.abs(dy) > 7 - Math.abs(dx) * 0.2) {
                            c = rgb(16, 20, 46);
                        }
                        t.ponerPixel(x, y, c);
                    } else {
                        t.ponerPixel(x, y, mezclar(rgb(230, 220, 190), rgb(160, 150, 130), n(x, y, 4, 2)));
                    }
                }
            }
        }
        for (int i = 0; i < 3; i++) {
            for (int x = 20; x < 44 - i * 4; x++) {
                t.ponerPixel(x, 40 + i * 4, rgb(200, 190, 210));
            }
        }
        return t;
    }

    private Textura espejo(boolean figura) {
        Textura t = azulejoBano();
        for (int y = 6; y < 46; y++) {
            for (int x = 12; x < 52; x++) {
                boolean marco = x < 14 || x > 49 || y < 8 || y > 43;
                int c;
                if (marco) {
                    c = rgb(140, 140, 136);
                } else {
                    double k = (double) (x - 14 + y - 8) / 72;
                    c = mezclar(rgb(40, 48, 54), rgb(70, 80, 88), k);
                    if ((x + y) % 23 == 0 || (x + y) % 23 == 1) {
                        c = variar(c, 18);
                    }
                }
                t.ponerPixel(x, y, c);
            }
        }
        if (figura) {
            for (int y = 14; y < 44; y++) {
                int a = y < 22 ? 4 : 7;
                for (int x = 32 - a; x <= 32 + a; x++) {
                    t.ponerPixel(x, y, rgb(6, 6, 8));
                }
            }
            t.ponerPixel(30, 17, rgb(220, 230, 240));
            t.ponerPixel(34, 17, rgb(220, 230, 240));
            t.ponerEmision(30, 17, 255);
            t.ponerEmision(34, 17, 255);
        }
        for (int y = 46; y < 64; y++) {
            for (int x = 0; x < T; x++) {
                if (y < 50) {
                    t.ponerPixel(x, y, rgb(60, 64, 64));
                }
            }
        }
        return t;
    }

    private Textura gabinete() {
        Textura t = azulejoCocina();
        for (int y = 0; y < 30; y++) {
            for (int x = 0; x < T; x++) {
                int c = variar(rgb(124, 112, 92), (int) ((n(x * 0.4, y * 2, 4, 2) - 0.5) * 14));
                if (x % 32 == 0 || y == 29 || y == 0) {
                    c = rgb(64, 58, 48);
                }
                if ((x % 32 == 27 || x % 32 == 28) && y > 18 && y < 25) {
                    c = rgb(170, 170, 160);
                }
                t.ponerPixel(x, y, c);
            }
        }
        return t;
    }

    private Textura alfombra() {
        Textura t = new Textura(T, T);
        Random r = new Random(9);
        for (int y = 0; y < T; y++) {
            for (int x = 0; x < T; x++) {
                int c = rgb(44, 46, 58);
                c = variar(c, (int) ((n(x, y, 16, 3) - 0.5) * 18) + r.nextInt(9) - 4);
                t.ponerPixel(x, y, c);
            }
        }
        return t;
    }

    private Textura madera(Color base) {
        Textura t = new Textura(T, T);
        Random r = new Random(base.getRGB());
        int[] tonos = new int[8];
        int[] desfase = new int[8];
        for (int i = 0; i < 8; i++) {
            tonos[i] = r.nextInt(16) - 8;
            desfase[i] = r.nextInt(64);
        }
        for (int y = 0; y < T; y++) {
            int tabla = y / 8;
            for (int x = 0; x < T; x++) {
                int c = variar(rgb(base), tonos[tabla]);
                double veta = n(x * 0.2 + desfase[tabla], y * 2.5, 4, 3);
                c = variar(c, (int) ((veta - 0.5) * 22));
                if (y % 8 == 0 || (x + desfase[tabla]) % 64 == 0) {
                    c = escalar(c, 0.45);
                }
                t.ponerPixel(x, y, c);
            }
        }
        return t;
    }

    private Textura ajedrez() {
        Textura t = new Textura(T, T);
        for (int y = 0; y < T; y++) {
            for (int x = 0; x < T; x++) {
                boolean claro = ((x / 16) + (y / 16)) % 2 == 0;
                int c = claro ? rgb(150, 144, 132) : rgb(48, 46, 46);
                c = variar(c, (int) ((n(x, y, 8, 3) - 0.5) * 20));
                t.ponerPixel(x, y, c);
            }
        }
        return t;
    }

    private Textura techo(boolean mancha) {
        Textura t = new Textura(T, T);
        for (int y = 0; y < T; y++) {
            for (int x = 0; x < T; x++) {
                int c = variar(rgb(84, 82, 78), (int) ((n(x, y, 8, 4) - 0.5) * 18));
                if (mancha) {
                    double dx = x - 30;
                    double dy = y - 34;
                    double d = Math.sqrt(dx * dx + dy * dy) + (n(x, y, 8, 2) - 0.5) * 8;
                    if (d < 18) {
                        c = mezclar(c, rgb(70, 58, 40), 0.4 + (d > 15 ? 0.3 : 0));
                    }
                }
                t.ponerPixel(x, y, c);
            }
        }
        return t;
    }

    private Textura camaLado() {
        Textura t = new Textura(T, T);
        for (int y = 0; y < T; y++) {
            for (int x = 0; x < T; x++) {
                int c;
                if (y < 44) {
                    double pliegue = Math.sin(x * 0.45 + n(x, y, 8, 2) * 4) * 0.12;
                    c = escalar(rgb(96, 32, 40), 0.85 + pliegue + (y < 4 ? 0.2 : 0));
                } else {
                    c = variar(rgb(58, 40, 30), (int) ((n(x * 0.3, y * 2, 4, 2) - 0.5) * 16));
                    if (y == 44) {
                        c = rgb(30, 20, 16);
                    }
                }
                t.ponerPixel(x, y, c);
            }
        }
        return t;
    }

    private Textura camaTope(boolean almohada) {
        Textura t = new Textura(T, T);
        for (int y = 0; y < T; y++) {
            for (int x = 0; x < T; x++) {
                double pliegue = Math.sin((x + y * 0.3) * 0.25 + n(x, y, 16, 2) * 5) * 0.12;
                int c = escalar(rgb(100, 34, 42), 0.9 + pliegue);
                if (x < 3 || x > 60) {
                    c = escalar(c, 0.7);
                }
                t.ponerPixel(x, y, c);
            }
        }
        if (almohada) {
            for (int y = 6; y < 30; y++) {
                for (int x = 8; x < 56; x++) {
                    double ex = (x - 32) / 24.0;
                    double ey = (y - 18) / 12.0;
                    double d = ex * ex * ex * ex + ey * ey * ey * ey;
                    if (d < 1) {
                        int c = escalar(rgb(176, 170, 160), 1.0 - d * 0.3);
                        t.ponerPixel(x, y, variar(c, (int) ((n(x, y, 8, 2) - 0.5) * 12)));
                    }
                }
            }
            for (int y = 0; y < 34; y++) {
                for (int x = 0; x < T; x++) {
                    if (y >= 32) {
                        t.ponerPixel(x, y, escalar(t.pixel(x, y), 1.25));
                    }
                }
            }
        }
        return t;
    }

    private Textura cajones(Color base, int filas) {
        Textura t = new Textura(T, T);
        int b = rgb(base);
        for (int y = 0; y < T; y++) {
            for (int x = 0; x < T; x++) {
                int c = variar(b, (int) ((n(x * 0.3, y * 2, 4, 3) - 0.5) * 20));
                if (y < 4) {
                    c = escalar(c, 1.2);
                }
                if (x == 0 || x == 63) {
                    c = escalar(c, 0.5);
                }
                t.ponerPixel(x, y, c);
            }
        }
        if (filas > 0) {
            int alto = 56 / filas;
            for (int f = 0; f < filas; f++) {
                int y0 = 6 + f * alto;
                for (int x = 4; x < 60; x++) {
                    t.ponerPixel(x, y0, escalar(t.pixel(x, y0), 0.5));
                    t.ponerPixel(x, y0 + alto - 2, escalar(t.pixel(x, y0 + alto - 2), 1.3));
                }
                for (int y = y0; y < y0 + alto - 1; y++) {
                    t.ponerPixel(4, y, escalar(t.pixel(4, y), 0.5));
                    t.ponerPixel(59, y, escalar(t.pixel(59, y), 1.3));
                }
                int cy = y0 + alto / 2;
                for (int x = 28; x < 36; x++) {
                    t.ponerPixel(x, cy, rgb(180, 170, 140));
                    t.ponerPixel(x, cy + 1, rgb(90, 80, 60));
                }
            }
        }
        return t;
    }

    private Textura tablero(Color base, boolean cuaderno) {
        Textura t = new Textura(T, T);
        int b = rgb(base);
        for (int y = 0; y < T; y++) {
            for (int x = 0; x < T; x++) {
                int c = variar(b, (int) ((n(x * 2.5, y * 0.25, 4, 3) - 0.5) * 24));
                if (x < 2 || x > 61 || y < 2 || y > 61) {
                    c = escalar(c, 0.7);
                }
                t.ponerPixel(x, y, c);
            }
        }
        if (cuaderno) {
            for (int y = 14; y < 40; y++) {
                for (int x = 10; x < 30; x++) {
                    int c = rgb(210, 208, 196);
                    if ((y - 14) % 4 == 0) {
                        c = rgb(140, 160, 200);
                    }
                    t.ponerPixel(x, y, c);
                }
            }
            for (int i = 0; i < 18; i++) {
                t.ponerPixel(36 + i, 20 + i / 2, rgb(200, 170, 40));
            }
            for (int i = 0; i < 26; i++) {
                t.ponerPixel(44 + (i % 2), 36 + i, rgb(30, 30, 32));
            }
        }
        return t;
    }

    private Textura sofaLado() {
        Textura t = new Textura(T, T);
        for (int y = 0; y < T; y++) {
            for (int x = 0; x < T; x++) {
                int c = variar(rgb(42, 58, 50), (int) ((n(x, y, 4, 2) - 0.5) * 14));
                if (y < 8) {
                    c = escalar(c, 1.2);
                }
                if (x % 21 == 0) {
                    c = escalar(c, 0.6);
                }
                if (y > 56) {
                    c = rgb(30, 22, 18);
                }
                t.ponerPixel(x, y, c);
            }
        }
        return t;
    }

    private Textura sofaTope() {
        Textura t = new Textura(T, T);
        for (int y = 0; y < T; y++) {
            for (int x = 0; x < T; x++) {
                double cojin = Math.sin(Math.PI * (y % 32) / 32.0);
                int c = escalar(rgb(46, 64, 54), 0.75 + cojin * 0.35);
                c = variar(c, (int) ((n(x, y, 4, 2) - 0.5) * 12));
                if (y > 52) {
                    c = escalar(rgb(38, 52, 44), 0.8);
                }
                t.ponerPixel(x, y, c);
            }
        }
        return t;
    }

    private Textura granito() {
        Textura t = new Textura(T, T);
        Random r = new Random(4);
        for (int y = 0; y < T; y++) {
            for (int x = 0; x < T; x++) {
                int c = variar(rgb(72, 72, 74), r.nextInt(30) - 15);
                if (r.nextInt(30) == 0) {
                    c = rgb(150, 146, 140);
                }
                t.ponerPixel(x, y, c);
            }
        }
        return t;
    }

    private Textura banera(boolean tope) {
        Textura t = new Textura(T, T);
        for (int y = 0; y < T; y++) {
            for (int x = 0; x < T; x++) {
                int c = variar(rgb(166, 172, 170), (int) ((n(x, y, 16, 2) - 0.5) * 14));
                if (tope) {
                    boolean borde = x < 8 || x > 55 || y < 8 || y > 55;
                    if (!borde) {
                        double onda = Math.sin((x + y) * 0.4 + n(x, y, 8, 2) * 6) * 0.15;
                        c = escalar(rgb(26, 34, 38), 0.9 + onda);
                    }
                } else if (y < 5) {
                    c = escalar(c, 1.15);
                } else if (y > 58) {
                    c = escalar(c, 0.6);
                }
                t.ponerPixel(x, y, c);
            }
        }
        return t;
    }

    private Textura lavamanosTope() {
        Textura t = banera(false);
        for (int y = 0; y < T; y++) {
            for (int x = 0; x < T; x++) {
                double dx = (x - 32) / 22.0;
                double dy = (y - 34) / 18.0;
                double d = dx * dx + dy * dy;
                if (d < 1) {
                    t.ponerPixel(x, y, escalar(rgb(150, 156, 156), 0.6 + d * 0.4));
                }
            }
        }
        for (int y = 6; y < 16; y++) {
            t.ponerPixel(32, y, rgb(190, 190, 196));
            t.ponerPixel(33, y, rgb(120, 120, 126));
        }
        return t;
    }

    private BufferedImage lienzo(int ancho, int alto) {
        return new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
    }

    private Graphics2D pincel(BufferedImage img) {
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        return g;
    }

    private Textura conEmision(BufferedImage color, BufferedImage emision) {
        Textura t = Textura.desdeImagen(color);
        if (emision != null) {
            int[] em = t.crearEmision();
            for (int y = 0; y < t.alto(); y++) {
                for (int x = 0; x < t.ancho(); x++) {
                    int p = emision.getRGB(x, y);
                    if ((p >>> 24) > 0) {
                        em[y * t.ancho() + x] = (p >> 16) & 255;
                    }
                }
            }
        }
        return t;
    }

    private Textura lampara() {
        BufferedImage img = lienzo(32, 48);
        BufferedImage em = lienzo(32, 48);
        Graphics2D g = pincel(img);
        Graphics2D e = pincel(em);
        Polygon pantalla = new Polygon(new int[]{9, 23, 27, 5}, new int[]{8, 8, 24, 24}, 4);
        g.setColor(new Color(196, 170, 120));
        g.fillPolygon(pantalla);
        g.setColor(new Color(150, 124, 84));
        g.drawPolygon(pantalla);
        e.setColor(new Color(200, 200, 200));
        e.fillPolygon(pantalla);
        g.setColor(new Color(60, 50, 40));
        g.fillRect(15, 24, 2, 16);
        g.fillOval(9, 39, 14, 8);
        g.dispose();
        e.dispose();
        return conEmision(img, em);
    }

    private Textura lamparaPie() {
        BufferedImage img = lienzo(32, 96);
        Graphics2D g = pincel(img);
        Polygon pantalla = new Polygon(new int[]{9, 23, 28, 4}, new int[]{4, 4, 24, 24}, 4);
        g.setColor(new Color(150, 136, 110));
        g.fillPolygon(pantalla);
        g.setColor(new Color(40, 36, 34));
        g.fillRect(15, 24, 2, 66);
        g.fillOval(8, 88, 16, 7);
        g.dispose();
        return conEmision(img, null);
    }

    public Textura reloj(String hora, boolean corrupto) {
        BufferedImage img = lienzo(48, 24);
        BufferedImage em = lienzo(48, 24);
        Graphics2D g = pincel(img);
        Graphics2D e = pincel(em);
        g.setColor(new Color(24, 22, 24));
        g.fillRoundRect(1, 3, 46, 20, 6, 6);
        g.setColor(new Color(10, 8, 8));
        g.fillRect(5, 7, 38, 12);
        Color digito = corrupto ? new Color(255, 30, 30) : new Color(220, 40, 30);
        g.setColor(digito);
        e.setColor(Color.WHITE);
        dibujarDigitos(g, hora, 7, 8);
        dibujarDigitos(e, hora, 7, 8);
        g.dispose();
        e.dispose();
        return conEmision(img, em);
    }

    private static final int[][] SEGMENTOS = {
        {1, 1, 1, 0, 1, 1, 1}, {0, 0, 1, 0, 0, 1, 0}, {1, 0, 1, 1, 1, 0, 1}, {1, 0, 1, 1, 0, 1, 1}, {0, 1, 1, 1, 0, 1, 0},
        {1, 1, 0, 1, 0, 1, 1}, {1, 1, 0, 1, 1, 1, 1}, {1, 0, 1, 0, 0, 1, 0}, {1, 1, 1, 1, 1, 1, 1}, {1, 1, 1, 1, 0, 1, 1}
    };

    private void dibujarDigitos(Graphics2D g, String texto, int x, int y) {
        int cx = x;
        for (char c : texto.toCharArray()) {
            if (c == ':') {
                g.fillRect(cx, y + 3, 1, 1);
                g.fillRect(cx, y + 7, 1, 1);
                cx += 3;
                continue;
            }
            if (c < '0' || c > '9') {
                cx += 8;
                continue;
            }
            int[] s = SEGMENTOS[c - '0'];
            if (s[0] == 1) {
                g.fillRect(cx + 1, y, 4, 1);
            }
            if (s[1] == 1) {
                g.fillRect(cx, y + 1, 1, 4);
            }
            if (s[2] == 1) {
                g.fillRect(cx + 5, y + 1, 1, 4);
            }
            if (s[3] == 1) {
                g.fillRect(cx + 1, y + 5, 4, 1);
            }
            if (s[4] == 1) {
                g.fillRect(cx, y + 6, 1, 4);
            }
            if (s[5] == 1) {
                g.fillRect(cx + 5, y + 6, 1, 4);
            }
            if (s[6] == 1) {
                g.fillRect(cx + 1, y + 10, 4, 1);
            }
            cx += 8;
        }
    }

    private Textura planta() {
        BufferedImage img = lienzo(48, 72);
        Graphics2D g = pincel(img);
        Random r = new Random(21);
        for (int i = 0; i < 14; i++) {
            double ang = -Math.PI / 2 + (r.nextDouble() - 0.5) * 2.4;
            int largo = 18 + r.nextInt(22);
            int x2 = 24 + (int) (Math.cos(ang) * largo);
            int y2 = 50 + (int) (Math.sin(ang) * largo);
            g.setColor(new Color(30 + r.nextInt(20), 60 + r.nextInt(40), 34 + r.nextInt(20)));
            g.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine(24, 50, x2, y2);
            g.fill(new Ellipse2D.Double(x2 - 4, y2 - 3, 8, 6));
        }
        g.setColor(new Color(110, 60, 40));
        g.fillPolygon(new int[]{12, 36, 32, 16}, new int[]{50, 50, 71, 71}, 4);
        g.setColor(new Color(80, 44, 30));
        g.fillRect(12, 50, 24, 3);
        g.dispose();
        return conEmision(img, null);
    }

    private Textura silla() {
        BufferedImage img = lienzo(48, 64);
        Graphics2D g = pincel(img);
        g.setColor(new Color(34, 34, 40));
        g.fillRoundRect(10, 2, 28, 30, 8, 8);
        g.setColor(new Color(26, 26, 30));
        g.fillRect(8, 34, 32, 8);
        g.setColor(new Color(60, 60, 64));
        g.fillRect(22, 42, 4, 12);
        g.fillRect(8, 54, 32, 3);
        g.fillOval(6, 56, 6, 6);
        g.fillOval(36, 56, 6, 6);
        g.fillOval(21, 56, 6, 6);
        g.dispose();
        return conEmision(img, null);
    }

    public Textura router(int fase) {
        BufferedImage img = lienzo(48, 32);
        BufferedImage em = lienzo(48, 32);
        Graphics2D g = pincel(img);
        Graphics2D e = pincel(em);
        g.setColor(new Color(30, 30, 34));
        g.fillRoundRect(4, 18, 40, 12, 4, 4);
        g.setColor(new Color(50, 50, 56));
        g.fillRect(8, 2, 3, 17);
        g.fillRect(37, 2, 3, 17);
        for (int i = 0; i < 5; i++) {
            boolean encendido = fase >= 0 && (((fase >> i) & 1) == 1 || i == 0);
            g.setColor(encendido ? new Color(60, 255, 120) : new Color(20, 50, 30));
            g.fillRect(10 + i * 6, 23, 3, 2);
            if (encendido) {
                e.setColor(Color.WHITE);
                e.fillRect(10 + i * 6, 23, 3, 2);
            }
        }
        g.dispose();
        e.dispose();
        return conEmision(img, em);
    }

    private Textura inodoro() {
        BufferedImage img = lienzo(48, 48);
        Graphics2D g = pincel(img);
        g.setColor(new Color(170, 174, 172));
        g.fillRoundRect(8, 0, 32, 20, 6, 6);
        g.fillOval(4, 18, 40, 16);
        g.fillPolygon(new int[]{12, 36, 32, 16}, new int[]{30, 30, 47, 47}, 4);
        g.setColor(new Color(120, 124, 122));
        g.drawOval(4, 18, 40, 16);
        g.dispose();
        return conEmision(img, null);
    }

    private Textura ropa() {
        BufferedImage img = lienzo(64, 32);
        Graphics2D g = pincel(img);
        Random r = new Random(8);
        Color[] c = {new Color(40, 50, 90), new Color(110, 110, 104), new Color(80, 30, 30), new Color(30, 30, 30)};
        for (int i = 0; i < 9; i++) {
            g.setColor(c[r.nextInt(c.length)]);
            int w = 20 + r.nextInt(24);
            int h = 8 + r.nextInt(10);
            g.fillOval(4 + r.nextInt(64 - w - 4), 32 - h - r.nextInt(8), w, h);
        }
        g.dispose();
        return conEmision(img, null);
    }

    private Textura peluche(boolean mira) {
        BufferedImage img = lienzo(32, 32);
        BufferedImage em = lienzo(32, 32);
        Graphics2D g = pincel(img);
        Graphics2D e = pincel(em);
        Color piel = new Color(120, 86, 56);
        g.setColor(piel);
        g.fillOval(7, 14, 18, 17);
        g.fillOval(9, 3, 14, 13);
        g.fillOval(7, 2, 6, 6);
        g.fillOval(19, 2, 6, 6);
        g.fillOval(4, 17, 6, 9);
        g.fillOval(22, 17, 6, 9);
        g.setColor(new Color(170, 140, 100));
        g.fillOval(13, 9, 6, 5);
        if (mira) {
            g.setColor(new Color(255, 40, 40));
            g.fillRect(12, 7, 2, 2);
            g.fillRect(18, 7, 2, 2);
            e.setColor(Color.WHITE);
            e.fillRect(12, 7, 2, 2);
            e.fillRect(18, 7, 2, 2);
        } else {
            g.setColor(new Color(10, 10, 10));
            g.fillRect(12, 7, 2, 2);
            g.fillRect(18, 7, 2, 2);
        }
        g.dispose();
        e.dispose();
        return conEmision(img, em);
    }

    private Textura seguidor() {
        int w = 64;
        int h = 170;
        BufferedImage img = lienzo(w, h);
        BufferedImage em = lienzo(w, h);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Graphics2D e = em.createGraphics();
        e.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color negro = new Color(9, 7, 11);
        g.setColor(negro);
        Path2D torso = new Path2D.Double();
        torso.moveTo(17, 50);
        torso.curveTo(22, 42, 42, 40, 46, 47);
        torso.curveTo(44, 70, 41, 88, 38, 108);
        torso.lineTo(27, 108);
        torso.curveTo(24, 88, 20, 70, 17, 50);
        torso.closePath();
        g.fill(torso);
        g.setStroke(new BasicStroke(3.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Path2D brazoI = new Path2D.Double();
        brazoI.moveTo(19, 50);
        brazoI.curveTo(12, 78, 10, 104, 9, 142);
        g.draw(brazoI);
        Path2D brazoD = new Path2D.Double();
        brazoD.moveTo(45, 49);
        brazoD.curveTo(52, 76, 55, 102, 56, 138);
        g.draw(brazoD);
        g.setStroke(new BasicStroke(1.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < 4; i++) {
            g.drawLine(9, 141, 5 + i * 3, 156 + (i % 2) * 4);
            g.drawLine(56, 137, 53 + i * 3, 152 + ((i + 1) % 2) * 4);
        }
        g.setStroke(new BasicStroke(4.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(29, 106, 26, 158);
        g.drawLine(36, 106, 40, 158);
        g.setStroke(new BasicStroke(3.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(31, 44, 34, 30);
        AffineTransform original = g.getTransform();
        AffineTransform originalE = e.getTransform();
        g.rotate(0.2, 34, 20);
        e.rotate(0.2, 34, 20);
        Ellipse2D cabeza = new Ellipse2D.Double(26, 3, 16, 31);
        g.setPaint(new java.awt.RadialGradientPaint(new java.awt.geom.Point2D.Double(33, 14), 17,
                new float[]{0f, 0.6f, 1f}, new Color[]{new Color(150, 154, 158), new Color(100, 104, 110), new Color(24, 24, 30)}));
        g.fill(cabeza);
        e.setColor(new Color(70, 70, 70));
        e.fill(cabeza);
        Path2D ojoI = new Path2D.Double();
        ojoI.moveTo(27.6, 16);
        ojoI.curveTo(28.5, 12.5, 31.5, 12.5, 32.3, 15.4);
        ojoI.curveTo(32, 19, 28.6, 19.6, 27.6, 16);
        Path2D ojoD = new Path2D.Double();
        ojoD.moveTo(35.2, 16);
        ojoD.curveTo(36, 13, 39.4, 13.2, 40, 16.4);
        ojoD.curveTo(39.4, 20, 35.8, 19.8, 35.2, 16);
        g.setColor(new Color(20, 18, 24, 200));
        g.fill(new Ellipse2D.Double(26.5, 11, 7.5, 10));
        g.fill(new Ellipse2D.Double(34.2, 11.5, 7.2, 10));
        g.setColor(new Color(2, 1, 3));
        g.fill(ojoI);
        g.fill(ojoD);
        Graphics2D borrar = em.createGraphics();
        borrar.setTransform(e.getTransform());
        borrar.setComposite(java.awt.AlphaComposite.Clear);
        borrar.fill(new Ellipse2D.Double(26.5, 11, 7.5, 10));
        borrar.fill(new Ellipse2D.Double(34.2, 11.5, 7.2, 10));
        borrar.dispose();
        g.setColor(new Color(4, 3, 5));
        g.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Path2D boca = new Path2D.Double();
        boca.moveTo(28.5, 27.5);
        boca.curveTo(31, 29, 36, 29.2, 39.5, 27.8);
        g.draw(boca);
        g.setColor(new Color(250, 250, 250));
        g.fill(new Ellipse2D.Double(29.3, 15.2, 1.5, 1.5));
        g.fill(new Ellipse2D.Double(37, 15.6, 1.5, 1.5));
        e.setColor(Color.WHITE);
        e.fill(new Ellipse2D.Double(29.3, 15.2, 1.5, 1.5));
        e.fill(new Ellipse2D.Double(37, 15.6, 1.5, 1.5));
        g.setColor(new Color(230, 20, 34));
        g.fill(new Ellipse2D.Double(32.8, 6, 2.4, 2.4));
        e.setColor(Color.WHITE);
        e.fill(new Ellipse2D.Double(32.8, 6, 2.4, 2.4));
        g.setTransform(original);
        e.setTransform(originalE);
        g.dispose();
        e.dispose();
        Textura t = conEmision(img, em);
        Random r = new Random(66);
        int[] px = t.pixeles();
        int[] emision = t.emision();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int i = y * w + x;
                int a = px[i] >>> 24;
                if (a == 0) {
                    continue;
                }
                if (a < 110 && (emision == null || emision[i] < 120)) {
                    px[i] = 0;
                    continue;
                }
                px[i] = px[i] | 0xFF000000;
                boolean borde = x == 0 || x == w - 1 || (px[i - 1] >>> 24) == 0 || (px[i + 1] >>> 24) == 0;
                if (borde && y > 40 && r.nextInt(3) == 0 && (emision == null || emision[i] == 0)) {
                    px[i] = 0;
                }
            }
        }
        for (int y = 140; y < h; y++) {
            double desvanecer = (y - 140) / 30.0;
            for (int x = 0; x < w; x++) {
                int i = y * w + x;
                if ((px[i] >>> 24) != 0 && r.nextDouble() < desvanecer * 0.9) {
                    px[i] = 0;
                }
            }
        }
        for (int i = 0; i < 70; i++) {
            int x = 18 + r.nextInt(30);
            int y = 120 + r.nextInt(48);
            if (r.nextDouble() > (y - 120) / 60.0) {
                t.ponerPixel(x, y, rgb(8, 7, 10));
            }
        }
        return t;
    }

    private Textura jugadorEspalda() {
        BufferedImage img = lienzo(64, 128);
        BufferedImage em = lienzo(64, 128);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Graphics2D e = pincel(em);
        g.setColor(new Color(52, 56, 72));
        g.fill(new Ellipse2D.Double(20, 14, 24, 28));
        Path2D cuerpo = new Path2D.Double();
        cuerpo.moveTo(12, 50);
        cuerpo.curveTo(18, 38, 46, 38, 52, 50);
        cuerpo.lineTo(50, 98);
        cuerpo.lineTo(14, 98);
        cuerpo.closePath();
        g.fill(cuerpo);
        g.fillRect(18, 98, 11, 28);
        g.fillRect(35, 98, 11, 28);
        g.setColor(new Color(90, 130, 190));
        g.setStroke(new BasicStroke(1.5f));
        g.drawArc(20, 14, 24, 28, 20, 140);
        g.setColor(new Color(150, 190, 240, 160));
        g.fill(new Ellipse2D.Double(22, 52, 20, 12));
        e.setColor(new Color(170, 170, 170));
        e.fill(new Ellipse2D.Double(22, 52, 20, 12));
        e.setColor(new Color(90, 90, 90));
        e.drawArc(20, 14, 24, 28, 20, 140);
        g.dispose();
        e.dispose();
        Textura t = conEmision(img, em);
        int[] px = t.pixeles();
        for (int i = 0; i < px.length; i++) {
            int a = px[i] >>> 24;
            px[i] = a < 100 ? 0 : px[i] | 0xFF000000;
        }
        return t;
    }

    private Textura telefonoMesa() {
        BufferedImage img = lienzo(32, 16);
        BufferedImage em = lienzo(32, 16);
        Graphics2D g = pincel(img);
        Graphics2D e = pincel(em);
        g.setColor(new Color(16, 16, 20));
        g.fillRoundRect(4, 9, 24, 6, 3, 3);
        g.setColor(new Color(130, 170, 230));
        g.fillRect(6, 10, 20, 3);
        e.setColor(Color.WHITE);
        e.fillRect(6, 10, 20, 3);
        g.setColor(new Color(60, 60, 60));
        g.drawLine(28, 13, 31, 15);
        g.dispose();
        e.dispose();
        return conEmision(img, em);
    }
}
