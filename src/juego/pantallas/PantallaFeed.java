package juego.pantallas;

import estructuras.Cola;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import juego.ConfiguracionAccesibilidad;
import juego.Decision;
import juego.EventoProgramado;
import juego.IndicadorBienestar;
import juego.Juego;
import juego.Jugador;
import juego.Mision;
import juego.Telefono;
import juego.TipoDecision;
import juego.TipoFinal;
import juego.audio.GestorAudio;
import juego.feed.ElementoFeed;
import juego.feed.EventoTerror;
import juego.feed.Feed;
import juego.feed.Notificacion;
import juego.interfaz.Dibujo;
import juego.interfaz.InterfazTelefono;
import juego.interfaz.Susto;
import juego.motor.Camara;
import juego.motor.EfectosPantalla;
import juego.motor.Entrada;
import juego.mundo.ElSeguidor;
import juego.mundo.Habitacion;
import juego.mundo.Interactuable;
import juego.mundo.Mundo3D;
import juego.mundo.Puerta;

public final class PantallaFeed extends Pantalla implements InterfazTelefono.Oyente {

    private static final double SEGUNDOS_POR_MINUTO = 1.5;
    private static final double RADIO_JUGADOR = 0.22;

    private int tension;
    private double tensionSuave;
    private double pulsoTension;

    private final Mundo3D mundo;
    private final Camara camara = new Camara();
    private final Jugador jugador;
    private final Telefono telefono;
    private final Feed feed;
    private final GestorAudio audio;
    private final ConfiguracionAccesibilidad configuracion;
    private final InterfazTelefono telefonoUI;
    private final DirectorTerror director;
    private final Hud hud;
    private final Cola<String> orientaciones = new Cola<>();

    private String orientacionActual;
    private double tiempoOrientacion;
    private String pista;
    private double tiempoPista;
    private double minutoFraccion;
    private double desgasteSueno;
    private double faseCaminar;
    private double ultimoPaso;
    private double proximaNotificacion = 18;
    private double tiempoBrilloEscritorio;
    private Interactuable enfrente;
    private double sacudida;
    private double pulsoGlitch;
    private boolean primeraApertura = true;
    private boolean noMolestarPremiado;

    private double susto = -1;
    private boolean sustoSuave;
    private Runnable despuesSusto;
    private String textoSustoSuave = "[El Seguidor te alcanzó]";
    private double fundido;
    private double fundidoObjetivo;

    private boolean ultimaNotificacion;
    private double tiempoUltima;
    private boolean secuenciaMala;
    private double tiempoMala;
    private int pasoMala;
    private boolean fotoMalaHecha;
    private boolean susurroFinalHecho;
    private boolean secuenciaBuena;
    private double tiempoBuena;

    private boolean aparicionPasilloHecha;
    private boolean cazaSala;
    private boolean finalPreparado;
    private double tiempoParaFinal = -1;

    public PantallaFeed(Juego juego) {
        super("PantallaFeed", juego);
        this.mundo = juego.mundo();
        this.jugador = juego.jugador();
        this.telefono = jugador.telefono();
        this.feed = juego.feed();
        this.audio = juego.gestorAudio();
        this.configuracion = juego.configuracion();
        this.telefonoUI = new InterfazTelefono(feed, telefono, this);
        this.director = new DirectorTerror(this, mundo, audio, feed, jugador, juego.azar());
        this.hud = new Hud(this);
    }

    @Override
    public void entrar() {
        audio.ambiente(1f);
        audio.latidos(true);
        mundo.seguidor().apagar();
        mundo.telefonoEnEscritorio(!telefono.enMano());
        tensionSuave = jugador.bienestar().nivelRiesgo() * 0.6;
        tension = (int) tensionSuave;
        pistaContextual(juego.nocheActual() == 1 ? "Pulsa TAB (o clic derecho) para mirar tu teléfono" : "TAB: teléfono · E: interactuar · F: linterna", 7);
        actualizarCamara(0);
        fundido = 1;
        fundidoObjetivo = 0;
    }

    @Override
    public void salir() {
        audio.zumbidoOido(0f);
        audio.subirTension(0f);
    }

    @Override
    public void alReanudar() {
        juego.entrada().consumirDX();
        juego.entrada().consumirDY();
    }

    @Override
    public boolean capturaRaton() {
        return !telefonoUI.abierto() && susto < 0;
    }

