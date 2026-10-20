package juego.mundo;

import estructuras.ListaEnlazada;
import java.util.Random;
import juego.audio.GestorAudio;
import juego.motor.Sprite;

public class ElSeguidor {

    public enum Estado {
        INACTIVO, APARICION, CAZA, DESVANECIENDO
    }

    public enum Resultado {
        NADA, ATRAPO, DESAPARECIO
    }

    private final Sprite sprite;
    private final Mundo3D mundo;
    private final GestorAudio audio;
    private final Random azar = new Random();

    private Estado estado = Estado.INACTIVO;
    private double x;
    private double y;
    private double duracion;
    private double tiempoObservado;
    private double tiempoRuta;
    private ListaEnlazada<Integer> ruta = new ListaEnlazada<>();
    private double tiempoPaso;
    private boolean observado;
    private double desvanecer;
    private int idRespiracion = -1;
    private double enfriamiento;
    private boolean silencioso;
    private double velocidadExtra = 1;

    public ElSeguidor(Sprite sprite, Mundo3D mundo, GestorAudio audio) {
        this.sprite = sprite;
        this.mundo = mundo;
        this.audio = audio;
        sprite.visible = false;
    }

    public void aparecer(double x, double y, double duracion) {
        this.x = x;
        this.y = y;
        this.duracion = duracion;
        this.tiempoObservado = 0;
        estado = Estado.APARICION;
        sprite.visible = true;
        sprite.alfa = 1f;
        sprite.distorsion = 0f;
        sincronizarSprite();
    }

    public void cazar(double x, double y, boolean silencioso) {
        this.x = x;
        this.y = y;
        this.silencioso = silencioso;
        estado = Estado.CAZA;
        tiempoObservado = 0;
        ruta.limpiar();
        tiempoRuta = 0;
        sprite.visible = true;
        sprite.alfa = 1f;
        sprite.distorsion = 0f;
        sincronizarSprite();
    }

    public void desaparecer(boolean conGlitch) {
        if (estado == Estado.INACTIVO || estado == Estado.DESVANECIENDO) {
            return;
        }
        if (conGlitch) {
            estado = Estado.DESVANECIENDO;
            desvanecer = 0.4;
            audio.reproducirEn("glitch", x, y, 0.7f, null);
        } else {
            apagar();
        }
    }

    public void apagar() {
        estado = Estado.INACTIVO;
        sprite.visible = false;
        ruta.limpiar();
        if (idRespiracion >= 0) {
            audio.detenerVoz(idRespiracion);
            idRespiracion = -1;
        }
    }

    public void enfriar(double segundos) {
        enfriamiento = segundos;
    }

    public boolean enfriando() {
        return enfriamiento > 0;
    }

    public void velocidadExtra(double factor) {
        velocidadExtra = factor;
    }

    public Resultado actualizar(double dt, double jx, double jy, double jang, boolean telefonoArriba,
            boolean linterna, float tension) {
        if (enfriamiento > 0) {
            enfriamiento -= dt;
        }
        if (estado == Estado.INACTIVO) {
            return Resultado.NADA;
        }
        double dx = x - jx;
        double dy = y - jy;
        double dist = Math.sqrt(dx * dx + dy * dy);
        double ang = normalizar(Math.atan2(dy, dx) - jang);
        boolean enVista = Math.abs(ang) < 0.62;
        boolean linea = enVista && mundo.casa().lineaDeVision(jx, jy, x, y);
        boolean iluminado = sprite.emisionActiva
                || (linterna && dist < 7.5 && Math.abs(ang) < 0.4)
                || mundo.iluminacion().brilloEn(x, y) > 0.14f
                || dist < 1.6;
        observado = linea && iluminado && !telefonoArriba;
        actualizarRespiracion(dist);

        if (estado == Estado.DESVANECIENDO) {
            desvanecer -= dt;
            sprite.distorsion = 0.9f;
            sprite.alfa = (float) Math.max(0, desvanecer / 0.4);
            if (desvanecer <= 0) {
                apagar();
                return Resultado.DESAPARECIO;
            }
            return Resultado.NADA;
        }

        sprite.distorsion = azar.nextFloat() < 0.04f ? 0.25f : 0f;

        if (estado == Estado.APARICION) {
            duracion -= dt;
            if (observado) {
                tiempoObservado += dt;
            }
            if (tiempoObservado > 1.3 || dist < 2.6) {
                desaparecer(true);
            } else if (duracion <= 0 && !observado) {
                apagar();
                return Resultado.DESAPARECIO;
            }
            return Resultado.NADA;
        }

        if (dist < 0.62) {
            return Resultado.ATRAPO;
        }
        if (observado) {
            tiempoObservado += dt;
            if (linterna && tiempoObservado > 3.2 && dist < 6) {
                desaparecer(true);
                enfriamiento = 18;
            }
            return Resultado.NADA;
        }
        tiempoObservado = Math.max(0, tiempoObservado - dt * 0.5);
        tiempoRuta -= dt;
        if (tiempoRuta <= 0 || ruta.estaVacia()) {
            recalcularRuta(jx, jy);
            tiempoRuta = 0.45;
        }
        double velocidad = (0.85 + tension * 0.95) * (telefonoArriba ? 1.45 : 1.0) * velocidadExtra;
        avanzar(dt, velocidad, jx, jy);
        tiempoPaso -= dt * velocidad;
        if (tiempoPaso <= 0) {
            tiempoPaso = 0.62;
            if (!silencioso || dist < 6) {
                audio.reproducirEn("paso_" + (1 + azar.nextInt(3)), x, y, 1.1f, dist < 9 ? "[Pasos cerca]" : null);
            }
        }
        sincronizarSprite();
        return Resultado.NADA;
    }

