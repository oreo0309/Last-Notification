package juego.mundo;

import estructuras.ListaEnlazada;
import estructuras.TablaHash;
import java.awt.image.BufferedImage;
import java.util.Random;
import juego.audio.GestorAudio;
import juego.motor.Camara;
import juego.motor.EfectosPantalla;
import juego.motor.GeneradorTexturas;
import juego.motor.Iluminacion;
import juego.motor.LuzPuntual;
import juego.motor.Renderizador3D;
import juego.motor.Sprite;
import juego.motor.Textura;

public final class Mundo3D {

    public static final int ANCHO_RENDER = 640;
    public static final int ALTO_RENDER = 360;

    private final TablaHash<String, Textura> texturas;
    private final GeneradorTexturas generador;
    private final GestorAudio audio;
    private final Casa casa;
    private final Iluminacion luz;
    private final Renderizador3D render;
    private final Renderizador3D renderFoto;
    private final EfectosPantalla efectos;
    private final EfectosPantalla efectosFoto;
    private final ListaEnlazada<Sprite> sprites = new ListaEnlazada<>();
    private final Random azar = new Random();
    private final ElSeguidor seguidor;
    private final Textura[] fasesRouter = new Textura[8];
    private final Textura routerApagado;

    private final Sprite lamparaMesa;
    private final Sprite reloj;
    private final Sprite peluche;
    private final Sprite router;
    private final Sprite telefonoMesa;
    private final Sprite jugadorFoto;

    private boolean lamparaEncendida;
    private boolean tvEncendida;
    private boolean apagon;
    private boolean tormenta;
    private boolean routerEncendido = true;
    private boolean reducirDestellos;
    private double parpadeo;
    private double pausaParpadeo;
    private boolean luzParpadeoEncendida = true;
    private double relampago;
    private double proximoRelampago = 20;
    private double truenoPendiente = -1;
    private double tRouter;
    private int faseRouter;
    private int idEstatica = -1;
    private int minutoReloj = -1;
    private boolean relojCorrupto;
    private boolean telefonoBrillando;
    private double brilloTelefono;
    private float nivelPerturbacion;
    private boolean espejoFigura;

    public Mundo3D(TablaHash<String, Textura> texturas, GeneradorTexturas generador, GestorAudio audio) {
        this.texturas = texturas;
        this.generador = generador;
        this.audio = audio;
        this.casa = new Casa(texturas);
        this.luz = new Iluminacion(casa);
        this.render = new Renderizador3D(ANCHO_RENDER, ALTO_RENDER);
        this.renderFoto = new Renderizador3D(240, 150);
        this.efectos = new EfectosPantalla(ANCHO_RENDER, ALTO_RENDER);
        this.efectosFoto = new EfectosPantalla(240, 150);
        efectosFoto.grano = 0.09f;
        efectosFoto.fuerzaVineta = 0.8f;
        efectosFoto.desaturacion = 0.35f;
        efectosFoto.brillo = 1.5f;
        for (int i = 0; i < 8; i++) {
            fasesRouter[i] = generador.router(i);
        }
        routerApagado = generador.router(-1);

        lamparaMesa = agregarSprite("lampara", 2.5, 1.45, 0.24, 0.22);
        reloj = agregarSprite("reloj", 2.78, 1.25, 0.24, 0.075);
        peluche = agregarSprite("peluche", 1.5, 2.45, 0.22, 0.2);
        agregarSprite("silla", 5.65, 2.15, 0, 0.42).conColision(0.18);
        agregarSprite("ropa", 4.3, 5.45, 0, 0.12);
        telefonoMesa = agregarSprite("telefono_mesa", 5.7, 1.4, 0.30, 0.06);
        agregarSprite("planta", 12.4, 13.45, 0, 0.5).conColision(0.2);
        agregarSprite("planta", 18.4, 13.45, 0, 0.45).conColision(0.2);
        agregarSprite("lampara_pie", 1.45, 13.4, 0, 0.82).conColision(0.15);
        router = agregarSprite("router", 1.5, 16.5, 0.28, 0.1);
        agregarSprite("inodoro", 13.5, 3.55, 0, 0.32).conColision(0.25);
        Sprite s = new Sprite(texturas.obtener("seguidor"), 0, 0, 0, 0.94);
        sprites.agregar(s);
        seguidor = new ElSeguidor(s, this, audio);
        jugadorFoto = new Sprite(texturas.obtener("jugador"), 0, 0, 0, 0.7);
        jugadorFoto.soloFoto = true;
        sprites.agregar(jugadorFoto);
        telefonoMesa.visible = false;

        luz.agregar(new LuzPuntual(4.0, 1.25, 0.46f, 0.56f, 0.86f, 7.5, LuzPuntual.CANAL_LUNA));
        luz.agregar(new LuzPuntual(8.5, 1.2, 0.40f, 0.50f, 0.80f, 7.0, LuzPuntual.CANAL_LUNA));
        luz.agregar(new LuzPuntual(1.3, 14.5, 0.38f, 0.46f, 0.74f, 8.0, LuzPuntual.CANAL_LUNA));
        luz.agregar(new LuzPuntual(16.5, 15.9, 0.34f, 0.42f, 0.70f, 7.0, LuzPuntual.CANAL_LUNA));
        luz.agregar(new LuzPuntual(2.5, 1.6, 1.10f, 0.80f, 0.46f, 7.0, LuzPuntual.CANAL_LAMPARA));
        luz.agregar(new LuzPuntual(8.5, 6.5, 0.62f, 0.36f, 0.14f, 4.5, LuzPuntual.CANAL_PASILLO));
        luz.agregar(new LuzPuntual(4.5, 13.3, 0.45f, 0.55f, 0.70f, 6.0, LuzPuntual.CANAL_TV));
        luz.agregar(new LuzPuntual(1.6, 16.2, 0.0f, 0.28f, 0.10f, 2.2, LuzPuntual.CANAL_ROUTER));
        luz.agregar(new LuzPuntual(18.3, 14.5, 0.55f, 0.58f, 0.62f, 3.5, LuzPuntual.CANAL_NEVERA));
        luz.hornear();
    }

