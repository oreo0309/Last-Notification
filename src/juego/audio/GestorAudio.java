package juego.audio;

import estructuras.Cola;
import estructuras.TablaHash;
import java.util.Random;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

public class GestorAudio {

    private static final int FR = Sintetizador.FRECUENCIA;
    private static final int BLOQUE = 512;
    private static final int MAX_VOCES = 40;

    private static class Voz {
        float[] datos;
        double posicion;
        double velocidad = 1;
        float ganancia;
        float pan;
        boolean bucle;
        boolean posicional;
        double x;
        double y;
        float envio;
        int id;
        float desvanecer = -1;
        float volumenActual = 1;
    }

    public static class Subtitulo {
        public final String texto;
        public double restante;

        Subtitulo(String texto, double duracion) {
            this.texto = texto;
            this.restante = duracion;
        }
    }

    private float volumen = 0.8f;
    private boolean silenciado;

    private final TablaHash<String, float[]> sonidos;
    private final Voz[] voces = new Voz[MAX_VOCES];
    private final Object candado = new Object();
    private final Cola<Subtitulo> subtitulos = new Cola<>();
    private final Random azar = new Random();
    private int siguienteId = 1;

    private volatile double oyenteX;
    private volatile double oyenteY;
    private volatile double oyenteAngulo;
    private volatile float tension;
    private volatile float tensionObjetivo;
    private volatile float lluvia;
    private volatile float lluviaObjetivo;
    private volatile float ambiente = 1f;
    private volatile boolean latidos = true;
    private volatile float zumbidoOido;

    private SourceDataLine linea;
    private Thread hilo;
    private volatile boolean activo;
    private boolean disponible;

    private double faseDron1;
    private double faseDron2;
    private double faseDron3;
    private double faseSub;
    private double faseOido;
    private double marron;
    private double lluviaLp;
    private double lluviaHp;
    private double lluviaPrev;
    private double tiempoLatido;
    private float[] latido;

    private final float[][] combs = new float[4][];
    private final int[] combPos = new int[4];
    private final float[] combFiltro = new float[4];
    private final float[][] allpass = new float[2][];
    private final int[] allpassPos = new int[2];

    public GestorAudio() {
        sonidos = new Sintetizador().generarTodo();
        latido = sonidos.obtener("latido");
        int[] tamComb = {1557, 1617, 1491, 1422};
        for (int i = 0; i < 4; i++) {
            combs[i] = new float[tamComb[i]];
        }
        allpass[0] = new float[556];
        allpass[1] = new float[225];
    }

    public void iniciar() {
        try {
            AudioFormat formato = new AudioFormat(FR, 16, 2, true, false);
            linea = AudioSystem.getSourceDataLine(formato);
            linea.open(formato, BLOQUE * 4 * 8);
            linea.start();
            disponible = true;
            activo = true;
            hilo = new Thread(this::bucle, "mezclador-audio");
            hilo.setDaemon(true);
            hilo.setPriority(Thread.MAX_PRIORITY);
            hilo.start();
        } catch (Exception | LinkageError e) {
            disponible = false;
            linea = null;
        }
    }

    public boolean disponible() {
        return disponible;
    }

    public void detener() {
        activo = false;
        if (linea != null) {
            try {
                linea.stop();
                linea.close();
            } catch (Exception e) {
                linea = null;
            }
        }
    }

    public int reproducir(String clave) {
        return reproducir(clave, 1f, 0f, null);
    }

    public int reproducir(String clave, float ganancia) {
        return reproducir(clave, ganancia, 0f, null);
    }

    public int reproducir(String clave, float ganancia, float pan, String subtitulo) {
        Voz v = crearVoz(clave, ganancia);
        if (v == null) {
            return -1;
        }
        v.pan = Math.max(-1f, Math.min(1f, pan));
        agregarSubtitulo(subtitulo, duracion(clave));
        return insertar(v);
    }

