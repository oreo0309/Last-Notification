package juego.interfaz;

import estructuras.TablaHash;
import java.awt.Color;
import java.awt.Font;
import juego.ConfiguracionAccesibilidad;

public final class Estilo {

    private static ConfiguracionAccesibilidad configuracion = new ConfiguracionAccesibilidad();
    private static final TablaHash<String, Font> FUENTES = new TablaHash<>(64);

    private Estilo() {
    }

    public static void configurar(ConfiguracionAccesibilidad c) {
        configuracion = c;
    }

    public static ConfiguracionAccesibilidad configuracion() {
        return configuracion;
    }

    public static boolean altoContraste() {
        return configuracion.altoContraste();
    }

    public static Color fondoPanel() {
        return altoContraste() ? new Color(0, 0, 0, 245) : new Color(10, 10, 16, 200);
    }

    public static Color fondoTarjeta() {
        return altoContraste() ? Color.BLACK : new Color(26, 26, 34);
    }

    public static Color fondoBoton() {
        return altoContraste() ? new Color(30, 30, 30) : new Color(44, 44, 58);
    }

    public static Color borde() {
        return altoContraste() ? Color.WHITE : new Color(255, 255, 255, 40);
    }

    public static Color texto() {
        return altoContraste() ? Color.WHITE : new Color(232, 232, 240);
    }

    public static Color textoSuave() {
        return altoContraste() ? new Color(230, 230, 230) : new Color(150, 150, 168);
    }

    public static Color acento() {
        return altoContraste() ? new Color(255, 230, 0) : new Color(148, 120, 255);
    }

    public static Color positivo() {
        switch (configuracion.filtroColor()) {
            case 1:
                return new Color(90, 170, 255);
            case 2:
                return new Color(60, 210, 210);
            case 3:
                return new Color(235, 235, 235);
            default:
                return altoContraste() ? new Color(90, 255, 120) : new Color(96, 206, 128);
        }
    }

    public static Color negativo() {
        switch (configuracion.filtroColor()) {
            case 1:
                return new Color(255, 156, 40);
            case 2:
                return new Color(235, 60, 120);
            case 3:
                return new Color(140, 140, 140);
            default:
                return altoContraste() ? new Color(255, 90, 90) : new Color(232, 84, 84);
        }
    }

    public static Color advertencia() {
        return altoContraste() ? new Color(255, 230, 0) : new Color(240, 190, 70);
    }

    public static Color peligro() {
        return configuracion.filtroColor() == 1 ? new Color(255, 120, 0) : new Color(225, 30, 42);
    }

    public static float escalaTexto() {
        return altoContraste() ? 1.12f : 1f;
    }

    public static Font fuente(int estilo, float tam) {
        return obtener("SansSerif", estilo, tam * escalaTexto());
    }

    public static Font mono(int estilo, float tam) {
        return obtener("Monospaced", estilo, tam * escalaTexto());
    }

    public static Font serif(int estilo, float tam) {
        return obtener("Serif", estilo, tam * escalaTexto());
    }

    public static Font fija(String familia, int estilo, float tam) {
        return obtener(familia, estilo, tam);
    }

    private static Font obtener(String familia, int estilo, float tam) {
        String clave = familia + "|" + estilo + "|" + Math.round(tam * 10);
        Font f = FUENTES.obtener(clave);
        if (f == null) {
            f = new Font(familia, estilo, 12).deriveFont(estilo, tam);
            FUENTES.poner(clave, f);
        }
        return f;
    }

    public static Color conAlfa(Color c, float alfa) {
        int a = Math.max(0, Math.min(255, Math.round(alfa * 255)));
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), a);
    }
}