    private Sprite agregarSprite(String clave, double x, double y, double z, double alto) {
        Sprite s = new Sprite(texturas.obtener(clave), x, y, z, alto);
        sprites.agregar(s);
        return s;
    }

    public void configurarNoche(int noche) {
        seguidor.apagar();
        tormenta = noche == 3;
        apagon = noche == 3;
        routerEncendido = true;
        lamparaEncendida = false;
        encenderTV(false);
        parpadeo = 0;
        relampago = 0;
        proximoRelampago = 12 + azar.nextDouble() * 10;
        espejoFigura = false;
        casa.cambiarEspecial('V', tormenta ? "ventana_lluvia" : "ventana");
        casa.cambiarEspecial('E', "espejo");
        for (Puerta p : casa.puertas()) {
            p.fijar(0);
        }
        Puerta cuarto = casa.puerta(7, 4);
        if (noche == 2) {
            cuarto.fijar(0.7);
        }
        Puerta bano = casa.puerta(9, 3);
        if (noche >= 2) {
            bano.fijar(0.35);
        }
        Puerta padres = casa.puerta(9, 8);
        padres.bloquear(true);
        casa.reconstruirNavegacion();
        audio.lluvia(tormenta);
        nivelPerturbacion = 0;
        actualizarTexturasPerturbadas(0, true);
        aplicarLuces();
        luz.hornear();
    }

