package juego.mundo;

public class Puerta {

    private final int x;
    private final int y;
    private final boolean vertical;
    private final String textura;
    private double apertura;
    private double objetivo;
    private double velocidad = 1.1;
    private boolean bloqueada;

    public Puerta(int x, int y, boolean vertical, String textura, boolean bloqueada) {
        this.x = x;
        this.y = y;
        this.vertical = vertical;
        this.textura = textura;
        this.bloqueada = bloqueada;
    }

    public boolean actualizar(double dt) {
        if (apertura == objetivo) {
            return false;
        }
        double paso = velocidad * dt;
        if (Math.abs(objetivo - apertura) <= paso) {
            apertura = objetivo;
            return true;
        }
        apertura += Math.signum(objetivo - apertura) * paso;
        return false;
    }

    public void abrir(double velocidad) {
        this.velocidad = velocidad;
        objetivo = 1;
    }

    public void cerrar(double velocidad) {
        this.velocidad = velocidad;
        objetivo = 0;
    }

    public void fijar(double valor) {
        apertura = valor;
        objetivo = valor;
    }

    public boolean estaAbierta() {
        return objetivo > 0.5;
    }

    public boolean transitable() {
        return apertura > 0.8;
    }

    public boolean enMovimiento() {
        return apertura != objetivo;
    }

    public double apertura() {
        return apertura;
    }

    public boolean vertical() {
        return vertical;
    }

    public String textura() {
        return textura;
    }

    public boolean bloqueada() {
        return bloqueada;
    }

    public void bloquear(boolean valor) {
        bloqueada = valor;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public double centroX() {
        return x + 0.5;
    }

    public double centroY() {
        return y + 0.5;
    }
}
