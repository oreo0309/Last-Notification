package pruebas;

import juego.ConfiguracionAccesibilidad;
import juego.Decision;
import juego.IndicadorBienestar;
import juego.Jugador;
import juego.Logro;
import juego.Mision;
import juego.Noche;
import juego.TipoDecision;
import juego.feed.ElementoFeed;
import juego.feed.EventoTerror;
import juego.feed.Feed;
import juego.feed.Notificacion;
import juego.feed.Publicacion;
import estructuras.ListaEnlazada;
import juego.pantallas.PantallaNombre;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

public final class PruebasLogica {

    private PruebasLogica() {
    }

    public static void ejecutar() throws Exception {
        bienestar();
        decisiones();
        feedYFabricas();
        eventos();
        logros();
        noches();
        entradas();
        configuracion();
    }

    private static void bienestar() {
        Prueba.grupo("IndicadorBienestar");
        IndicadorBienestar b = new IndicadorBienestar(50, 50, 50, 50, 50);
        b.modificar(IndicadorBienestar.SUENO, 80);
        Prueba.igual("los valores no pasan de 100", 100, b.sueno());
        b.modificar(IndicadorBienestar.ANSIEDAD, -90);
        Prueba.igual("los valores no bajan de 0", 0, b.ansiedad());
        Prueba.verdadero("bienestar sano no está en riesgo", !new IndicadorBienestar().enRiesgo());
        IndicadorBienestar mal = new IndicadorBienestar(20, 30, 85, 40, 40);
        Prueba.verdadero("sueño bajo y ansiedad alta = en riesgo", mal.enRiesgo());
        Prueba.verdadero("más riesgo cuando empeora", mal.nivelRiesgo() > new IndicadorBienestar().nivelRiesgo());
    }

    private static void decisiones() {
        Prueba.grupo("Decision y Jugador");
        Jugador j = new Jugador("Prueba");
        j.horaActual(23 * 60);
        int suenoAntes = j.bienestar().sueno();
        Decision d = new Decision(TipoDecision.VER_MAS, "Leer todo")
                .efecto(IndicadorBienestar.SUENO, -4).efecto(IndicadorBienestar.ANSIEDAD, 3).minutos(10);
        j.decidir(d);
        Prueba.igual("aplica efecto en sueño", suenoAntes - 4, j.bienestar().sueno());
        Prueba.igual("avanza el tiempo", 23 * 60 + 10, j.horaActual());
        Prueba.igual("cuenta decisiones dañinas", 1, j.decisionesDaninas());
        Prueba.verdadero("impacto negativo", d.impacto() < 0);
        Decision buena = new Decision(TipoDecision.REPORTAR, "Reportar").efecto(IndicadorBienestar.ANSIEDAD, -5);
        Prueba.verdadero("impacto positivo al bajar ansiedad", buena.impacto() > 0);
        j.decidir(buena);
        Prueba.igual("cuenta reportes", 1, j.reportes());
        Prueba.igual("historial guarda decisiones", 2, j.historial().tamano());
        j.descansar(Jugador.MINUTO_DESPERTAR - 8 * 60, 1);
        Prueba.verdadero("dormir 8 horas recupera el sueño", j.bienestar().sueno() >= 90);
        Prueba.igual("formato de hora", "03:12", Jugador.horaTexto(Jugador.MINUTO_LIMITE));
    }

    private static void feedYFabricas() {
        Prueba.grupo("Feed y Factory Method");
        Feed feed = new Feed(new Random(3));
        feed.reiniciar(1, "Ana");
        feed.generarContenido(10);
        Prueba.igual("genera 10 elementos", 10, feed.elementos().tamano());
        boolean todosConDecision = true;
        for (ElementoFeed e : feed.elementos()) {
            todosConDecision &= !e.decisiones().estaVacia();
        }
        Prueba.verdadero("cada publicación ofrece decisiones", todosConDecision);
        Prueba.verdadero("scroll a un elemento nuevo cuenta como visto", feed.hacerScroll(1));
        Prueba.verdadero("volver arriba no es nuevo", !feed.hacerScroll(-1));
        for (int i = 0; i < 20; i++) {
            feed.hacerScroll(1);
        }
        Prueba.verdadero("el scroll es infinito (genera más)", feed.elementos().tamano() > 20);
        Publicacion reto = feed.fabricaPublicacion().crearEspecial("reto");
        boolean tieneBandera = false;
        for (Decision d : reto.decisiones()) {
            tieneBandera |= "reto_reportado".equals(d.bandera());
        }
        Prueba.verdadero("el reto peligroso permite cumplir la misión", tieneBandera);
        Notificacion mama = feed.fabricaNotificacion().crearEspecial("mama");
        Prueba.verdadero("el mensaje de mamá no se puede silenciar", !mama.silenciable());
        feed.encolarNotificacion(new Notificacion("eco", "baja", 1, true));
        feed.encolarNotificacion(mama);
        feed.encolarNotificacion(new Notificacion("eco", "media", 3, true));
        Prueba.igual("la cola de prioridad entrega primero lo más urgente", 5, feed.siguienteNotificacion().urgencia());
        feed.actualizarTension(95);
        int perturbadoras = 0;
        for (int i = 0; i < 60; i++) {
            Notificacion n = (Notificacion) feed.fabricaNotificacion().crear();
            if (n.dispara() != null) {
                perturbadoras++;
            }
        }
        Prueba.verdadero("con tensión alta aparecen notificaciones que disparan terror", perturbadoras > 10);
    }

