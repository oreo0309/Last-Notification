package juego.pantallas;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import juego.IndicadorBienestar;
import juego.Juego;
import juego.Jugador;
import juego.audio.GestorAudio;
import juego.interfaz.Dibujo;
import juego.interfaz.Estilo;
import juego.motor.Camara;
import juego.mundo.Mundo3D;
import juego.mundo.Puerta;

public class PantallaSegura extends Pantalla {

    private static final double CICLO = 8.0;

    private int calma;
    private double calmaReal;
    private final PantallaFeed origen;
    private final boolean finalJuego;
    private final Mundo3D mundo;
    private final Jugador jugador;
    private final Camara camara = new Camara(1.55, 2.6, 0.3);
    private double fase;
    private double minutoFraccion;
    private double acumuladoAnsiedad;
    private int faseAnterior = -1;
    private double proximaTentacion = 11;
    private int tentaciones;
    private double tiempoTentacion;
    private boolean figura;
    private double terminando = -1;
    private double tensionInicial;
    private String mensaje;

    public PantallaSegura(Juego juego, PantallaFeed origen, boolean finalJuego) {
        super("PantallaSegura", juego);
        this.origen = origen;
        this.finalJuego = finalJuego;
        this.mundo = juego.mundo();
        this.jugador = juego.jugador();
    }

    @Override
    public void entrar() {
        camara.ojo = 0.34;
        camara.inclinacion = 0.03;
        tensionInicial = origen.tension();
        mundo.seguidor().apagar();
        mundo.iluminacion().linterna = 0;
        mundo.iluminacion().brilloPantalla = 0;
        juego.gestorAudio().ambiente(0.55f);
        juego.gestorAudio().subirTension((float) (tensionInicial / 140.0));
        figura = tensionInicial > 48 && !juego.configuracion().sinJumpscares();
        if (figura) {
            Puerta p = mundo.casa().puerta(7, 4);
            if (!p.estaAbierta()) {
                mundo.abrirPuertaLento(p);
            }
        }
        mensaje = finalJuego
                ? "El teléfono está apagado. Solo quedan la lluvia y tu respiración."
                : "Te acuestas. El teléfono quedó en el escritorio.";
        if (finalJuego) {
            proximaTentacion = 9999;
        }
    }

    @Override
    public void salir() {
        juego.gestorAudio().ambiente(1f);
    }

