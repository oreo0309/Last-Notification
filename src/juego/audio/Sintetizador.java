package juego.audio;

import estructuras.TablaHash;
import java.util.Random;

public final class Sintetizador {

    public static final int FRECUENCIA = 44100;
    private static final double DOS_PI = Math.PI * 2;

    private final Random azar = new Random(2026);

    public TablaHash<String, float[]> generarTodo() {
        TablaHash<String, float[]> s = new TablaHash<>(64);
        s.poner("notificacion", notificacion(false));
        s.poner("notificacion_rara", notificacion(true));
        s.poner("vibracion", vibracion());
        s.poner("scroll", scroll());
        s.poner("decision_buena", decisionBuena());
        s.poner("decision_mala", decisionMala());
        s.poner("glitch", glitch());
        s.poner("susurro_1", susurro(1.7, 1));
        s.poner("susurro_2", susurro(2.2, 2));
        s.poner("susurro_3", susurro(1.3, 3));
        s.poner("paso_1", paso(1));
        s.poner("paso_2", paso(2));
        s.poner("paso_3", paso(3));
        s.poner("golpe", golpe(180));
        s.poner("golpes", golpes(180, 3, 0.38));
        s.poner("golpes_vidrio", golpesVidrio());
        s.poner("crujido", crujido(1.8));
        s.poner("puerta_abre", crujido(0.6));
        s.poner("puerta_cierra", puertaCierra());
        s.poner("portazo", portazo());
        s.poner("stinger", stinger());
        s.poner("stinger_suave", stingerSuave());
        s.poner("trueno", trueno());
        s.poner("estatica", estatica());
        s.poner("latido", latido());
        s.poner("respira_in", respiracion(true, 1.0f));
        s.poner("respira_out", respiracion(false, 1.0f));
        s.poner("respiracion_cerca", respiracionCerca());
        s.poner("tecleo", tecleo());
        s.poner("interruptor", interruptor());
        s.poner("zumbido", zumbido());
        s.poner("tono_llamada", tonoLlamada(false));
        s.poner("tono_roto", tonoLlamada(true));
        s.poner("eco", ecoInicio());
        s.poner("router_off", routerOff());
        s.poner("apagar", apagar());
        s.poner("amanecer", amanecer());
        s.poner("menu_mover", tono(660, 0.06, 0.25));
        s.poner("menu_elegir", tono(880, 0.12, 0.3));
        s.poner("error", error());
        return s;
    }

    private float[] buffer(double segundos) {
        return new float[(int) (segundos * FRECUENCIA)];
    }

    private static double env(double t, double ataque, double caida) {
        if (t < 0) {
            return 0;
        }
        if (t < ataque) {
            return t / ataque;
        }
        return Math.exp(-(t - ataque) / caida);
    }

    private void normalizar(float[] b, float pico) {
        float max = 0;
        for (float v : b) {
            max = Math.max(max, Math.abs(v));
        }
        if (max <= 0) {
            return;
        }
        float k = pico / max;
        for (int i = 0; i < b.length; i++) {
            b[i] *= k;
        }
    }

    private void suavizarBordes(float[] b, double fadeSeg) {
        int n = Math.min(b.length / 2, (int) (fadeSeg * FRECUENCIA));
        for (int i = 0; i < n; i++) {
            float k = (float) i / n;
            b[i] *= k;
            b[b.length - 1 - i] *= k;
        }
    }

    private float[] tono(double f, double dur, double vol) {
        float[] b = buffer(dur);
        for (int i = 0; i < b.length; i++) {
            double t = (double) i / FRECUENCIA;
            b[i] = (float) (Math.sin(DOS_PI * f * t) * env(t, 0.004, dur / 3) * vol);
        }
        return b;
    }

    private float[] notificacion(boolean rara) {
        float[] b = buffer(rara ? 1.3 : 0.7);
        double fase1 = 0;
        double fase2 = 0;
        for (int i = 0; i < b.length; i++) {
            double t = (double) i / FRECUENCIA;
            double bajada = rara ? Math.pow(0.55, t) : 1;
            fase1 += DOS_PI * 1046.5 * bajada / FRECUENCIA;
            fase2 += DOS_PI * 1568.0 * bajada / FRECUENCIA;
            double a = Math.sin(fase1) * env(t, 0.003, 0.12) + Math.sin(fase1 * 2) * 0.15 * env(t, 0.003, 0.05);
            double c = (Math.sin(fase2) + Math.sin(fase2 * 2) * 0.12) * env(t - 0.11, 0.003, 0.22);
            double v = a * 0.6 + c * 0.6;
            if (rara) {
                v *= 0.7 + 0.3 * Math.sin(DOS_PI * 37 * t);
                v += (azar.nextDouble() - 0.5) * 0.08 * env(t, 0.01, 0.4);
                v += Math.sin(DOS_PI * 70 * t) * 0.25 * env(t - 0.2, 0.3, 0.5);
            }
            b[i] = (float) v;
        }
        normalizar(b, 0.55f);
        return b;
    }

