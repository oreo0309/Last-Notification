package juego.interfaz;

import java.util.Random;

public class ContextoVisual {

    public double corrupcion;
    public double tiempo;
    public boolean enfocado;
    public boolean mostrarDecisiones = true;
    public int opcionResaltada = -1;
    public String nombreJugador = "";
    public final Random azar = new Random();

    public long semillaCorrupcion() {
        return (long) (tiempo * 7);
    }
}
