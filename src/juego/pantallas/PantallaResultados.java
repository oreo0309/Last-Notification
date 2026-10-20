package juego.pantallas;

import estructuras.ArbolBinarioBusqueda;
import estructuras.ListaEnlazada;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import juego.Decision;
import juego.IndicadorBienestar;
import juego.Juego;
import juego.Jugador;
import juego.Logro;
import juego.TipoFinal;
import juego.interfaz.Dibujo;
import juego.interfaz.Estilo;

public class PantallaResultados extends Pantalla {

    private final TipoFinal tipoFinal;
    private final int puntaje;
    private final ListaEnlazada<String> opciones = new ListaEnlazada<>();
    private final ListaEnlazada<Rectangle> zonas = new ListaEnlazada<>();
    private final ListaEnlazada<Decision> mejores = new ListaEnlazada<>();
    private final ListaEnlazada<Decision> peores = new ListaEnlazada<>();
    private int seleccion;

    public PantallaResultados(Juego juego, TipoFinal tipoFinal, int puntaje) {
        super("PantallaResultados", juego);
        this.tipoFinal = tipoFinal;
        this.puntaje = puntaje;
    }

    @Override
    public void entrar() {
        if (tipoFinal == TipoFinal.ULTIMA_NOTIFICACION) {
            opciones.agregar("Reintentar la noche " + juego.nocheActual());
        }
        opciones.agregar("Jugar de nuevo");
        opciones.agregar("Menú principal");
        ArbolBinarioBusqueda<Integer, Decision> arbol = new ArbolBinarioBusqueda<>();
        for (Decision d : juego.jugador().historial()) {
            if (d.impacto() != 0) {
                arbol.insertar(d.impacto(), d);
            }
        }
        for (ArbolBinarioBusqueda.Par<Integer, Decision> p : arbol.mayoresPrimero(3)) {
            if (p.clave() > 0) {
                mejores.agregar(p.valor());
            }
        }
        int i = 0;
        for (ArbolBinarioBusqueda.Par<Integer, Decision> p : arbol.enOrden()) {
            if (i++ >= 3 || p.clave() >= 0) {
                break;
            }
            peores.agregar(p.valor());
        }
        juego.gestorAudio().detenerTodo();
        juego.gestorAudio().lluvia(false);
        juego.gestorAudio().subirTension(tipoFinal.positivo() ? 0f : 0.35f);
        juego.gestorAudio().zumbidoOido(0f);
        if (tipoFinal.positivo()) {
            juego.gestorAudio().ambiente(0.2f);
            juego.gestorAudio().reproducir("amanecer", 0.7f, 0f, "[Amanece. Se escuchan pájaros]");
        } else {
            juego.gestorAudio().reproducir("stinger_suave", 0.5f);
        }
    }

    @Override
    public void actualizar(double dt) {
        tiempo += dt;
        int mx = juego.entrada().ratonX();
        int my = juego.entrada().ratonY();
        int i = 0;
        for (Rectangle r : zonas) {
            if (r.contains(mx, my)) {
                seleccion = i;
            }
            i++;
        }
    }

    @Override
    public void salir() {
        juego.gestorAudio().ambiente(1f);
    }

