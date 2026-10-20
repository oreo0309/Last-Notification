package juego.pantallas;

import estructuras.ColaPrioridad;
import java.util.Random;
import juego.Jugador;
import juego.audio.GestorAudio;
import juego.feed.EventoTerror;
import juego.feed.Feed;
import juego.feed.Notificacion;
import juego.feed.Publicacion;
import juego.motor.Camara;
import juego.mundo.ElSeguidor;
import juego.mundo.Habitacion;
import juego.mundo.Mundo3D;
import juego.mundo.Puerta;

public class DirectorTerror {

    private static class AccionDiferida {
        final double momento;
        final Runnable accion;

        AccionDiferida(double momento, Runnable accion) {
            this.momento = momento;
            this.accion = accion;
        }
    }

    private final PantallaFeed pantalla;
    private final Mundo3D mundo;
    private final GestorAudio audio;
    private final Feed feed;
    private final Jugador jugador;
    private final Random azar;
    private final ColaPrioridad<AccionDiferida> diferidas =
            new ColaPrioridad<>((a, b) -> Double.compare(b.momento, a.momento));

    private double reloj;
    private double proximoEvento = 35;
    private double enfriamientoCaza = 40;
    private boolean trampaAlCerrar;
    private double[] puntoTrampa;
    private double tiempoBajaTension;

    public DirectorTerror(PantallaFeed pantalla, Mundo3D mundo, GestorAudio audio, Feed feed, Jugador jugador, Random azar) {
        this.pantalla = pantalla;
        this.mundo = mundo;
        this.audio = audio;
        this.feed = feed;
        this.jugador = jugador;
        this.azar = azar;
    }

    public void actualizar(double dt, int tension, int noche, boolean permitirEventos) {
        reloj += dt;
        while (!diferidas.estaVacia() && diferidas.ver().momento <= reloj) {
            diferidas.extraer().accion.run();
        }
        if (!permitirEventos) {
            return;
        }
        if (enfriamientoCaza > 0) {
            enfriamientoCaza -= dt;
        }
        proximoEvento -= dt * (0.6 + tension / 60.0);
        if (proximoEvento <= 0) {
            proximoEvento = 30 + azar.nextDouble() * 35 - tension * 0.15;
            if (!mundo.seguidor().activo() && !pantalla.enSecuencia()) {
                feed.fabricaEventoTerror().configurar(noche, tension, jugador.nombre());
                EventoTerror e = (EventoTerror) feed.fabricaEventoTerror().crear();
                if (!EventoTerror.CAZA.equals(e.tipo()) || enfriamientoCaza <= 0) {
                    ejecutar(e);
                }
            }
        }
        ElSeguidor s = mundo.seguidor();
        boolean puedeCazar = noche >= 2 ? tension >= 74 : tension >= 88;
        if (puedeCazar && !s.activo() && enfriamientoCaza <= 0 && !s.enfriando() && !pantalla.enSecuencia()) {
            iniciarCaza(tension >= 85 ? 4 : 3);
        }
        if (s.estado() == ElSeguidor.Estado.CAZA && noche < 3) {
            if (tension < 40) {
                tiempoBajaTension += dt;
                if (tiempoBajaTension > 12) {
                    s.desaparecer(true);
                    pantalla.orientar("La calma lo aleja. Tus buenas decisiones bajaron la tensión.");
                    enfriamientoCaza = 60;
                }
            } else {
                tiempoBajaTension = 0;
            }
        }
    }

    public void diferir(double segundos, Runnable accion) {
        diferidas.insertar(new AccionDiferida(reloj + segundos, accion));
    }

