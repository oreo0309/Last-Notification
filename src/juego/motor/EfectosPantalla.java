package juego.motor;

import java.util.Random;

public class EfectosPantalla {

    private final int ancho;
    private final int alto;
    private final float[] vineta;
    private final int[] copia;
    private final byte[] tablaRuido;
    private final Random azar = new Random();

    public float grano = 0.06f;
    public float fuerzaVineta = 0.55f;
    public float aberracion;
    public float glitch;
    public float desaturacion;
    public float tinteRojo;
    public float brillo = 1f;
    public boolean escalaGrises;
    public float oscurecer;

    public EfectosPantalla(int ancho, int alto) {
        this.ancho = ancho;
        this.alto = alto;
        this.vineta = new float[ancho * alto];
        this.copia = new int[ancho * alto];
        this.tablaRuido = new byte[ancho * alto * 2 + 7919];
        for (int i = 0; i < tablaRuido.length; i++) {
            tablaRuido[i] = (byte) (azar.nextInt(256) - 128);
        }
        for (int y = 0; y < alto; y++) {
            for (int x = 0; x < ancho; x++) {
                double nx = (x - ancho / 2.0) / (ancho / 2.0);
                double ny = (y - alto / 2.0) / (alto / 2.0);
                double d = Math.sqrt(nx * nx * 0.8 + ny * ny);
                vineta[y * ancho + x] = (float) Math.max(0, Math.min(1, (d - 0.45) / 0.9));
            }
        }
    }

    public void aplicar(int[] px) {
        float v = fuerzaVineta;
        float gr = grano * 255f;
        float des = Math.min(1f, desaturacion + (escalaGrises ? 1f : 0f));
        float rojo = tinteRojo;
        float br = brillo * (1f - oscurecer);
        int desfase = azar.nextInt(ancho * alto);
        float escalaRuido = gr / 256f;
        for (int i = 0; i < px.length; i++) {
            int c = px[i];
            float r = (c >> 16) & 255;
            float g = (c >> 8) & 255;
            float b = c & 255;
            if (des > 0) {
                float l = r * 0.3f + g * 0.59f + b * 0.11f;
                r += (l - r) * des;
                g += (l - g) * des;
                b += (l - b) * des;
            }
            if (rojo > 0) {
                r = r + (r * 0.4f + 40f) * rojo;
                g = g * (1f - rojo * 0.55f);
                b = b * (1f - rojo * 0.5f);
            }
            float f = br * (1f - vineta[i] * v);
            r *= f;
            g *= f;
            b *= f;
            if (gr > 0) {
                float n = tablaRuido[i + desfase] * escalaRuido;
                r += n;
                g += n;
                b += n;
            }
            int ir = r < 0 ? 0 : (r > 255 ? 255 : (int) r);
            int ig = g < 0 ? 0 : (g > 255 ? 255 : (int) g);
            int ib = b < 0 ? 0 : (b > 255 ? 255 : (int) b);
            px[i] = (ir << 16) | (ig << 8) | ib;
        }
        if (aberracion > 0.05f) {
            int desp = Math.max(1, Math.round(aberracion * 4));
            System.arraycopy(px, 0, copia, 0, px.length);
            for (int y = 0; y < alto; y++) {
                int fila = y * ancho;
                for (int x = 0; x < ancho; x++) {
                    int xr = Math.min(ancho - 1, x + desp);
                    int xb = Math.max(0, x - desp);
                    int r = (copia[fila + xr] >> 16) & 255;
                    int g = (copia[fila + x] >> 8) & 255;
                    int b = copia[fila + xb] & 255;
                    px[fila + x] = (r << 16) | (g << 8) | b;
                }
            }
        }
        if (glitch > 0.02f) {
            int franjas = (int) (glitch * 10) + 1;
            for (int k = 0; k < franjas; k++) {
                if (azar.nextFloat() > glitch) {
                    continue;
                }
                int y0 = azar.nextInt(alto);
                int h = 1 + azar.nextInt(Math.max(2, (int) (alto * 0.06)));
                int desp = (int) ((azar.nextFloat() - 0.5f) * ancho * 0.12f * glitch);
                boolean invertir = azar.nextFloat() < glitch * 0.15f;
                for (int y = y0; y < Math.min(alto, y0 + h); y++) {
                    int fila = y * ancho;
                    System.arraycopy(px, fila, copia, 0, ancho);
                    for (int x = 0; x < ancho; x++) {
                        int sx = x - desp;
                        if (sx < 0) {
                            sx += ancho;
                        } else if (sx >= ancho) {
                            sx -= ancho;
                        }
                        int c = copia[sx];
                        px[fila + x] = invertir ? (~c) & 0xFFFFFF : c;
                    }
                }
            }
        }
    }
}