    private static void eventos() {
        Prueba.grupo("EventoTerror");
        EventoTerror susto = new EventoTerror(EventoTerror.JUMPSCARE, 5);
        EventoTerror suave = susto.versionSuave();
        Prueba.igual("la versión suave cambia el susto", EventoTerror.JUMPSCARE_SUAVE, suave.tipo());
        Prueba.verdadero("la versión suave es menos intensa", suave.intensidad() < susto.intensidad());
        Prueba.igual("presencia suave se vuelve susurro", EventoTerror.SUSURRO, new EventoTerror(EventoTerror.PRESENCIA, 4).versionSuave().tipo());
    }

    private static void logros() {
        Prueba.grupo("Logros (recompensas)");
        ListaEnlazada<Decision> buenas = new ListaEnlazada<>();
        buenas.agregar(new Decision(TipoDecision.REPORTAR, "Reportar"));
        buenas.agregar(new Decision(TipoDecision.RESPONDER_CALMA, "Responder con calma"));
        ListaEnlazada<Logro> l = Logro.evaluarNoche(buenas, 23 * 60 + 20, 3, 0, true, 20);
        Prueba.verdadero("noche ejemplar desbloquea los 9 logros", l.tamano() == Logro.values().length);
        ListaEnlazada<Decision> malas = new ListaEnlazada<>();
        malas.agregar(new Decision(TipoDecision.COMPARARSE, "Compararte"));
        ListaEnlazada<Logro> pocos = Logro.evaluarNoche(malas, 1440 + 150, 25, 2, false, 80);
        Prueba.igual("noche de malos hábitos no da logros", 0, pocos.tamano());
        Jugador j = new Jugador("Ana");
        Prueba.verdadero("un logro nuevo se registra", j.agregarLogro(Logro.MODO_NOCHE));
        Prueba.verdadero("un logro repetido no se duplica", !j.agregarLogro(Logro.MODO_NOCHE));
    }

    private static void noches() {
        Prueba.grupo("Noches y misiones");
        for (int n = 1; n <= Noche.TOTAL; n++) {
            Noche noche = Noche.crear(n);
            Prueba.verdadero("la noche " + n + " tiene misiones", noche.misiones().tamano() >= 3);
            Prueba.verdadero("la noche " + n + " empieza antes de medianoche", noche.horaInicio() < 1440);
        }
        Noche n1 = Noche.crear(1);
        Prueba.verdadero("no hay eventos antes de su minuto", n1.siguienteListo(n1.horaInicio()) == null);
        Prueba.igual("el primer evento programado es el de mamá", "mama", n1.siguienteListo(n1.horaInicio() + 3).clave());
        Mision m = new Mision("Prueba", "x", "pista");
        Prueba.verdadero("misión empieza sin cumplir", !m.cumplida());
    }

    private static void entradas() {
        Prueba.grupo("Validación de entradas (RF11)");
        Prueba.verdadero("nombre vacío rechazado", PantallaNombre.validar("") != null);
        Prueba.verdadero("nombre con números rechazado", PantallaNombre.validar("Ana3") != null);
        Prueba.verdadero("nombre muy largo rechazado", PantallaNombre.validar("Maximilianoooooooo") != null);
        Prueba.verdadero("nombre con tilde y espacio aceptado", PantallaNombre.validar("José María") == null);
    }

    private static void configuracion() throws Exception {
        Prueba.grupo("ConfiguracionAccesibilidad (archivo)");
        Path tmp = Files.createTempFile("config", ".txt");
        ConfiguracionAccesibilidad c = new ConfiguracionAccesibilidad();
        c.altoContraste(true);
        c.sinJumpscares(true);
        c.filtroColor(2);
        c.volumen(0.3f);
        Prueba.verdadero("se guarda en archivo", c.guardar(tmp));
        ConfiguracionAccesibilidad leida = new ConfiguracionAccesibilidad();
        leida.cargar(tmp);
        Prueba.verdadero("lee alto contraste", leida.altoContraste());
        Prueba.verdadero("lee reducción de sustos", leida.sinJumpscares());
        Prueba.igual("lee filtro de color", 2, leida.filtroColor());
        Files.writeString(tmp, "volumen=abc\nfiltroColor=99\nbasura sin igual\naltoContraste=quizas\n");
        ConfiguracionAccesibilidad corrupta = new ConfiguracionAccesibilidad();
        corrupta.cargar(tmp);
        Prueba.verdadero("un archivo dañado no rompe el juego", corrupta.volumen() == 0.8f && corrupta.filtroColor() == 3 && !corrupta.altoContraste());
        Files.deleteIfExists(tmp);
    }
}
