package juego.pantallas;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import juego.IndicadorBienestar;
import juego.Jugador;
import juego.Mision;
import juego.Telefono;
import juego.audio.GestorAudio;
import juego.feed.Notificacion;
import juego.interfaz.Dibujo;
import juego.interfaz.Estilo;
import juego.mundo.Habitacion;
import juego.mundo.Interactuable;

public class Hud {

    private final PantallaFeed pantalla;
    private boolean mostrarMisiones = true;
    private boolean mostrarIndicadores = true;
    private GestorAudio.Subtitulo[] subtitulos = new GestorAudio.Subtitulo[0];
    private double ultimoTiempo;

    public Hud(PantallaFeed pantalla) {
        this.pantalla = pantalla;
    }

    public void alternarMisiones() {
        mostrarMisiones = !mostrarMisiones;
    }

    public void alternarIndicadores() {
        mostrarIndicadores = !mostrarIndicadores;
    }

    public void dibujar(Graphics2D g) {
        boolean telefonoAbierto = pantalla.telefonoUI().abierto();
        dibujarReloj(g);
        if (mostrarMisiones && !telefonoAbierto) {
            dibujarMisiones(g);
        }
        if (mostrarIndicadores) {
            dibujarIndicadores(g, telefonoAbierto);
        }
        if (!telefonoAbierto) {
            dibujarMira(g);
            dibujarInteraccion(g);
            dibujarAviso(g);
        }
    }

    public void dibujarSobreTelefono(Graphics2D g) {
        dibujarPista(g);
        dibujarOrientacion(g);
        double ahora = pantalla.tiempoTotal();
        double dt = Math.max(0, ahora - ultimoTiempo);
        ultimoTiempo = ahora;
        dibujarSubtitulos(g, dt);
    }

    private void dibujarReloj(Graphics2D g) {
        Jugador j = pantalla.jugador();
        Telefono t = pantalla.telefono();
        int x = 1280 - 200;
        int y = 18;
        Dibujo.panel(g, x, y, 182, 64, 16);
        g.setFont(Estilo.mono(Font.BOLD, 28));
        boolean tarde = j.horaActual() >= 1440 + 150;
        g.setColor(tarde ? Estilo.peligro() : Estilo.texto());
        String hora = j.horaTexto();
        if (pantalla.tension() > 85 && pantalla.juego().azar().nextDouble() < 0.04) {
            hora = "03:12";
        }
        g.drawString(hora, x + 14, y + 38);
        g.setFont(Estilo.fuente(Font.PLAIN, 11));
        g.setColor(Estilo.textoSuave());
        g.drawString("Noche " + pantalla.juego().nocheActual() + " · " + pantalla.juego().noche().dia(), x + 14, y + 55);
        if (t.enMano()) {
            Dibujo.bateria(g, x + 118, y + 18, 26, 12, t.bateria() / 100.0, Estilo.texto());
            g.setFont(Estilo.fuente(Font.PLAIN, 11));
            g.setColor(Estilo.textoSuave());
            g.drawString((int) t.bateria() + "%", x + 120, y + 46);
        } else {
            g.setFont(Estilo.fuente(Font.PLAIN, 10));
            g.setColor(Estilo.textoSuave());
            g.drawString("cargando", x + 120, y + 30);
        }
        int iy = y + 72;
        int ix = 1280 - 30;
        if (t.linterna()) {
            g.setColor(Estilo.advertencia());
            g.fill(new Ellipse2D.Double(ix - 4, iy, 12, 12));
            g.setFont(Estilo.fuente(Font.PLAIN, 11));
            g.drawString("Linterna", ix - 58, iy + 10);
            iy += 18;
        }
        if (t.noMolestar()) {
            Dibujo.luna(g, ix - 4, iy, 12, Estilo.acento());
            g.setFont(Estilo.fuente(Font.PLAIN, 11));
            g.setColor(Estilo.acento());
            g.drawString("No molestar", ix - 76, iy + 10);
        }
    }