    public void actualizar(double dt, float tension, double jx, double jy, double jang) {
        boolean rehornear = false;
        for (Puerta p : casa.puertas()) {
            boolean antes = p.apertura() >= 0.3;
            p.actualizar(dt);
            boolean despues = p.apertura() >= 0.3;
            if (antes != despues || p.enMovimiento()) {
                rehornear = true;
            }
        }
        if (rehornear) {
            luz.hornear();
        }
        if (parpadeo > 0) {
            parpadeo -= dt;
            pausaParpadeo -= dt;
            if (pausaParpadeo <= 0) {
                luzParpadeoEncendida = !luzParpadeoEncendida;
                pausaParpadeo = luzParpadeoEncendida ? 0.03 + azar.nextDouble() * 0.12 : 0.04 + azar.nextDouble() * 0.25;
            }
            if (parpadeo <= 0) {
                luzParpadeoEncendida = true;
            }
        }
        if (tormenta) {
            proximoRelampago -= dt;
            if (proximoRelampago <= 0) {
                relampago();
                proximoRelampago = 16 + azar.nextDouble() * 22;
            }
        }
        if (relampago > 0) {
            relampago -= dt * (reducirDestellos ? 1.5 : 4.0);
            if (relampago < 0) {
                relampago = 0;
            }
        }
        if (truenoPendiente > 0) {
            truenoPendiente -= dt;
            if (truenoPendiente <= 0) {
                audio.reproducir("trueno", 0.9f, (float) (azar.nextDouble() - 0.5), "[Trueno]");
            }
        }
        tRouter += dt;
        if (tRouter > 0.14) {
            tRouter = 0;
            faseRouter = azar.nextInt(8);
            router.cambiarTextura(routerEncendido ? fasesRouter[faseRouter] : routerApagado);
        }
        if (tvEncendida) {
            GeneradorTexturas.pintarEstatica(casa.especial('T'), azar, true, 0.7f + azar.nextFloat() * 0.3f);
        }
        if (telefonoBrillando) {
            brilloTelefono = Math.min(1, brilloTelefono + dt * 4);
        } else {
            brilloTelefono = Math.max(0, brilloTelefono - dt * 1.5);
        }
        telefonoMesa.emisionActiva = brilloTelefono > 0.05;
        nivelPerturbacion += (tension - nivelPerturbacion) * (float) Math.min(1, dt * 0.5);
        actualizarTexturasPerturbadas(jang, false, jx, jy);
        boolean seguidorEnOscuridad = parpadeo > 0 && !luzParpadeoEncendida;
        seguidor.sprite().emisionActiva = !seguidorEnOscuridad;
        aplicarLuces();
        audio.actualizarOyente(jx, jy, jang);
    }

    private void actualizarTexturasPerturbadas(double jang, boolean forzar) {
        actualizarTexturasPerturbadas(jang, forzar, -100, -100);
    }

    private void actualizarTexturasPerturbadas(double jang, boolean forzar, double jx, double jy) {
        float t = nivelPerturbacion;
        cambiarSiNoMira('H', t > 0.55f ? "poster_ojo" : "poster", 0.5, 3.5, jx, jy, jang, forzar);
        cambiarSiNoMira('R', t > 0.5f ? "cuadro_perturbado" : "cuadro", 7.5, 9.5, jx, jy, jang, forzar);
        cambiarSiNoMira('A', t > 0.7f ? "armario_abierto" : "armario", 0.5, 4.8, jx, jy, jang, forzar);
        cambiarSiNoMira('E', espejoFigura ? "espejo_figura" : "espejo", 13.5, 0.5, jx, jy, jang, forzar);
        String clavePeluche = t > 0.65f ? "peluche_mira" : "peluche";
        Textura tp = texturas.obtener(clavePeluche);
        if (peluche.textura != tp && (forzar || !mirando(peluche.x, peluche.y, jx, jy, jang))) {
            peluche.cambiarTextura(tp);
        }
    }

    private void cambiarSiNoMira(char tipo, String clave, double ox, double oy, double jx, double jy, double jang, boolean forzar) {
        Textura nueva = texturas.obtener(clave);
        if (casa.especial(tipo) == nueva) {
            return;
        }
        if (forzar || !mirando(ox, oy, jx, jy, jang)) {
            casa.cambiarEspecial(tipo, clave);
        }
    }

    private boolean mirando(double ox, double oy, double jx, double jy, double jang) {
        double dx = ox - jx;
        double dy = oy - jy;
        double d = Math.sqrt(dx * dx + dy * dy);
        if (d < 0.001) {
            return true;
        }
        double dot = (dx * Math.cos(jang) + dy * Math.sin(jang)) / d;
        return dot > 0.45 && d < 14;
    }

    private void aplicarLuces() {
        float flicker = (parpadeo > 0 && !luzParpadeoEncendida) ? 0.0f : 1.0f;
        float electrico = apagon ? 0f : flicker;
        luz.factor(LuzPuntual.CANAL_LAMPARA, lamparaEncendida ? electrico : 0f);
        luz.factor(LuzPuntual.CANAL_PASILLO, electrico * (apagon ? 0 : 1));
        luz.factor(LuzPuntual.CANAL_TV, tvEncendida ? (0.55f + azar.nextFloat() * 0.45f) * flicker : 0f);
        luz.factor(LuzPuntual.CANAL_ROUTER, routerEncendido ? 1f : 0f);
        luz.factor(LuzPuntual.CANAL_NEVERA, 0f);
        float flash = (float) relampago * (reducirDestellos ? 3.5f : 12f);
        luz.factor(LuzPuntual.CANAL_LUNA, (tormenta ? 0.55f : 1f) + flash);
        lamparaMesa.emisionActiva = lamparaEncendida && electrico > 0;
        luz.combinar();
    }

