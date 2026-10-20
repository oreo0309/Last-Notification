package juego.interfaz;

import estructuras.ListaEnlazada;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;
import juego.Decision;
import juego.IndicadorBienestar;
import juego.Telefono;
import juego.feed.ElementoFeed;
import juego.feed.Feed;
import juego.feed.Notificacion;

public class InterfazTelefono {

    public interface Oyente {
        void alDecidir(ElementoFeed elemento, Decision decision);

        void alDesplazar(int direccion);

        void alAlternarNoMolestar();

        void alCerrar();
    }

    public static final int ANCHO = 400;
    public static final int ALTO = 680;
    public static final int X = (1280 - ANCHO) / 2;
    public static final int Y = 30;
    private static final int MARGEN = 14;

    private final Feed feed;
    private final Telefono telefono;
    private final Oyente oyente;
    private final ContextoVisual ctx = new ContextoVisual();

    private boolean abierto;
    private double subida;
    private double animacionScroll;
    private int ultimoFoco = -1;
    private int opcionResaltada = -1;
    private Notificacion aviso;
    private double tiempoAviso;
    private Decision ultimaDecision;
    private double tiempoEfectos;
    private double corrupcionBase;
    private double pulsoGlitch;
    private String banner;
    private double tiempoBanner;
    private String reloj = "00:00";
    private boolean llamadaEntrante;
    private double tiempoLlamada;

    public InterfazTelefono(Feed feed, Telefono telefono, Oyente oyente) {
        this.feed = feed;
        this.telefono = telefono;
        this.oyente = oyente;
    }

    public void abrir() {
        abierto = true;
        opcionResaltada = -1;
    }

    public void cerrar() {
        abierto = false;
        opcionResaltada = -1;
    }

    public boolean abierto() {
        return abierto;
    }

    public double subida() {
        return subida;
    }

    public boolean visible() {
        return subida > 0.01;
    }

    public void actualizar(double dt, int tension, String hora, String nombreJugador) {
        double objetivo = abierto ? 1 : 0;
        subida += (objetivo - subida) * Math.min(1, dt * 9);
        if (Math.abs(subida - objetivo) < 0.002) {
            subida = objetivo;
        }
        animacionScroll *= Math.pow(0.0008, dt);
        if (Math.abs(animacionScroll) < 0.5) {
            animacionScroll = 0;
        }
        if (tiempoAviso > 0) {
            tiempoAviso -= dt;
        }
        if (tiempoEfectos > 0) {
            tiempoEfectos -= dt;
        }
        if (tiempoBanner > 0) {
            tiempoBanner -= dt;
        }
        if (pulsoGlitch > 0) {
            pulsoGlitch = Math.max(0, pulsoGlitch - dt * 1.5);
        }
        if (tiempoLlamada > 0) {
            tiempoLlamada -= dt;
            if (tiempoLlamada <= 0) {
                llamadaEntrante = false;
            }
        }
        corrupcionBase = Math.max(0, (tension - 45) / 55.0);
        ctx.tiempo += dt;
        ctx.corrupcion = Math.min(1, corrupcionBase * 0.8 + pulsoGlitch);
        ctx.nombreJugador = nombreJugador;
        reloj = hora;
        int foco = feed.nivelScroll();
        if (ultimoFoco >= 0 && foco != ultimoFoco) {
            opcionResaltada = -1;
        }
        ultimoFoco = foco;
    }

    public void glitch(double fuerza) {
        pulsoGlitch = Math.max(pulsoGlitch, fuerza);
    }

    public void mostrarAviso(Notificacion n) {
        aviso = n;
        tiempoAviso = 4.5;
    }

    public Notificacion avisoActual() {
        return tiempoAviso > 0 ? aviso : null;
    }

    public double tiempoAviso() {
        return tiempoAviso;
    }

    public void mostrarEfectos(Decision d) {
        ultimaDecision = d;
        tiempoEfectos = 3.2;
    }

    public void banner(String texto, double segundos) {
        banner = texto;
        tiempoBanner = segundos;
    }

    public void llamada(double segundos) {
        llamadaEntrante = true;
        tiempoLlamada = segundos;
    }