    @Override
    public void actualizar(double dt) {
        tiempo += dt;
        minutoFraccion += dt;
        while (minutoFraccion >= 1) {
            minutoFraccion -= 1;
            jugador.avanzarMinutos(1);
        }
        if (terminando >= 0) {
            terminando += dt;
            mundo.efectos().oscurecer = (float) Math.min(1, terminando / 2.5);
            mundo.actualizar(dt, 0, camara.x, camara.y, camara.angulo);
            if (terminando > 3.2) {
                terminando = -100;
                origen.dormido(jugador.horaActual());
            }
            return;
        }
        boolean espacio = juego.entrada().presionada(KeyEvent.VK_SPACE);
        fase = (fase + dt) % CICLO;
        boolean inhalar = fase < CICLO / 2;
        int faseActual = inhalar ? 0 : 1;
        if (faseActual != faseAnterior) {
            faseAnterior = faseActual;
            juego.gestorAudio().reproducir(inhalar ? "respira_in" : "respira_out", 0.35f, 0f, null);
        }
        double enFase = inhalar ? fase : fase - CICLO / 2;
        boolean gracia = enFase < 0.5;
        boolean correcto = espacio == inhalar;
        if (correcto) {
            calmaReal += dt * 4.4;
            acumuladoAnsiedad += dt;
            if (acumuladoAnsiedad >= 2.5) {
                acumuladoAnsiedad = 0;
                jugador.bienestar().modificar(IndicadorBienestar.ANSIEDAD, -1);
            }
        } else if (!gracia) {
            calmaReal -= dt * 2.4;
        }
        calmaReal = Math.max(0, Math.min(100, calmaReal));
        calma = (int) calmaReal;
        if (!finalJuego) {
            proximaTentacion -= dt;
            if (proximaTentacion <= 0 && tentaciones < 2) {
                tentaciones++;
                proximaTentacion = 13 + juego.azar().nextDouble() * 8;
                tiempoTentacion = 4.5;
                juego.gestorAudio().reproducirEn("vibracion", 5.7, 1.4, 1.0f, "[Tu teléfono vibra en el escritorio]");
                mundo.brilloTelefonoEscritorio(true);
            }
        }
        if (tiempoTentacion > 0) {
            tiempoTentacion -= dt;
            if (tiempoTentacion <= 0) {
                mundo.brilloTelefonoEscritorio(false);
            }
        }
        if (figura) {
            double avance = calmaReal / 100.0;
            if (tiempo > 3 && !mundo.seguidor().activo() && avance < 0.9) {
                mundo.seguidor().aparecer(8.4, 4.5, 9999);
            }
            if (mundo.seguidor().activo()) {
                mundo.seguidor().sprite().alfa = (float) Math.max(0, 1 - avance * 1.1);
                if (avance >= 0.9) {
                    mundo.seguidor().apagar();
                    juego.gestorAudio().agregarSubtitulo("[La figura de la puerta se desvanece]", 3);
                }
            }
        }
        float tensionActual = (float) (tensionInicial / 100.0 * (1 - calmaReal / 100.0));
        juego.gestorAudio().subirTension(tensionActual * 0.8f);
        mundo.efectos().grano = 0.05f + tensionActual * 0.08f;
        mundo.efectos().fuerzaVineta = 0.75f - (float) (calmaReal / 100.0) * 0.25f;
        mundo.efectos().aberracion = 0;
        mundo.efectos().glitch = 0;
        mundo.efectos().tinteRojo = 0;
        mundo.efectos().oscurecer = 0.25f;
        mundo.actualizar(dt, tensionActual, camara.x, camara.y, camara.angulo);
        if (calmaReal >= 100) {
            terminando = 0;
            juego.gestorAudio().reproducir("respira_out", 0.4f, 0f, "[Te quedas dormido]");
        }
    }