    private void dibujarMisiones(Graphics2D g) {
        int x = 18;
        int y = 18;
        int ancho = 360;
        Font fuente = Estilo.fuente(Font.PLAIN, 14);
        g.setFont(fuente);
        int alto = 40;
        Mision pendiente = pantalla.juego().misionPendiente();
        for (Mision m : pantalla.juego().misiones()) {
            alto += Dibujo.medirParrafo(g, fuente, m.objetivo(), ancho - 50, 1.05f) + 8;
        }
        if (pendiente != null) {
            alto += Dibujo.medirParrafo(g, Estilo.fuente(Font.ITALIC, 12), pendiente.pista(), ancho - 50, 1.0f) + 4;
        }
        Dibujo.panel(g, x, y, ancho, alto, 16);
        g.setFont(Estilo.fuente(Font.BOLD, 13));
        g.setColor(Estilo.acento());
        g.drawString("OBJETIVOS  (J para ocultar)", x + 16, y + 24);
        int cy = y + 36;
        for (Mision m : pantalla.juego().misiones()) {
            Dibujo.casilla(g, x + 16, cy + 1, 16, m.cumplida(), m.cumplida() ? Estilo.positivo() : Estilo.texto());
            g.setFont(fuente);
            g.setColor(m.cumplida() ? Estilo.textoSuave() : Estilo.texto());
            int h = Dibujo.parrafo(g, m.objetivo(), x + 42, cy, ancho - 50, 1.05f);
            if (m.cumplida()) {
                g.setColor(Estilo.conAlfa(Estilo.textoSuave(), 0.8f));
                g.drawLine(x + 42, cy + 9, x + 42 + Math.min(ancho - 60, g.getFontMetrics().stringWidth(m.objetivo())), cy + 9);
            }
            cy += h + 8;
            if (m == pendiente) {
                g.setFont(Estilo.fuente(Font.ITALIC, 12));
                g.setColor(Estilo.advertencia());
                cy += Dibujo.parrafo(g, m.pista(), x + 42, cy - 4, ancho - 50, 1.0f) + 4;
            }
        }
    }

    private void dibujarIndicadores(Graphics2D g, boolean compacto) {
        IndicadorBienestar b = pantalla.jugador().bienestar();
        int x = compacto ? 18 : 18;
        int y = 720 - 178;
        int ancho = compacto ? 300 : 300;
        Dibujo.panel(g, x, y, ancho, 160, 16);
        g.setFont(Estilo.fuente(Font.BOLD, 12));
        g.setColor(Estilo.acento());
        g.drawString("BIENESTAR  (I para ocultar)", x + 14, y + 22);
        int cy = y + 34;
        double glitch = Math.max(0, (pantalla.tension() - 70) / 30.0);
        for (String clave : IndicadorBienestar.TODOS) {
            int v = b.valor(clave);
            boolean inverso = IndicadorBienestar.esInverso(clave);
            double bueno = inverso ? 1 - v / 100.0 : v / 100.0;
            Color c = bueno > 0.6 ? Estilo.positivo() : (bueno > 0.35 ? Estilo.advertencia() : Estilo.negativo());
            g.setFont(Estilo.fuente(Font.PLAIN, 12));
            g.setColor(Estilo.texto());
            int sacudida = glitch > 0 && pantalla.juego().azar().nextDouble() < glitch * 0.3 ? 2 : 0;
            g.drawString(IndicadorBienestar.nombreVisible(clave), x + 14 + sacudida, cy + 12);
            Dibujo.barra(g, x + 116, cy + 3, 130, 10, v / 100.0, c);
            g.setColor(Estilo.textoSuave());
            g.drawString(String.valueOf(v), x + 256, cy + 12);
            if (inverso) {
                g.setFont(Estilo.fuente(Font.PLAIN, 9));
                g.drawString("(baja = bien)", x + 116, cy + 25);
            }
            cy += 24;
        }
    }

    private void dibujarMira(Graphics2D g) {
        boolean hay = pantalla.enfrente() != null;
        g.setColor(hay ? Estilo.acento() : new Color(255, 255, 255, 120));
        int r = hay ? 5 : 3;
        g.fill(new Ellipse2D.Double(640 - r, 360 - r, r * 2, r * 2));
    }

