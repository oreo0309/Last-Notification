package juego;

import estructuras.Cola;
import estructuras.ListaEnlazada;
import estructuras.Pila;
import estructuras.TablaHash;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.Random;
import juego.audio.GestorAudio;
import juego.feed.Feed;
import juego.interfaz.Dibujo;
import juego.interfaz.Estilo;
import juego.motor.Entrada;
import juego.motor.EventoEntrada;
import juego.motor.GeneradorTexturas;
import juego.motor.Textura;
import juego.motor.Ventana;
import juego.mundo.Mundo3D;
import juego.pantallas.Pantalla;
import juego.pantallas.PantallaFeed;
import juego.pantallas.PantallaMenu;
import juego.pantallas.PantallaResultados;
import juego.pantallas.PantallaTransicion;

public class Juego {

    private static final double PASO = 1.0 / 60.0;

    private int semana = 1;
    private int nocheActual;
    private Pantalla pantallaActual;

    private final Jugador jugador;
    private final Feed feed;
    private final GestorAudio gestorAudio;
    private final ConfiguracionAccesibilidad configuracion;
    private final ListaEnlazada<Mision> misiones = new ListaEnlazada<>();
    private final Pila<Pantalla> pilaPantallas = new Pila<>();
    private final TablaHash<String, Boolean> banderas = new TablaHash<>();
    private final Cola<Runnable> tareasPendientes = new Cola<>();
    private final Random azar = new Random();
    private final Ventana ventana;
    private final Entrada entrada;
    private final TablaHash<String, Textura> texturas;
    private final GeneradorTexturas generador;
    private final Mundo3D mundo;
    private final RegistroPuntajes registro;
    private final Path carpetaDatos;

    private Noche noche;
    private IndicadorBienestar respaldoNoche;
    private boolean corriendo = true;
    private String mensajeError;
    private double tiempoError;
    private int erroresRecientes;
    private double ventanaErrores;
    private int ultimoPuntaje;
    private int historialInicioNoche;
    private int vistasInicioNoche;
    private int atrapadasInicioNoche;
    private final ListaEnlazada<Logro> logrosNoche = new ListaEnlazada<>();
    private boolean mostrarFps;
    private int cuadros;
    private int fps;
    private long inicioConteo = System.nanoTime();

    public Juego(Ventana ventana, Entrada entrada) {
        this.ventana = ventana;
        this.entrada = entrada;
        this.carpetaDatos = Paths.get("datos");
        this.configuracion = new ConfiguracionAccesibilidad();
        configuracion.cargar(carpetaDatos.resolve("configuracion.txt"));
        Estilo.configurar(configuracion);
        this.gestorAudio = new GestorAudio();
        this.generador = new GeneradorTexturas();
        this.texturas = generador.generarTodo();
        this.mundo = new Mundo3D(texturas, generador, gestorAudio);
        this.jugador = new Jugador("Sebas");
        this.feed = new Feed(azar);
        this.registro = new RegistroPuntajes(carpetaDatos.resolve("puntajes.csv"));
    }