    public void animarDesplazamiento(int direccion, int alto) {
        animacionScroll += direccion * alto;
    }

    public ContextoVisual contexto() {
        return ctx;
    }

    public boolean teclado(int codigo) {
        if (!abierto) {
            return false;
        }
        ElementoFeed foco = feed.enfocado();
        switch (codigo) {
            case KeyEvent.VK_W:
            case KeyEvent.VK_UP:
                oyente.alDesplazar(-1);
                return true;
            case KeyEvent.VK_S:
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_SPACE:
                oyente.alDesplazar(1);
                return true;
            case KeyEvent.VK_M:
                oyente.alAlternarNoMolestar();
                return true;
            case KeyEvent.VK_TAB:
            case KeyEvent.VK_ESCAPE:
            case KeyEvent.VK_Q:
                oyente.alCerrar();
                return true;
            case KeyEvent.VK_A:
            case KeyEvent.VK_LEFT:
                moverResaltado(foco, -1);
                return true;
            case KeyEvent.VK_D:
            case KeyEvent.VK_RIGHT:
                moverResaltado(foco, 1);
                return true;
            case KeyEvent.VK_ENTER:
            case KeyEvent.VK_E:
                if (foco != null && opcionResaltada >= 0 && opcionResaltada < foco.decisiones().tamano() && !foco.resuelto()) {
                    oyente.alDecidir(foco, foco.decisiones().obtener(opcionResaltada));
                    opcionResaltada = -1;
                } else if (foco != null && !foco.resuelto() && !foco.decisiones().estaVacia()) {
                    opcionResaltada = 0;
                }
                return true;
            default:
                break;
        }
        if (codigo >= KeyEvent.VK_1 && codigo <= KeyEvent.VK_4) {
            int i = codigo - KeyEvent.VK_1;
            if (foco != null && !foco.resuelto() && i < foco.decisiones().tamano()) {
                oyente.alDecidir(foco, foco.decisiones().obtener(i));
                opcionResaltada = -1;
            }
            return true;
        }
        if (codigo >= KeyEvent.VK_NUMPAD1 && codigo <= KeyEvent.VK_NUMPAD4) {
            return teclado(KeyEvent.VK_1 + (codigo - KeyEvent.VK_NUMPAD1));
        }
        return false;
    }

    private void moverResaltado(ElementoFeed foco, int dir) {
        if (foco == null || foco.resuelto() || foco.decisiones().estaVacia()) {
            return;
        }
        int n = foco.decisiones().tamano();
        opcionResaltada = opcionResaltada < 0 ? 0 : Math.floorMod(opcionResaltada + dir, n);
    }

    public boolean clic(int x, int y) {
        if (!abierto) {
            return false;
        }
        ElementoFeed foco = feed.enfocado();
        if (foco != null && !foco.resuelto()) {
            int i = 0;
            for (Rectangle r : foco.zonasDecision()) {
                if (r.contains(x, y) && i < foco.decisiones().tamano()) {
                    oyente.alDecidir(foco, foco.decisiones().obtener(i));
                    return true;
                }
                i++;
            }
        }
        Rectangle telefonoRect = new Rectangle(X, Y, ANCHO, ALTO);
        if (!telefonoRect.contains(x, y)) {
            oyente.alCerrar();
        }
        return true;
    }

    public void raton(int x, int y) {
        if (!abierto) {
            return;
        }
        ElementoFeed foco = feed.enfocado();
        if (foco == null) {
            return;
        }
        int i = 0;
        for (Rectangle r : foco.zonasDecision()) {
            if (r.contains(x, y)) {
                opcionResaltada = i;
                return;
            }
            i++;
        }
    }

