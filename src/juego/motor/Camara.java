package juego.motor;

public class Camara {

    public double x;
    public double y;
    public double angulo;
    public double ojo = 0.6;
    public double inclinacion;
    public double tanMitadFov = 0.66;

    public Camara() {
    }

    public Camara(double x, double y, double angulo) {
        this.x = x;
        this.y = y;
        this.angulo = angulo;
    }

    public double dirX() {
        return Math.cos(angulo);
    }

    public double dirY() {
        return Math.sin(angulo);
    }

    public void copiar(Camara otra) {
        x = otra.x;
        y = otra.y;
        angulo = otra.angulo;
        ojo = otra.ojo;
        inclinacion = otra.inclinacion;
        tanMitadFov = otra.tanMitadFov;
    }
}