    private void dibujarInteraccion(Graphics2D g) {
        Interactuable i = pantalla.enfrente();
        if (i == null) {
            return;
        }
        String texto = textoAccion(i);
        g.setFont(Estilo.fuente(Font.BOLD, 17));
        FontMetrics fm = g.getFontMetrics();
        int w = fm.stringWidth(texto) + 70;
        int x = 640 - w / 2;
        int y = 400;
        Dibujo.panel(g, x, y, w, 40, 14);
        Dibujo.rectRedondo(g, x + 10, y + 8, 26, 24, 8, Estilo.acento());
        g.setColor(Estilo.altoContraste() ? Color.BLACK : Color.WHITE);
        Dibujo.centrado(g, "E", x + 23, y + 26);
        g.setColor(Estilo.texto());
        g.drawString(texto, x + 48, y + 26);
    }

    private String textoAccion(Interactuable i) {
        Telefono t = pantalla.telefono();
        switch (i.tipo()) {
            case PUERTA:
                if (i.puerta().bloqueada()) {
                    return "Intentar abrir";
                }
                return i.puerta().estaAbierta() ? "Cerrar la puerta" : "Abrir la puerta";
            case CAMA:
                return pantalla.juego().nocheActual() == 3 ? "Acostarte" : "Acostarte a dormir";
            case ESCRITORIO:
                return t.enMano() ? "Dejar el teléfono cargando" : "Tomar el teléfono";
            case LAMPARA:
                return pantalla.mundo().lamparaEncendida() ? "Apagar la lámpara" : "Encender la lámpara";
            case ROUTER:
                return pantalla.mundo().routerEncendido() && pantalla.juego().nocheActual() == 3 ? "Apagar el router" : "Mirar el router";
            case VENTANA:
                return "Mirar por la ventana";
            case ESPEJO:
                return "Mirarte al espejo";
            case CUADRO:
                return "Mirar la foto";
            case PUERTA_PRINCIPAL:
                return "Revisar la puerta principal";
            case NEVERA:
                return "Abrir la nevera";
            case TV:
                return pantalla.mundo().tvEncendida() ? "Apagar el televisor" : "Mirar el televisor";
            case ARMARIO:
                return "Revisar el armario";
            case PELUCHE:
                return "Mirar el peluche";
            case LIBRERO:
                return "Mirar los libros";
            default:
                return "Interactuar";
        }
    }

    private void dibujarAviso(Graphics2D g) {
        Notificacion n = pantalla.telefonoUI().avisoActual();
        if (n == null || !pantalla.telefono().enMano()) {
            return;
        }
        double t = pantalla.telefonoUI().tiempoAviso();
        float alfa = (float) Math.min(1, Math.min(t, 4.5 - t) * 3);
        int x = 1280 - 400;
        int y = 120 + (int) ((1 - Math.min(1, (4.5 - t) * 4)) * -20);
        n.mostrarAviso(g, x, y, 380, alfa, pantalla.telefonoUI().contexto());
        g.setFont(Estilo.fuente(Font.PLAIN, 11));
        g.setColor(Estilo.conAlfa(Estilo.textoSuave(), alfa));
        g.drawString("TAB para abrir el teléfono", x + 250, y + 86);
    }

    private void dibujarPista(Graphics2D g) {
        String p = pantalla.pista();
        if (p == null) {
            return;
        }
        float alfa = (float) Math.min(1, pantalla.tiempoPista() * 2);
        g.setFont(Estilo.fuente(Font.BOLD, 16));
        boolean abierto = pantalla.telefonoUI().abierto();
        int w = Math.min(abierto ? 900 : 680, g.getFontMetrics().stringWidth(p) + 40);
        int alto = Dibujo.medirParrafo(g, g.getFont(), p, w - 40, 1.1f) + 20;
        int x = 640 - w / 2;
        int y = abierto ? 4 : 452;
        g.setColor(Estilo.conAlfa(Estilo.fondoPanel(), Math.min(alfa, Estilo.altoContraste() ? 1f : 0.85f)));
        g.fillRoundRect(x, y, w, alto, 16, 16);
        g.setColor(Estilo.conAlfa(Estilo.texto(), alfa));
        Dibujo.parrafoCentrado(g, p, 640, y + 26, w - 40, 1.1f);
    }