    public void dibujar(Graphics2D g) {
        if (subida <= 0.01) {
            return;
        }
        double t = 1 - subida;
        int y0 = (int) (Y + t * t * 760);
        int x0 = X;
        g.setColor(new Color(0, 0, 0, (int) (110 * subida)));
        g.fillRect(0, 0, 1280, 720);
        Dibujo.rectRedondo(g, x0 - 6, y0 - 6, ANCHO + 12, ALTO + 40, 58, new Color(8, 8, 10));
        Dibujo.rectRedondo(g, x0, y0, ANCHO, ALTO + 30, 52, new Color(22, 22, 28));
        int sx = x0 + MARGEN;
        int sy = y0 + MARGEN;
        int sw = ANCHO - MARGEN * 2;
        int sh = ALTO - MARGEN * 2 + 20;
        Shape clipOriginal = g.getClip();
        RoundRectangle2D pantalla = new RoundRectangle2D.Float(sx, sy, sw, sh, 40, 40);
        g.clip(pantalla);
        if (!telefono.puedeUsarse() && telefono.bateria() <= 0) {
            g.setColor(Color.BLACK);
            g.fill(pantalla);
            g.setFont(Estilo.fuente(Font.BOLD, 18));
            g.setColor(new Color(150, 150, 150));
            Dibujo.centrado(g, "Batería agotada", sx + sw / 2, sy + sh / 2);
            g.setClip(clipOriginal);
            return;
        }
        Color fondo = Estilo.altoContraste() ? Color.BLACK : new Color(12, 12, 18);
        if (ctx.corrupcion > 0.5) {
            int rojo = (int) (12 + 30 * (ctx.corrupcion - 0.5) * (0.6 + 0.4 * Math.sin(ctx.tiempo * 5)));
            fondo = new Color(rojo, 8, 12);
        }
        g.setColor(fondo);
        g.fill(pantalla);
        dibujarFeed(g, sx, sy + 86, sw, sh - 86 - 50);
        dibujarBarraEstado(g, sx, sy, sw);
        dibujarCabecera(g, sx, sy + 30, sw);
        dibujarNavegacion(g, sx, sy + sh - 50, sw);
        dibujarEfectos(g, sx, sy + sh - 50, sw);
        if (tiempoAviso > 0 && aviso != null) {
            float alfa = (float) Math.min(1, Math.min(tiempoAviso, 4.5 - tiempoAviso) * 3);
            int caida = (int) (Math.min(1, (4.5 - tiempoAviso) * 4) * 12);
            aviso.mostrarAviso(g, sx + 10, sy + caida - 4 + 30, sw - 20, alfa, ctx);
        }
        if (tiempoBanner > 0 && banner != null) {
            g.setFont(Estilo.fuente(Font.BOLD, 13));
            int bw = g.getFontMetrics().stringWidth(banner) + 30;
            Dibujo.rectRedondo(g, sx + (sw - bw) / 2.0, sy + 92, bw, 28, 14, new Color(180, 40, 40, 230));
            g.setColor(Color.WHITE);
            Dibujo.centrado(g, banner, sx + sw / 2, sy + 111);
        }
        if (llamadaEntrante) {
            dibujarLlamada(g, sx, sy, sw, sh);
        }
        dibujarGlitchPantalla(g, sx, sy, sw, sh);
        g.setClip(clipOriginal);
        g.setColor(new Color(255, 255, 255, 18));
        g.setStroke(new BasicStroke(1.5f));
        g.draw(new RoundRectangle2D.Float(x0 + 0.5f, y0 + 0.5f, ANCHO - 1, ALTO + 29, 52, 52));
        Dibujo.rectRedondo(g, x0 + ANCHO / 2.0 - 40, y0 + MARGEN + 6, 80, 18, 18, Color.BLACK);
    }

    private void dibujarBarraEstado(Graphics2D g, int x, int y, int w) {
        g.setColor(new Color(0, 0, 0, 120));
        g.fillRect(x, y, w, 30);
        g.setFont(Estilo.fuente(Font.BOLD, 14));
        g.setColor(Estilo.texto());
        String hora = ctx.corrupcion > 0.75 && ctx.azar.nextDouble() < 0.05 ? "03:12" : reloj;
        g.drawString(hora, x + 34, y + 21);
        int bx = x + w - 70;
        Dibujo.bateria(g, bx, y + 10, 24, 11, telefono.bateria() / 100.0, Estilo.texto());
        g.setFont(Estilo.fuente(Font.PLAIN, 11));
        g.drawString((int) telefono.bateria() + "%", bx + 30, y + 20);
        Dibujo.wifi(g, bx - 26, y + 7, 16, Estilo.texto(), telefono.sinWifi());
        if (telefono.noMolestar()) {
            Dibujo.luna(g, bx - 48, y + 8, 14, Estilo.acento());
        }
    }

