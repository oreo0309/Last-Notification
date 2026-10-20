package juego.motor;

import estructuras.ListaEnlazada;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Random;

public class Renderizador3D {

    private final int ancho;
    private final int alto;
    private final BufferedImage imagen;
    private final int[] pixeles;
    private final float[] profundidad;
    private final float[] cono;
    private final Random azar = new Random();

    private MapaRender mapa;
    private Iluminacion luz;
    private double px;
    private double py;
    private double ojo;
    private double horizonte;
    private double proyeccion;
    private double dirX;
    private double dirY;
    private double planoX;
    private double planoY;
    private float niebla = 0.045f;

    public float linternaR = 1.0f;
    public float linternaG = 0.93f;
    public float linternaB = 0.80f;

    public Renderizador3D(int ancho, int alto) {
        this.ancho = ancho;
        this.alto = alto;
        this.imagen = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
        this.pixeles = ((DataBufferInt) imagen.getRaster().getDataBuffer()).getData();
        this.profundidad = new float[ancho * alto];
        this.cono = new float[ancho * alto];
        for (int y = 0; y < alto; y++) {
            for (int x = 0; x < ancho; x++) {
                double nx = (x + 0.5 - ancho / 2.0) / (ancho * 0.30);
                double ny = (y + 0.5 - alto / 2.0) / (alto * 0.46);
                double r = Math.sqrt(nx * nx + ny * ny);
                double f = Math.max(0, 1 - r);
                f = f * f * (3 - 2 * f);
                double centro = Math.max(0, 1 - r * 2.2);
                cono[y * ancho + x] = (float) ((f * 0.95 + centro * 0.45 + 0.05) * 1.3);
            }
        }
    }

    public BufferedImage imagen() {
        return imagen;
    }

    public int[] pixeles() {
        return pixeles;
    }

    public int ancho() {
        return ancho;
    }

    public int alto() {
        return alto;
    }

    public void niebla(float valor) {
        this.niebla = valor;
    }

    public void renderizar(Camara camara, MapaRender mapa, Iluminacion luz, ListaEnlazada<Sprite> sprites, boolean incluirFoto) {
        this.mapa = mapa;
        this.luz = luz;
        this.px = camara.x;
        this.py = camara.y;
        this.ojo = camara.ojo;
        this.dirX = Math.cos(camara.angulo);
        this.dirY = Math.sin(camara.angulo);
        this.planoX = -dirY * camara.tanMitadFov;
        this.planoY = dirX * camara.tanMitadFov;
        this.proyeccion = (ancho / 2.0) / camara.tanMitadFov;
        this.horizonte = alto / 2.0 + camara.inclinacion * alto;
        for (int x = 0; x < ancho; x++) {
            columna(x);
        }
        if (sprites != null) {
            dibujarSprites(sprites, incluirFoto);
        }
    }

    private int fila(double dist, double h) {
        double y = horizonte - (h - ojo) * proyeccion / dist;
        int f = (int) Math.ceil(y - 0.5);
        if (f < 0) {
            return 0;
        }
        return Math.min(f, alto);
    }