    public int reproducirEn(String clave, double x, double y, float ganancia, String subtitulo) {
        Voz v = crearVoz(clave, ganancia);
        if (v == null) {
            return -1;
        }
        v.posicional = true;
        v.x = x;
        v.y = y;
        v.envio = 0.35f;
        agregarSubtitulo(subtitulo, duracion(clave));
        return insertar(v);
    }

    public int reproducirBucle(String clave, float ganancia) {
        Voz v = crearVoz(clave, ganancia);
        if (v == null) {
            return -1;
        }
        v.bucle = true;
        return insertar(v);
    }

    public int reproducirBucleEn(String clave, double x, double y, float ganancia) {
        Voz v = crearVoz(clave, ganancia);
        if (v == null) {
            return -1;
        }
        v.bucle = true;
        v.posicional = true;
        v.x = x;
        v.y = y;
        v.envio = 0.3f;
        return insertar(v);
    }

    public void variarTono(int id, double velocidad) {
        synchronized (candado) {
            for (Voz v : voces) {
                if (v != null && v.id == id) {
                    v.velocidad = velocidad;
                }
            }
        }
    }

    public void mover(int id, double x, double y) {
        synchronized (candado) {
            for (Voz v : voces) {
                if (v != null && v.id == id) {
                    v.x = x;
                    v.y = y;
                }
            }
        }
    }

    public void cambiarGanancia(int id, float ganancia) {
        synchronized (candado) {
            for (Voz v : voces) {
                if (v != null && v.id == id) {
                    v.ganancia = ganancia;
                }
            }
        }
    }

    public void detenerVoz(int id) {
        synchronized (candado) {
            for (Voz v : voces) {
                if (v != null && v.id == id) {
                    v.desvanecer = 0.15f;
                }
            }
        }
    }

    public void detenerTodo() {
        synchronized (candado) {
            for (int i = 0; i < voces.length; i++) {
                if (voces[i] != null) {
                    voces[i].desvanecer = 0.2f;
                }
            }
        }
    }

    private Voz crearVoz(String clave, float ganancia) {
        float[] datos = sonidos.obtener(clave);
        if (datos == null) {
            return null;
        }
        Voz v = new Voz();
        v.datos = datos;
        v.ganancia = ganancia;
        return v;
    }

    private int insertar(Voz v) {
        synchronized (candado) {
            v.id = siguienteId++;
            int libre = -1;
            for (int i = 0; i < voces.length; i++) {
                if (voces[i] == null) {
                    libre = i;
                    break;
                }
            }
            if (libre < 0) {
                libre = azar.nextInt(voces.length);
            }
            voces[libre] = v;
            return v.id;
        }
    }

    private double duracion(String clave) {
        float[] d = sonidos.obtener(clave);
        return d == null ? 2.0 : Math.max(2.2, d.length / (double) FR + 0.6);
    }

    public void agregarSubtitulo(String texto, double duracion) {
        if (texto == null || texto.isEmpty()) {
            return;
        }
        synchronized (subtitulos) {
            int n = subtitulos.tamano();
            boolean repetido = false;
            for (int i = 0; i < n; i++) {
                Subtitulo s = subtitulos.desencolar();
                if (s.texto.equals(texto)) {
                    s.restante = Math.max(s.restante, duracion);
                    repetido = true;
                }
                subtitulos.encolar(s);
            }
            if (repetido) {
                return;
            }
            subtitulos.encolar(new Subtitulo(texto, duracion));
            while (subtitulos.tamano() > 3) {
                subtitulos.desencolar();
            }
        }
    }

    public Subtitulo[] subtitulosActivos(double dt) {
        synchronized (subtitulos) {
            int n = subtitulos.tamano();
            Subtitulo[] lista = new Subtitulo[n];
            for (int i = 0; i < n; i++) {
                Subtitulo s = subtitulos.desencolar();
                s.restante -= dt;
                lista[i] = s;
            }
            int vigentes = 0;
            for (Subtitulo s : lista) {
                if (s.restante > 0) {
                    subtitulos.encolar(s);
                    vigentes++;
                }
            }
            Subtitulo[] resultado = new Subtitulo[vigentes];
            int k = 0;
            for (Subtitulo s : lista) {
                if (s.restante > 0) {
                    resultado[k++] = s;
                }
            }
            return resultado;
        }
    }

