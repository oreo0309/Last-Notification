package juego.pantallas;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import juego.Juego;
import juego.feed.Notificacion;
import juego.interfaz.ContextoVisual;
import juego.interfaz.Dibujo;
import juego.interfaz.Estilo;
import juego.motor.Camara;
import juego.mundo.Mundo3D;

public class PantallaMenu extends Pantalla {

    private static final String[] OPCIONES = {"Iniciar juego", "Ayuda", "Configuración", "Récords", "Salir"};

    private int opciones;
    private final Rectangle[] zonas = new Rectangle[OPCIONES.length];
    private final Camara camara = new Camara(8.5, 0.9, Math.PI / 2);
    private final ContextoVisual ctx = new ContextoVisual();
    private final Notificacion aviso = new Notificacion("@tu_seguidor", "comenzó a seguirte", 3, false);
    private double proximaAparicion = 9;
    private double tiempoAviso = -1;
    private double proximoAviso = 4;

    public PantallaMenu(Juego juego) {
        super("PantallaMenu", juego);
    }

    @Override
    public void entrar() {
        Mundo3D m = juego.mundo();
        m.configurarNoche(1);
        m.casa().puerta(7, 4).fijar(1);
        m.iluminacion().linterna = 0;
        m.iluminacion().brilloPantalla = 0;
        m.efectos().oscurecer = 0;
        m.efectos().tinteRojo = 0;
        m.efectos().glitch = 0;
        m.efectos().aberracion = 0;
        juego.gestorAudio().ambiente(1f);
        juego.gestorAudio().subirTension(0.22f);
        juego.gestorAudio().lluvia(false);
    }

    @Override
    public void salir() {
        juego.mundo().seguidor().apagar();
    }

    @Override
    public void alReanudar() {
        juego.mundo().efectos().oscurecer = 0;
    }

    @Override
    public void actualizar(double dt) {
        tiempo += dt;
        ctx.tiempo = tiempo;
        Mundo3D m = juego.mundo();
        camara.y = 0.9 + Math.sin(tiempo * 0.05) * 0.25;
        camara.angulo = Math.PI / 2 + Math.sin(tiempo * 0.13) * 0.05;
        camara.ojo = 0.6 + Math.sin(tiempo * 0.7) * 0.004;
        proximaAparicion -= dt;
        if (proximaAparicion <= 0) {
            proximaAparicion = 16 + juego.azar().nextDouble() * 12;
            m.seguidor().aparecer(8.5, 11.4, 2.5);
            m.parpadeo(0.8);
        }
        m.seguidor().actualizar(dt, camara.x, camara.y, camara.angulo, true, false, 0.1f);
        m.efectos().grano = 0.08f;
        m.efectos().fuerzaVineta = 0.85f;
        m.efectos().desaturacion = 0.3f;
        m.actualizar(dt, 0.25f, camara.x, camara.y, camara.angulo);
        proximoAviso -= dt;
        if (proximoAviso <= 0 && tiempoAviso < 0) {
            tiempoAviso = 0;
            juego.gestorAudio().reproducir("notificacion_rara", 0.35f, 0f, null);
        }
        if (tiempoAviso >= 0) {
            tiempoAviso += dt;
            if (tiempoAviso > 4.5) {
                tiempoAviso = -1;
                proximoAviso = 14 + juego.azar().nextDouble() * 10;
            }
        }
        int mx = juego.entrada().ratonX();
        int my = juego.entrada().ratonY();
        for (int i = 0; i < zonas.length; i++) {
            if (zonas[i] != null && zonas[i].contains(mx, my) && opciones != i) {
                opciones = i;
                juego.gestorAudio().reproducir("menu_mover", 0.3f);
            }
        }
    }

