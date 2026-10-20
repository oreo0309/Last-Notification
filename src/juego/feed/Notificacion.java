package juego.feed;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;
import juego.interfaz.ContextoVisual;
import juego.interfaz.Dibujo;
import juego.interfaz.Estilo;

public class Notificacion extends ElementoBase {

    private final String titulo;
    private final String mensaje;
    private final int urgencia;
    private final boolean silenciable;
    private EventoTerror dispara;
    private boolean esMensaje;
    private String clave;

    public Notificacion(String titulo, String mensaje, int urgencia, boolean silenciable) {
        super(titulo);
        this.titulo = titulo;
        this.mensaje = mensaje;
        this.urgencia = urgencia;
        this.silenciable = silenciable;
    }

    public Notificacion comoMensaje() {
        esMensaje = true;
        return this;
    }

    public Notificacion disparando(EventoTerror evento) {
        this.dispara = evento;
        return this;
    }

    public Notificacion conClave(String clave) {
        this.clave = clave;
        return this;
    }

    @Override
    protected String cuerpo(ContextoVisual contexto) {
        return mensaje.replace("{nombre}", contexto.nombreJugador);
    }

    @Override
    protected String subtitulo(ContextoVisual contexto) {
        return esMensaje ? "Mensaje directo · ahora" : "Notificación · ahora";
    }

    @Override
    protected boolean mostrarReacciones() {
        return false;
    }

    @Override
    protected boolean perturbador() {
        return dispara != null;
    }

    @Override
    protected Color fondo(ContextoVisual contexto) {
        if (perturbador()) {
            return new Color(24, 8, 12);
        }
        return Estilo.altoContraste() ? Color.BLACK : new Color(28, 32, 48);
    }

    public void mostrarAviso(Graphics2D g, int x, int y, int ancho, float alfa, ContextoVisual contexto) {
        int alto = 70;
        Color fondo = perturbador() ? new Color(40, 6, 12) : (Estilo.altoContraste() ? Color.BLACK : new Color(32, 32, 44));
        g.setColor(Estilo.conAlfa(fondo, 0.94f * alfa));
        g.fill(new RoundRectangle2D.Float(x, y, ancho, alto, 18, 18));
        g.setColor(Estilo.conAlfa(perturbador() ? Estilo.peligro() : Estilo.borde(), alfa));
        g.setStroke(new BasicStroke(Estilo.altoContraste() ? 2f : 1f));
        g.draw(new RoundRectangle2D.Float(x + 0.5f, y + 0.5f, ancho - 1, alto - 1, 18, 18));
        if (titulo.startsWith("@")) {
            Dibujo.avatar(g, x + 12, y + 15, 40, titulo, perturbador(), contexto.tiempo);
        } else {
            Dibujo.rectRedondo(g, x + 12, y + 15, 40, 40, 12, Estilo.acento());
            Dibujo.logoEco(g, x + 16, y + 19, 32, Estilo.altoContraste() ? Color.BLACK : Color.WHITE);
        }
        g.setFont(Estilo.fuente(Font.BOLD, 14));
        g.setColor(Estilo.conAlfa(perturbador() ? new Color(255, 90, 100) : Estilo.texto(), alfa));
        String t = Dibujo.corromper(titulo, perturbador() ? 0.3 : contexto.corrupcion * 0.3, contexto.semillaCorrupcion());
        g.drawString(t, x + 62, y + 28);
        g.setFont(Estilo.fuente(Font.PLAIN, 13));
        g.setColor(Estilo.conAlfa(Estilo.textoSuave(), alfa));
        String cuerpo = cuerpo(contexto);
        String recorte = recortar(g, cuerpo, ancho - 78);
        g.drawString(Dibujo.corromper(recorte, contexto.corrupcion * 0.4, contexto.semillaCorrupcion() + 3), x + 62, y + 50);
    }

    private static String recortar(Graphics2D g, String texto, int ancho) {
        if (g.getFontMetrics().stringWidth(texto) <= ancho) {
            return texto;
        }
        String r = texto;
        while (r.length() > 3 && g.getFontMetrics().stringWidth(r + "...") > ancho) {
            r = r.substring(0, r.length() - 1);
        }
        return r + "...";
    }

    @Override
    public int impactoAnsiedad() {
        return urgencia >= 4 ? 4 : 1;
    }

    public String titulo() {
        return titulo;
    }

    public String mensaje() {
        return mensaje;
    }

    public int urgencia() {
        return urgencia;
    }

    public boolean silenciable() {
        return silenciable;
    }

    public EventoTerror dispara() {
        return dispara;
    }

    public boolean esMensaje() {
        return esMensaje;
    }

    public String clave() {
        return clave;
    }
}