    @Override
    public void actualizar(double dt) {
        tiempo += dt;
        if (susto >= 0) {
            actualizarSusto(dt);
            return;
        }
        if (ultimaNotificacion) {
            actualizarUltimaNotificacion(dt);
        }
        if (secuenciaMala) {
            actualizarSecuenciaMala(dt);
        }
        if (secuenciaBuena) {
            actualizarSecuenciaBuena(dt);
        }
        avanzarTiempo(dt);
        actualizarBateria(dt);
        Entrada entrada = juego.entrada();
        if (!telefonoUI.abierto() && !ultimaNotificacion && !secuenciaBuena) {
            mover(dt, entrada);
            enfrente = mundo.interactuableEnfrente(jugador.x(), jugador.y(), jugador.angulo());
        } else {
            entrada.consumirDX();
            entrada.consumirDY();
            enfrente = null;
            telefonoUI.raton(entrada.ratonX(), entrada.ratonY());
        }
        if (!enSecuencia()) {
            actualizarNotificaciones(dt);
            procesarProgramados();
        }
        director.actualizar(dt, tension, juego.nocheActual(), !enSecuencia());
        ElSeguidor.Resultado r = mundo.seguidor().actualizar(dt, jugador.x(), jugador.y(), jugador.angulo(),
                telefonoUI.abierto(), telefono.linterna(), tension / 100f);
        if (r == ElSeguidor.Resultado.ATRAPO && !enSecuencia()) {
            atrapado();
        }
        actualizarTension(dt);
        evaluarMisiones();
        disparadores(dt);
        if (jugador.horaActual() >= Jugador.MINUTO_LIMITE && !enSecuencia()) {
            iniciarUltimaNotificacion();
        }
        actualizarLuces();
        mundo.actualizarReloj(jugador.horaActual(), tension > 85 && juego.azar().nextDouble() < 0.3);
        mundo.actualizar(dt, tension / 100f, jugador.x(), jugador.y(), jugador.angulo());
        telefonoUI.actualizar(dt, tension, jugador.horaTexto(), jugador.nombre());
        actualizarEfectos(dt);
        actualizarMensajes(dt);
        if (tiempoBrilloEscritorio > 0) {
            tiempoBrilloEscritorio -= dt;
            if (tiempoBrilloEscritorio <= 0) {
                mundo.brilloTelefonoEscritorio(false);
            }
        }
        fundido += (fundidoObjetivo - fundido) * Math.min(1, dt * 2.5);
        actualizarCamara(dt);
    }

    private void avanzarTiempo(double dt) {
        double factor = telefonoUI.abierto() ? 1.4 : 1.0;
        minutoFraccion += dt / SEGUNDOS_POR_MINUTO * factor;
        if (telefonoUI.abierto()) {
            jugador.sumarMinutosPantalla(dt / SEGUNDOS_POR_MINUTO * factor);
        }
        while (minutoFraccion >= 1) {
            minutoFraccion -= 1;
            jugador.avanzarMinutos(1);
            alPasarMinuto();
        }
    }

    private void alPasarMinuto() {
        IndicadorBienestar b = jugador.bienestar();
        double tarde = jugador.horaActual() >= 1440 ? 0.35 : 0.15;
        desgasteSueno += tarde + (telefonoUI.abierto() ? 0.2 : 0);
        while (desgasteSueno >= 1) {
            desgasteSueno -= 1;
            b.modificar(IndicadorBienestar.SUENO, -1);
        }
        if (telefonoUI.abierto() && juego.azar().nextDouble() < 0.25) {
            b.modificar(IndicadorBienestar.CONCENTRACION, -1);
        }
    }

    private void actualizarBateria(double dt) {
        if (telefono.enMano()) {
            if (telefonoUI.abierto()) {
                telefono.consumir(dt * 0.06);
            }
            if (telefono.linterna()) {
                telefono.consumir(dt * 0.1);
            }
        } else if (!mundo.apagon()) {
            telefono.cargar(dt * 0.5);
        }
        if (telefono.bateria() <= 0 && telefonoUI.abierto()) {
            telefonoUI.cerrar();
            orientar("Tu teléfono se quedó sin batería.");
        }
    }

    private void mover(double dt, Entrada entrada) {
        double sens = configuracion.sensibilidad();
        double dx = entrada.consumirDX();
        double dy = entrada.consumirDY();
        jugador.angulo(jugador.angulo() + dx * 0.0026 * sens);
        jugador.inclinacion(jugador.inclinacion() - dy * 0.0011 * sens);
        if (entrada.presionada(KeyEvent.VK_LEFT)) {
            jugador.angulo(jugador.angulo() - dt * 2.3);
        }
        if (entrada.presionada(KeyEvent.VK_RIGHT)) {
            jugador.angulo(jugador.angulo() + dt * 2.3);
        }
        if (entrada.presionada(KeyEvent.VK_PAGE_UP)) {
            jugador.inclinacion(jugador.inclinacion() + dt * 0.6);
        }
        if (entrada.presionada(KeyEvent.VK_PAGE_DOWN)) {
            jugador.inclinacion(jugador.inclinacion() - dt * 0.6);
        }
        double adelante = 0;
        double lado = 0;
        if (entrada.presionada(KeyEvent.VK_W) || entrada.presionada(KeyEvent.VK_UP)) {
            adelante += 1;
        }
        if (entrada.presionada(KeyEvent.VK_S) || entrada.presionada(KeyEvent.VK_DOWN)) {
            adelante -= 1;
        }
        if (entrada.presionada(KeyEvent.VK_D)) {
            lado += 1;
        }
        if (entrada.presionada(KeyEvent.VK_A)) {
            lado -= 1;
        }
        double magnitud = Math.sqrt(adelante * adelante + lado * lado);
        if (magnitud > 0) {
            boolean corre = entrada.presionada(KeyEvent.VK_SHIFT);
            double velocidad = (corre ? 2.7 : 1.6) / magnitud;
            double cos = Math.cos(jugador.angulo());
            double sin = Math.sin(jugador.angulo());
            double mx = (cos * adelante - sin * lado) * velocidad * dt;
            double my = (sin * adelante + cos * lado) * velocidad * dt;
            double[] nueva = mundo.moverConColision(jugador.x(), jugador.y(), mx, my, RADIO_JUGADOR);
            double recorrido = Math.hypot(nueva[0] - jugador.x(), nueva[1] - jugador.y());
            jugador.x(nueva[0]);
            jugador.y(nueva[1]);
            faseCaminar += recorrido * 5.2;
            if (faseCaminar - ultimoPaso > Math.PI) {
                ultimoPaso = faseCaminar;
                audio.reproducir("paso_" + (1 + juego.azar().nextInt(2)), corre ? 0.32f : 0.2f, 0f, null);
            }
        }
    }

