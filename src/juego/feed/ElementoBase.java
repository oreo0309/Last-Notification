package juego.feed;

import estructuras.ListaEnlazada;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import juego.Decision;
import juego.interfaz.ContextoVisual;
import juego.interfaz.Dibujo;
import juego.interfaz.Estilo;

public abstract class ElementoBase implements ElementoFeed {

    protected final String autor;
    protected BufferedImage imagen;
    protected final ListaEnlazada<Decision> decisiones = new ListaEnlazada<>();
    private final ListaEnlazada<Rectangle> zonas = new ListaEnlazada<>();
    protected Decision decisionTomada;
    protected boolean resuelto;
    protected boolean visto;
    protected int minutosAtras;
    protected int meGusta;
    protected int comentarios;
    protected String respuestaResuelto;

    protected ElementoBase(String autor) {
        this.autor = autor;
    }

    protected abstract String cuerpo(ContextoVisual contexto);

    protected String subtitulo(ContextoVisual contexto) {
        return minutosAtras <= 0 ? "ahora" : "hace " + minutosAtras + " min";
    }

    protected boolean perturbador() {
        return false;
    }

    protected boolean mostrarReacciones() {
        return true;
    }

    protected Color fondo(ContextoVisual contexto) {
        return Estilo.fondoTarjeta();
    }

    protected Color colorBorde(ContextoVisual contexto) {
        return contexto.enfocado ? Estilo.acento() : Estilo.borde();
    }

    @Override
    public int mostrar(Graphics2D g, int x, int y, int ancho, ContextoVisual contexto) {
        return maquetar(g, x, y, ancho, contexto, true);
    }

    @Override
    public int medirAlto(Graphics2D g, int ancho, ContextoVisual contexto) {
        return maquetar(g, 0, 0, ancho, contexto, false);
    }