    public void limpiarSubtitulos() {
        synchronized (subtitulos) {
            subtitulos.limpiar();
        }
    }

    public void actualizarOyente(double x, double y, double angulo) {
        oyenteX = x;
        oyenteY = y;
        oyenteAngulo = angulo;
    }

    public void subirTension(float nivel) {
        tensionObjetivo = Math.max(0f, Math.min(1f, nivel));
    }

    public float tension() {
        return tension;
    }

    public void lluvia(boolean activa) {
        lluviaObjetivo = activa ? 1f : 0f;
    }

    public void ambiente(float nivel) {
        ambiente = nivel;
    }

    public void latidos(boolean activos) {
        latidos = activos;
    }

    public void zumbidoOido(float nivel) {
        zumbidoOido = nivel;
    }

    public float volumen() {
        return volumen;
    }

    public void volumen(float valor) {
        volumen = Math.max(0f, Math.min(1f, valor));
    }

    public boolean silenciado() {
        return silenciado;
    }

    public void silenciar(boolean valor) {
        silenciado = valor;
    }

    private void bucle() {
        float[] izq = new float[BLOQUE];
        float[] der = new float[BLOQUE];
        float[] envio = new float[BLOQUE];
        byte[] salida = new byte[BLOQUE * 4];
        while (activo) {
            try {
                mezclar(izq, der, envio);
                for (int i = 0; i < BLOQUE; i++) {
                    float m = (silenciado ? 0f : volumen) * 0.62f;
                    float l = suave(izq[i] * m);
                    float r = suave(der[i] * m);
                    int li = (int) (l * 32000);
                    int ri = (int) (r * 32000);
                    salida[i * 4] = (byte) li;
                    salida[i * 4 + 1] = (byte) (li >> 8);
                    salida[i * 4 + 2] = (byte) ri;
                    salida[i * 4 + 3] = (byte) (ri >> 8);
                }
                linea.write(salida, 0, salida.length);
            } catch (Exception e) {
                activo = false;
            }
        }
    }

    private static float suave(float v) {
        if (v > 1.5f) {
            return 1f;
        }
        if (v < -1.5f) {
            return -1f;
        }
        return v - (4f / 27f) * v * v * v;
    }

    private void mezclar(float[] izq, float[] der, float[] envio) {
        for (int i = 0; i < BLOQUE; i++) {
            izq[i] = 0;
            der[i] = 0;
            envio[i] = 0;
        }
        ambiente(izq, der);
        synchronized (candado) {
            double ox = oyenteX;
            double oy = oyenteY;
            double oa = oyenteAngulo;
            for (int k = 0; k < voces.length; k++) {
                Voz v = voces[k];
                if (v == null) {
                    continue;
                }
                float g = v.ganancia;
                float pan = v.pan;
                if (v.posicional) {
                    double dx = v.x - ox;
                    double dy = v.y - oy;
                    double d = Math.sqrt(dx * dx + dy * dy);
                    double ang = Math.atan2(dy, dx) - oa;
                    pan = (float) Math.sin(ang);
                    float detras = (float) Math.cos(ang) < 0 ? 0.8f : 1f;
                    g = (float) (g / (1 + d * 0.45)) * detras;
                }
                float gi = g * (float) Math.sqrt(0.5 * (1 - pan));
                float gd = g * (float) Math.sqrt(0.5 * (1 + pan));
                float[] datos = v.datos;
                boolean terminada = false;
                for (int i = 0; i < BLOQUE; i++) {
                    int p = (int) v.posicion;
                    if (p >= datos.length - 1) {
                        if (v.bucle) {
                            v.posicion -= datos.length - 1;
                            p = (int) v.posicion;
                        } else {
                            terminada = true;
                            break;
                        }
                    }
                    float f = (float) (v.posicion - p);
                    float s = datos[p] + (datos[p + 1] - datos[p]) * f;
                    if (v.desvanecer > 0) {
                        v.volumenActual -= 1f / (v.desvanecer * FR);
                        if (v.volumenActual <= 0) {
                            terminada = true;
                            break;
                        }
                        s *= v.volumenActual;
                    }
                    izq[i] += s * gi;
                    der[i] += s * gd;
                    envio[i] += s * g * v.envio;
                    v.posicion += v.velocidad;
                }
                if (terminada) {
                    voces[k] = null;
                }
            }
        }
        reverberar(izq, der, envio);
    }

