package juego;

import estructuras.ListaEnlazada;

public class Jugador {

    public static final int MINUTO_DESPERTAR = 1440 + 390;
    public static final int MINUTO_LIMITE = 1440 + 192;

    private String nombre;
    private int horaActual;
    private final IndicadorBienestar bienestar = new IndicadorBienestar();
    private final Telefono telefono = new Telefono();
    private final ListaEnlazada<Decision> historial = new ListaEnlazada<>();
    private final ListaEnlazada<Logro> logros = new ListaEnlazada<>();
    private final double[] horasDormidas = new double[3];

    private double x;
    private double y;
    private double angulo;
    private double inclinacion;

    private int publicacionesVistas;
    private double minutosEnPantalla;
    private int reportes;
    private int vecesAtrapado;
    private int decisionesSaludables;
    private int decisionesDaninas;

    public Jugador(String nombre) {
        this.nombre = nombre;
    }

    public void decidir(Decision decision) {
        decision.aplicarEfecto(bienestar);
        historial.agregar(decision);
        horaActual += decision.minutos();
        if (decision.tipo().saludable()) {
            decisionesSaludables++;
        } else if (decision.tipo().danina()) {
            decisionesDaninas++;
        }
        if (decision.tipo() == TipoDecision.REPORTAR) {
            reportes++;
        }
    }

    public void descansar(int minutoDormir, int noche) {
        double horas = Math.max(0, (MINUTO_DESPERTAR - minutoDormir) / 60.0);
        if (noche >= 1 && noche <= horasDormidas.length) {
            horasDormidas[noche - 1] = horas;
        }
        int objetivoSueno = (int) Math.round(Math.min(96, 28 + horas * 9.5));
        bienestar.fijar(IndicadorBienestar.SUENO, objetivoSueno);
        bienestar.modificar(IndicadorBienestar.ANSIEDAD, horas >= 6 ? -14 : (horas >= 4 ? -6 : 4));
        bienestar.modificar(IndicadorBienestar.CONCENTRACION, horas >= 6 ? 12 : (horas >= 4 ? 2 : -10));
    }

    public boolean agregarLogro(Logro logro) {
        if (logros.contiene(logro)) {
            return false;
        }
        logros.agregar(logro);
        return true;
    }

    public ListaEnlazada<Logro> logros() {
        return logros;
    }

    public void registrarPublicacionVista() {
        publicacionesVistas++;
    }

    public void sumarMinutosPantalla(double minutos) {
        minutosEnPantalla += minutos;
    }

    public void registrarAtrapado() {
        vecesAtrapado++;
    }

    public void avanzarMinutos(int minutos) {
        horaActual += minutos;
    }

    public String horaTexto() {
        int m = Math.floorMod(horaActual, 1440);
        return String.format("%02d:%02d", m / 60, m % 60);
    }

    public static String horaTexto(int minutos) {
        int m = Math.floorMod(minutos, 1440);
        return String.format("%02d:%02d", m / 60, m % 60);
    }

    public void posicionar(double x, double y, double angulo) {
        this.x = x;
        this.y = y;
        this.angulo = angulo;
        this.inclinacion = 0;
    }

    public void reiniciarPartida() {
        historial.limpiar();
        logros.limpiar();
        bienestar.restaurar(new IndicadorBienestar());
        for (int i = 0; i < horasDormidas.length; i++) {
            horasDormidas[i] = 0;
        }
        publicacionesVistas = 0;
        minutosEnPantalla = 0;
        reportes = 0;
        vecesAtrapado = 0;
        decisionesSaludables = 0;
        decisionesDaninas = 0;
    }

    public String nombre() {
        return nombre;
    }

    public void nombre(String nombre) {
        this.nombre = nombre;
    }

    public int horaActual() {
        return horaActual;
    }

    public void horaActual(int minutos) {
        this.horaActual = minutos;
    }

    public IndicadorBienestar bienestar() {
        return bienestar;
    }

    public Telefono telefono() {
        return telefono;
    }

    public ListaEnlazada<Decision> historial() {
        return historial;
    }

    public double[] horasDormidas() {
        return horasDormidas;
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public double angulo() {
        return angulo;
    }

    public double inclinacion() {
        return inclinacion;
    }

    public void x(double v) {
        x = v;
    }

    public void y(double v) {
        y = v;
    }

    public void angulo(double v) {
        angulo = v;
    }

    public void inclinacion(double v) {
        inclinacion = Math.max(-0.35, Math.min(0.35, v));
    }

    public int publicacionesVistas() {
        return publicacionesVistas;
    }

    public double minutosEnPantalla() {
        return minutosEnPantalla;
    }

    public int reportes() {
        return reportes;
    }

    public int vecesAtrapado() {
        return vecesAtrapado;
    }

    public int decisionesSaludables() {
        return decisionesSaludables;
    }

    public int decisionesDaninas() {
        return decisionesDaninas;
    }
}
