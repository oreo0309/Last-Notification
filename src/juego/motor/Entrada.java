package juego.motor;

import estructuras.Cola;

public class Entrada {

    private final boolean[] teclas = new boolean[1024];
    private final Cola<EventoEntrada> eventos = new Cola<>();
    private double ratonDX;
    private double ratonDY;
    private int ratonX;
    private int ratonY;
    private boolean botonDerecho;

    public synchronized void presionar(int codigo, char caracter) {
        if (codigo >= 0 && codigo < teclas.length) {
            teclas[codigo] = true;
        }
        eventos.encolar(new EventoEntrada(EventoEntrada.Tipo.TECLA_PRESIONADA, codigo, caracter, 0, 0, 0));
    }

    public synchronized void soltar(int codigo) {
        if (codigo >= 0 && codigo < teclas.length) {
            teclas[codigo] = false;
        }
        eventos.encolar(new EventoEntrada(EventoEntrada.Tipo.TECLA_SOLTADA, codigo, '\0', 0, 0, 0));
    }

    public synchronized void clic(int x, int y, int boton) {
        eventos.encolar(new EventoEntrada(EventoEntrada.Tipo.CLIC, boton, '\0', x, y, 0));
    }

    public synchronized void rueda(int rotacion) {
        eventos.encolar(new EventoEntrada(EventoEntrada.Tipo.RUEDA, 0, '\0', 0, 0, rotacion));
    }

    public synchronized void moverRaton(int x, int y) {
        ratonX = x;
        ratonY = y;
    }

    public synchronized void acumularMovimiento(double dx, double dy) {
        ratonDX += dx;
        ratonDY += dy;
    }

    public synchronized void botonDerecho(boolean presionado) {
        botonDerecho = presionado;
    }

    public synchronized boolean botonDerecho() {
        return botonDerecho;
    }

    public synchronized EventoEntrada siguienteEvento() {
        return eventos.estaVacia() ? null : eventos.desencolar();
    }

    public synchronized boolean presionada(int codigo) {
        return codigo >= 0 && codigo < teclas.length && teclas[codigo];
    }

    public synchronized double consumirDX() {
        double v = ratonDX;
        ratonDX = 0;
        return v;
    }

    public synchronized double consumirDY() {
        double v = ratonDY;
        ratonDY = 0;
        return v;
    }

    public synchronized int ratonX() {
        return ratonX;
    }

    public synchronized int ratonY() {
        return ratonY;
    }

    public synchronized void soltarTodo() {
        for (int i = 0; i < teclas.length; i++) {
            teclas[i] = false;
        }
        ratonDX = 0;
        ratonDY = 0;
    }
}