    private float[] vibracion() {
        float[] b = buffer(0.95);
        double lp = 0;
        double compuerta = 0;
        for (int i = 0; i < b.length; i++) {
            double t = (double) i / FRECUENCIA;
            boolean activo = (t < 0.32) || (t > 0.5 && t < 0.82);
            compuerta += ((activo ? 1 : 0) - compuerta) * 0.003;
            double cuadrada = Math.sin(DOS_PI * 168 * t) > 0 ? 1 : -1;
            cuadrada += (azar.nextDouble() - 0.5) * 0.4;
            lp += (cuadrada - lp) * 0.08;
            double golpeteo = 0.75 + 0.25 * Math.sin(DOS_PI * 42 * t);
            b[i] = (float) (lp * golpeteo * compuerta);
        }
        normalizar(b, 0.5f);
        return b;
    }

    private float[] scroll() {
        float[] b = buffer(0.04);
        double hp = 0;
        double prev = 0;
        for (int i = 0; i < b.length; i++) {
            double t = (double) i / FRECUENCIA;
            double n = azar.nextDouble() * 2 - 1;
            hp = 0.6 * (hp + n - prev);
            prev = n;
            b[i] = (float) (hp * env(t, 0.0005, 0.006));
        }
        normalizar(b, 0.18f);
        return b;
    }

    private float[] decisionBuena() {
        float[] b = buffer(0.9);
        double[] notas = {523.25, 659.25, 783.99, 1046.5};
        for (int i = 0; i < b.length; i++) {
            double t = (double) i / FRECUENCIA;
            double v = 0;
            for (int k = 0; k < notas.length; k++) {
                double tt = t - k * 0.07;
                v += Math.sin(DOS_PI * notas[k] * t) * env(tt, 0.01, 0.3) * 0.4;
            }
            b[i] = (float) v;
        }
        normalizar(b, 0.35f);
        return b;
    }

    private float[] decisionMala() {
        float[] b = buffer(1.0);
        double lp = 0;
        for (int i = 0; i < b.length; i++) {
            double t = (double) i / FRECUENCIA;
            double saw1 = 2 * ((110 * t) % 1) - 1;
            double saw2 = 2 * ((116.5 * t) % 1) - 1;
            double v = (saw1 + saw2) * 0.5;
            lp += (v - lp) * 0.04;
            b[i] = (float) (lp * env(t, 0.01, 0.35));
        }
        normalizar(b, 0.45f);
        return b;
    }

    private float[] glitch() {
        float[] b = buffer(0.45);
        double f = 300;
        double fase = 0;
        for (int i = 0; i < b.length; i++) {
            double t = (double) i / FRECUENCIA;
            if (i % 900 == 0) {
                f = 80 + azar.nextDouble() * 2400;
            }
            fase += DOS_PI * f / FRECUENCIA;
            double v = Math.signum(Math.sin(fase)) * 0.6 + (azar.nextDouble() - 0.5) * 0.7;
            v = Math.round(v * 4) / 4.0;
            b[i] = (float) (v * env(t, 0.002, 0.18));
        }
        normalizar(b, 0.5f);
        return b;
    }