    public BufferedImage renderizar(Camara camara) {
        render.renderizar(camara, casa, luz, sprites, false);
        efectos.aplicar(render.pixeles());
        return render.imagen();
    }

    public BufferedImage tomarFoto(Camara camara, double jugadorX, double jugadorY, boolean incluirJugador, boolean incluirSeguidor) {
        jugadorFoto.x = jugadorX;
        jugadorFoto.y = jugadorY;
        jugadorFoto.visible = incluirJugador;
        boolean visibleAntes = seguidor.sprite().visible;
        if (!incluirSeguidor) {
            seguidor.sprite().visible = false;
        }
        float linterna = luz.linterna;
        float pantalla = luz.brilloPantalla;
        luz.linterna = 0;
        luz.brilloPantalla = 3.2f;
        luz.combinar();
        renderFoto.renderizar(camara, casa, luz, sprites, true);
        efectosFoto.aplicar(renderFoto.pixeles());
        luz.linterna = linterna;
        luz.brilloPantalla = pantalla;
        seguidor.sprite().visible = visibleAntes;
        jugadorFoto.visible = false;
        BufferedImage copia = new BufferedImage(renderFoto.ancho(), renderFoto.alto(), BufferedImage.TYPE_INT_RGB);
        copia.getGraphics().drawImage(renderFoto.imagen(), 0, 0, null);
        return copia;
    }

    public double[] moverConColision(double x, double y, double dx, double dy, double radio) {
        double nx = x + dx;
        if (!colisiona(nx, y, radio)) {
            x = nx;
        }
        double ny = y + dy;
        if (!colisiona(x, ny, radio)) {
            y = ny;
        }
        return new double[]{x, y};
    }

    public boolean colisiona(double x, double y, double radio) {
        int x0 = (int) Math.floor(x - radio);
        int x1 = (int) Math.floor(x + radio);
        int y0 = (int) Math.floor(y - radio);
        int y1 = (int) Math.floor(y + radio);
        for (int cy = y0; cy <= y1; cy++) {
            for (int cx = x0; cx <= x1; cx++) {
                if (casa.solido(cx, cy)) {
                    return true;
                }
            }
        }
        for (Sprite s : sprites) {
            if (s.radioColision > 0) {
                double ddx = s.x - x;
                double ddy = s.y - y;
                double r = s.radioColision + radio;
                if (ddx * ddx + ddy * ddy < r * r) {
                    return true;
                }
            }
        }
        return false;
    }

    public Interactuable interactuableEnfrente(double x, double y, double ang) {
        Interactuable mejor = null;
        double mejorPuntaje = Double.MAX_VALUE;
        double dirX = Math.cos(ang);
        double dirY = Math.sin(ang);
        for (Interactuable i : casa.interactuables()) {
            if (!i.activo()) {
                continue;
            }
            double dx = i.x() - x;
            double dy = i.y() - y;
            double d = Math.sqrt(dx * dx + dy * dy);
            if (d > 1.45 || d < 0.01) {
                continue;
            }
            double dot = (dx * dirX + dy * dirY) / d;
            if (dot < 0.72) {
                continue;
            }
            double puntaje = d * (2 - dot);
            if (puntaje < mejorPuntaje) {
                mejorPuntaje = puntaje;
                mejor = i;
            }
        }
        return mejor;
    }

    public void alternarPuerta(Puerta p) {
        if (p.bloqueada()) {
            audio.reproducirEn("golpe", p.centroX(), p.centroY(), 0.35f, null);
            return;
        }
        if (p.estaAbierta()) {
            p.cerrar(1.6);
            audio.reproducirEn("puerta_cierra", p.centroX(), p.centroY(), 0.8f, null);
        } else {
            p.abrir(1.2);
            audio.reproducirEn("puerta_abre", p.centroX(), p.centroY(), 0.8f, null);
        }
    }

    public void abrirPuertaLento(Puerta p) {
        if (p.estaAbierta() || p.bloqueada()) {
            return;
        }
        p.abrir(0.35);
        audio.reproducirEn("crujido", p.centroX(), p.centroY(), 1.0f, "[Una puerta cruje al abrirse]");
    }

    public void portazo(Puerta p) {
        p.cerrar(6);
        audio.reproducirEn("portazo", p.centroX(), p.centroY(), 1.0f, "[¡Portazo!]");
    }

