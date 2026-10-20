package juego.pantallas;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import juego.Juego;
import juego.interfaz.Dibujo;
import juego.interfaz.Estilo;

public class PantallaPausa extends Pantalla {

    private static final String[] OPCIONES = {"Continuar", "Ayuda", "Configuración", "Volver al menú principal"};

    private final Pantalla fondo;
    private int seleccion;
    private boolean confirmando;
    private final Rectangle[] zonas = new Rectangle[OPCIONES.length];

    public PantallaPausa(Juego juego, Pantalla fondo) {
        super("PantallaPausa", juego);
        this.fondo = fondo;
    }

    @Override
    public void entrar() {
        juego.gestorAudio().ambiente(0.4f);
    }

    @Override
    public void actualizar(double dt) {
        tiempo += dt;
        int mx = juego.entrada().ratonX();
        int my = juego.entrada().ratonY();
        for (int i = 0; i < zonas.length; i++) {
            if (zonas[i] != null && zonas[i].contains(mx, my)) {
                seleccion = i;
            }
        }
    }

    @Override
    public void salir() {
        juego.gestorAudio().ambiente(1f);
    }

    @Override
    public void dibujar(Graphics2D g) {
        fondo.dibujar(g);
        Dibujo.suavizado(g);
        g.setColor(new Color(0, 0, 0, 185));
        g.fillRect(0, 0, 1280, 720);
        g.setFont(Estilo.fija("SansSerif", Font.BOLD, 54));
        g.setColor(Color.WHITE);
        Dibujo.centrado(g, "PAUSA", 640, 200);
        int y = 290;
        for (int i = 0; i < OPCIONES.length; i++) {
            boolean sel = i == seleccion;
            g.setFont(Estilo.fuente(sel ? Font.BOLD : Font.PLAIN, 24));
            int w = g.getFontMetrics().stringWidth(OPCIONES[i]);
            zonas[i] = new Rectangle(640 - w / 2 - 30, y - 30, w + 60, 42);
            if (sel) {
                Dibujo.rectRedondo(g, 640 - w / 2.0 - 30, y - 30, w + 60, 42, 12, Estilo.conAlfa(Estilo.acento(), 0.3f));
            }
            g.setColor(sel ? Color.WHITE : new Color(180, 180, 195));
            Dibujo.centrado(g, OPCIONES[i], 640, y);
            y += 56;
        }
        if (confirmando) {
            Dibujo.panel(g, 390, 540, 500, 90, 16);
            g.setFont(Estilo.fuente(Font.BOLD, 17));
            g.setColor(Estilo.advertencia());
            Dibujo.centrado(g, "¿Volver al menú? Perderás el progreso de esta partida.", 640, 575);
            g.setFont(Estilo.fuente(Font.PLAIN, 15));
            g.setColor(Estilo.texto());
            Dibujo.centrado(g, "ENTER: sí, volver  ·  ESC: cancelar", 640, 605);
        }
        g.setFont(Estilo.fuente(Font.PLAIN, 13));
        g.setColor(new Color(160, 160, 175));
        Dibujo.centrado(g, "ESC para continuar", 640, 680);
    }

    private void elegir() {
        switch (seleccion) {
            case 0:
                juego.desapilarPantalla();
                break;
            case 1:
                juego.apilarPantalla(new PantallaAyuda(juego, true));
                break;
            case 2:
                juego.apilarPantalla(new PantallaConfiguracion(juego));
                break;
            default:
                if (confirmando) {
                    juego.gestorAudio().detenerTodo();
                    juego.cambiarPantalla(new PantallaMenu(juego));
                } else {
                    confirmando = true;
                }
                break;
        }
    }

    @Override
    public void alPresionarTecla(int codigo, char caracter) {
        if (confirmando) {
            if (codigo == KeyEvent.VK_ENTER) {
                elegir();
            } else if (codigo == KeyEvent.VK_ESCAPE) {
                confirmando = false;
            }
            return;
        }
        switch (codigo) {
            case KeyEvent.VK_ESCAPE:
            case KeyEvent.VK_P:
                juego.desapilarPantalla();
                break;
            case KeyEvent.VK_UP:
            case KeyEvent.VK_W:
                seleccion = Math.floorMod(seleccion - 1, OPCIONES.length);
                break;
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_S:
                seleccion = Math.floorMod(seleccion + 1, OPCIONES.length);
                break;
            case KeyEvent.VK_ENTER:
            case KeyEvent.VK_SPACE:
            case KeyEvent.VK_E:
                elegir();
                break;
            default:
                break;
        }
    }

    @Override
    public void alHacerClic(int x, int y, int boton) {
        for (int i = 0; i < zonas.length; i++) {
            if (zonas[i] != null && zonas[i].contains(x, y)) {
                seleccion = i;
                elegir();
                return;
            }
        }
    }
}