    private float[] susurro(double dur, int semilla) {
        Random r = new Random(semilla * 991L);
        float[] b = buffer(dur);
        double[] estado = new double[8];
        int silabas = (int) (dur * 5);
        double[] inicios = new double[silabas];
        double[] largos = new double[silabas];
        double[] formantes = new double[silabas];
        double t0 = 0.05;
        for (int k = 0; k < silabas; k++) {
            inicios[k] = t0;
            largos[k] = 0.09 + r.nextDouble() * 0.16;
            formantes[k] = 900 + r.nextDouble() * 1900;
            t0 += largos[k] + 0.03 + r.nextDouble() * 0.12;
        }
        for (int i = 0; i < b.length; i++) {
            double t = (double) i / FRECUENCIA;
            double amp = 0;
            double formante = 1500;
            for (int k = 0; k < silabas; k++) {
                double tt = t - inicios[k];
                if (tt >= 0 && tt < largos[k]) {
                    amp = Math.sin(Math.PI * tt / largos[k]);
                    formante = formantes[k] * (1 + 0.15 * Math.sin(tt * 20));
                }
            }
            double ruido = r.nextDouble() * 2 - 1;
            double v = bandaPasante(ruido, formante, 6, estado, 0) * 0.7 + bandaPasante(ruido, formante * 2.3, 8, estado, 4) * 0.4;
            b[i] = (float) (v * amp);
        }
        suavizarBordes(b, 0.05);
        normalizar(b, 0.4f);
        return b;
    }

    private double bandaPasante(double x, double f, double q, double[] e, int o) {
        double w = DOS_PI * f / FRECUENCIA;
        double alfa = Math.sin(w) / (2 * q);
        double a0 = 1 + alfa;
        double a1 = -2 * Math.cos(w);
        double a2 = 1 - alfa;
        double y = (alfa * x - alfa * e[o + 1] - a1 * e[o + 2] - a2 * e[o + 3]) / a0;
        e[o + 1] = e[o];
        e[o] = x;
        e[o + 3] = e[o + 2];
        e[o + 2] = y;
        return y;
    }

    private float[] paso(int variante) {
        Random r = new Random(variante * 77L);
        float[] b = buffer(0.32);
        double lp = 0;
        double fase = 0;
        double res = 0;
        double resPrev = 0;
        for (int i = 0; i < b.length; i++) {
            double t = (double) i / FRECUENCIA;
            double ruido = r.nextDouble() * 2 - 1;
            lp += (ruido - lp) * 0.05;
            fase += DOS_PI * (62 + variante * 6) * (1 - t) / FRECUENCIA;
            double golpe = Math.sin(fase) * env(t, 0.003, 0.05);
            double crujir = 0;
            if (variante == 3) {
                double impulso = (i % (int) (FRECUENCIA / 38.0) == 0) ? 1 : 0;
                double nuevo = impulso + 1.94 * res - 0.95 * resPrev;
                resPrev = res;
                res = nuevo;
                crujir = res * 0.02 * env(t - 0.05, 0.05, 0.08);
            }
            b[i] = (float) (golpe * 0.9 + lp * 1.6 * env(t, 0.002, 0.07) + crujir);
        }
        normalizar(b, 0.6f);
        return b;
    }

    private float[] golpe(double f) {
        float[] b = buffer(0.22);
        double[] estado = new double[4];
        for (int i = 0; i < b.length; i++) {
            double t = (double) i / FRECUENCIA;
            double ruido = (azar.nextDouble() * 2 - 1) * env(t, 0.0008, 0.012);
            double cuerpo = bandaPasante(ruido, f, 9, estado, 0) * 8;
            double grave = Math.sin(DOS_PI * 95 * t) * env(t, 0.001, 0.04);
            b[i] = (float) (cuerpo + grave + ruido * 0.3);
        }
        normalizar(b, 0.85f);
        return b;
    }

    private float[] golpes(double f, int veces, double separacion) {
        float[] uno = golpe(f);
        float[] b = buffer(separacion * veces + 0.3);
        for (int k = 0; k < veces; k++) {
            int ini = (int) (k * separacion * FRECUENCIA);
            float g = k == veces - 1 ? 1.0f : 0.85f;
            for (int i = 0; i < uno.length && ini + i < b.length; i++) {
                b[ini + i] += uno[i] * g;
            }
        }
        return b;
    }

    private float[] golpesVidrio() {
        float[] b = buffer(1.3);
        double[] estado = new double[4];
        for (int i = 0; i < b.length; i++) {
            double t = (double) i / FRECUENCIA;
            double impulso = 0;
            for (int k = 0; k < 3; k++) {
                double tt = t - k * 0.4;
                if (tt >= 0 && tt < 0.003) {
                    impulso += azar.nextDouble() * 2 - 1;
                }
            }
            b[i] = (float) (bandaPasante(impulso, 2600, 20, estado, 0) * 20 + impulso * 0.2);
        }
        normalizar(b, 0.55f);
        return b;
    }