    @Override
    public void dibujar(Graphics2D g) {
        camara.ojo = 0.34 + Math.sin(fase / CICLO * Math.PI * 2 - Math.PI / 2) * 0.006;
        BufferedImage img = mundo.renderizar(camara);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(img, -4, -4, 1288, 728, null);
        Dibujo.suavizado(g);
        if (terminando >= 0 || terminando == -100) {
            g.setColor(new Color(0, 0, 0, (int) (Math.min(1, Math.max(0, terminando) / 2.5) * 255)));
            g.fillRect(0, 0, 1280, 720);
            if (terminando > 1.2) {
                g.setFont(Estilo.fuente(Font.PLAIN, 24));
                g.setColor(new Color(220, 220, 230, (int) (Math.min(1, (terminando - 1.2) / 1.0) * 255)));
                Dibujo.centrado(g, "Te quedaste dormido a las " + jugador.horaTexto() + ".", 640, 370);
            }
            return;
        }
        boolean inhalar = fase < CICLO / 2;
        double enFase = inhalar ? fase / (CICLO / 2) : 1 - (fase - CICLO / 2) / (CICLO / 2);
        double suavizado = 0.5 - 0.5 * Math.cos(enFase * Math.PI);
        double radio = 60 + suavizado * 70;
        int cx = 640;
        int cy = 330;
        g.setColor(new Color(0, 0, 0, 90));
        g.fill(new Ellipse2D.Double(cx - 150, cy - 150, 300, 300));
        Color guia = Estilo.altoContraste() ? new Color(255, 230, 0) : new Color(150, 190, 255);
        g.setColor(Estilo.conAlfa(guia, 0.22f));
        g.fill(new Ellipse2D.Double(cx - radio, cy - radio, radio * 2, radio * 2));
        g.setColor(guia);
        g.setStroke(new BasicStroke(3f));
        g.draw(new Ellipse2D.Double(cx - radio, cy - radio, radio * 2, radio * 2));
        boolean espacio = juego.entrada().presionada(KeyEvent.VK_SPACE);
        g.setColor(espacio ? Estilo.positivo() : new Color(255, 255, 255, 90));
        g.fill(new Ellipse2D.Double(cx - 8, cy - 8, 16, 16));
        g.setFont(Estilo.fuente(Font.BOLD, 30));
        g.setColor(Color.WHITE);
        Dibujo.centrado(g, inhalar ? "Inhala..." : "Exhala...", cx, cy - 170);
        g.setFont(Estilo.fuente(Font.PLAIN, 16));
        g.setColor(new Color(220, 220, 230));
        Dibujo.centrado(g, inhalar ? "Mantén presionada la barra ESPACIO" : "Suelta la barra ESPACIO", cx, cy + 180);
        int bw = 420;
        Dibujo.panel(g, cx - bw / 2 - 16, 560, bw + 32, 64, 14);
        g.setFont(Estilo.fuente(Font.BOLD, 14));
        g.setColor(Estilo.texto());
        g.drawString("Calma", cx - bw / 2, 582);
        g.drawString(calma + "%", cx + bw / 2 - 40, 582);
        Dibujo.barra(g, cx - bw / 2, 592, bw, 14, calma / 100.0, Estilo.positivo());
        g.setFont(Estilo.fuente(Font.ITALIC, 15));
        g.setColor(new Color(200, 200, 215));
        Dibujo.centrado(g, mensaje, cx, 40);
        if (tiempo < 10) {
            g.setFont(Estilo.fuente(Font.PLAIN, 14));
            Dibujo.centrado(g, "Respirar despacio calma el cuerpo y ayuda a dormir. Sigue el círculo.", cx, 66);
        }
        if (tiempoTentacion > 0) {
            g.setFont(Estilo.fuente(Font.BOLD, 15));
            g.setColor(Estilo.advertencia());
            Dibujo.centrado(g, "El teléfono vibra en el escritorio...   [TAB] levantarte a mirarlo", cx, 660);
        }
        dibujarSubtitulos(g);
    }

    private void dibujarSubtitulos(Graphics2D g) {
        GestorAudio.Subtitulo[] subs = juego.gestorAudio().subtitulosActivos(1.0 / 60);
        if (!juego.configuracion().subtitulos()) {
            return;
        }
        g.setFont(Estilo.fuente(Font.BOLD, 16));
        int y = 700;
        for (int i = subs.length - 1; i >= 0; i--) {
            int w = g.getFontMetrics().stringWidth(subs[i].texto) + 28;
            g.setColor(new Color(0, 0, 0, 200));
            g.fillRoundRect(640 - w / 2, y - 22, w, 30, 10, 10);
            g.setColor(Color.WHITE);
            Dibujo.centrado(g, subs[i].texto, 640, y - 2);
            y -= 36;
        }
    }

    @Override
    public void alPresionarTecla(int codigo, char caracter) {
        if (terminando >= 0) {
            return;
        }
        if (codigo == KeyEvent.VK_TAB && !finalJuego) {
            mundo.seguidor().apagar();
            mundo.brilloTelefonoEscritorio(false);
            juego.desapilarPantalla();
            origen.levantado();
        } else if (codigo == KeyEvent.VK_ESCAPE) {
            juego.apilarPantalla(new PantallaPausa(juego, this));
        }
    }

    @Override
    public void alPerderFoco() {
        if (terminando < 0) {
            juego.apilarPantalla(new PantallaPausa(juego, this));
        }
    }

    public int calma() {
        return calma;
    }
}