    private void columna(int x) {
        double camX = 2.0 * (x + 0.5) / ancho - 1.0;
        double rdx = dirX + planoX * camX;
        double rdy = dirY + planoY * camX;
        int mapX = (int) Math.floor(px);
        int mapY = (int) Math.floor(py);
        double deltaX = rdx == 0 ? 1e30 : Math.abs(1.0 / rdx);
        double deltaY = rdy == 0 ? 1e30 : Math.abs(1.0 / rdy);
        int pasoX;
        int pasoY;
        double ladoX;
        double ladoY;
        if (rdx < 0) {
            pasoX = -1;
            ladoX = (px - mapX) * deltaX;
        } else {
            pasoX = 1;
            ladoX = (mapX + 1.0 - px) * deltaX;
        }
        if (rdy < 0) {
            pasoY = -1;
            ladoY = (py - mapY) * deltaY;
        } else {
            pasoY = 1;
            ladoY = (mapY + 1.0 - py) * deltaY;
        }
        int abajo = alto;
        double hPrev = 0;
        int prevX = mapX;
        int prevY = mapY;
        Textura topePrev = null;
        for (int iter = 0; iter < 96; iter++) {
            double dist;
            int lado;
            if (ladoX < ladoY) {
                dist = ladoX;
                ladoX += deltaX;
                mapX += pasoX;
                lado = 0;
            } else {
                dist = ladoY;
                ladoY += deltaY;
                mapY += pasoY;
                lado = 1;
            }
            if (dist < 1e-4) {
                dist = 1e-4;
            }
            if (hPrev < ojo) {
                int ys = fila(dist, hPrev);
                if (ys < abajo) {
                    plano(x, ys, abajo, hPrev, rdx, rdy, topePrev);
                    abajo = ys;
                }
            }
            if (mapa.esPuerta(mapX, mapY)) {
                double apertura = mapa.aperturaPuerta(mapX, mapY);
                double t;
                double coord;
                if (mapa.puertaVertical(mapX, mapY)) {
                    t = rdx == 0 ? 1e30 : (mapX + 0.5 - px) / rdx;
                    coord = py + t * rdy - mapY;
                } else {
                    t = rdy == 0 ? 1e30 : (mapY + 0.5 - py) / rdy;
                    coord = px + t * rdx - mapX;
                }
                double salida = Math.min(ladoX, ladoY);
                if (t >= dist - 1e-6 && t <= salida + 1e-6 && coord >= 0 && coord < 1 && coord >= apertura) {
                    int ys = fila(t, 0);
                    if (ys < abajo) {
                        plano(x, ys, abajo, 0, rdx, rdy, null);
                        abajo = ys;
                    }
                    double u = coord - apertura;
                    double wx = px + rdx * t - rdx * 0.02;
                    double wy = py + rdy * t - rdy * 0.02;
                    int ytop = fila(t, 1);
                    muro(x, ytop, abajo, t, u, mapa.texturaPuerta(mapX, mapY), 1.0, wx, wy);
                    techo(x, 0, Math.min(ytop, abajo), rdx, rdy);
                    return;
                }
                hPrev = 0;
                topePrev = null;
                prevX = mapX;
                prevY = mapY;
                continue;
            }
            double h = mapa.altura(mapX, mapY);
            double u;
            if (lado == 0) {
                u = py + dist * rdy;
            } else {
                u = px + dist * rdx;
            }
            u -= Math.floor(u);
            if (lado == 0 && rdx > 0) {
                u = 1 - u;
            }
            if (lado == 1 && rdy < 0) {
                u = 1 - u;
            }
            double wx = px + rdx * dist - rdx * 0.03;
            double wy = py + rdy * dist - rdy * 0.03;
            if (h >= 1) {
                int ytop = fila(dist, 1);
                int ybase = Math.min(abajo, fila(dist, hPrev));
                Textura tex = mapa.texturaMuro(mapX, mapY, prevX, prevY);
                muro(x, ytop, ybase, dist, u, tex, 1.0, wx, wy);
                techo(x, 0, Math.min(ytop, abajo), rdx, rdy);
                return;
            }
            if (h > hPrev) {
                int ytop = fila(dist, h);
                int ybase = Math.min(abajo, fila(dist, hPrev));
                if (ytop < ybase) {
                    muro(x, ytop, ybase, dist, u, mapa.texturaLado(mapX, mapY), h, wx, wy);
                }
                abajo = Math.min(abajo, ytop);
            }
            hPrev = h;
            topePrev = h > 0 ? mapa.texturaTope(mapX, mapY) : null;
            prevX = mapX;
            prevY = mapY;
            if (abajo <= 0) {
                return;
            }
        }
        techo(x, 0, abajo, rdx, rdy);
    }

    private void iluminar(double wx, double wy, double d, int idx) {
        luz.muestrear(wx, wy);
        float atenuacion = 1f / (1f + (float) d * niebla);
        float l = luz.linterna;
        float r = luz.lr * atenuacion;
        float g = luz.lg * atenuacion;
        float b = luz.lb * atenuacion;
        if (l > 0) {
            float f = l * cono[idx] / (1f + 0.07f * (float) (d * d));
            r += f * linternaR;
            g += f * linternaG;
            b += f * linternaB;
        }
        float p = luz.brilloPantalla;
        if (p > 0) {
            float f = p / (1f + 0.8f * (float) (d * d));
            r += f * 0.42f;
            g += f * 0.58f;
            b += f * 0.95f;
        }
        luz.lr = r;
        luz.lg = g;
        luz.lb = b;
    }

    private static int sombrear(int c, float r, float g, float b) {
        int cr = (int) (((c >> 16) & 255) * r);
        int cg = (int) (((c >> 8) & 255) * g);
        int cb = (int) ((c & 255) * b);
        if (cr > 255) {
            cr = 255;
        }
        if (cg > 255) {
            cg = 255;
        }
        if (cb > 255) {
            cb = 255;
        }
        return (cr << 16) | (cg << 8) | cb;
    }