    public void ejecutar() {
        ventana.mostrar();
        gestorAudio.iniciar();
        configuracion.aplicar(this);
        cambiarPantalla(new PantallaMenu(this));
        long anterior = System.nanoTime();
        double acumulado = 0;
        while (corriendo && ventana.abierta()) {
            long ahora = System.nanoTime();
            double dt = (ahora - anterior) / 1e9;
            anterior = ahora;
            if (dt > 0.25) {
                dt = 0.25;
            }
            procesarTareas();
            procesarEntrada();
            acumulado += dt;
            while (acumulado >= PASO) {
                actualizar(PASO);
                acumulado -= PASO;
            }
            ventana.dibujar(this::dibujar);
            cuadros++;
            if (System.nanoTime() - inicioConteo >= 1_000_000_000L) {
                fps = cuadros;
                cuadros = 0;
                inicioConteo = System.nanoTime();
            }
            long usado = System.nanoTime() - ahora;
            long objetivo = 1_000_000_000L / 72;
            if (usado < objetivo) {
                try {
                    Thread.sleep((objetivo - usado) / 1_000_000L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    corriendo = false;
                }
            }
        }
        salir();
    }

    public void ejecutarEnBucle(Runnable tarea) {
        synchronized (tareasPendientes) {
            tareasPendientes.encolar(tarea);
        }
    }

    private void procesarTareas() {
        while (true) {
            Runnable tarea;
            synchronized (tareasPendientes) {
                if (tareasPendientes.estaVacia()) {
                    return;
                }
                tarea = tareasPendientes.desencolar();
            }
            try {
                tarea.run();
            } catch (RuntimeException ex) {
                registrarError(ex);
            }
        }
    }

    private void procesarEntrada() {
        EventoEntrada e;
        while ((e = entrada.siguienteEvento()) != null) {
            try {
                switch (e.tipo) {
                    case TECLA_PRESIONADA:
                        if (e.codigo == KeyEvent.VK_F9) {
                            mostrarFps = !mostrarFps;
                        } else if (e.codigo == KeyEvent.VK_F11) {
                            configuracion.pantallaCompleta(!configuracion.pantallaCompleta());
                            ventana.pantallaCompleta(configuracion.pantallaCompleta());
                        } else {
                            pantallaActual.alPresionarTecla(e.codigo, e.caracter);
                        }
                        break;
                    case TECLA_SOLTADA:
                        pantallaActual.alSoltarTecla(e.codigo);
                        break;
                    case CLIC:
                        pantallaActual.alHacerClic(e.x, e.y, e.codigo);
                        break;
                    case RUEDA:
                        pantallaActual.alMoverRueda(e.valor);
                        break;
                    default:
                        break;
                }
            } catch (RuntimeException ex) {
                registrarError(ex);
            }
        }
        ventana.capturarRaton(pantallaActual.capturaRaton());
        if (ventana.consumirFocoPerdido()) {
            pantallaActual.alPerderFoco();
        }
    }

    private void actualizar(double dt) {
        try {
            pantallaActual.actualizar(dt);
        } catch (RuntimeException ex) {
            registrarError(ex);
        }
        if (tiempoError > 0) {
            tiempoError -= dt;
        }
        if (ventanaErrores > 0) {
            ventanaErrores -= dt;
        } else {
            erroresRecientes = 0;
        }
    }

    private void dibujar(Graphics2D g) {
        try {
            pantallaActual.dibujar(g);
        } catch (RuntimeException ex) {
            registrarError(ex);
        }
        if (mostrarFps) {
            g.setFont(Estilo.mono(Font.BOLD, 14));
            g.setColor(new Color(0, 0, 0, 170));
            g.fillRect(Ventana.ANCHO / 2 - 45, 2, 90, 22);
            g.setColor(fps >= 50 ? new Color(120, 255, 140) : new Color(255, 200, 80));
            g.drawString(fps + " FPS", Ventana.ANCHO / 2 - 34, 18);
        }
        if (tiempoError > 0 && mensajeError != null) {
            Dibujo.suavizado(g);
            g.setFont(Estilo.fuente(Font.BOLD, 14));
            int w = g.getFontMetrics().stringWidth(mensajeError) + 40;
            Dibujo.rectRedondo(g, (Ventana.ANCHO - w) / 2.0, 20, w, 36, 12, new Color(120, 20, 20, 220));
            g.setColor(Color.WHITE);
            Dibujo.centrado(g, mensajeError, Ventana.ANCHO / 2, 43);
        }
    }

    public void registrarError(Throwable error) {
        mensajeError = "Ocurrió un problema inesperado, pero el juego sigue funcionando.";
        tiempoError = 4;
        erroresRecientes++;
        ventanaErrores = 2;
        try {
            Files.createDirectories(carpetaDatos);
            StringWriter texto = new StringWriter();
            error.printStackTrace(new PrintWriter(texto));
            try (BufferedWriter escritor = Files.newBufferedWriter(carpetaDatos.resolve("errores.log"), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                escritor.write(LocalDateTime.now() + " en " + (pantallaActual == null ? "?" : pantallaActual.nombre()));
                escritor.newLine();
                escritor.write(texto.toString());
                escritor.newLine();
            }
        } catch (IOException e) {
            mensajeError = "Ocurrió un problema inesperado (no se pudo guardar el registro).";
        }
        if (erroresRecientes > 20) {
            erroresRecientes = 0;
            pilaPantallas.limpiar();
            cambiarPantalla(new PantallaMenu(this));
        }
    }

    public void cambiarPantalla(Pantalla nueva) {
        while (!pilaPantallas.estaVacia()) {
            Pantalla p = pilaPantallas.desapilar();
            p.salir();
        }
        if (pantallaActual != null) {
            pantallaActual.salir();
        }
        pantallaActual = nueva;
        entrada.soltarTodo();
        nueva.entrar();
    }

    public void apilarPantalla(Pantalla superpuesta) {
        pilaPantallas.apilar(pantallaActual);
        pantallaActual = superpuesta;
        entrada.soltarTodo();
        superpuesta.entrar();
    }

    public void desapilarPantalla() {
        if (pilaPantallas.estaVacia()) {
            return;
        }
        pantallaActual.salir();
        pantallaActual = pilaPantallas.desapilar();
        entrada.soltarTodo();
        pantallaActual.alReanudar();
    }

    public void nuevaPartida(String nombre) {
        jugador.nombre(nombre);
        jugador.reiniciarPartida();
        nocheActual = 0;
        iniciarNoche();
    }

    public void iniciarNoche() {
        nocheActual++;
        noche = Noche.crear(nocheActual);
        misiones.limpiar();
        for (Mision m : noche.misiones()) {
            m.reiniciar();
            misiones.agregar(m);
        }
        banderas.claves().forEach(banderas::eliminar);
        respaldoNoche = jugador.bienestar().copia();
        historialInicioNoche = jugador.historial().tamano();
        vistasInicioNoche = jugador.publicacionesVistas();
        atrapadasInicioNoche = jugador.vecesAtrapado();
        jugador.horaActual(noche.horaInicio());
        jugador.posicionar(3.3, 3.6, -0.75);
        jugador.telefono().reiniciar(noche.bateriaInicial());
        mundo.configurarNoche(nocheActual);
        feed.reiniciar(nocheActual, jugador.nombre());
        feed.generarContenido(5);
        gestorAudio.detenerTodo();
        gestorAudio.limpiarSubtitulos();
        String titulo = "NOCHE " + nocheActual;
        String subtitulo = "Semana " + semana + " · " + noche.dia() + " · " + jugador.horaTexto();
        StringBuilder cuerpo = new StringBuilder(noche.introduccion()).append("\n\nObjetivos:");
        for (Mision m : misiones) {
            cuerpo.append("\n•  ").append(m.objetivo());
        }
        cambiarPantalla(new PantallaTransicion(this, titulo, subtitulo, cuerpo.toString(),
                () -> cambiarPantalla(new PantallaFeed(this))));
    }

    public void terminarNoche(int minutoDormir) {
        IndicadorBienestar antes = jugador.bienestar().copia();
        evaluarLogros(minutoDormir, antes.ansiedad());
        jugador.descansar(minutoDormir, nocheActual);
        if (nocheActual >= Noche.TOTAL) {
            finalizarPartida(jugador.bienestar().bienestarGeneral() >= 50 ? TipoFinal.DESCONECTADO : TipoFinal.AL_LIMITE);
            return;
        }
        double horas = jugador.horasDormidas()[nocheActual - 1];
        int h = (int) horas;
        int m = (int) Math.round((horas - h) * 60);
        StringBuilder cuerpo = new StringBuilder();
        cuerpo.append("Te dormiste a las ").append(Jugador.horaTexto(minutoDormir))
                .append(" y dormiste ").append(h).append(" h ").append(m).append(" min.\n\n");
        for (String clave : IndicadorBienestar.TODOS) {
            int delta = jugador.bienestar().valor(clave) - antes.valor(clave);
            cuerpo.append(IndicadorBienestar.nombreVisible(clave)).append(": ")
                    .append(jugador.bienestar().valor(clave))
                    .append(delta == 0 ? "" : (delta > 0 ? "  (+" + delta + ")" : "  (" + delta + ")"))
                    .append("\n");
        }
        cuerpo.append("\n").append(horas >= 7 ? "¡Excelente! Dormir 7 horas o más te deja con energía para el día siguiente."
                : (horas >= 5 ? "¡Lo lograste! Dormiste, aunque no lo suficiente: los adolescentes necesitan entre 8 y 10 horas."
                : "Sobreviviste a la noche, pero dormiste muy poco. El cansancio aumenta la ansiedad y baja la concentración."));
        cambiarPantalla(new PantallaTransicion(this, "NOCHE " + nocheActual + " SUPERADA", noche.dia() + " · 06:30 · Amanece",
                cuerpo.toString(), logrosNoche, this::iniciarNoche));
    }

    private void evaluarLogros(int minutoDormir, int ansiedadFinal) {
        ListaEnlazada<Decision> decisionesNoche = new ListaEnlazada<>();
        int i = 0;
        for (Decision d : jugador.historial()) {
            if (i++ >= historialInicioNoche) {
                decisionesNoche.agregar(d);
            }
        }
        boolean noMolestar = jugador.telefono().noMolestar() || bandera("no_molestar");
        logrosNoche.limpiar();
        for (Logro l : Logro.evaluarNoche(decisionesNoche, minutoDormir, jugador.publicacionesVistas() - vistasInicioNoche,
                jugador.vecesAtrapado() - atrapadasInicioNoche, noMolestar, ansiedadFinal)) {
            logrosNoche.agregar(l);
            jugador.agregarLogro(l);
        }
    }

    public void fallarNoche() {
        finalizarPartida(TipoFinal.ULTIMA_NOTIFICACION);
    }

    public void reintentarNoche() {
        if (respaldoNoche != null) {
            jugador.bienestar().restaurar(respaldoNoche);
        }
        nocheActual = Math.max(0, nocheActual - 1);
        iniciarNoche();
    }

    public void finalizarPartida(TipoFinal tipoFinal) {
        ultimoPuntaje = calcularPuntaje(tipoFinal);
        if (tipoFinal != TipoFinal.ULTIMA_NOTIFICACION) {
            registro.agregar(jugador.nombre(), ultimoPuntaje, tipoFinal);
        }
        cambiarPantalla(new PantallaResultados(this, tipoFinal, ultimoPuntaje));
    }

    public int calcularPuntaje(TipoFinal tipoFinal) {
        double horas = 0;
        for (double h : jugador.horasDormidas()) {
            horas += h;
        }
        int puntaje = jugador.bienestar().bienestarGeneral() * 10
                + jugador.decisionesSaludables() * 15
                - jugador.decisionesDaninas() * 10
                - jugador.vecesAtrapado() * 40
                + (int) Math.round(horas * 25)
                + jugador.logros().tamano() * 25
                + (tipoFinal == TipoFinal.DESCONECTADO ? 300 : tipoFinal == TipoFinal.AL_LIMITE ? 120 : 0);
        return Math.max(0, puntaje);
    }

    public boolean bandera(String clave) {
        return banderas.obtenerOPorDefecto(clave, false);
    }

    public void marcar(String clave) {
        if (clave != null) {
            banderas.poner(clave, true);
        }
    }

    public void desmarcar(String clave) {
        banderas.eliminar(clave);
    }

    public Mision misionPendiente() {
        return misiones.buscar(m -> !m.cumplida());
    }

    public boolean todasCumplidasExcepto(String bandera) {
        return misiones.contar(m -> !m.cumplida() && !m.bandera().equals(bandera)) == 0;
    }

    public void guardarConfiguracion() {
        configuracion.guardar(carpetaDatos.resolve("configuracion.txt"));
    }

    public void salir() {
        corriendo = false;
        guardarConfiguracion();
        gestorAudio.detener();
        ventana.cerrar();
        System.exit(0);
    }

    public void solicitarSalida() {
        corriendo = false;
    }

    public int semana() {
        return semana;
    }

    public int nocheActual() {
        return nocheActual;
    }

    public Pantalla pantallaActual() {
        return pantallaActual;
    }

    public Jugador jugador() {
        return jugador;
    }

    public Feed feed() {
        return feed;
    }

    public GestorAudio gestorAudio() {
        return gestorAudio;
    }

    public ConfiguracionAccesibilidad configuracion() {
        return configuracion;
    }

    public ListaEnlazada<Mision> misiones() {
        return misiones;
    }

    public Noche noche() {
        return noche;
    }

    public Random azar() {
        return azar;
    }

    public Ventana ventana() {
        return ventana;
    }

    public Entrada entrada() {
        return entrada;
    }

    public Mundo3D mundo() {
        return mundo;
    }

    public TablaHash<String, Textura> texturas() {
        return texturas;
    }

    public RegistroPuntajes registro() {
        return registro;
    }

    public int ultimoPuntaje() {
        return ultimoPuntaje;
    }
}