    private float[] crujido(double dur) {
        float[] b = buffer(dur);
        double[] estado = new double[8];
        double fase = 0;
        double frec = 40;
        for (int i = 0; i < b.length; i++) {
            double t = (double) i / FRECUENCIA;
            frec += (azar.nextDouble() - 0.5) * 2.5;
            frec = Math.max(18, Math.min(110, frec + (60 - frec) * 0.0005));
            fase += frec / FRECUENCIA;
            double pulso = 0;
            if (fase >= 1) {
                fase -= 1;
                pulso = 1 + azar.nextDouble();
            }
            double amp = Math.sin(Math.PI * t / dur);
            amp = Math.pow(amp, 0.6);
            double v = bandaPasante(pulso, 720, 14, estado, 0) * 3 + bandaPasante(pulso, 1550, 18, estado, 4) * 2;
            b[i] = (float) (v * amp);
        }
        normalizar(b, 0.5f);
        return b;
    }

    private float[] puertaCierra() {
        float[] b = buffer(0.5);
        float[] c = crujido(0.25);
        System.arraycopy(c, 0, b, 0, c.length);
        float[] g = golpe(140);
        int ini = (int) (0.22 * FRECUENCIA);
        for (int i = 0; i < g.length && ini + i < b.length; i++) {
            b[ini + i] += g[i] * 0.6f;
        }
        normalizar(b, 0.5f);
        return b;
    }

    private float[] portazo() {
        float[] b = buffer(1.2);
        double lp = 0;
        for (int i = 0; i < b.length; i++) {
            double t = (double) i / FRECUENCIA;
            double ruido = azar.nextDouble() * 2 - 1;
            lp += (ruido - lp) * 0.12;
            double boom = Math.sin(DOS_PI * 52 * t) * env(t, 0.002, 0.25);
            double vibr = Math.sin(DOS_PI * 210 * t) * env(t - 0.02, 0.01, 0.12) * (0.5 + 0.5 * Math.sin(DOS_PI * 31 * t));
            b[i] = (float) (lp * env(t, 0.001, 0.06) * 2 + boom + vibr * 0.3);
        }
        normalizar(b, 0.95f);
        return b;
    }

    private float[] stinger() {
        float[] b = buffer(1.9);
        double[] notas = {233.08, 246.94, 261.63, 277.18, 466.16, 493.88};
        double[] fases = new double[notas.length];
        double faseCh = 0;
        double faseMod = 0;
        for (int i = 0; i < b.length; i++) {
            double t = (double) i / FRECUENCIA;
            double caida = Math.pow(0.6, t);
            double v = 0;
            for (int k = 0; k < notas.length; k++) {
                fases[k] += notas[k] * caida / FRECUENCIA;
                v += (2 * (fases[k] % 1) - 1) * 0.22;
            }
            faseMod += DOS_PI * 310 / FRECUENCIA;
            faseCh += DOS_PI * (2100 + Math.sin(faseMod) * 900) / FRECUENCIA;
            double chillido = Math.sin(faseCh) * 0.5 * env(t, 0.01, 0.5);
            double ruido = (azar.nextDouble() * 2 - 1) * env(t, 0.002, 0.18);
            double sub = Math.sin(DOS_PI * 38 * t) * env(t, 0.005, 0.6);
            double cuerpo = v * env(t, 0.004, 0.7);
            double total = cuerpo + chillido + ruido * 0.9 + sub;
            b[i] = (float) Math.tanh(total * 1.6);
        }
        suavizarBordes(b, 0.002);
        normalizar(b, 0.98f);
        return b;
    }

    private float[] stingerSuave() {
        float[] b = buffer(2.8);
        double lp = 0;
        for (int i = 0; i < b.length; i++) {
            double t = (double) i / FRECUENCIA;
            double amp = t < 1.0 ? t : Math.exp(-(t - 1.0) / 0.7);
            double ruido = azar.nextDouble() * 2 - 1;
            lp += (ruido - lp) * 0.01;
            double v = Math.sin(DOS_PI * 55 * t) + Math.sin(DOS_PI * 58.3 * t) * 0.8 + lp * 3;
            b[i] = (float) (v * amp);
        }
        normalizar(b, 0.45f);
        return b;
    }