    public void ejecutar(EventoTerror original) {
        EventoTerror e = pantalla.configuracion().sinJumpscares() ? original.versionSuave() : original;
        double px = jugador.x();
        double py = jugador.y();
        double ang = jugador.angulo();
        int intensidad = e.intensidad();
        switch (e.tipo()) {
            case EventoTerror.PARPADEO:
                mundo.parpadeo(1.2 + intensidad * 0.5);
                if (intensidad >= 3 && !mundo.seguidor().activo()) {
                    double[] p = puntoEnVista(4, 8);
                    if (p != null) {
                        mundo.seguidor().aparecer(p[0], p[1], 1.6 + intensidad * 0.3);
                    }
                }
                break;
            case EventoTerror.SUSURRO:
                susurro(px - Math.cos(ang) * 1.3, py - Math.sin(ang) * 1.3);
                break;
            case EventoTerror.PASOS:
                pasosAcercandose();
                break;
            case EventoTerror.GOLPES:
                golpes();
                break;
            case EventoTerror.GOLPES_VENTANA:
                audio.reproducirEn("golpes_vidrio", 4.0, 0.9, 1.0f, "[Tres golpes en la ventana de tu cuarto]");
                break;
            case EventoTerror.PUERTA:
                puertaSeAbre();
                break;
            case EventoTerror.PORTAZO:
                portazo();
                break;
            case EventoTerror.APARICION:
                aparicion(e.suave());
                break;
            case EventoTerror.PRESENCIA:
                presencia(2.0);
                break;
            case EventoTerror.FOTO:
                foto(e);
                break;
            case EventoTerror.GLITCH:
                audio.reproducir("glitch", e.suave() ? 0.3f : 0.7f, 0f, "[Interferencia]");
                pantalla.pulsoGlitch(e.suave() ? 0.25 : 0.6 + intensidad * 0.08);
                break;
            case EventoTerror.TV:
                if (!mundo.tvEncendida()) {
                    mundo.encenderTV(true);
                    audio.agregarSubtitulo("[El televisor de la sala se encendió solo]", 3.5);
                }
                break;
            case EventoTerror.ESPEJO:
                mundo.mostrarFiguraEspejo(true);
                diferir(14, () -> mundo.mostrarFiguraEspejo(false));
                if (mundo.casa().habitacionEn(px, py) == Habitacion.BANO) {
                    susurro(px - Math.cos(ang), py - Math.sin(ang));
                }
                break;
            case EventoTerror.LLAMADA:
                llamada();
                break;
            case EventoTerror.CAZA:
                iniciarCaza(intensidad);
                break;
            case EventoTerror.MENSAJE:
                feed.insertarEnFoco(e);
                audio.reproducir("tecleo", 0.6f, 0f, "[Alguien está escribiendo...]");
                break;
            case EventoTerror.TELEFONO_MESA:
                pantalla.vibrarTelefono("[Tu teléfono vibra]");
                break;
            default:
                break;
        }
    }

    private void susurro(double x, double y) {
        String[] frases = {
            "[Susurro detrás de ti: \"...una más...\"]",
            "[Susurro: \"..." + jugador.nombre().toLowerCase() + "...\"]",
            "[Susurro: \"...sigue mirando...\"]",
            "[Susurro: \"...no te duermas...\"]",
            "[Susurro: \"...te estoy viendo...\"]"
        };
        audio.reproducirEn("susurro_" + (1 + azar.nextInt(3)), x, y, 1.2f, frases[azar.nextInt(frases.length)]);
    }

    private void pasosAcercandose() {
        double x = 8.5;
        double yInicio = 11.0;
        int n = 5 + azar.nextInt(3);
        for (int i = 0; i < n; i++) {
            final double yy = yInicio - i * 1.2;
            final boolean primero = i == 0;
            diferir(i * 0.7, () -> audio.reproducirEn("paso_" + (1 + azar.nextInt(3)), x, yy, 1.0f,
                    primero ? "[Pasos lentos en el pasillo]" : null));
        }
    }

    private void golpes() {
        Habitacion h = mundo.casa().habitacionEn(jugador.x(), jugador.y());
        if (h == Habitacion.CUARTO) {
            Puerta p = mundo.casa().puerta(7, 4);
            if (p.estaAbierta()) {
                audio.reproducirEn("golpes", 9.5, 8.5, 1.0f, "[Tres golpes en la puerta del cuarto de mamá]");
            } else {
                audio.reproducirEn("golpes", 7.5, 4.5, 1.1f, "[Alguien golpea la puerta de tu cuarto. Tres veces.]");
            }
        } else {
            audio.reproducirEn("golpes", 6.5, 17.0, 1.1f, "[Tres golpes en la puerta principal]");
        }
    }

    public void golpesPadres() {
        audio.reproducirEn("golpes", 9.5, 8.5, 1.0f, "[Golpes desde adentro del cuarto de mamá. Pero ella no está.]");
    }