    private void actualizarCamara(double dt) {
        camara.x = jugador.x();
        camara.y = jugador.y();
        camara.angulo = jugador.angulo();
        double bob = Math.sin(faseCaminar) * 0.012;
        double respiracion = Math.sin(tiempo * 1.7) * 0.003 * (1 + tension / 50.0);
        camara.ojo = 0.6 + bob + respiracion;
        double inclinacionTel = telefonoUI.abierto() ? -0.05 : 0;
        camara.inclinacion = jugador.inclinacion() + inclinacionTel;
        if (sacudida > 0) {
            camara.angulo += (juego.azar().nextDouble() - 0.5) * 0.04 * sacudida;
            camara.inclinacion += (juego.azar().nextDouble() - 0.5) * 0.05 * sacudida;
            sacudida = Math.max(0, sacudida - dt * 1.5);
        }
    }

    private void actualizarNotificaciones(double dt) {
        if (telefono.apagado()) {
            return;
        }
        proximaNotificacion -= dt;
        if (proximaNotificacion <= 0) {
            proximaNotificacion = 17 + juego.azar().nextDouble() * 15 - tension * 0.07;
            feed.fabricaNotificacion().configurar(juego.nocheActual(), tension, jugador.nombre());
            recibirNotificacion((Notificacion) feed.fabricaNotificacion().crear());
        }
        if (telefonoUI.avisoActual() == null && feed.hayNotificaciones()) {
            mostrarNotificacion(feed.siguienteNotificacion());
        }
    }

    private void recibirNotificacion(Notificacion n) {
        if (telefono.noMolestar() && n.silenciable()) {
            telefono.sumarSinLeer(1);
            return;
        }
        feed.encolarNotificacion(n);
    }

    public void forzarNotificacion(Notificacion n) {
        feed.encolarNotificacion(n);
    }

    private void mostrarNotificacion(Notificacion n) {
        if (n == null || telefono.apagado()) {
            return;
        }
        telefonoUI.mostrarAviso(n);
        boolean rara = n.dispara() != null;
        if (!telefonoUI.abierto()) {
            telefono.sumarSinLeer(1);
        }
        String sub = "[Notificación: " + n.titulo() + "]";
        if (telefono.enMano()) {
            audio.reproducir("vibracion", 0.5f, 0f, null);
            audio.reproducir(rara ? "notificacion_rara" : "notificacion", rara ? 0.75f : 0.5f, 0f, sub);
        } else {
            vibrarTelefono("[Tu teléfono vibra en el escritorio: " + n.titulo() + "]");
        }
        if (n.esMensaje()) {
            feed.insertarEnFoco(n);
        }
        if (rara) {
            final EventoTerror evento = n.dispara();
            director.diferir(1.2, () -> director.ejecutar(evento));
        }
        pulsoTension += n.urgencia();
    }

    public void vibrarTelefono(String subtitulo) {
        if (telefono.enMano()) {
            audio.reproducir("vibracion", 0.6f, 0f, subtitulo);
        } else {
            audio.reproducirEn("vibracion", 5.7, 1.4, 1.1f, subtitulo);
            mundo.brilloTelefonoEscritorio(true);
            tiempoBrilloEscritorio = 3.5;
        }
    }

    private void procesarProgramados() {
        EventoProgramado ev;
        while ((ev = juego.noche().siguienteListo(jugador.horaActual())) != null) {
            director.programado(ev.clave());
        }
    }

    private void actualizarTension(double dt) {
        IndicadorBienestar b = jugador.bienestar();
        double hora = Math.max(0, (jugador.horaActual() - 1440) / 192.0) * 18;
        double objetivo = b.nivelRiesgo() * 0.72 + feed.vistosSeguidos() * 2.4 + hora
                + (telefonoUI.abierto() ? 6 : 0) + (juego.nocheActual() - 1) * 7 + pulsoTension;
        if (mundo.seguidor().estado() == ElSeguidor.Estado.CAZA) {
            objetivo += 12;
        }
        tensionSuave += (objetivo - tensionSuave) * Math.min(1, dt * 0.45);
        tension = (int) Math.max(0, Math.min(100, Math.round(tensionSuave)));
        pulsoTension = Math.max(0, pulsoTension - dt * 0.4);
        if (((int) (tiempo * 2)) % 2 == 0) {
            feed.actualizarTension(tension);
        }
        audio.subirTension(tension / 100f);
        audio.zumbidoOido(tension > 85 ? (tension - 85) / 15f : 0f);
    }

    private void evaluarMisiones() {
        for (Mision m : juego.misiones()) {
            if (m.evaluar(juego)) {
                orientarYa("Objetivo cumplido: " + m.objetivo());
                audio.reproducir("decision_buena", 0.5f, 0f, null);
            }
        }
    }