    private float[] trueno() {
        float[] b = buffer(5.0);
        double lp = 0;
        double lp2 = 0;
        double mod = 1;
        for (int i = 0; i < b.length; i++) {
            double t = (double) i / FRECUENCIA;
            double ruido = azar.nextDouble() * 2 - 1;
            lp += (ruido - lp) * 0.02;
            lp2 += (lp - lp2) * 0.05;
            if (i % 2205 == 0) {
                mod = 0.4 + azar.nextDouble() * 0.9;
            }
            double crack = ruido * env(t, 0.001, 0.05) * 0.7;
            double rumor = lp2 * 14 * mod * env(t, 0.15, 1.6);
            b[i] = (float) (crack + rumor);
        }
        normalizar(b, 0.9f);
        return b;
    }

    private float[] estatica() {
        float[] b = buffer(2.0);
        for (int i = 0; i < b.length; i++) {
            double t = (double) i / FRECUENCIA;
            b[i] = (float) ((azar.nextDouble() * 2 - 1) * 0.6 + Math.sin(DOS_PI * 60 * t) * 0.15);
        }
        normalizar(b, 0.3f);
        return b;
    }

    private float[] latido() {
        float[] b = buffer(0.6);
        for (int i = 0; i < b.length; i++) {
            double t = (double) i / FRECUENCIA;
            double lub = Math.sin(DOS_PI * 52 * t) * env(t, 0.004, 0.06);
            double dub = Math.sin(DOS_PI * 44 * t) * env(t - 0.2, 0.004, 0.07) * 0.75;
            b[i] = (float) (lub + dub);
        }
        normalizar(b, 0.9f);
        return b;
    }

    private float[] respiracion(boolean entrada, float vol) {
        float[] b = buffer(1.6);
        double lp = 0;
        double hp = 0;
        double prev = 0;
        for (int i = 0; i < b.length; i++) {
            double t = (double) i / FRECUENCIA;
            double k = t / 1.6;
            double amp = Math.sin(Math.PI * k);
            double ruido = azar.nextDouble() * 2 - 1;
            double corte = entrada ? 0.05 + k * 0.15 : 0.2 - k * 0.15;
            lp += (ruido - lp) * corte;
            hp = 0.97 * (hp + lp - prev);
            prev = lp;
            b[i] = (float) (hp * amp * vol);
        }
        normalizar(b, 0.3f);
        return b;
    }

    private float[] respiracionCerca() {
        float[] b = buffer(3.2);
        double lp = 0;
        for (int i = 0; i < b.length; i++) {
            double t = (double) i / FRECUENCIA;
            double ciclo = (t % 1.6) / 1.6;
            double amp = Math.sin(Math.PI * ciclo);
            amp *= amp;
            double ruido = azar.nextDouble() * 2 - 1;
            lp += (ruido - lp) * 0.07;
            double raspa = 1 + 0.6 * Math.sin(DOS_PI * 33 * t) * (t % 3.2 > 1.6 ? 1 : 0.3);
            b[i] = (float) (lp * amp * raspa);
        }
        normalizar(b, 0.45f);
        return b;
    }

    private float[] tecleo() {
        float[] b = buffer(1.4);
        double t0 = 0.02;
        while (t0 < 1.3) {
            int ini = (int) (t0 * FRECUENCIA);
            for (int i = 0; i < 400 && ini + i < b.length; i++) {
                double t = (double) i / FRECUENCIA;
                b[ini + i] += (float) ((azar.nextDouble() * 2 - 1) * env(t, 0.0003, 0.003));
            }
            t0 += 0.08 + azar.nextDouble() * 0.12;
        }
        normalizar(b, 0.25f);
        return b;
    }

    private float[] interruptor() {
        float[] b = buffer(0.06);
        for (int i = 0; i < b.length; i++) {
            double t = (double) i / FRECUENCIA;
            b[i] = (float) ((azar.nextDouble() * 2 - 1) * env(t, 0.0003, 0.004) + Math.sin(DOS_PI * 1800 * t) * env(t, 0.0003, 0.01) * 0.4);
        }
        normalizar(b, 0.4f);
        return b;
    }

    private float[] zumbido() {
        float[] b = buffer(1.2);
        for (int i = 0; i < b.length; i++) {
            double t = (double) i / FRECUENCIA;
            double saw = 2 * ((120 * t) % 1) - 1;
            double crepitar = azar.nextDouble() < 0.002 ? (azar.nextDouble() * 2 - 1) * 3 : 0;
            double amp = 0.6 + 0.4 * Math.sin(DOS_PI * 7 * t);
            b[i] = (float) ((saw * 0.4 + Math.sin(DOS_PI * 240 * t) * 0.3 + crepitar) * amp);
        }
        suavizarBordes(b, 0.05);
        normalizar(b, 0.3f);
        return b;
    }

