package juego;

import estructuras.TablaHash;

public class IndicadorBienestar {

    public static final String SUENO = "sueno";
    public static final String AUTOESTIMA = "autoestima";
    public static final String ANSIEDAD = "ansiedad";
    public static final String CONCENTRACION = "concentracion";
    public static final String RELACIONES = "relaciones";
    public static final String[] TODOS = {SUENO, AUTOESTIMA, ANSIEDAD, CONCENTRACION, RELACIONES};

    private int sueno;
    private int autoestima;
    private int ansiedad;
    private int concentracion;
    private int relaciones;

    public IndicadorBienestar() {
        this(72, 62, 28, 66, 60);
    }

    public IndicadorBienestar(int sueno, int autoestima, int ansiedad, int concentracion, int relaciones) {
        this.sueno = limitar(sueno);
        this.autoestima = limitar(autoestima);
        this.ansiedad = limitar(ansiedad);
        this.concentracion = limitar(concentracion);
        this.relaciones = limitar(relaciones);
    }

    public void modificar(String indicador, int delta) {
        switch (indicador) {
            case SUENO:
                sueno = limitar(sueno + delta);
                break;
            case AUTOESTIMA:
                autoestima = limitar(autoestima + delta);
                break;
            case ANSIEDAD:
                ansiedad = limitar(ansiedad + delta);
                break;
            case CONCENTRACION:
                concentracion = limitar(concentracion + delta);
                break;
            case RELACIONES:
                relaciones = limitar(relaciones + delta);
                break;
            default:
                break;
        }
    }

    public void modificar(TablaHash<String, Integer> deltas) {
        for (String clave : TODOS) {
            Integer d = deltas.obtener(clave);
            if (d != null) {
                modificar(clave, d);
            }
        }
    }

    public int valor(String indicador) {
        switch (indicador) {
            case SUENO:
                return sueno;
            case AUTOESTIMA:
                return autoestima;
            case ANSIEDAD:
                return ansiedad;
            case CONCENTRACION:
                return concentracion;
            case RELACIONES:
                return relaciones;
            default:
                return 0;
        }
    }

    public void fijar(String indicador, int valor) {
        modificar(indicador, valor - valor(indicador));
    }

    public boolean enRiesgo() {
        return sueno < 30 || autoestima < 30 || ansiedad > 70 || concentracion < 30 || relaciones < 30 || nivelRiesgo() > 60;
    }

    public int nivelRiesgo() {
        double riesgo = ansiedad * 0.34 + (100 - sueno) * 0.26 + (100 - autoestima) * 0.16
                + (100 - concentracion) * 0.12 + (100 - relaciones) * 0.12;
        return limitar((int) Math.round(riesgo));
    }

    public int bienestarGeneral() {
        return limitar((sueno + autoestima + (100 - ansiedad) + concentracion + relaciones) / 5);
    }

    public IndicadorBienestar copia() {
        return new IndicadorBienestar(sueno, autoestima, ansiedad, concentracion, relaciones);
    }

    public void restaurar(IndicadorBienestar otro) {
        sueno = otro.sueno;
        autoestima = otro.autoestima;
        ansiedad = otro.ansiedad;
        concentracion = otro.concentracion;
        relaciones = otro.relaciones;
    }

    public static String nombreVisible(String indicador) {
        switch (indicador) {
            case SUENO:
                return "Sueño";
            case AUTOESTIMA:
                return "Autoestima";
            case ANSIEDAD:
                return "Ansiedad";
            case CONCENTRACION:
                return "Concentración";
            case RELACIONES:
                return "Relaciones";
            default:
                return indicador;
        }
    }

    public static boolean esInverso(String indicador) {
        return ANSIEDAD.equals(indicador);
    }

    private static int limitar(int v) {
        return Math.max(0, Math.min(100, v));
    }

    public int sueno() {
        return sueno;
    }

    public int autoestima() {
        return autoestima;
    }

    public int ansiedad() {
        return ansiedad;
    }

    public int concentracion() {
        return concentracion;
    }

    public int relaciones() {
        return relaciones;
    }
}
