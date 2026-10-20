package juego.motor;

import java.awt.image.BufferedImage;

public class Textura {

    private final int ancho;
    private final int alto;
    private final int[] pixeles;
    private int[] emision;

    public Textura(int ancho, int alto) {
        this.ancho = ancho;
        this.alto = alto;
        this.pixeles = new int[ancho * alto];
    }

    public static Textura desdeImagen(BufferedImage imagen) {
        Textura t = new Textura(imagen.getWidth(), imagen.getHeight());
        imagen.getRGB(0, 0, t.ancho, t.alto, t.pixeles, 0, t.ancho);
        return t;
    }

    public void copiarDesde(BufferedImage imagen) {
        imagen.getRGB(0, 0, ancho, alto, pixeles, 0, ancho);
    }

    public BufferedImage aImagen() {
        BufferedImage imagen = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
        imagen.setRGB(0, 0, ancho, alto, pixeles, 0, ancho);
        return imagen;
    }

    public int[] crearEmision() {
        if (emision == null) {
            emision = new int[ancho * alto];
        }
        return emision;
    }

    public void ponerEmision(int x, int y, int intensidad) {
        crearEmision()[y * ancho + x] = Math.max(0, Math.min(255, intensidad));
    }

    public int ancho() {
        return ancho;
    }

    public int alto() {
        return alto;
    }

    public int[] pixeles() {
        return pixeles;
    }

    public int[] emision() {
        return emision;
    }

    public boolean tieneEmision() {
        return emision != null;
    }

    public int pixel(int x, int y) {
        return pixeles[y * ancho + x];
    }

    public void ponerPixel(int x, int y, int argb) {
        if (x >= 0 && y >= 0 && x < ancho && y < alto) {
            pixeles[y * ancho + x] = argb;
        }
    }
}