    private void dibujarOrientacion(Graphics2D g) {
        String o = pantalla.orientacionActual();
        if (o == null) {
            return;
        }
        double t = pantalla.tiempoOrientacion();
        float alfa = (float) Math.min(1, Math.min(t, 5.5 - t) * 3);
        g.setFont(Estilo.fuente(Font.PLAIN, 15));
        int w = 520;
        int alto = Dibujo.medirParrafo(g, g.getFont(), o, w - 70, 1.1f) + 24;
        boolean abierto = pantalla.telefonoUI().abierto();
        if (abierto) {
            w = 404;
            alto = Dibujo.medirParrafo(g, g.getFont(), o, w - 70, 1.1f) + 24;
        }
        int x = abierto ? 860 : 640 - w / 2;
        int y = abierto ? 300 : 720 - 150 - alto;
        g.setColor(Estilo.conAlfa(Estilo.fondoPanel(), alfa * 0.95f));
        g.fillRoundRect(x, y, w, alto, 16, 16);
        g.setColor(Estilo.conAlfa(Estilo.acento(), alfa));
        g.setStroke(new BasicStroke(2f));
        g.drawRoundRect(x, y, w, alto, 16, 16);
        g.fill(new Ellipse2D.Double(x + 16, y + alto / 2.0 - 13, 26, 26));
        g.setColor(Estilo.conAlfa(Estilo.altoContraste() ? Color.BLACK : Color.WHITE, alfa));
        g.setFont(Estilo.fuente(Font.BOLD, 16));
        Dibujo.centrado(g, "i", x + 29, (int) (y + alto / 2.0 + 6));
        g.setFont(Estilo.fuente(Font.PLAIN, 15));
        g.setColor(Estilo.conAlfa(Estilo.texto(), alfa));
        Dibujo.parrafo(g, o, x + 54, y + 12, w - 70, 1.1f);
    }

    public void dibujarSubtitulos(Graphics2D g, double dt) {
        subtitulos = pantalla.audio().subtitulosActivos(dt);
        if (!pantalla.configuracion().subtitulos() || subtitulos.length == 0) {
            return;
        }
        boolean abierto = pantalla.telefonoUI().abierto();
        g.setFont(Estilo.fuente(Font.BOLD, abierto ? 15 : 17));
        FontMetrics fm = g.getFontMetrics();
        int centro = abierto ? 1062 : 640;
        int y = 720 - 40;
        for (int i = subtitulos.length - 1; i >= 0; i--) {
            String texto = subtitulos[i].texto;
            if (abierto && fm.stringWidth(texto) > 380) {
                texto = texto.substring(0, Math.min(texto.length(), 46)) + "...]";
            }
            int w = fm.stringWidth(texto) + 28;
            g.setColor(new Color(0, 0, 0, Estilo.altoContraste() ? 255 : 190));
            g.fillRoundRect(centro - w / 2, y - 24, w, 32, 10, 10);
            g.setColor(texto.contains("Susurro") || texto.contains("@tu_seguidor") ? new Color(255, 200, 200) : Color.WHITE);
            Dibujo.centrado(g, texto, centro, y - 2);
            y -= 38;
        }
    }

    public void dibujarUltimaNotificacion(Graphics2D g, double t) {
        int alfa = (int) (Math.min(1, t * 1.5) * 220);
        g.setColor(new Color(0, 0, 0, alfa));
        g.fillRect(0, 0, 1280, 720);
        g.setFont(Estilo.mono(Font.BOLD, 120));
        g.setColor(new Color(225, 30, 42, Math.min(255, alfa + 30)));
        Dibujo.textoGlitch(g, "03:12", 640 - g.getFontMetrics().stringWidth("03:12") / 2, 340, Math.min(1, t / 2), pantalla.juego().azar());
        g.setFont(Estilo.fuente(Font.PLAIN, 28));
        g.setColor(new Color(255, 255, 255, Math.min(255, alfa)));
        Dibujo.centrado(g, "Sigues despierto.", 640, 410);
        if (t > 1.5) {
            g.setFont(Estilo.fuente(Font.ITALIC, 18));
            g.setColor(new Color(200, 200, 200, (int) (Math.min(1, (t - 1.5)) * 255)));
            Dibujo.centrado(g, "La última notificación siempre llega a esta hora.", 640, 450);
        }
    }

    static String nombreHabitacion(Habitacion h) {
        return h == null ? "" : h.nombre();
    }
}