    private void disparadores(double dt) {
        int noche = juego.nocheActual();
        Habitacion aqui = mundo.casa().habitacionEn(jugador.x(), jugador.y());
        Puerta puertaCuarto = mundo.casa().puerta(7, 4);
        if (noche == 2 && !aparicionPasilloHecha && jugador.x() > 7.55 && !enSecuencia()) {
            aparicionPasilloHecha = true;
            double sin = Math.sin(jugador.angulo());
            if (sin < -0.4) {
                director.aparicionEn(8.5, 1.6, 6);
            } else {
                director.aparicionEn(8.5, 11.3, 6);
            }
            mundo.parpadeo(1.5);
        }
        if (noche == 2 && !juego.bandera("puerta_cerrada") && puertaCuarto.apertura() == 0 && !puertaCuarto.enMovimiento()
                && aqui == Habitacion.CUARTO) {
            juego.marcar("puerta_cerrada");
        }
        if (noche == 3 && !cazaSala && (aqui == Habitacion.SALA || aqui == Habitacion.COCINA)) {
            cazaSala = true;
            if (mundo.seguidor().activo()) {
                mundo.seguidor().apagar();
            }
            director.iniciarCazaEn(16.5, 15.5, 3);
            audio.agregarSubtitulo("[Algo se mueve en la cocina]", 3);
        }
        if (noche == 3 && juego.bandera("router_apagado") && !juego.bandera("volver_cuarto") && aqui == Habitacion.CUARTO) {
            juego.marcar("volver_cuarto");
            if (mundo.seguidor().activo()) {
                mundo.seguidor().desaparecer(true);
            }
            mundo.seguidor().enfriar(999);
            tiempoParaFinal = 2.5;
        }
        if (tiempoParaFinal > 0) {
            tiempoParaFinal -= dt;
            if (tiempoParaFinal <= 0) {
                prepararFinal();
            }
        }
    }

    private void prepararFinal() {
        if (finalPreparado) {
            return;
        }
        finalPreparado = true;
        Notificacion apagar = feed.fabricaNotificacion().crearEspecial("apagar");
        feed.insertarEnFoco(apagar);
        audio.reproducir("eco", 0.7f, 0f, "[El teléfono se ilumina solo]");
        if (telefono.enMano() && !telefono.apagado()) {
            telefonoUI.abrir();
            telefonoUI.banner("eco quiere hablar contigo", 4);
        } else {
            vibrarTelefono("[Tu teléfono se encendió solo en el escritorio]");
            orientar("Tu teléfono se encendió solo. Tómalo del escritorio (E) y ábrelo (TAB).");
        }
    }

    private void actualizarLuces() {
        float linterna = 0f;
        if (telefono.linterna()) {
            linterna = 1f;
            if (telefono.bateria() < 12 || tension > 88) {
                linterna = juego.azar().nextDouble() < 0.12 ? 0.15f : 0.95f;
            }
        }
        mundo.iluminacion().linterna = linterna;
        mundo.iluminacion().brilloPantalla = (float) (telefonoUI.subida() * 0.5);
    }

    private void actualizarEfectos(double dt) {
        EfectosPantalla ef = mundo.efectos();
        double t = tension / 100.0;
        if (pulsoGlitch > 0) {
            pulsoGlitch = Math.max(0, pulsoGlitch - dt * 1.2);
        }
        boolean suave = configuracion.sinJumpscares();
        ef.grano = (float) (0.035 + t * 0.09);
        ef.fuerzaVineta = (float) (0.5 + t * 0.35);
        ef.aberracion = (float) (Math.max(0, t - 0.55) * 1.6 + pulsoGlitch * (suave ? 0.5 : 1.5));
        ef.glitch = (float) (Math.max(0, t - 0.72) * 0.5 + pulsoGlitch * (suave ? 0.3 : 1));
        ef.desaturacion = (float) (t * 0.35);
        ef.tinteRojo = (float) (Math.max(0, t - 0.8) * 0.6);
        ef.oscurecer = (float) (fundido * 0.98);
    }

    private void actualizarMensajes(double dt) {
        if (tiempoOrientacion > 0) {
            tiempoOrientacion -= dt;
        }
        if (tiempoOrientacion <= 0 && !orientaciones.estaVacia()) {
            orientacionActual = orientaciones.desencolar();
            tiempoOrientacion = 5.5;
        }
        if (tiempoPista > 0) {
            tiempoPista -= dt;
        }
    }

    public void orientar(String texto) {
        if (texto == null || texto.isEmpty()) {
            return;
        }
        if (orientaciones.tamano() > 3) {
            orientaciones.desencolar();
        }
        orientaciones.encolar(texto);
    }

    public void orientarYa(String texto) {
        if (orientacionActual != null && tiempoOrientacion > 1) {
            orientaciones.encolar(orientacionActual);
        }
        orientacionActual = texto;
        tiempoOrientacion = 5.5;
    }

    public void pistaContextual(String texto, double segundos) {
        pista = texto;
        tiempoPista = segundos;
    }

    public void pulsoGlitch(double fuerza) {
        pulsoGlitch = Math.max(pulsoGlitch, fuerza);
        telefonoUI.glitch(fuerza);
    }

    public void sacudir(double fuerza) {
        if (!configuracion.sinJumpscares()) {
            sacudida = Math.max(sacudida, fuerza);
        }
    }

    public void llamadaEntrante(double segundos) {
        telefonoUI.llamada(segundos);
        pulsoTension += 6;
    }