    @Override
    public void dibujar(Graphics2D g) {
        if (tipoFinal.positivo()) {
            double k = Math.min(1, tiempo / 6);
            g.setPaint(new GradientPaint(0, 0, mezcla(new Color(10, 12, 30), new Color(70, 90, 150), k),
                    0, 720, mezcla(new Color(20, 14, 30), new Color(240, 160, 110), k)));
        } else {
            g.setPaint(new GradientPaint(0, 0, new Color(20, 2, 6), 0, 720, new Color(4, 2, 4)));
        }
        g.fillRect(0, 0, 1280, 720);
        Dibujo.suavizado(g);
        if (!tipoFinal.positivo()) {
            for (int i = 0; i < 300; i++) {
                int v = juego.azar().nextInt(60);
                g.setColor(new Color(v, v / 3, v / 3));
                g.fillRect(juego.azar().nextInt(1280), juego.azar().nextInt(720), 2, 2);
            }
        }
        float alfa = (float) Math.min(1, tiempo / 1.2);
        g.setFont(Estilo.fija("SansSerif", Font.BOLD, 50));
        g.setColor(Estilo.conAlfa(Color.WHITE, alfa));
        Dibujo.textoGlitch(g, tipoFinal.titulo().toUpperCase(), 70, 92, tipoFinal.positivo() ? 0 : 0.4, juego.azar());
        g.setFont(Estilo.fuente(Font.ITALIC, 18));
        g.setColor(Estilo.conAlfa(new Color(230, 230, 240), alfa));
        Dibujo.parrafo(g, tipoFinal.relato(), 72, 112, 700, 1.2f);
        Jugador j = juego.jugador();
        IndicadorBienestar b = j.bienestar();
        dibujarLogros(g, j);
        Dibujo.panel(g, 70, 200, 420, 210, 16);
        g.setFont(Estilo.fuente(Font.BOLD, 15));
        g.setColor(Estilo.acento());
        g.drawString("TU BIENESTAR AL FINAL", 90, 228);
        int y = 246;
        for (String clave : IndicadorBienestar.TODOS) {
            int v = b.valor(clave);
            boolean inverso = IndicadorBienestar.esInverso(clave);
            double bueno = inverso ? 1 - v / 100.0 : v / 100.0;
            Color c = bueno > 0.6 ? Estilo.positivo() : (bueno > 0.35 ? Estilo.advertencia() : Estilo.negativo());
            g.setFont(Estilo.fuente(Font.PLAIN, 14));
            g.setColor(Estilo.texto());
            g.drawString(IndicadorBienestar.nombreVisible(clave) + (inverso ? " (baja = bien)" : ""), 90, y + 12);
            Dibujo.barra(g, 290, y + 2, 140, 11, v / 100.0 * Math.min(1, tiempo / 2), c);
            g.drawString(String.valueOf(v), 442, y + 12);
            y += 30;
        }
        Dibujo.panel(g, 510, 200, 330, 210, 16);
        g.setFont(Estilo.fuente(Font.BOLD, 15));
        g.setColor(Estilo.acento());
        g.drawString("TU NOCHE EN NÚMEROS", 530, 228);
        g.setFont(Estilo.fuente(Font.PLAIN, 14));
        g.setColor(Estilo.texto());
        String[] lineas = {
            "Horas dormidas: " + horas(j),
            "Publicaciones vistas: " + j.publicacionesVistas(),
            "Minutos en la pantalla: " + Math.round(j.minutosEnPantalla()),
            "Decisiones saludables: " + j.decisionesSaludables(),
            "Decisiones dañinas: " + j.decisionesDaninas(),
            "Contenido reportado: " + j.reportes(),
            "Veces que te alcanzó: " + j.vecesAtrapado()
        };
        int ly = 254;
        for (String l : lineas) {
            g.drawString(l, 530, ly);
            ly += 22;
        }
        Dibujo.panel(g, 860, 200, 350, 210, 16);
        g.setFont(Estilo.fuente(Font.BOLD, 15));
        g.setColor(Estilo.acento());
        g.drawString("PUNTAJE", 880, 228);
        g.setFont(Estilo.fija("SansSerif", Font.BOLD, 54));
        g.setColor(Estilo.texto());
        int mostrado = (int) (puntaje * Math.min(1, tiempo / 2.5));
        g.drawString(String.valueOf(mostrado), 880, 292);
        g.setFont(Estilo.fuente(Font.PLAIN, 13));
        g.setColor(Estilo.textoSuave());
        Dibujo.parrafo(g, tipoFinal == TipoFinal.ULTIMA_NOTIFICACION
                ? "Este final no entra en los récords. ¡Inténtalo otra vez!"
                : "Guardado en los récords con el nombre " + j.nombre() + ".", 880, 312, 310, 1.15f);
        Dibujo.panel(g, 70, 428, 640, 200, 16);
        g.setFont(Estilo.fuente(Font.BOLD, 15));
        g.setColor(Estilo.positivo());
        g.drawString("Decisiones que te ayudaron", 90, 455);
        g.setFont(Estilo.fuente(Font.PLAIN, 13));
        g.setColor(Estilo.texto());
        int dy = 476;
        if (mejores.estaVacia()) {
            g.drawString("Todavía ninguna. ¡La próxima vez!", 100, dy);
            dy += 20;
        }
        for (Decision d : mejores) {
            g.drawString("+  " + recortar(g, d.texto(), 580), 100, dy);
            dy += 20;
        }
        dy += 8;
        g.setFont(Estilo.fuente(Font.BOLD, 15));
        g.setColor(Estilo.negativo());
        g.drawString("Decisiones que te afectaron", 90, dy);
        dy += 21;
        g.setFont(Estilo.fuente(Font.PLAIN, 13));
        g.setColor(Estilo.texto());
        if (peores.estaVacia()) {
            g.drawString("Ninguna. ¡Muy bien!", 100, dy);
        }
        for (Decision d : peores) {
            g.drawString("−  " + recortar(g, d.texto(), 580), 100, dy);
            dy += 20;
        }
        Dibujo.panel(g, 730, 428, 480, 200, 16);
        g.setFont(Estilo.fuente(Font.BOLD, 15));
        g.setColor(Estilo.acento());
        g.drawString("PARA PENSAR", 750, 455);
        g.setFont(Estilo.fuente(Font.PLAIN, 15));
        g.setColor(Estilo.texto());
        Dibujo.parrafo(g, tipoFinal.reflexion(), 750, 466, 440, 1.2f);
        zonas.limpiar();
        int x = 70;
        int i = 0;
        for (String o : opciones) {
            boolean sel = i == seleccion;
            g.setFont(Estilo.fuente(Font.BOLD, 17));
            int w = g.getFontMetrics().stringWidth(o) + 50;
            Rectangle r = new Rectangle(x, 648, w, 46);
            zonas.agregar(r);
            Dibujo.rectRedondo(g, x, 648, w, 46, 14, sel ? Estilo.acento() : Estilo.fondoBoton());
            g.setColor(sel ? (Estilo.altoContraste() ? Color.BLACK : Color.WHITE) : Estilo.texto());
            Dibujo.centrado(g, o, x + w / 2, 677);
            x += w + 14;
            i++;
        }
    }