    private void dibujarCabecera(Graphics2D g, int x, int y, int w) {
        g.setColor(Estilo.altoContraste() ? Color.BLACK : new Color(16, 16, 24));
        g.fillRect(x, y, w, 56);
        g.setColor(Estilo.borde());
        g.drawLine(x, y + 55, x + w, y + 55);
        Dibujo.logoEco(g, x + 16, y + 12, 32, Estilo.acento());
        g.setFont(Estilo.fuente(Font.BOLD, 24));
        g.setColor(Estilo.texto());
        String nombreApp = ctx.corrupcion > 0.6 && ((long) (ctx.tiempo * 3) % 5 == 0) ? "eco eco eco" : "eco";
        g.drawString(nombreApp, x + 54, y + 37);
        Dibujo.campana(g, x + w - 44, y + 15, 26, Estilo.texto());
        int sinLeer = telefono.sinLeer();
        if (sinLeer > 0) {
            String n = sinLeer > 99 ? "99+" : String.valueOf(sinLeer);
            g.setFont(Estilo.fuente(Font.BOLD, 11));
            int bw = Math.max(18, g.getFontMetrics().stringWidth(n) + 8);
            Dibujo.rectRedondo(g, x + w - 30, y + 9, bw, 18, 18, Estilo.peligro());
            g.setColor(Color.WHITE);
            Dibujo.centrado(g, n, x + w - 30 + bw / 2, y + 22);
        }
    }

    private void dibujarFeed(Graphics2D g, int x, int y, int w, int h) {
        Shape clip = g.getClip();
        g.clipRect(x, y, w, h);
        ListaEnlazada<ElementoFeed> elementos = feed.elementos();
        int foco = feed.nivelScroll();
        int cx = x + 10;
        int cw = w - 20;
        int yFoco = y + 14 + (int) animacionScroll;
        if (!elementos.estaVacia()) {
            ElementoFeed actual = elementos.obtener(foco);
            ctx.enfocado = true;
            ctx.opcionResaltada = opcionResaltada;
            int altoFoco = actual.mostrar(g, cx, yFoco, cw, ctx);
            ctx.enfocado = false;
            ctx.opcionResaltada = -1;
            int yAbajo = yFoco + altoFoco + 12;
            for (int i = foco + 1; i < elementos.tamano() && yAbajo < y + h; i++) {
                yAbajo += elementos.obtener(i).mostrar(g, cx, yAbajo, cw, ctx) + 12;
            }
            int yArriba = yFoco - 12;
            for (int i = foco - 1; i >= 0 && yArriba > y; i--) {
                ElementoFeed e = elementos.obtener(i);
                int alto = e.medirAlto(g, cw, ctx);
                yArriba -= alto;
                e.mostrar(g, cx, yArriba, cw, ctx);
                yArriba -= 12;
            }
        }
        g.setPaint(new GradientPaint(x, y, new Color(0, 0, 0, 160), x, y + 24, new Color(0, 0, 0, 0)));
        g.fillRect(x, y, w, 24);
        g.setClip(clip);
    }

    private void dibujarNavegacion(Graphics2D g, int x, int y, int w) {
        g.setColor(Estilo.altoContraste() ? Color.BLACK : new Color(16, 16, 24));
        g.fillRect(x, y, w, 50);
        g.setColor(Estilo.borde());
        g.drawLine(x, y, x + w, y);
        g.setFont(Estilo.fuente(Font.PLAIN, 12));
        g.setColor(Estilo.textoSuave());
        Dibujo.centrado(g, "W/S desplazar  ·  1-3 decidir  ·  M no molestar  ·  TAB cerrar", x + w / 2, y + 22);
        g.setFont(Estilo.fuente(Font.PLAIN, 11));
        String estado = telefono.noMolestar() ? "No molestar: ACTIVADO" : "No molestar: desactivado";
        g.setColor(telefono.noMolestar() ? Estilo.positivo() : Estilo.textoSuave());
        Dibujo.centrado(g, estado, x + w / 2, y + 40);
    }