    @Override
    public void alDecidir(ElementoFeed elemento, Decision decision) {
        if (elemento.resuelto()) {
            return;
        }
        elemento.resolver(decision);
        jugador.decidir(decision);
        if (decision.bandera() != null) {
            juego.marcar(decision.bandera());
        }
        pulsoTension += decision.tension();
        if (decision.tipo().saludable()) {
            audio.reproducir("decision_buena", 0.45f, 0f, null);
        } else if (decision.tipo().danina()) {
            audio.reproducir("decision_mala", 0.55f, 0f, null);
        } else {
            audio.reproducir("menu_elegir", 0.3f, 0f, null);
        }
        telefonoUI.mostrarEfectos(decision);
        orientar(decision.orientacion());
        if ("no_molestar".equals(decision.bandera())) {
            telefono.noMolestar(true);
            noMolestarPremiado = true;
        }
        switch (decision.tipo()) {
            case CERRAR_APP:
                feed.reiniciarRacha();
                director.diferir(0.8, this::alCerrar);
                break;
            case APAGAR_TELEFONO:
                iniciarSecuenciaBuena();
                break;
            case UN_MOMENTO_MAS:
                iniciarSecuenciaMala();
                break;
            default:
                break;
        }
    }

    @Override
    public void alDesplazar(int direccion) {
        if (secuenciaMala) {
            return;
        }
        int altoAnterior = 260;
        boolean nuevo = feed.hacerScroll(direccion);
        telefonoUI.animarDesplazamiento(direccion, altoAnterior);
        audio.reproducir("scroll", 0.6f, 0f, null);
        if (nuevo) {
            ElementoFeed e = feed.enfocado();
            jugador.registrarPublicacionVista();
            jugador.avanzarMinutos(2);
            IndicadorBienestar b = jugador.bienestar();
            b.modificar(IndicadorBienestar.SUENO, -1);
            b.modificar(IndicadorBienestar.CONCENTRACION, -1);
            int impacto = e.impactoAnsiedad();
            b.modificar(IndicadorBienestar.ANSIEDAD, Math.max(-2, impacto / 2 + 1));
            pulsoTension += 1;
            int racha = feed.vistosSeguidos();
            if (racha == 8 || racha == 16 || racha == 24) {
                ElementoFeed aviso = feed.fabricaPublicacion().crearEspecial("bienestar");
                if (aviso != null) {
                    feed.insertarEn(feed.nivelScroll() + 1, aviso);
                }
                orientar("Llevas " + racha + " publicaciones seguidas. El scroll infinito está diseñado para no terminar nunca.");
            }
        }
    }

    @Override
    public void alAlternarNoMolestar() {
        boolean nuevo = !telefono.noMolestar();
        telefono.noMolestar(nuevo);
        audio.reproducir("interruptor", 0.6f, 0f, null);
        if (nuevo) {
            juego.marcar("no_molestar");
            telefonoUI.banner("No molestar activado", 2.5);
            if (!noMolestarPremiado) {
                noMolestarPremiado = true;
                Decision d = new Decision(TipoDecision.SILENCIAR, "Activar No molestar")
                        .efecto(IndicadorBienestar.SUENO, 3).efecto(IndicadorBienestar.ANSIEDAD, -4);
                jugador.decidir(d);
                telefonoUI.mostrarEfectos(d);
            }
            orientar("Con \"No molestar\" las notificaciones no te interrumpen. (Algunas cosas no se pueden silenciar...)");
        } else {
            telefonoUI.banner("No molestar desactivado", 2.5);
        }
    }

    @Override
    public void alCerrar() {
        if (secuenciaMala || !telefonoUI.abierto()) {
            return;
        }
        telefonoUI.cerrar();
        feed.reiniciarRacha();
        director.alCerrarTelefono();
        audio.reproducir("interruptor", 0.25f, 0f, null);
    }

    private void abrirTelefono() {
        if (!telefono.enMano()) {
            pistaContextual("Tu teléfono está en el escritorio. Acércate y pulsa E para tomarlo.", 4);
            audio.reproducir("error", 0.4f, 0f, null);
            return;
        }
        if (telefono.apagado()) {
            pistaContextual("El teléfono está apagado.", 3);
            return;
        }
        if (telefono.bateria() <= 0) {
            pistaContextual("Batería agotada.", 3);
            return;
        }
        telefonoUI.abrir();
        telefono.leerTodo();
        if (primeraApertura) {
            primeraApertura = false;
            audio.reproducir("eco", 0.6f, 0f, null);
            pistaContextual("W/S o rueda: desplazar · 1, 2, 3 o clic: decidir · M: No molestar · TAB: cerrar", 7);
        }
    }

    private void alternarLinterna() {
        if (!telefono.enMano()) {
            pistaContextual("La linterna es la del teléfono, y está en el escritorio.", 3);
            return;
        }
        if (telefono.bateria() <= 0) {
            pistaContextual("Sin batería.", 2);
            return;
        }
        telefono.linterna(!telefono.linterna());
        audio.reproducir("interruptor", 0.5f, 0f, null);
    }

