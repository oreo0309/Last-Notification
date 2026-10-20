package pruebas;

public final class Prueba {

    private static int exitosas;
    private static int fallidas;
    private static String grupo = "";

    private Prueba() {
    }

    public static void grupo(String nombre) {
        grupo = nombre;
        System.out.println();
        System.out.println("== " + nombre + " ==");
    }

    public static void verdadero(String descripcion, boolean condicion) {
        if (condicion) {
            exitosas++;
            System.out.println("  [OK]    " + descripcion);
        } else {
            fallidas++;
            System.out.println("  [FALLA] " + grupo + ": " + descripcion);
        }
    }

    public static void igual(String descripcion, Object esperado, Object obtenido) {
        boolean ok = esperado == null ? obtenido == null : esperado.equals(obtenido);
        verdadero(descripcion + (ok ? "" : " (esperado " + esperado + ", obtenido " + obtenido + ")"), ok);
    }

    public static void lanza(String descripcion, Runnable accion, Class<? extends Throwable> tipo) {
        try {
            accion.run();
            verdadero(descripcion + " (no lanzó excepción)", false);
        } catch (Throwable t) {
            verdadero(descripcion, tipo.isInstance(t));
        }
    }

    public static int resumen() {
        System.out.println();
        System.out.println("Resultado: " + exitosas + " pruebas correctas, " + fallidas + " fallidas.");
        return fallidas;
    }
}