    public void parpadeo(double segundos) {
        parpadeo = Math.max(parpadeo, segundos);
        pausaParpadeo = 0;
        audio.reproducir("zumbido", 0.6f, 0f, "[Zumbido eléctrico, las luces parpadean]");
    }

    public boolean enParpadeo() {
        return parpadeo > 0;
    }

    public void relampago() {
        relampago = 1.0;
        truenoPendiente = 0.4 + azar.nextDouble() * 1.4;
    }

    public double intensidadRelampago() {
        return relampago;
    }

    public void encenderTV(boolean encender) {
        tvEncendida = encender;
        GeneradorTexturas.pintarEstatica(casa.especial('T'), azar, encender, 1f);
        if (encender && idEstatica < 0) {
            idEstatica = audio.reproducirBucleEn("estatica", 4.5, 12.9, 0.9f);
        } else if (!encender && idEstatica >= 0) {
            audio.detenerVoz(idEstatica);
            idEstatica = -1;
        }
    }

    public boolean tvEncendida() {
        return tvEncendida;
    }

    public void lampara(boolean encender) {
        lamparaEncendida = encender;
        audio.reproducirEn("interruptor", 2.5, 1.5, 0.8f, null);
    }

    public boolean lamparaEncendida() {
        return lamparaEncendida;
    }

    public boolean apagon() {
        return apagon;
    }

    public void apagarRouter() {
        routerEncendido = false;
        router.cambiarTextura(routerApagado);
        audio.reproducirEn("router_off", 1.5, 16.5, 0.9f, "[El router se apaga]");
    }

    public boolean routerEncendido() {
        return routerEncendido;
    }

    public void actualizarReloj(int minutos, boolean corrupto) {
        if (minutos == minutoReloj && corrupto == relojCorrupto) {
            return;
        }
        minutoReloj = minutos;
        relojCorrupto = corrupto;
        int m = Math.floorMod(minutos, 1440);
        String texto = corrupto ? "03:12" : String.format("%02d:%02d", m / 60, m % 60);
        reloj.cambiarTextura(generador.reloj(texto, corrupto));
    }

    public void telefonoEnEscritorio(boolean visible) {
        telefonoMesa.visible = visible;
        if (!visible) {
            telefonoBrillando = false;
            brilloTelefono = 0;
        }
    }

    public void brilloTelefonoEscritorio(boolean brillando) {
        telefonoBrillando = brillando;
    }

    public void mostrarFiguraEspejo(boolean valor) {
        espejoFigura = valor;
    }

    public void reducirDestellos(boolean valor) {
        reducirDestellos = valor;
    }

    public double[] puntoOculto(double jx, double jy, double jang, int minPasos, int maxPasos) {
        int origen = casa.indice((int) Math.floor(jx), (int) Math.floor(jy));
        int[] distancias = casa.navegacion().distancias(origen);
        ListaEnlazada<Integer> candidatos = new ListaEnlazada<>();
        for (int i = 0; i < distancias.length; i++) {
            int d = distancias[i];
            if (d < minPasos || d > maxPasos) {
                continue;
            }
            int cx = i % casa.ancho();
            int cy = i / casa.ancho();
            if (casa.esPuerta(cx, cy)) {
                continue;
            }
            if (casa.lineaDeVision(jx, jy, cx + 0.5, cy + 0.5) && mirando(cx + 0.5, cy + 0.5, jx, jy, jang)) {
                continue;
            }
            candidatos.agregar(i);
        }
        if (candidatos.estaVacia()) {
            return null;
        }
        int elegido = candidatos.obtener(azar.nextInt(candidatos.tamano()));
        return new double[]{elegido % casa.ancho() + 0.5, elegido / casa.ancho() + 0.5};
    }

    public double[] puntoDetras(double jx, double jy, double jang, double distancia) {
        for (double d = distancia; d >= 1.4; d -= 0.3) {
            double x = jx - Math.cos(jang) * d;
            double y = jy - Math.sin(jang) * d;
            if (!colisiona(x, y, 0.2) && casa.lineaDeVision(jx, jy, x, y)) {
                return new double[]{x, y};
            }
        }
        return null;
    }

    public Casa casa() {
        return casa;
    }

    public Iluminacion iluminacion() {
        return luz;
    }

    public EfectosPantalla efectos() {
        return efectos;
    }

    public ElSeguidor seguidor() {
        return seguidor;
    }

    public ListaEnlazada<Sprite> sprites() {
        return sprites;
    }

    public Renderizador3D renderizador() {
        return render;
    }
}
