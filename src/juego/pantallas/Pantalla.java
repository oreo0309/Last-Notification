package juego.pantallas;

import java.awt.Graphics2D;
import juego.Juego;

public abstract class Pantalla {

    protected final String nombre;
    protected final Juego juego;
    protected double tiempo;

    protected Pantalla(String nombre, Juego juego) {
        this.nombre = nombre;
        this.juego = juego;
    }

    public abstract void entrar();

    public abstract void actualizar(double dt);

    public abstract void salir();

    public abstract void dibujar(Graphics2D g);

    public void alPresionarTecla(int codigo, char caracter) {
    }

    public void alSoltarTecla(int codigo) {
    }

    public void alHacerClic(int x, int y, int boton) {
    }

    public void alMoverRueda(int rotacion) {
    }

    public void alPerderFoco() {
    }

    public void alReanudar() {
    }

    public boolean capturaRaton() {
        return false;
    }

    public String nombre() {
        return nombre;
    }
}
