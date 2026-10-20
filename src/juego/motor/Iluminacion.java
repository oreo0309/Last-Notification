package juego.motor;

import estructuras.ListaEnlazada;

public class Iluminacion {

    private final MapaRender mapa;
    private final int anchoMapa;
    private final int altoMapa;
    private final int anchoEsquinas;
    private final float[][][] canales;
    private final float[] totalR;
    private final float[] totalG;
    private final float[] totalB;
    private final float[] factores = new float[LuzPuntual.CANALES];
    private final ListaEnlazada<LuzPuntual> luces = new ListaEnlazada<>();

    public float ambiente = 0.035f;
    public float linterna;
    public float brilloPantalla;
    public float lr;
    public float lg;
    public float lb;

    public Iluminacion(MapaRender mapa) {
        this.mapa = mapa;
        this.anchoMapa = mapa.ancho();
        this.altoMapa = mapa.alto();
        this.anchoEsquinas = anchoMapa + 1;
        int esquinas = (anchoMapa + 1) * (altoMapa + 1);
        canales = new float[LuzPuntual.CANALES][3][esquinas];
        totalR = new float[esquinas];
        totalG = new float[esquinas];
        totalB = new float[esquinas];
        for (int i = 0; i < factores.length; i++) {
            factores[i] = 1f;
        }
    }

    public void agregar(LuzPuntual luz) {
        luces.agregar(luz);
    }

    public void factor(int canal, float valor) {
        factores[canal] = Math.max(0f, valor);
    }

    public float factor(int canal) {
        return factores[canal];
    }

    public void hornear() {
        int celdas = anchoMapa * altoMapa;
        float[][][] porCelda = new float[LuzPuntual.CANALES][3][celdas];
        for (LuzPuntual luz : luces) {
            int x0 = Math.max(0, (int) Math.floor(luz.x - luz.radio));
            int x1 = Math.min(anchoMapa - 1, (int) Math.ceil(luz.x + luz.radio));
            int y0 = Math.max(0, (int) Math.floor(luz.y - luz.radio));
            int y1 = Math.min(altoMapa - 1, (int) Math.ceil(luz.y + luz.radio));
            for (int cy = y0; cy <= y1; cy++) {
                for (int cx = x0; cx <= x1; cx++) {
                    if (!celdaAbierta(cx, cy)) {
                        continue;
                    }
                    double dx = cx + 0.5 - luz.x;
                    double dy = cy + 0.5 - luz.y;
                    double d = Math.sqrt(dx * dx + dy * dy);
                    if (d > luz.radio || !visible(luz.x, luz.y, cx + 0.5, cy + 0.5)) {
                        continue;
                    }
                    double f = (1.0 - d / luz.radio) / (1.0 + 0.1 * d * d);
                    int i = cy * anchoMapa + cx;
                    porCelda[luz.canal][0][i] += (float) (luz.r * f);
                    porCelda[luz.canal][1][i] += (float) (luz.g * f);
                    porCelda[luz.canal][2][i] += (float) (luz.b * f);
                }
            }
        }
        for (int c = 0; c < LuzPuntual.CANALES; c++) {
            for (int ey = 0; ey <= altoMapa; ey++) {
                for (int ex = 0; ex <= anchoMapa; ex++) {
                    float r = 0;
                    float g = 0;
                    float b = 0;
                    int n = 0;
                    for (int oy = -1; oy <= 0; oy++) {
                        for (int ox = -1; ox <= 0; ox++) {
                            int cx = ex + ox;
                            int cy = ey + oy;
                            if (cx < 0 || cy < 0 || cx >= anchoMapa || cy >= altoMapa || !celdaAbierta(cx, cy)) {
                                continue;
                            }
                            int i = cy * anchoMapa + cx;
                            r += porCelda[c][0][i];
                            g += porCelda[c][1][i];
                            b += porCelda[c][2][i];
                            n++;
                        }
                    }
                    int e = ey * anchoEsquinas + ex;
                    canales[c][0][e] = n == 0 ? 0 : r / n;
                    canales[c][1][e] = n == 0 ? 0 : g / n;
                    canales[c][2][e] = n == 0 ? 0 : b / n;
                }
            }
        }
        combinar();
    }

    public void combinar() {
        for (int e = 0; e < totalR.length; e++) {
            float r = ambiente;
            float g = ambiente;
            float b = ambiente * 1.25f;
            for (int c = 0; c < LuzPuntual.CANALES; c++) {
                float f = factores[c];
                if (f == 0f) {
                    continue;
                }
                r += canales[c][0][e] * f;
                g += canales[c][1][e] * f;
                b += canales[c][2][e] * f;
            }
            totalR[e] = r;
            totalG[e] = g;
            totalB[e] = b;
        }
    }

    public void muestrear(double x, double y) {
        if (x < 0) {
            x = 0;
        }
        if (y < 0) {
            y = 0;
        }
        if (x > anchoMapa - 0.001) {
            x = anchoMapa - 0.001;
        }
        if (y > altoMapa - 0.001) {
            y = altoMapa - 0.001;
        }
        int ix = (int) x;
        int iy = (int) y;
        float fx = (float) (x - ix);
        float fy = (float) (y - iy);
        int e00 = iy * anchoEsquinas + ix;
        int e10 = e00 + 1;
        int e01 = e00 + anchoEsquinas;
        int e11 = e01 + 1;
        float w00 = (1 - fx) * (1 - fy);
        float w10 = fx * (1 - fy);
        float w01 = (1 - fx) * fy;
        float w11 = fx * fy;
        lr = totalR[e00] * w00 + totalR[e10] * w10 + totalR[e01] * w01 + totalR[e11] * w11;
        lg = totalG[e00] * w00 + totalG[e10] * w10 + totalG[e01] * w01 + totalG[e11] * w11;
        lb = totalB[e00] * w00 + totalB[e10] * w10 + totalB[e01] * w01 + totalB[e11] * w11;
    }

    public float brilloEn(double x, double y) {
        muestrear(x, y);
        return (lr + lg + lb) / 3f;
    }

    private boolean celdaAbierta(int x, int y) {
        return !mapa.bloqueaLuz(x, y);
    }

    public boolean visible(double x0, double y0, double x1, double y1) {
        double dx = x1 - x0;
        double dy = y1 - y0;
        double d = Math.sqrt(dx * dx + dy * dy);
        int pasos = (int) Math.ceil(d / 0.08);
        int inicioX = (int) Math.floor(x0);
        int inicioY = (int) Math.floor(y0);
        int finX = (int) Math.floor(x1);
        int finY = (int) Math.floor(y1);
        for (int i = 1; i < pasos; i++) {
            double t = (double) i / pasos;
            int cx = (int) Math.floor(x0 + dx * t);
            int cy = (int) Math.floor(y0 + dy * t);
            if ((cx == inicioX && cy == inicioY) || (cx == finX && cy == finY)) {
                continue;
            }
            if (mapa.bloqueaLuz(cx, cy)) {
                return false;
            }
        }
        return true;
    }
}
