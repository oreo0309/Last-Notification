package juego;

public class Telefono {

    private double bateria = 100;
    private boolean enMano = true;
    private boolean linterna;
    private boolean noMolestar;
    private boolean apagado;
    private boolean sinWifi;
    private int sinLeer;

    public void reiniciar(double bateria) {
        this.bateria = bateria;
        enMano = true;
        linterna = false;
        noMolestar = false;
        apagado = false;
        sinWifi = false;
        sinLeer = 0;
    }

    public void consumir(double cantidad) {
        bateria = Math.max(0, bateria - cantidad);
        if (bateria <= 0) {
            linterna = false;
        }
    }

    public void cargar(double cantidad) {
        bateria = Math.min(100, bateria + cantidad);
    }

    public boolean puedeUsarse() {
        return enMano && !apagado && bateria > 0;
    }

    public double bateria() {
        return bateria;
    }

    public boolean enMano() {
        return enMano;
    }

    public void enMano(boolean valor) {
        enMano = valor;
        if (!valor) {
            linterna = false;
        }
    }

    public boolean linterna() {
        return linterna && puedeUsarse();
    }

    public void linterna(boolean valor) {
        linterna = valor && puedeUsarse();
    }

    public boolean noMolestar() {
        return noMolestar;
    }

    public void noMolestar(boolean valor) {
        noMolestar = valor;
    }

    public boolean apagado() {
        return apagado;
    }

    public void apagar() {
        apagado = true;
        linterna = false;
    }

    public boolean sinWifi() {
        return sinWifi;
    }

    public void sinWifi(boolean valor) {
        sinWifi = valor;
    }

    public int sinLeer() {
        return sinLeer;
    }

    public void sumarSinLeer(int n) {
        sinLeer = Math.max(0, sinLeer + n);
    }

    public void leerTodo() {
        sinLeer = 0;
    }
}
