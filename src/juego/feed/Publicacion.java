package juego.feed;

import java.awt.Color;
import juego.interfaz.ContextoVisual;

public class Publicacion extends ElementoBase {

    private final String texto;
    private final TipoPost tipo;
    private final int impactoAnsiedad;
    private String clave;

    public Publicacion(String autor, String texto, TipoPost tipo, int impactoAnsiedad) {
        super(autor);
        this.texto = texto;
        this.tipo = tipo;
        this.impactoAnsiedad = impactoAnsiedad;
    }

    public Publicacion conClave(String clave) {
        this.clave = clave;
        return this;
    }

    @Override
    protected String cuerpo(ContextoVisual contexto) {
        return texto.replace("{nombre}", contexto.nombreJugador);
    }

    @Override
    protected boolean perturbador() {
        return tipo == TipoPost.PERTURBADOR;
    }

    @Override
    protected Color fondo(ContextoVisual contexto) {
        if (perturbador()) {
            return new Color(22, 8, 10);
        }
        return super.fondo(contexto);
    }

    @Override
    protected String subtitulo(ContextoVisual contexto) {
        if (perturbador()) {
            return "hace 0 segundos · te está mirando";
        }
        return super.subtitulo(contexto) + " · Público";
    }

    @Override
    public int impactoAnsiedad() {
        return impactoAnsiedad;
    }

    public String texto() {
        return texto;
    }

    public TipoPost tipo() {
        return tipo;
    }

    public String clave() {
        return clave;
    }
}
