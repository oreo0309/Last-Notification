package juego.feed;

import java.util.Random;
import juego.Decision;
import juego.TipoDecision;

public abstract class FabricaContenido {

    protected int noche = 1;
    protected int tension;
    protected String nombreJugador = "";
    protected final Random azar;
    protected final GeneradorImagenes imagenes;

    protected FabricaContenido(Random azar, GeneradorImagenes imagenes) {
        this.azar = azar;
        this.imagenes = imagenes;
    }

    public void configurar(int noche, int tension, String nombreJugador) {
        this.noche = noche;
        this.tension = Math.max(0, Math.min(100, tension));
        this.nombreJugador = nombreJugador == null ? "" : nombreJugador;
    }

    public abstract ElementoFeed crear();

    protected static Decision d(TipoDecision tipo, String texto) {
        return new Decision(tipo, texto);
    }

    protected int entre(int min, int max) {
        return min + azar.nextInt(Math.max(1, max - min + 1));
    }
}