    private static int emitir(int sombreado, int c, int emision) {
        if (emision <= 0) {
            return sombreado;
        }
        float e = emision / 255f;
        int er = (int) (((c >> 16) & 255) * e);
        int eg = (int) (((c >> 8) & 255) * e);
        int eb = (int) ((c & 255) * e);
        int sr = (sombreado >> 16) & 255;
        int sg = (sombreado >> 8) & 255;
        int sb = sombreado & 255;
        return (Math.max(sr, er) << 16) | (Math.max(sg, eg) << 8) | Math.max(sb, eb);
    }

    private void muro(int x, int y0, int y1, double dist, double u, Textura tex, double hTex, double wx, double wy) {
        if (y0 >= y1) {
            return;
        }
        int tw = tex.ancho();
        int th = tex.alto();
        int tx = (int) (u * tw);
        if (tx >= tw) {
            tx = tw - 1;
        }
        if (tx < 0) {
            tx = 0;
        }
        int[] tp = tex.pixeles();
        int[] te = tex.emision();
        luz.muestrear(wx, wy);
        float baseR = luz.lr;
        float baseG = luz.lg;
        float baseB = luz.lb;
        float atenuacion = 1f / (1f + (float) dist * niebla);
        float lin = luz.linterna / (1f + 0.07f * (float) (dist * dist));
        float pan = luz.brilloPantalla / (1f + 0.8f * (float) (dist * dist));
        float fd = (float) dist;
        double escala = dist / proyeccion;
        for (int y = y0; y < y1; y++) {
            int idx = y * ancho + x;
            double z = ojo - (y + 0.5 - horizonte) * escala;
            double v = (hTex - z) / hTex;
            int ty = (int) (v * th);
            if (ty < 0) {
                ty = 0;
            } else if (ty >= th) {
                ty = th - 1;
            }
            int t = ty * tw + tx;
            int c = tp[t];
            float k = lin * cono[idx];
            float r = baseR * atenuacion + k * linternaR + pan * 0.42f;
            float g = baseG * atenuacion + k * linternaG + pan * 0.58f;
            float b = baseB * atenuacion + k * linternaB + pan * 0.95f;
            int s = sombrear(c, r, g, b);
            if (te != null) {
                s = emitir(s, c, te[t]);
            }
            pixeles[idx] = s;
            profundidad[idx] = fd;
        }
    }

    private void plano(int x, int y0, int y1, double h, double rdx, double rdy, Textura respaldo) {
        double alturaRel = ojo - h;
        for (int y = y0; y < y1; y++) {
            double dy = y + 0.5 - horizonte;
            if (dy <= 0.01) {
                continue;
            }
            double d = alturaRel * proyeccion / dy;
            double wx = px + rdx * d;
            double wy = py + rdy * d;
            int cx = (int) Math.floor(wx);
            int cy = (int) Math.floor(wy);
            Textura tex;
            if (h <= 0) {
                tex = mapa.texturaPiso(cx, cy);
            } else {
                tex = mapa.texturaTope(cx, cy);
                if (tex == null) {
                    tex = respaldo;
                }
            }
            int idx = y * ancho + x;
            if (tex == null) {
                pixeles[idx] = 0;
                profundidad[idx] = (float) d;
                continue;
            }
            int tw = tex.ancho();
            int th = tex.alto();
            int tx = (int) ((wx - cx) * tw);
            int ty = (int) ((wy - cy) * th);
            if (tx < 0) {
                tx = 0;
            } else if (tx >= tw) {
                tx = tw - 1;
            }
            if (ty < 0) {
                ty = 0;
            } else if (ty >= th) {
                ty = th - 1;
            }
            int t = ty * tw + tx;
            int c = tex.pixeles()[t];
            iluminar(wx, wy, d, idx);
            int s = sombrear(c, luz.lr, luz.lg, luz.lb);
            if (tex.emision() != null) {
                s = emitir(s, c, tex.emision()[t]);
            }
            pixeles[idx] = s;
            profundidad[idx] = (float) d;
        }
    }

    private void techo(int x, int y0, int y1, double rdx, double rdy) {
        double alturaRel = 1.0 - ojo;
        for (int y = y0; y < y1; y++) {
            double dy = horizonte - (y + 0.5);
            int idx = y * ancho + x;
            if (dy <= 0.01) {
                pixeles[idx] = 0;
                profundidad[idx] = 1e9f;
                continue;
            }
            double d = alturaRel * proyeccion / dy;
            double wx = px + rdx * d;
            double wy = py + rdy * d;
            int cx = (int) Math.floor(wx);
            int cy = (int) Math.floor(wy);
            Textura tex = mapa.texturaTecho(cx, cy);
            if (tex == null) {
                pixeles[idx] = 0;
                profundidad[idx] = (float) d;
                continue;
            }
            int tw = tex.ancho();
            int th = tex.alto();
            int tx = (int) ((wx - cx) * tw);
            int ty = (int) ((wy - cy) * th);
            if (tx < 0) {
                tx = 0;
            } else if (tx >= tw) {
                tx = tw - 1;
            }
            if (ty < 0) {
                ty = 0;
            } else if (ty >= th) {
                ty = th - 1;
            }
            int c = tex.pixeles()[ty * tw + tx];
            iluminar(wx, wy, d, idx);
            pixeles[idx] = sombrear(c, luz.lr * 0.8f, luz.lg * 0.8f, luz.lb * 0.8f);
            profundidad[idx] = (float) d;
        }
    }

