package juego.motor;

import java.util.Random;

public final class Ruido {

    private final int[] permutacion = new int[512];

    public Ruido(long semilla) {
        Random azar = new Random(semilla);
        int[] base = new int[256];
        for (int i = 0; i < 256; i++) {
            base[i] = i;
        }
        for (int i = 255; i > 0; i--) {
            int j = azar.nextInt(i + 1);
            int t = base[i];
            base[i] = base[j];
            base[j] = t;
        }
        for (int i = 0; i < 512; i++) {
            permutacion[i] = base[i & 255];
        }
    }

    private double valor(int x, int y) {
        return permutacion[permutacion[x & 255] + (y & 255)] / 255.0;
    }

    public double suave(double x, double y, int periodo) {
        int x0 = (int) Math.floor(x);
        int y0 = (int) Math.floor(y);
        double fx = x - x0;
        double fy = y - y0;
        int x1 = x0 + 1;
        int y1 = y0 + 1;
        if (periodo > 0) {
            x0 = Math.floorMod(x0, periodo);
            y0 = Math.floorMod(y0, periodo);
            x1 = Math.floorMod(x1, periodo);
            y1 = Math.floorMod(y1, periodo);
        }
        double a = valor(x0, y0);
        double b = valor(x1, y0);
        double c = valor(x0, y1);
        double d = valor(x1, y1);
        double sx = fx * fx * (3 - 2 * fx);
        double sy = fy * fy * (3 - 2 * fy);
        double arriba = a + (b - a) * sx;
        double abajo = c + (d - c) * sx;
        return arriba + (abajo - arriba) * sy;
    }

    public double fbm(double x, double y, int octavas, int periodo) {
        double suma = 0;
        double amplitud = 0.5;
        double frecuencia = 1;
        double total = 0;
        int p = periodo;
        for (int i = 0; i < octavas; i++) {
            suma += suave(x * frecuencia, y * frecuencia, p) * amplitud;
            total += amplitud;
            amplitud *= 0.5;
            frecuencia *= 2;
            if (p > 0) {
                p *= 2;
            }
        }
        return suma / total;
    }
}