    @Override
    public void dibujar(Graphics2D g) {
        BufferedImage img = juego.mundo().renderizar(camara);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(img, 0, 0, 1280, 720, null);
        Dibujo.suavizado(g);
        g.setPaint(new GradientPaint(0, 0, new Color(0, 0, 0, 235), 700, 0, new Color(0, 0, 0, 40)));
        g.fillRect(0, 0, 1280, 720);
        double glitch = (Math.sin(tiempo * 3.1) > 0.97 || juego.azar().nextDouble() < 0.01) ? 0.9 : 0.12;
        g.setFont(Estilo.fija("SansSerif", Font.BOLD, 64));
        g.setColor(new Color(236, 236, 244));
        Dibujo.textoGlitch(g, "ÚLTIMA", 80, 170, glitch, juego.azar());
        Dibujo.textoGlitch(g, "NOTIFICACIÓN", 80, 238, glitch, juego.azar());
        Dibujo.rectRedondo(g, 520, 120, 44, 44, 44, Estilo.peligro());
        g.setFont(Estilo.fija("SansSerif", Font.BOLD, 24));
        g.setColor(Color.WHITE);
        Dibujo.centrado(g, "1", 542, 151);
        g.setFont(Estilo.fuente(Font.PLAIN, 16));
        g.setColor(new Color(190, 190, 205));
        g.drawString("Un juego de terror psicológico sobre el uso saludable de las redes sociales", 82, 278);
        int y = 350;
        for (int i = 0; i < OPCIONES.length; i++) {
            boolean sel = i == opciones;
            g.setFont(Estilo.fuente(sel ? Font.BOLD : Font.PLAIN, sel ? 28 : 24));
            int w = g.getFontMetrics().stringWidth(OPCIONES[i]);
            zonas[i] = new Rectangle(70, y - 32, Math.max(300, w + 60), 44);
            if (sel) {
                Dibujo.rectRedondo(g, 70, y - 32, w + 60, 44, 12, Estilo.conAlfa(Estilo.acento(), 0.25f));
                g.setColor(Estilo.acento());
                g.setStroke(new BasicStroke(2f));
                g.drawLine(78, y - 22, 78, y + 2);
            }
            g.setColor(sel ? Color.WHITE : new Color(170, 170, 185));
            g.drawString(OPCIONES[i], 96, y);
            y += 56;
        }
        g.setFont(Estilo.fuente(Font.PLAIN, 13));
        g.setColor(new Color(150, 150, 165));
        g.drawString("↑/↓ o ratón para elegir  ·  ENTER para aceptar  ·  F11 pantalla completa", 82, 668);
        g.drawString("PowerGames · Universidad del Norte · Programación Orientada a Objetos", 82, 692);
        if (tiempoAviso >= 0) {
            float alfa = (float) Math.min(1, Math.min(tiempoAviso, 4.5 - tiempoAviso) * 2.5);
            aviso.mostrarAviso(g, 1280 - 400, 30, 380, alfa, ctx);
        }
        if (!juego.gestorAudio().disponible()) {
            g.setFont(Estilo.fuente(Font.ITALIC, 12));
            g.setColor(Estilo.advertencia());
            g.drawString("No se encontró un dispositivo de audio: el juego funcionará sin sonido (con subtítulos).", 760, 700);
        }
    }

    private void elegir() {
        juego.gestorAudio().reproducir("menu_elegir", 0.4f);
        switch (opciones) {
            case 0:
                juego.cambiarPantalla(new PantallaNombre(juego));
                break;
            case 1:
                juego.apilarPantalla(new PantallaAyuda(juego, false));
                break;
            case 2:
                juego.apilarPantalla(new PantallaConfiguracion(juego));
                break;
            case 3:
                juego.apilarPantalla(new PantallaRecords(juego));
                break;
            default:
                juego.solicitarSalida();
                break;
        }
    }

    @Override
    public void alPresionarTecla(int codigo, char caracter) {
        switch (codigo) {
            case KeyEvent.VK_UP:
            case KeyEvent.VK_W:
                opciones = Math.floorMod(opciones - 1, OPCIONES.length);
                juego.gestorAudio().reproducir("menu_mover", 0.3f);
                break;
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_S:
                opciones = Math.floorMod(opciones + 1, OPCIONES.length);
                juego.gestorAudio().reproducir("menu_mover", 0.3f);
                break;
            case KeyEvent.VK_ENTER:
            case KeyEvent.VK_SPACE:
            case KeyEvent.VK_E:
                elegir();
                break;
            case KeyEvent.VK_ESCAPE:
                opciones = OPCIONES.length - 1;
                break;
            default:
                break;
        }
    }

    @Override
    public void alHacerClic(int x, int y, int boton) {
        for (int i = 0; i < zonas.length; i++) {
            if (zonas[i] != null && zonas[i].contains(x, y)) {
                opciones = i;
                elegir();
                return;
            }
        }
    }
}
