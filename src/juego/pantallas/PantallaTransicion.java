package juego.pantallas;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import juego.Juego;
import juego.interfaz.Dibujo;
import juego.interfaz.Estilo;

public class PantallaTransicion extends Pantalla {

    private final String titulo;
    private final String subtitulo;
    private final String cuerpo;
    private final Runnable alContinuar;
    private double caracteres;
    private boolean continuado;

    public PantallaTransicion(Juego juego, String titulo, String subtitulo, String cuerpo, Runnable alContinuar) {
        super("PantallaTransicion", juego);
        this.titulo = titulo;
        this.subtitulo = subtitulo;
        this.cuerpo = cuerpo;
        this.alContinuar = alContinuar;
    }

    @Override
    public void entrar() {
        juego.gestorAudio().subirTension(0.15f);
        juego.gestorAudio().reproducir("stinger_suave", 0.35f);
    }

    @Override
    public void actualizar(double dt) {
        tiempo += dt;
        if (tiempo > 1.0) {
            caracteres += dt * 70;
        }
    }

    @Override
    public void salir() {
    }

    @Override
    public void dibujar(Graphics2D g) {
        g.setColor(new Color(4, 4, 8));
        g.fillRect(0, 0, 1280, 720);
        Dibujo.suavizado(g);
        float alfa = (float) Math.min(1, tiempo / 0.8);
        g.setFont(Estilo.fija("SansSerif", Font.BOLD, 72));
        g.setColor(Estilo.conAlfa(new Color(236, 236, 244), alfa));
        Dibujo.textoGlitch(g, titulo, 640 - g.getFontMetrics().stringWidth(titulo) / 2, 170, tiempo < 1.2 ? 0.8 : 0.08, juego.azar());
        g.setFont(Estilo.fuente(Font.PLAIN, 20));
        g.setColor(Estilo.conAlfa(Estilo.textoSuave(), alfa));
        Dibujo.centrado(g, subtitulo, 640, 210);
        int visibles = (int) Math.min(cuerpo.length(), caracteres);
        g.setFont(Estilo.fuente(Font.PLAIN, 19));
        g.setColor(Estilo.texto());
        Dibujo.parrafo(g, cuerpo.substring(0, visibles), 250, 260, 780, 1.25f);
        if (visibles >= cuerpo.length()) {
            float parpadeo = (float) (0.55 + 0.45 * Math.sin(tiempo * 4));
            g.setFont(Estilo.fuente(Font.BOLD, 16));
            g.setColor(Estilo.conAlfa(Estilo.acento(), parpadeo));
            Dibujo.centrado(g, "Pulsa ENTER para continuar", 640, 670);
        } else if (tiempo > 1.0) {
            g.setFont(Estilo.fuente(Font.PLAIN, 13));
            g.setColor(Estilo.textoSuave());
            Dibujo.centrado(g, "ENTER para mostrar todo", 640, 670);
        }
    }

    private void continuar() {
        if (continuado) {
            return;
        }
        if (caracteres < cuerpo.length()) {
            caracteres = cuerpo.length();
            return;
        }
        continuado = true;
        juego.gestorAudio().reproducir("menu_elegir", 0.3f);
        alContinuar.run();
    }

    @Override
    public void alPresionarTecla(int codigo, char caracter) {
        if (codigo == KeyEvent.VK_ENTER || codigo == KeyEvent.VK_SPACE || codigo == KeyEvent.VK_E) {
            continuar();
        }
    }

    @Override
    public void alHacerClic(int x, int y, int boton) {
        continuar();
    }
}