    private void dibujarLogros(Graphics2D g, Jugador j) {
        Logro[] todos = Logro.values();
        int x = 860;
        int y = 40;
        Dibujo.panel(g, x, y, 350, 140, 16);
        g.setFont(Estilo.fuente(Font.BOLD, 15));
        g.setColor(Estilo.advertencia());
        g.drawString("LOGROS  " + j.logros().tamano() + " / " + todos.length, x + 20, y + 28);
        for (int i = 0; i < todos.length; i++) {
            double cx = x + 34 + i * 35.5;
            double cy = y + 66;
            if (j.logros().contiene(todos[i])) {
                Medalla.dibujar(g, cx, cy, 15, (float) Math.min(1, Math.max(0, (tiempo - 1.0 - i * 0.12) * 3)));
            } else {
                g.setColor(new Color(255, 255, 255, 40));
                g.fill(new java.awt.geom.Ellipse2D.Double(cx - 15, cy - 15, 30, 30));
            }
        }
        g.setFont(Estilo.fuente(Font.PLAIN, 12));
        g.setColor(Estilo.textoSuave());
        String ultimo = j.logros().estaVacia() ? "Gana logros con hábitos saludables." : "Último: " + j.logros().ultimo().nombre();
        g.drawString(ultimo, x + 20, y + 110);
        g.drawString("Cada logro suma 25 puntos.", x + 20, y + 127);
    }

    private static String recortar(Graphics2D g, String t, int ancho) {
        if (g.getFontMetrics().stringWidth(t) <= ancho) {
            return t;
        }
        String r = t;
        while (r.length() > 3 && g.getFontMetrics().stringWidth(r + "...") > ancho) {
            r = r.substring(0, r.length() - 1);
        }
        return r + "...";
    }

    private static String horas(Jugador j) {
        StringBuilder sb = new StringBuilder();
        double[] h = j.horasDormidas();
        for (int i = 0; i < h.length; i++) {
            if (i > 0) {
                sb.append(" · ");
            }
            sb.append(h[i] <= 0 ? "—" : String.format("%.1f h", h[i]));
        }
        return sb.toString();
    }

    private static Color mezcla(Color a, Color b, double t) {
        return new Color((int) (a.getRed() + (b.getRed() - a.getRed()) * t),
                (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                (int) (a.getBlue() + (b.getBlue() - a.getBlue()) * t));
    }

    private void elegir() {
        String o = opciones.obtener(seleccion);
        juego.gestorAudio().reproducir("menu_elegir", 0.4f);
        if (o.startsWith("Reintentar")) {
            juego.reintentarNoche();
        } else if (o.startsWith("Jugar")) {
            juego.nuevaPartida(juego.jugador().nombre());
        } else {
            juego.cambiarPantalla(new PantallaMenu(juego));
        }
    }

    @Override
    public void alPresionarTecla(int codigo, char caracter) {
        if (tiempo < 1.0) {
            return;
        }
        switch (codigo) {
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
            case KeyEvent.VK_UP:
                seleccion = Math.floorMod(seleccion - 1, opciones.tamano());
                break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_TAB:
                seleccion = Math.floorMod(seleccion + 1, opciones.tamano());
                break;
            case KeyEvent.VK_ENTER:
            case KeyEvent.VK_SPACE:
                elegir();
                break;
            case KeyEvent.VK_ESCAPE:
                juego.cambiarPantalla(new PantallaMenu(juego));
                break;
            default:
                break;
        }
    }

    @Override
    public void alHacerClic(int x, int y, int boton) {
        if (tiempo < 1.0) {
            return;
        }
        int i = 0;
        for (Rectangle r : zonas) {
            if (r.contains(x, y)) {
                seleccion = i;
                elegir();
                return;
            }
            i++;
        }
    }
}