    private void puertaSeAbre() {
        Puerta p = mundo.casa().puerta(7, 4);
        if (!p.estaAbierta()) {
            mundo.abrirPuertaLento(p);
        } else {
            Puerta bano = mundo.casa().puerta(9, 3);
            if (!bano.estaAbierta()) {
                mundo.abrirPuertaLento(bano);
            } else {
                pasosAcercandose();
            }
        }
    }

    private void portazo() {
        Puerta mejor = null;
        double mejorD = Double.MAX_VALUE;
        for (Puerta p : mundo.casa().puertas()) {
            if (!p.estaAbierta() || p.bloqueada()) {
                continue;
            }
            double dx = p.centroX() - jugador.x();
            double dy = p.centroY() - jugador.y();
            double d = dx * dx + dy * dy;
            if (d > 1.2 && d < mejorD) {
                mejorD = d;
                mejor = p;
            }
        }
        if (mejor != null) {
            mundo.portazo(mejor);
            pantalla.sacudir(0.4);
        } else {
            golpes();
        }
    }

    private void aparicion(boolean suave) {
        if (mundo.seguidor().activo()) {
            return;
        }
        double[] p = puntoEnVista(suave ? 6 : 4, suave ? 10 : 9);
        if (p == null) {
            p = mundo.puntoOculto(jugador.x(), jugador.y(), jugador.angulo(), 5, 10);
        }
        if (p != null) {
            mundo.seguidor().aparecer(p[0], p[1], suave ? 4 : 7);
            if (!suave) {
                audio.reproducirEn("respiracion_cerca", p[0], p[1], 0.6f, null);
            }
        }
    }

    public void aparicionEn(double x, double y, double duracion) {
        if (!mundo.seguidor().activo()) {
            mundo.seguidor().aparecer(x, y, duracion);
        }
    }

    public void presencia(double distancia) {
        if (mundo.seguidor().activo()) {
            return;
        }
        double[] p = mundo.puntoDetras(jugador.x(), jugador.y(), jugador.angulo(), distancia);
        if (p != null) {
            mundo.seguidor().aparecer(p[0], p[1], 3.5);
            audio.reproducirEn("respiracion_cerca", p[0], p[1], 0.9f, "[Algo respira justo detrás de ti]");
        } else {
            susurro(jugador.x(), jugador.y());
        }
    }

    private void foto(EventoTerror e) {
        double[] punto = mundo.puntoDetras(jugador.x(), jugador.y(), jugador.angulo(), 2.6);
        if (punto == null) {
            punto = new double[]{jugador.x() - Math.cos(jugador.angulo()) * 0.9, jugador.y() - Math.sin(jugador.angulo()) * 0.9};
        }
        Camara camara = new Camara(punto[0], punto[1], Math.atan2(jugador.y() - punto[1], jugador.x() - punto[0]));
        camara.ojo = 0.72;
        camara.inclinacion = -0.06;
        e.imagen(mundo.tomarFoto(camara, jugador.x(), jugador.y(), true, false));
        feed.insertarEnFoco(e);
        audio.reproducir("notificacion_rara", 0.8f, 0f, "[Notificación: @tu_seguidor publicó una foto]");
        trampaAlCerrar = true;
        puntoTrampa = punto;
    }

    public void fotoFinal(String texto) {
        EventoTerror e = feed.fabricaEventoTerror().crearFoto();
        e.conTexto(texto);
        double[] punto = mundo.puntoDetras(jugador.x(), jugador.y(), jugador.angulo(), 1.8);
        if (punto == null) {
            punto = new double[]{jugador.x() - Math.cos(jugador.angulo()) * 0.9, jugador.y() - Math.sin(jugador.angulo()) * 0.9};
        }
        Camara camara = new Camara(punto[0], punto[1], Math.atan2(jugador.y() - punto[1], jugador.x() - punto[0]));
        camara.ojo = 0.75;
        e.imagen(mundo.tomarFoto(camara, jugador.x(), jugador.y(), true, false));
        feed.insertarEnFoco(e);
    }

    public void alCerrarTelefono() {
        if (trampaAlCerrar && puntoTrampa != null && !mundo.seguidor().activo()) {
            trampaAlCerrar = false;
            final double[] p = puntoTrampa;
            diferir(0.6, () -> {
                if (!mundo.seguidor().activo()) {
                    mundo.seguidor().aparecer(p[0], p[1], 3.0);
                    audio.reproducirEn("respiracion_cerca", p[0], p[1], 0.8f, "[Algo respira detrás de ti]");
                }
            });
        }
    }