    private float[] tonoLlamada(boolean roto) {
        float[] b = buffer(2.6);
        double[] melodia = {784, 988, 1175, 988, 784, 988, 1175, 1568};
        double fase = 0;
        for (int i = 0; i < b.length; i++) {
            double t = (double) i / FRECUENCIA;
            int nota = (int) (t / 0.16) % melodia.length;
            double tn = t % 0.16;
            double f = melodia[nota];
            if (roto) {
                f *= 0.5 + 0.5 * Math.pow(0.7, t) + Math.sin(t * 13) * 0.03;
            }
            fase += DOS_PI * f / FRECUENCIA;
            double v = (Math.sin(fase) + 0.3 * Math.sin(fase * 3)) * env(tn, 0.004, 0.08);
            if (roto && azar.nextDouble() < 0.0008) {
                v *= -3;
            }
            b[i] = (float) (v * ((t % 1.3) < 1.28 ? 1 : 0));
        }
        normalizar(b, 0.45f);
        return b;
    }

    private float[] ecoInicio() {
        float[] base = buffer(0.25);
        for (int i = 0; i < base.length; i++) {
            double t = (double) i / FRECUENCIA;
            base[i] = (float) ((Math.sin(DOS_PI * 1318.5 * t) + Math.sin(DOS_PI * 1975.5 * t) * 0.5) * env(t, 0.005, 0.07));
        }
        float[] b = buffer(1.4);
        float g = 1f;
        for (int k = 0; k < 5; k++) {
            int ini = (int) (k * 0.21 * FRECUENCIA);
            for (int i = 0; i < base.length && ini + i < b.length; i++) {
                b[ini + i] += base[i] * g;
            }
            g *= 0.45f;
        }
        normalizar(b, 0.35f);
        return b;
    }

    private float[] routerOff() {
        float[] b = buffer(0.9);
        double fase = 0;
        for (int i = 0; i < b.length; i++) {
            double t = (double) i / FRECUENCIA;
            double f = 1400 * Math.pow(0.02, t / 0.8);
            fase += DOS_PI * f / FRECUENCIA;
            b[i] = (float) (Math.sin(fase) * env(t, 0.005, 0.5) * 0.6);
        }
        float[] clic = interruptor();
        for (int i = 0; i < clic.length; i++) {
            b[i] += clic[i];
        }
        normalizar(b, 0.45f);
        return b;
    }

    private float[] apagar() {
        float[] b = buffer(1.0);
        double[] notas = {880, 660, 440};
        for (int i = 0; i < b.length; i++) {
            double t = (double) i / FRECUENCIA;
            double v = 0;
            for (int k = 0; k < notas.length; k++) {
                v += Math.sin(DOS_PI * notas[k] * t) * env(t - k * 0.18, 0.005, 0.15);
            }
            b[i] = (float) v;
        }
        normalizar(b, 0.4f);
        return b;
    }

    private float[] amanecer() {
        float[] b = buffer(6.0);
        double[] acorde = {261.63, 329.63, 392.0, 523.25};
        for (int i = 0; i < b.length; i++) {
            double t = (double) i / FRECUENCIA;
            double amp = Math.min(1, t / 2.0) * Math.min(1, (6.0 - t) / 2.0);
            double v = 0;
            for (double f : acorde) {
                v += Math.sin(DOS_PI * f * t + Math.sin(t * 0.7) * 0.5) * 0.25;
            }
            double pajaro = 0;
            double tp = t % 1.7;
            if (t > 1.5 && tp < 0.18) {
                pajaro = Math.sin(DOS_PI * (3200 + 900 * Math.sin(tp * 90)) * t) * Math.sin(Math.PI * tp / 0.18) * 0.25;
            }
            b[i] = (float) (v * amp + pajaro);
        }
        normalizar(b, 0.4f);
        return b;
    }

    private float[] error() {
        float[] b = buffer(0.25);
        for (int i = 0; i < b.length; i++) {
            double t = (double) i / FRECUENCIA;
            b[i] = (float) ((Math.signum(Math.sin(DOS_PI * 180 * t)) * 0.4) * env(t, 0.003, 0.08));
        }
        normalizar(b, 0.3f);
        return b;
    }
}