    private void interactuar() {
        if (enfrente == null) {
            return;
        }
        int noche = juego.nocheActual();
        switch (enfrente.tipo()) {
            case PUERTA:
                Puerta p = enfrente.puerta();
                if (p.bloqueada()) {
                    pistaContextual("El cuarto de mamá está cerrado con llave.", 3);
                }
                mundo.alternarPuerta(p);
                break;
            case CAMA:
                intentarDormir();
                break;
            case ESCRITORIO:
                usarEscritorio();
                break;
            case LAMPARA:
                if (mundo.apagon()) {
                    pistaContextual("No hay luz.", 2.5);
                    audio.reproducir("interruptor", 0.5f, 0f, null);
                } else {
                    mundo.lampara(!mundo.lamparaEncendida());
                }
                break;
            case ROUTER:
                if (noche == 3 && mundo.routerEncendido()) {
                    mundo.apagarRouter();
                    juego.marcar("router_apagado");
                    telefono.sinWifi(true);
                    telefonoUI.banner("Sin conexión", 3);
                    director.diferir(3.5, () -> forzarNotificacion(feed.fabricaNotificacion().crearEspecial("router_apagado")));
                    mundo.seguidor().velocidadExtra(1.15);
                    orientar("Router apagado. Ahora vuelve a tu cuarto.");
                } else if (!mundo.routerEncendido()) {
                    pistaContextual("El router está apagado. Sus luces siguen parpadeando débilmente.", 3);
                } else {
                    pistaContextual("El router de la casa. Sus luces verdes parpadean en la oscuridad.", 3);
                }
                break;
            case VENTANA:
                pistaContextual(noche == 3 ? "La lluvia golpea el vidrio. Afuera no se ve nada."
                        : (tension > 60 ? "La calle está vacía. Pero sientes que alguien te mira desde afuera." : "La calle está vacía. Demasiado vacía."), 3.5);
                break;
            case ESPEJO:
                if (tension > 55 && !configuracion.sinJumpscares()) {
                    director.ejecutar(new EventoTerror(EventoTerror.ESPEJO, 3));
                    pistaContextual("Por un segundo, el reflejo no se movió contigo.", 3.5);
                } else {
                    pistaContextual("Te ves cansado. Tienes ojeras.", 3);
                }
                break;
            case CUADRO:
                pistaContextual(tension > 50 ? "Las caras de la foto... no estaban así." : "Una foto de la familia en la playa. Hace mucho no van.", 3.5);
                break;
            case PUERTA_PRINCIPAL:
                pistaContextual("La puerta principal está cerrada con llave. Mamá llega a las 7:00.", 3.5);
                break;
            case NEVERA:
                pistaContextual("No tienes hambre. Solo estás despierto.", 3);
                break;
            case TV:
                if (mundo.tvEncendida()) {
                    mundo.encenderTV(false);
                    pistaContextual("Apagas el televisor. La estática se queda zumbando en tus oídos.", 3.5);
                } else {
                    pistaContextual("El televisor está apagado. Mejor así.", 3);
                }
                break;
            case ARMARIO:
                pistaContextual(tension > 70 ? "La puerta del armario estaba entreabierta. ¿La dejaste así?" : "Tu ropa. Nada más.", 3.5);
                break;
            case PELUCHE:
                pistaContextual(tension > 65 ? "¿Siempre estuvo mirando hacia acá?" : "Tu viejo oso de peluche.", 3);
                break;
            case LIBRERO:
                pistaContextual("Libros que dejaste de leer cuando empezaste a usar eco.", 3.5);
                break;
            default:
                break;
        }
    }

    private void usarEscritorio() {
        if (telefono.enMano()) {
            telefono.enMano(false);
            if (telefonoUI.abierto()) {
                telefonoUI.cerrar();
            }
            mundo.telefonoEnEscritorio(true);
            juego.marcar("telefono_cargando");
            audio.reproducir("interruptor", 0.4f, 0f, null);
            if (mundo.apagon()) {
                orientar("Dejas el teléfono en el escritorio. Sin luz no carga, pero está lejos de tu cama.");
            } else {
                orientar("Dejar el teléfono cargando lejos de la cama ayuda a dormir mejor.");
            }
        } else {
            telefono.enMano(true);
            mundo.telefonoEnEscritorio(false);
            audio.reproducir("interruptor", 0.4f, 0f, null);
            pistaContextual("Tomas el teléfono. " + (int) telefono.bateria() + "% de batería.", 3);
        }
    }

    private void intentarDormir() {
        int noche = juego.nocheActual();
        if (noche == 3) {
            if (!juego.bandera("router_apagado")) {
                pistaContextual("No puedes dormir con @tu_seguidor escribiéndote sin parar. Apaga el router de la sala.", 4.5);
            } else if (!juego.bandera("telefono_apagado")) {
                pistaContextual("Primero apaga el teléfono (ábrelo con TAB).", 4);
                if (!finalPreparado) {
                    prepararFinal();
                }
            } else {
                juego.apilarPantalla(new PantallaSegura(juego, this, true));
            }
            return;
        }
        if (telefono.enMano()) {
            pistaContextual("No vas a dormir con el teléfono en la mano. Déjalo cargando en el escritorio (E).", 4.5);
            audio.reproducir("error", 0.4f, 0f, null);
            return;
        }
        for (Mision m : juego.misiones()) {
            if (!m.cumplida() && !"dormir".equals(m.bandera()) && !"telefono_cargando".equals(m.bandera())) {
                pistaContextual("Antes de dormir: " + m.objetivo() + ". " + m.pista(), 5);
                audio.reproducir("error", 0.4f, 0f, null);
                return;
            }
        }
        juego.apilarPantalla(new PantallaSegura(juego, this, false));
    }

    public void dormido(int minuto) {
        juego.marcar("dormir");
        evaluarMisiones();
        juego.terminarNoche(minuto);
    }

    public void levantado() {
        jugador.posicionar(2.6, 2.4, 0.2);
        jugador.bienestar().modificar(IndicadorBienestar.ANSIEDAD, 4);
        orientar("Te levantaste a revisar el teléfono. Se te va otra vez el sueño.");
    }