    private void llamada() {
        audio.reproducir("tono_roto", 0.8f, 0f, "[Tu teléfono suena: Número desconocido]");
        pantalla.llamadaEntrante(5.5);
        diferir(5.6, () -> feed.encolarNotificacion(new Notificacion("Llamada perdida", "Número desconocido (3:12)", 2, false)));
    }

    public void iniciarCaza(int intensidad) {
        ElSeguidor s = mundo.seguidor();
        if (s.activo()) {
            return;
        }
        double[] p = mundo.puntoOculto(jugador.x(), jugador.y(), jugador.angulo(), 8, 16);
        if (p == null) {
            p = mundo.puntoOculto(jugador.x(), jugador.y(), jugador.angulo(), 5, 30);
        }
        if (p == null) {
            return;
        }
        iniciarCazaEn(p[0], p[1], intensidad);
    }

    public void iniciarCazaEn(double x, double y, int intensidad) {
        ElSeguidor s = mundo.seguidor();
        boolean suave = pantalla.configuracion().sinJumpscares();
        s.velocidadExtra(suave ? 0.75 : 0.85 + intensidad * 0.08);
        s.cazar(x, y, false);
        tiempoBajaTension = 0;
        enfriamientoCaza = 50;
        audio.reproducir("stinger_suave", 0.7f, 0f, "[Algo empezó a moverse por la casa]");
        pantalla.orientar("Te está buscando. Si lo miras, no se mueve. La linterna (F) lo hace retroceder.");
    }

    public void programado(String clave) {
        switch (clave) {
            case "mama":
            case "mama2":
            case "seguidor_sigue":
            case "wifi":
            case "llamada":
            case "juli_buenas":
                Notificacion n = feed.fabricaNotificacion().crearEspecial(clave);
                if (n != null) {
                    pantalla.forzarNotificacion(n);
                }
                break;
            case "reto":
            case "anonimo":
                Publicacion p = feed.fabricaPublicacion().crearEspecial(clave);
                if (p != null) {
                    feed.insertarEn(feed.nivelScroll() + 2, p);
                }
                break;
            case "golpes_ventana":
                ejecutar(new EventoTerror(EventoTerror.GOLPES_VENTANA, 2));
                break;
            case "pasos_pasillo":
                ejecutar(new EventoTerror(EventoTerror.PASOS, 2));
                break;
            case "parpadeo":
                ejecutar(new EventoTerror(EventoTerror.PARPADEO, 3));
                break;
            case "susurro":
                ejecutar(new EventoTerror(EventoTerror.SUSURRO, 2));
                break;
            case "presencia":
                ejecutar(new EventoTerror(EventoTerror.PRESENCIA, 4));
                break;
            case "foto":
                ejecutar(feed.fabricaEventoTerror().crearFoto());
                break;
            case "golpes_padres":
                golpesPadres();
                break;
            case "tv":
                ejecutar(new EventoTerror(EventoTerror.TV, 2));
                break;
            case "apagon":
                audio.reproducir("trueno", 1.0f, -0.3f, "[Trueno. Se fue la luz en toda la casa]");
                mundo.relampago();
                pantalla.orientar("Se fue la luz. Usa la linterna del teléfono con F (gasta batería).");
                break;
            case "caza":
                if (!mundo.seguidor().activo()) {
                    iniciarCaza(3);
                }
                break;
            default:
                break;
        }
    }

    private double[] puntoEnVista(double min, double max) {
        double px = jugador.x();
        double py = jugador.y();
        double ang = jugador.angulo();
        for (int intento = 0; intento < 40; intento++) {
            double a = ang + (azar.nextDouble() - 0.5) * 0.7;
            double d = min + azar.nextDouble() * (max - min);
            double x = px + Math.cos(a) * d;
            double y = py + Math.sin(a) * d;
            if (!mundo.colisiona(x, y, 0.25) && mundo.casa().lineaDeVision(px, py, x, y)) {
                return new double[]{x, y};
            }
        }
        return null;
    }

    public double reloj() {
        return reloj;
    }
}
