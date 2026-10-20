package juego.motor;

public class LuzPuntual {

    public static final int CANAL_LUNA = 0;
    public static final int CANAL_LAMPARA = 1;
    public static final int CANAL_PASILLO = 2;
    public static final int CANAL_TV = 3;
    public static final int CANAL_ROUTER = 4;
    public static final int CANAL_NEVERA = 5;
    public static final int CANALES = 6;

    public final double x;
    public final double y;
    public final float r;
    public final float g;
    public final float b;
    public final double radio;
    public final int canal;

    public LuzPuntual(double x, double y, float r, float g, float b, double radio, int canal) {
        this.x = x;
        this.y = y;
        this.r = r;
        this.g = g;
        this.b = b;
        this.radio = radio;
        this.canal = canal;
    }
}