    private void dibujarSprites(ListaEnlazada<Sprite> sprites, boolean incluirFoto) {
        for (Sprite s : sprites) {
            double dx = s.x - px;
            double dy = s.y - py;
            s.distanciaCache = dx * dx + dy * dy;
        }
        sprites.ordenar((a, b) -> Double.compare(b.distanciaCache, a.distanciaCache));
        double invDet = 1.0 / (planoX * dirY - dirX * planoY);
        for (Sprite s : sprites) {
            if (!s.visible || s.alfa <= 0.01f || (s.soloFoto && !incluirFoto)) {
                continue;
            }
            double dx = s.x - px;
            double dy = s.y - py;
            double tX = invDet * (dirY * dx - dirX * dy);
            double tY = invDet * (-planoY * dx + planoX * dy);
            if (tY <= 0.08) {
                continue;
            }
            double pantallaX = (ancho / 2.0) * (1 + tX / tY);
            double altoPix = s.alto * proyeccion / tY;
            double anchoPix = s.ancho * proyeccion / tY;
            double yBase = horizonte + (ojo - s.z) * proyeccion / tY;
            double yTope = yBase - altoPix;
            double xIni = pantallaX - anchoPix / 2;
            int x0 = Math.max(0, (int) Math.floor(xIni));
            int x1 = Math.min(ancho, (int) Math.ceil(xIni + anchoPix));
            int y0 = Math.max(0, (int) Math.floor(yTope));
            int y1 = Math.min(alto, (int) Math.ceil(yBase));
            if (x0 >= x1 || y0 >= y1) {
                continue;
            }
            Textura tex = s.textura;
            int tw = tex.ancho();
            int th = tex.alto();
            int[] tp = tex.pixeles();
            int[] te = s.emisionActiva ? tex.emision() : null;
            luz.muestrear(s.x, s.y);
            float atenuacion = 1f / (1f + (float) tY * niebla);
            float baseR = luz.lr * atenuacion;
            float baseG = luz.lg * atenuacion;
            float baseB = luz.lb * atenuacion;
            float lin = luz.linterna / (1f + 0.07f * (float) (tY * tY));
            float pan = luz.brilloPantalla / (1f + 0.8f * (float) (tY * tY));
            float fd = (float) tY;
            float alfa = s.alfa;
            for (int y = y0; y < y1; y++) {
                int desplazamiento = 0;
                if (s.distorsion > 0 && azar.nextFloat() < s.distorsion) {
                    desplazamiento = (int) ((azar.nextFloat() - 0.5f) * anchoPix * s.distorsion);
                }
                int ty = (int) ((y + 0.5 - yTope) / altoPix * th);
                if (ty < 0 || ty >= th) {
                    continue;
                }
                for (int x = x0; x < x1; x++) {
                    int idx = y * ancho + x;
                    if (fd >= profundidad[idx]) {
                        continue;
                    }
                    int tx = (int) ((x + 0.5 - xIni - desplazamiento) / anchoPix * tw);
                    if (tx < 0 || tx >= tw) {
                        continue;
                    }
                    int t = ty * tw + tx;
                    int c = tp[t];
                    if ((c >>> 24) == 0) {
                        continue;
                    }
                    float k = lin * cono[idx];
                    int col = sombrear(c, baseR + k * linternaR + pan * 0.42f, baseG + k * linternaG + pan * 0.58f,
                            baseB + k * linternaB + pan * 0.95f);
                    if (te != null) {
                        col = emitir(col, c, te[t]);
                    }
                    if (alfa < 1f) {
                        int o = pixeles[idx];
                        int r = (int) (((col >> 16) & 255) * alfa + ((o >> 16) & 255) * (1 - alfa));
                        int g = (int) (((col >> 8) & 255) * alfa + ((o >> 8) & 255) * (1 - alfa));
                        int b = (int) ((col & 255) * alfa + (o & 255) * (1 - alfa));
                        col = (r << 16) | (g << 8) | b;
                    }
                    pixeles[idx] = col;
                }
            }
        }
    }

    public double profundidadCentro() {
        return profundidad[(alto / 2) * ancho + ancho / 2];
    }
}
