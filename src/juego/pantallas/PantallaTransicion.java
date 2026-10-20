package juego.pantallas;

import estructuras.ListaEnlazada;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import juego.Juego;
import juego.Logro;
import juego.interfaz.Dibujo;
import juego.interfaz.Estilo;

public class PantallaTransicion extends Pantalla {

    private static final double INTERVALO_LOGRO = 0.7;

    private final String titulo;
    private final String subtitulo;
    private final String cuerpo;
    private final Runnable alContinuar;
    private final ListaEnlazada<Logro> logros = new ListaEnlazada<>();
    private final boolean celebracion;
    private double caracteres;
    private boolean continuado;
    private double tiempoLogros = -1;
    private int logrosMostrados;

    public PantallaTransicion(Juego juego, String titulo, String subtitulo, String cuerpo, Runnable alContinuar) {
        this(juego, titulo, subtitulo, cuerpo, null, alContinuar);
    }

    public PantallaTransicion(Juego juego, String titulo, String subtitulo, String cuerpo, ListaEnlazada<Logro> logros,
            Runnable alContinuar) {
        super("PantallaTransicion", juego);
        this.titulo = titulo;
        this.subtitulo = subtitulo;
        this.cuerpo = cuerpo;
        this.alContinuar = alContinuar;
        this.celebracion = logros != null;
        if (logros != null) {
            for (Logro l : logros) {
                this.logros.agregar(l);
            }
        }
    }

    @Override
    public void entrar() {
        if (celebracion) {
            juego.gestorAudio().subirTension(0f);
            juego.gestorAudio().reproducir("nivel", 0.7f, 0f, "[Música: noche superada]");
        } else {
            juego.gestorAudio().subirTension(0.15f);
            juego.gestorAudio().reproducir("stinger_suave", 0.35f);
        }
    }

    @Override
    public void actualizar(double dt) {
        tiempo += dt;
        if (tiempo > 1.0) {
            caracteres += dt * 70;
        }
        if (caracteres >= cuerpo.length() && tiempoLogros < 0) {
            tiempoLogros = 0;
        }
        if (tiempoLogros >= 0) {
            tiempoLogros += dt;
            int deberian = Math.min(logros.tamano(), (int) (tiempoLogros / INTERVALO_LOGRO) + 1);
            while (logrosMostrados < deberian) {
                logrosMostrados++;
                juego.gestorAudio().reproducir("logro", 0.55f, 0f, null);
            }
        }
    }

    @Override
    public void salir() {
    }

    @Override
    public void dibujar(Graphics2D g) {
        g.setColor(celebracion ? new Color(8, 10, 22) : new Color(4, 4, 8));
        g.fillRect(0, 0, 1280, 720);
        Dibujo.suavizado(g);
        float alfa = (float) Math.min(1, tiempo / 0.8);
        boolean conLogros = !logros.estaVacia();
        int centro = conLogros ? 430 : 640;
        g.setFont(Estilo.fija("SansSerif", Font.BOLD, conLogros ? 56 : 72));
        g.setColor(Estilo.conAlfa(celebracion ? new Color(255, 236, 170) : new Color(236, 236, 244), alfa));
        int anchoTitulo = g.getFontMetrics().stringWidth(titulo);
        Dibujo.textoGlitch(g, titulo, centro - anchoTitulo / 2, 150, celebracion ? 0 : (tiempo < 1.2 ? 0.8 : 0.08), juego.azar());
        g.setFont(Estilo.fuente(Font.PLAIN, 20));
        g.setColor(Estilo.conAlfa(Estilo.textoSuave(), alfa));
        Dibujo.centrado(g, subtitulo, centro, 190);
        int visibles = (int) Math.min(cuerpo.length(), caracteres);
        g.setFont(Estilo.fuente(Font.PLAIN, conLogros ? 18 : 19));
        g.setColor(Estilo.texto());
        int xCuerpo = conLogros ? 110 : 250;
        int anchoCuerpo = conLogros ? 640 : 780;
        Dibujo.parrafo(g, cuerpo.substring(0, visibles), xCuerpo, 236, anchoCuerpo, 1.22f);
        if (conLogros) {
            dibujarLogros(g);
        }
        boolean listo = visibles >= cuerpo.length() && logrosMostrados >= logros.tamano();
        if (listo) {
            float parpadeo = (float) (0.55 + 0.45 * Math.sin(tiempo * 4));
            g.setFont(Estilo.fuente(Font.BOLD, 16));
            g.setColor(Estilo.conAlfa(Estilo.acento(), parpadeo));
            Dibujo.centrado(g, "Pulsa ENTER para continuar", 640, 690);
        } else if (tiempo > 1.0) {
            g.setFont(Estilo.fuente(Font.PLAIN, 13));
            g.setColor(Estilo.textoSuave());
            Dibujo.centrado(g, "ENTER para mostrar todo", 640, 690);
        }
    }

    private void dibujarLogros(Graphics2D g) {
        int x = 800;
        int y = 120;
        Dibujo.panel(g, x - 20, y - 40, 470, Math.min(560, 70 + logros.tamano() * 62), 18);
        g.setFont(Estilo.fuente(Font.BOLD, 15));
        g.setColor(Estilo.advertencia());
        g.drawString("LOGROS DE ESTA NOCHE", x, y - 10);
        int i = 0;
        for (Logro l : logros) {
            if (i >= logrosMostrados || i >= 8) {
                break;
            }
            int cy = y + 10 + i * 62;
            double aparicion = Math.min(1, (tiempoLogros - i * INTERVALO_LOGRO) / 0.3);
            float alfa = (float) Math.max(0, aparicion);
            Medalla.dibujar(g, x + 22, cy + 22, 22 * (0.6 + 0.4 * aparicion), alfa);
            g.setFont(Estilo.fuente(Font.BOLD, 16));
            g.setColor(Estilo.conAlfa(Estilo.texto(), alfa));
            g.drawString(l.nombre(), x + 56, cy + 18);
            g.setFont(Estilo.fuente(Font.PLAIN, 13));
            g.setColor(Estilo.conAlfa(Estilo.textoSuave(), alfa));
            g.drawString(l.descripcion(), x + 56, cy + 38);
            i++;
        }
    }

    private void continuar() {
        if (continuado) {
            return;
        }
        if (caracteres < cuerpo.length() || logrosMostrados < logros.tamano()) {
            caracteres = cuerpo.length();
            tiempoLogros = Math.max(tiempoLogros, logros.tamano() * INTERVALO_LOGRO);
            logrosMostrados = logros.tamano();
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