    private void atrapado() {
        mundo.seguidor().apagar();
        jugador.registrarAtrapado();
        if (telefonoUI.abierto()) {
            telefonoUI.cerrar();
        }
        iniciarSusto(() -> {
            IndicadorBienestar b = jugador.bienestar();
            b.modificar(IndicadorBienestar.ANSIEDAD, 22);
            b.modificar(IndicadorBienestar.SUENO, -12);
            b.modificar(IndicadorBienestar.AUTOESTIMA, -4);
            jugador.avanzarMinutos(25);
            jugador.posicionar(2.6, 2.5, 0.25);
            Puerta cuarto = mundo.casa().puerta(7, 4);
            cuarto.fijar(0);
            mundo.seguidor().enfriar(40);
            pulsoTension = 0;
            tensionSuave *= 0.75;
            fundido = 1;
            fundidoObjetivo = 0;
            orientar("Despiertas junto a tu cama, sudando. Perdiste 25 minutos. ¿Fue un sueño?");
            if (juego.nocheActual() == 3 && juego.bandera("router_apagado") && !juego.bandera("volver_cuarto")) {
                juego.marcar("volver_cuarto");
                tiempoParaFinal = 2.0;
            }
        });
    }

    private void iniciarSusto(Runnable despues) {
        sustoSuave = configuracion.sinJumpscares();
        susto = 0;
        despuesSusto = despues;
        audio.detenerTodo();
        if (sustoSuave) {
            audio.reproducir("stinger_suave", 0.6f, 0f, textoSustoSuave);
        } else {
            audio.reproducir("stinger", 1.0f, 0f, "[Grito distorsionado]");
        }
    }

    private void actualizarSusto(double dt) {
        susto += dt;
        double duracion = sustoSuave ? 2.4 : 1.7;
        juego.entrada().consumirDX();
        juego.entrada().consumirDY();
        if (susto >= duracion) {
            susto = -1;
            Runnable r = despuesSusto;
            despuesSusto = null;
            if (r != null) {
                r.run();
            }
        }
    }

    private void iniciarUltimaNotificacion() {
        ultimaNotificacion = true;
        tiempoUltima = 0;
        telefonoUI.cerrar();
        mundo.seguidor().apagar();
        mundo.parpadeo(3.5);
        audio.reproducir("tono_roto", 0.8f, 0f, "[Todas las pantallas de la casa se encienden a la vez]");
        orientaciones.limpiar();
        orientacionActual = null;
    }

    private void actualizarUltimaNotificacion(double dt) {
        tiempoUltima += dt;
        if (tiempoUltima > 3.2 && !mundo.seguidor().activo() && susto < 0) {
            double x = jugador.x() + Math.cos(jugador.angulo()) * 1.1;
            double y = jugador.y() + Math.sin(jugador.angulo()) * 1.1;
            if (mundo.colisiona(x, y, 0.1)) {
                x = jugador.x() + Math.cos(jugador.angulo()) * 0.7;
                y = jugador.y() + Math.sin(jugador.angulo()) * 0.7;
            }
            mundo.seguidor().aparecer(x, y, 10);
        }
        if (tiempoUltima > 4.0 && susto < 0) {
            ultimaNotificacion = false;
            textoSustoSuave = "[Son las 3:12 a. m. Ya no estás solo]";
            iniciarSusto(juego::fallarNoche);
        }
    }

    private void iniciarSecuenciaMala() {
        secuenciaMala = true;
        tiempoMala = 0;
        pasoMala = 0;
        audio.reproducir("glitch", 0.6f, 0f, "[La pantalla empieza a deslizarse sola]");
    }

    private void actualizarSecuenciaMala(double dt) {
        tiempoMala += dt;
        pulsoGlitch(Math.min(1, tiempoMala / 5));
        if (tiempoMala < 4.5 && tiempoMala > pasoMala * 0.4 + 0.6) {
            pasoMala++;
            feed.hacerScroll(1);
            telefonoUI.animarDesplazamiento(1, 260);
            audio.reproducir("scroll", 0.8f, 0f, null);
            jugador.avanzarMinutos(9);
        }
        if (!fotoMalaHecha && tiempoMala >= 4.5) {
            fotoMalaHecha = true;
            director.fotoFinal("@" + jugador.nombre().toLowerCase() + " · Ahora eres contenido.");
            audio.reproducir("notificacion_rara", 0.9f, 0f, "[Nueva publicación: tú]");
        }
        if (tiempoMala > 7.2 && susto < 0) {
            secuenciaMala = false;
            telefonoUI.cerrar();
            textoSustoSuave = "[Ahora eres contenido]";
            iniciarSusto(() -> juego.finalizarPartida(TipoFinal.AHORA_ERES_CONTENIDO));
        }
    }

    private void iniciarSecuenciaBuena() {
        secuenciaBuena = true;
        tiempoBuena = 0;
        director.diferir(0.9, () -> {
            telefono.apagar();
            telefonoUI.cerrar();
            audio.reproducir("apagar", 0.7f, 0f, "[El teléfono se apaga]");
            mundo.seguidor().apagar();
        });
    }