    private void ambiente(float[] izq, float[] der) {
        float amb = ambiente;
        for (int i = 0; i < BLOQUE; i++) {
            tension += (tensionObjetivo - tension) * 0.00002f;
            lluvia += (lluviaObjetivo - lluvia) * 0.00002f;
            float t = tension;
            faseDron1 += 2 * Math.PI * 55.0 / FR;
            faseDron2 += 2 * Math.PI * (55.0 + 0.25 + t * 2.8) / FR;
            faseDron3 += 2 * Math.PI * 58.27 / FR;
            faseSub += 2 * Math.PI * 27.5 / FR;
            double dron = (Math.sin(faseDron1) + Math.sin(faseDron2)) * (0.03 + t * 0.06)
                    + Math.sin(faseDron3) * t * t * 0.07 + Math.sin(faseSub) * t * 0.08;
            double blanco = azar.nextDouble() * 2 - 1;
            marron = (marron + blanco * 0.02) * 0.998;
            double sala = marron * 0.6;
            double v = (dron + sala) * amb;
            float l = (float) v;
            float r = (float) v;
            if (lluvia > 0.001f) {
                lluviaLp += (blanco - lluviaLp) * 0.35;
                lluviaHp = 0.92 * (lluviaHp + lluviaLp - lluviaPrev);
                lluviaPrev = lluviaLp;
                double gota = azar.nextDouble() < 0.0015 ? (azar.nextDouble() - 0.5) * 0.6 : 0;
                float ll = (float) ((lluviaHp * 0.11 + gota) * lluvia);
                l += ll;
                r += (float) ((lluviaHp * 0.11 - gota * 0.5) * lluvia);
            }
            float oido = zumbidoOido;
            if (oido > 0.001f) {
                faseOido += 2 * Math.PI * 6900.0 / FR;
                float z = (float) (Math.sin(faseOido) * 0.012 * oido);
                l += z;
                r += z;
            }
            izq[i] += l;
            der[i] += r;
        }
        if (latidos && tension > 0.5f && latido != null) {
            double ritmo = 0.75 + (tension - 0.5) * 1.6;
            for (int i = 0; i < BLOQUE; i++) {
                tiempoLatido += ritmo / FR;
                double pos = (tiempoLatido % 1.0) * FR;
                int p = (int) pos;
                if (p < latido.length) {
                    float g = (tension - 0.5f) * 1.4f;
                    izq[i] += latido[p] * g;
                    der[i] += latido[p] * g;
                }
            }
        }
    }

    private void reverberar(float[] izq, float[] der, float[] envio) {
        for (int i = 0; i < BLOQUE; i++) {
            float entrada = envio[i] * 0.25f;
            float suma = 0;
            for (int c = 0; c < 4; c++) {
                float[] buf = combs[c];
                int pos = combPos[c];
                float salida = buf[pos];
                combFiltro[c] = salida * 0.7f + combFiltro[c] * 0.3f;
                buf[pos] = entrada + combFiltro[c] * 0.8f;
                combPos[c] = (pos + 1) % buf.length;
                suma += salida;
            }
            for (int a = 0; a < 2; a++) {
                float[] buf = allpass[a];
                int pos = allpassPos[a];
                float guardado = buf[pos];
                float salida = -suma + guardado;
                buf[pos] = suma + guardado * 0.5f;
                allpassPos[a] = (pos + 1) % buf.length;
                suma = salida;
            }
            izq[i] += suma * 0.9f;
            der[i] += suma * 0.85f;
        }
    }
}