    private int maquetar(Graphics2D g, int x, int y, int ancho, ContextoVisual ctx, boolean dibujar) {
        int alto = 0;
        if (dibujar) {
            alto = maquetar(g, x, y, ancho, ctx, false);
            Dibujo.rectRedondo(g, x, y, ancho, alto, 20, fondo(ctx));
            g.setColor(colorBorde(ctx));
            g.setStroke(new BasicStroke(ctx.enfocado ? 2.2f : 1f));
            g.draw(new RoundRectangle2D.Float(x + 0.5f, y + 0.5f, ancho - 1, alto - 1, 20, 20));
            zonas.limpiar();
        }
        int cy = y + 12;
        boolean ojo = perturbador() || (ctx.corrupcion > 0.6 && (autor.hashCode() & 3) == 0);
        if (dibujar) {
            Dibujo.avatar(g, x + 12, cy, 38, autor, ojo, ctx.tiempo);
            g.setFont(Estilo.fuente(Font.BOLD, 15));
            g.setColor(perturbador() ? new Color(235, 60, 70) : Estilo.texto());
            String nombre = Dibujo.corromper(autor, ctx.corrupcion * 0.6, ctx.semillaCorrupcion() + autor.length());
            Dibujo.textoGlitch(g, nombre, x + 60, cy + 16, perturbador() ? 0.6 : ctx.corrupcion * 0.5, ctx.azar);
            g.setFont(Estilo.fuente(Font.PLAIN, 12));
            g.setColor(Estilo.textoSuave());
            g.drawString(subtitulo(ctx), x + 60, cy + 34);
        }
        cy += 52;
        Font fuenteCuerpo = Estilo.fuente(Font.PLAIN, 16);
        String texto = Dibujo.corromper(cuerpo(ctx), ctx.corrupcion * (perturbador() ? 1.2 : 0.8), ctx.semillaCorrupcion() + 7);
        g.setFont(fuenteCuerpo);
        if (dibujar) {
            g.setColor(Estilo.texto());
            cy += Dibujo.parrafo(g, texto, x + 14, cy, ancho - 28, 1.12f) + 8;
        } else {
            cy += Dibujo.medirParrafo(g, fuenteCuerpo, texto, ancho - 28, 1.12f) + 8;
        }
        if (imagen != null) {
            int iw = ancho - 24;
            int ih = iw * imagen.getHeight() / imagen.getWidth();
            if (dibujar) {
                Shape clipAnterior = g.getClip();
                g.clip(new RoundRectangle2D.Float(x + 12, cy, iw, ih, 14, 14));
                g.drawImage(imagen, x + 12, cy, iw, ih, null);
                if (ctx.corrupcion > 0.3 && ctx.azar.nextDouble() < ctx.corrupcion * 0.2) {
                    g.setColor(new Color(255, 255, 255, 40));
                    g.fillRect(x + 12, cy + ctx.azar.nextInt(Math.max(1, ih)), iw, 2 + ctx.azar.nextInt(6));
                }
                g.setClip(clipAnterior);
            }
            cy += ih + 10;
        }
        if (mostrarReacciones()) {
            if (dibujar) {
                Dibujo.corazon(g, x + 16, cy + 2, 16, perturbador() ? new Color(160, 20, 30) : new Color(235, 80, 110));
                g.setFont(Estilo.fuente(Font.PLAIN, 13));
                g.setColor(Estilo.textoSuave());
                g.drawString(formatear(meGusta, ctx), x + 38, cy + 15);
                Dibujo.burbuja(g, x + 110, cy + 2, 16, Estilo.textoSuave());
                g.drawString(formatear(comentarios, ctx), x + 132, cy + 15);
            }
            cy += 26;
        }
        Font fuenteBoton = Estilo.fuente(Font.PLAIN, 14);
        if (!resuelto && ctx.enfocado && ctx.mostrarDecisiones && !decisiones.estaVacia()) {
            int i = 0;
            for (Decision d : decisiones) {
                int altoTexto = Dibujo.medirParrafo(g, fuenteBoton, d.texto(), ancho - 76, 1.05f);
                int altoBoton = Math.max(38, altoTexto + 16);
                if (dibujar) {
                    boolean resaltado = ctx.opcionResaltada == i;
                    Color fondoBoton = resaltado ? Estilo.conAlfa(Estilo.acento(), 0.35f) : Estilo.fondoBoton();
                    Dibujo.rectRedondo(g, x + 12, cy, ancho - 24, altoBoton, 12, fondoBoton);
                    if (Estilo.altoContraste() || resaltado) {
                        g.setColor(resaltado ? Estilo.acento() : Color.WHITE);
                        g.setStroke(new BasicStroke(1.5f));
                        g.draw(new RoundRectangle2D.Float(x + 12, cy, ancho - 24, altoBoton, 12, 12));
                    }
                    Dibujo.rectRedondo(g, x + 20, cy + altoBoton / 2.0 - 11, 22, 22, 8, Estilo.acento());
                    g.setFont(Estilo.fuente(Font.BOLD, 13));
                    g.setColor(Estilo.altoContraste() ? Color.BLACK : Color.WHITE);
                    Dibujo.centrado(g, String.valueOf(i + 1), x + 31, (int) (cy + altoBoton / 2.0 + 5));
                    g.setFont(fuenteBoton);
                    g.setColor(Estilo.texto());
                    Dibujo.parrafo(g, d.texto(), x + 52, cy + (altoBoton - altoTexto) / 2, ancho - 76, 1.05f);
                    zonas.agregar(new Rectangle(x + 12, cy, ancho - 24, altoBoton));
                }
                cy += altoBoton + 6;
                i++;
            }
            cy += 4;
        } else if (resuelto && decisionTomada != null) {
            String etiqueta = respuestaResuelto != null ? respuestaResuelto : "Elegiste: " + decisionTomada.texto();
            int altoTexto = Dibujo.medirParrafo(g, fuenteBoton, etiqueta, ancho - 60, 1.05f);
            if (dibujar) {
                Color c = decisionTomada.tipo().saludable() ? Estilo.positivo()
                        : (decisionTomada.tipo().danina() ? Estilo.negativo() : Estilo.textoSuave());
                if (decisionTomada.tipo().danina()) {
                    Dibujo.cruz(g, x + 18, cy + 4, 12, c);
                } else {
                    Dibujo.check(g, x + 16, cy + 2, 16, c);
                }
                g.setFont(fuenteBoton);
                g.setColor(c);
                Dibujo.parrafo(g, etiqueta, x + 42, cy, ancho - 60, 1.05f);
            }
            cy += altoTexto + 10;
        } else if (!decisiones.estaVacia() && !ctx.enfocado) {
            if (dibujar) {
                g.setFont(Estilo.fuente(Font.ITALIC, 12));
                g.setColor(Estilo.textoSuave());
                g.drawString("Tienes que decidir algo aquí", x + 16, cy + 12);
            }
            cy += 22;
        }
        return cy - y + 4;
    }

    private String formatear(int n, ContextoVisual ctx) {
        if (ctx.corrupcion > 0.7 && ctx.azar.nextDouble() < 0.3) {
            return "3:12";
        }
        if (n >= 1000) {
            return String.format("%.1f mil", n / 1000.0);
        }
        return String.valueOf(n);
    }

    public ElementoBase agregarDecision(Decision d) {
        decisiones.agregar(d);
        return this;
    }

    public ElementoBase conImagen(BufferedImage imagen) {
        this.imagen = imagen;
        return this;
    }

    public ElementoBase conReacciones(int meGusta, int comentarios, int minutosAtras) {
        this.meGusta = meGusta;
        this.comentarios = comentarios;
        this.minutosAtras = minutosAtras;
        return this;
    }

    public void respuestaAlResolver(String texto) {
        respuestaResuelto = texto;
    }

    public BufferedImage imagen() {
        return imagen;
    }

    public void imagen(BufferedImage nueva) {
        imagen = nueva;
    }

    @Override
    public ListaEnlazada<Decision> decisiones() {
        return decisiones;
    }

    @Override
    public ListaEnlazada<Rectangle> zonasDecision() {
        return zonas;
    }

    @Override
    public boolean resuelto() {
        return resuelto;
    }

    @Override
    public void resolver(Decision decision) {
        resuelto = true;
        decisionTomada = decision;
    }

    @Override
    public Decision decisionTomada() {
        return decisionTomada;
    }

    @Override
    public boolean visto() {
        return visto;
    }

    @Override
    public void marcarVisto() {
        visto = true;
    }

    @Override
    public String autor() {
        return autor;
    }
}