    private void actualizarSecuenciaBuena(double dt) {
        tiempoBuena += dt;
        tensionSuave = Math.max(0, tensionSuave - dt * 12);
        if (tiempoBuena > 1.5) {
            audio.ambiente((float) Math.max(0.25, 1 - (tiempoBuena - 1.5) / 3));
        }
        if (!susurroFinalHecho && tiempoBuena > 3.5 && !configuracion.sinJumpscares()) {
            susurroFinalHecho = true;
            audio.reproducirEn("susurro_3", jugador.x() - Math.cos(jugador.angulo()), jugador.y() - Math.sin(jugador.angulo()), 0.5f,
                    "[Un susurro muy débil: \"...mañana...\"]");
        }
        if (tiempoBuena > 5.5) {
            secuenciaBuena = false;
            jugador.posicionar(2.6, 2.4, 0.2);
            juego.apilarPantalla(new PantallaSegura(juego, this, true));
        }
    }

    public boolean enSecuencia() {
        return ultimaNotificacion || secuenciaMala || secuenciaBuena || susto >= 0;
    }

    @Override
    public void dibujar(Graphics2D g) {
        if (susto >= 0) {
            double dur = sustoSuave ? 2.4 : 1.7;
            if (sustoSuave) {
                dibujarMundo(g);
                Dibujo.suavizado(g);
                Susto.dibujarSuave(g, susto / dur, textoSustoSuave);
            } else {
                Dibujo.suavizado(g);
                Susto.dibujar(g, susto / dur, jugador.nombre(), juego.azar());
            }
            hud.dibujarSubtitulos(g, 0);
            return;
        }
        dibujarMundo(g);
        Dibujo.suavizado(g);
        hud.dibujar(g);
        telefonoUI.dibujar(g);
        hud.dibujarSobreTelefono(g);
        if (ultimaNotificacion) {
            hud.dibujarUltimaNotificacion(g, tiempoUltima);
        }
    }

    private void dibujarMundo(Graphics2D g) {
        BufferedImage imagen = mundo.renderizar(camara);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        int dx = 0;
        int dy = 0;
        if (sacudida > 0) {
            dx = (int) ((juego.azar().nextDouble() - 0.5) * 18 * sacudida);
            dy = (int) ((juego.azar().nextDouble() - 0.5) * 18 * sacudida);
        }
        g.drawImage(imagen, dx - 4, dy - 4, 1288, 728, null);
        if (configuracion.altoContraste() && enfrente != null) {
            g.setColor(new Color(255, 230, 0, 60));
            g.fillRect(0, 0, 1280, 6);
            g.fillRect(0, 714, 1280, 6);
        }
    }

    @Override
    public void alPresionarTecla(int codigo, char caracter) {
        if (susto >= 0) {
            return;
        }
        if (telefonoUI.abierto()) {
            if (codigo == KeyEvent.VK_F1) {
                juego.apilarPantalla(new PantallaAyuda(juego, true));
                return;
            }
            telefonoUI.teclado(codigo);
            return;
        }
        if (enSecuencia()) {
            return;
        }
        switch (codigo) {
            case KeyEvent.VK_TAB:
                abrirTelefono();
                break;
            case KeyEvent.VK_E:
            case KeyEvent.VK_ENTER:
                interactuar();
                break;
            case KeyEvent.VK_F:
                alternarLinterna();
                break;
            case KeyEvent.VK_ESCAPE:
            case KeyEvent.VK_P:
                juego.apilarPantalla(new PantallaPausa(juego, this));
                break;
            case KeyEvent.VK_J:
                hud.alternarMisiones();
                break;
            case KeyEvent.VK_I:
                hud.alternarIndicadores();
                break;
            case KeyEvent.VK_F1:
            case KeyEvent.VK_H:
                juego.apilarPantalla(new PantallaAyuda(juego, true));
                break;
            default:
                break;
        }
    }

    @Override
    public void alHacerClic(int x, int y, int boton) {
        if (susto >= 0 || enSecuencia() && !telefonoUI.abierto()) {
            return;
        }
        if (boton == 3) {
            if (telefonoUI.abierto()) {
                alCerrar();
            } else {
                abrirTelefono();
            }
            return;
        }
        if (telefonoUI.abierto()) {
            telefonoUI.clic(x, y);
        }
    }

    @Override
    public void alMoverRueda(int rotacion) {
        if (telefonoUI.abierto() && rotacion != 0) {
            alDesplazar(rotacion > 0 ? 1 : -1);
        }
    }

    @Override
    public void alPerderFoco() {
        if (!enSecuencia()) {
            juego.apilarPantalla(new PantallaPausa(juego, this));
        }
    }

    public void dibujarComoFondo(Graphics2D g) {
        dibujarMundo(g);
        Dibujo.suavizado(g);
        telefonoUI.dibujar(g);
    }

    int tension() {
        return tension;
    }

    Jugador jugador() {
        return jugador;
    }

    Telefono telefono() {
        return telefono;
    }

    Juego juego() {
        return juego;
    }

    Mundo3D mundo() {
        return mundo;
    }

    InterfazTelefono telefonoUI() {
        return telefonoUI;
    }

    Interactuable enfrente() {
        return enfrente;
    }

    String orientacionActual() {
        return tiempoOrientacion > 0 ? orientacionActual : null;
    }

    double tiempoOrientacion() {
        return tiempoOrientacion;
    }

    String pista() {
        return tiempoPista > 0 ? pista : null;
    }

    double tiempoPista() {
        return tiempoPista;
    }

    GestorAudio audio() {
        return audio;
    }

    ConfiguracionAccesibilidad configuracion() {
        return configuracion;
    }

    Camara camara() {
        return camara;
    }

    double tiempoTotal() {
        return tiempo;
    }
}
