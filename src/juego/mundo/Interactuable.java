package juego.mundo;

public class Interactuable {

    public enum Tipo {
        PUERTA, CAMA, ESCRITORIO, LAMPARA, ROUTER, VENTANA, ESPEJO, CUADRO, PUERTA_PRINCIPAL, NEVERA, TV, ARMARIO, PELUCHE, LIBRERO
    }

    private final Tipo tipo;
    private final double x;
    private final double y;
    private final Puerta puerta;
    private boolean activo = true;

    public Interactuable(Tipo tipo, double x, double y) {
        this(tipo, x, y, null);
    }

    public Interactuable(Tipo tipo, double x, double y, Puerta puerta) {
        this.tipo = tipo;
        this.x = x;
        this.y = y;
        this.puerta = puerta;
    }

    public Tipo tipo() {
        return tipo;
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public Puerta puerta() {
        return puerta;
    }

    public boolean activo() {
        return activo;
    }

    public void activo(boolean valor) {
        activo = valor;
    }
}