    private void actualizarRespiracion(double dist) {
        boolean cerca = sprite.visible && dist < 5.5 && estado != Estado.DESVANECIENDO;
        if (cerca && idRespiracion < 0) {
            idRespiracion = audio.reproducirBucleEn("respiracion_cerca", x, y, 0.9f);
            audio.agregarSubtitulo("[Respiración muy cerca]", 2.5);
        } else if (!cerca && idRespiracion >= 0) {
            audio.detenerVoz(idRespiracion);
            idRespiracion = -1;
        }
        if (idRespiracion >= 0) {
            audio.mover(idRespiracion, x, y);
        }
    }

    private void recalcularRuta(double jx, double jy) {
        Casa casa = mundo.casa();
        int origen = casa.indice((int) Math.floor(x), (int) Math.floor(y));
        int destino = casa.indice((int) Math.floor(jx), (int) Math.floor(jy));
        ListaEnlazada<Integer> camino = casa.navegacion().caminoMasCorto(origen, destino);
        if (!camino.estaVacia()) {
            camino.eliminarPrimero();
        }
        ruta = camino;
    }

    private void avanzar(double dt, double velocidad, double jx, double jy) {
        Casa casa = mundo.casa();
        double objetivoX;
        double objetivoY;
        if (ruta.estaVacia()) {
            objetivoX = jx;
            objetivoY = jy;
        } else {
            int siguiente = ruta.primero();
            int cx = siguiente % casa.ancho();
            int cy = siguiente / casa.ancho();
            Puerta puerta = casa.puerta(cx, cy);
            if (puerta != null && !puerta.transitable()) {
                if (!puerta.estaAbierta()) {
                    mundo.abrirPuertaLento(puerta);
                }
                return;
            }
            objetivoX = cx + 0.5;
            objetivoY = cy + 0.5;
        }
        double dx = objetivoX - x;
        double dy = objetivoY - y;
        double d = Math.sqrt(dx * dx + dy * dy);
        double paso = velocidad * dt;
        if (d <= paso) {
            x = objetivoX;
            y = objetivoY;
            if (!ruta.estaVacia()) {
                ruta.eliminarPrimero();
            }
        } else {
            x += dx / d * paso;
            y += dy / d * paso;
        }
    }

    private void sincronizarSprite() {
        sprite.x = x;
        sprite.y = y;
    }

    private static double normalizar(double a) {
        while (a > Math.PI) {
            a -= Math.PI * 2;
        }
        while (a < -Math.PI) {
            a += Math.PI * 2;
        }
        return a;
    }

    public Estado estado() {
        return estado;
    }

    public boolean activo() {
        return estado != Estado.INACTIVO;
    }

    public boolean observado() {
        return observado;
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public double distanciaA(double px, double py) {
        double dx = x - px;
        double dy = y - py;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public Sprite sprite() {
        return sprite;
    }
}
