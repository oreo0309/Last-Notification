package juego.motor;

public class Sprite {

    public double x;
    public double y;
    public double z;
    public double ancho;
    public double alto;
    public Textura textura;
    public boolean visible = true;
    public boolean emisionActiva = true;
    public float alfa = 1f;
    public float distorsion;
    public double radioColision;
    public double distanciaCache;
    public boolean soloFoto;

    public Sprite(Textura textura, double x, double y, double z, double alto) {
        this.textura = textura;
        this.x = x;
        this.y = y;
        this.z = z;
        this.alto = alto;
        this.ancho = alto * textura.ancho() / (double) textura.alto();
    }

    public Sprite conColision(double radio) {
        this.radioColision = radio;
        return this;
    }

    public void cambiarTextura(Textura nueva) {
        this.textura = nueva;
        this.ancho = alto * nueva.ancho() / (double) nueva.alto();
    }
}