    private void dibujarEfectos(Graphics2D g, int x, int yBase, int w) {
        if (tiempoEfectos <= 0 || ultimaDecision == null) {
            return;
        }
        float alfa = (float) Math.min(1, tiempoEfectos * 2);
        int chips = 0;
        for (String clave : IndicadorBienestar.TODOS) {
            if (ultimaDecision.efectoEn(clave) != 0) {
                chips++;
            }
        }
        if (chips == 0) {
            return;
        }
        g.setFont(Estilo.fuente(Font.BOLD, 13));
        int filaY = yBase - 40 - (int) ((3.2 - tiempoEfectos) * 6);
        int cxChip = x + 12;
        for (String clave : IndicadorBienestar.TODOS) {
            int d = ultimaDecision.efectoEn(clave);
            if (d == 0) {
                continue;
            }
            boolean bueno = IndicadorBienestar.esInverso(clave) ? d < 0 : d > 0;
            String texto = (d > 0 ? "+" : "−") + Math.abs(d) + " " + IndicadorBienestar.nombreVisible(clave);
            int cw = g.getFontMetrics().stringWidth(texto) + 18;
            if (cxChip + cw > x + w - 8) {
                cxChip = x + 12;
                filaY -= 32;
            }
            Color c = bueno ? Estilo.positivo() : Estilo.negativo();
            Dibujo.rectRedondo(g, cxChip, filaY, cw, 26, 13, Estilo.conAlfa(new Color(0, 0, 0), 0.85f * alfa));
            g.setColor(Estilo.conAlfa(c, alfa));
            g.setStroke(new BasicStroke(1.5f));
            g.draw(new RoundRectangle2D.Float(cxChip, filaY, cw, 26, 13, 13));
            g.drawString(texto, cxChip + 9, filaY + 18);
            cxChip += cw + 6;
        }
    }

    private void dibujarLlamada(Graphics2D g, int x, int y, int w, int h) {
        g.setColor(new Color(8, 10, 14, 245));
        g.fillRect(x, y, w, h);
        g.setFont(Estilo.fuente(Font.PLAIN, 16));
        g.setColor(Estilo.textoSuave());
        Dibujo.centrado(g, "Llamada entrante", x + w / 2, y + 140);
        g.setFont(Estilo.fuente(Font.BOLD, 26));
        g.setColor(Color.WHITE);
        Dibujo.centrado(g, Dibujo.corromper("Número desconocido", 0.3, (long) (ctx.tiempo * 5)), x + w / 2, y + 180);
        double pulso = 1 + 0.12 * Math.sin(ctx.tiempo * 8);
        int r = (int) (34 * pulso);
        Dibujo.rectRedondo(g, x + w / 2.0 - r, y + h - 170 - r, r * 2, r * 2, r * 2, new Color(200, 40, 40));
        g.setFont(Estilo.fuente(Font.PLAIN, 13));
        g.setColor(Estilo.textoSuave());
        Dibujo.centrado(g, "No puedes colgar.", x + w / 2, y + h - 100);
    }

    private void dibujarGlitchPantalla(Graphics2D g, int x, int y, int w, int h) {
        double c = ctx.corrupcion;
        if (c < 0.15) {
            return;
        }
        int lineas = (int) (c * 8);
        for (int i = 0; i < lineas; i++) {
            if (ctx.azar.nextDouble() > c * 0.6) {
                continue;
            }
            int ly = y + ctx.azar.nextInt(h);
            int lh = 1 + ctx.azar.nextInt(4);
            g.setColor(new Color(255, 255, 255, 20 + ctx.azar.nextInt(50)));
            g.fillRect(x, ly, w, lh);
            if (ctx.azar.nextDouble() < c * 0.3) {
                g.setColor(new Color(255, 0, 40, 60));
                g.fillRect(x + ctx.azar.nextInt(w), ly, 40 + ctx.azar.nextInt(120), lh + 6);
            }
        }
        g.setColor(new Color(0, 0, 0, (int) (40 * c)));
        for (int ly = y; ly < y + h; ly += 3) {
            g.drawLine(x, ly, x + w, ly);
        }
    }
}
