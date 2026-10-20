package juego;

import estructuras.TablaHash;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfiguracionAccesibilidad {

    public static final String[] FILTROS = {"Ninguno", "Protanopía / Deuteranopía", "Tritanopía", "Escala de grises"};

    private boolean altoContraste;
    private boolean sinJumpscares;
    private boolean subtitulos = true;
    private int filtroColor;
    private float volumen = 0.8f;
    private double sensibilidad = 1.0;
    private float brillo = 1.15f;
    private boolean pantallaCompleta;

    public void aplicar(Juego juego) {
        juego.gestorAudio().volumen(volumen);
        juego.mundo().reducirDestellos(sinJumpscares);
        juego.mundo().efectos().escalaGrises = filtroColor == 3;
        juego.mundo().efectos().brillo = brillo;
        juego.mundo().iluminacion().ambiente = altoContraste ? 0.09f : 0.035f;
        juego.mundo().iluminacion().combinar();
        juego.ventana().pantallaCompleta(pantallaCompleta);
    }

    public void cargar(Path archivo) {
        if (!Files.exists(archivo)) {
            return;
        }
        TablaHash<String, String> valores = new TablaHash<>();
        try (BufferedReader lector = Files.newBufferedReader(archivo, StandardCharsets.UTF_8)) {
            String linea;
            while ((linea = lector.readLine()) != null) {
                int igual = linea.indexOf('=');
                if (igual <= 0) {
                    continue;
                }
                valores.poner(linea.substring(0, igual).trim(), linea.substring(igual + 1).trim());
            }
        } catch (IOException e) {
            return;
        }
        altoContraste = leerBooleano(valores, "altoContraste", altoContraste);
        sinJumpscares = leerBooleano(valores, "sinJumpscares", sinJumpscares);
        subtitulos = leerBooleano(valores, "subtitulos", subtitulos);
        pantallaCompleta = leerBooleano(valores, "pantallaCompleta", pantallaCompleta);
        filtroColor = (int) leerNumero(valores, "filtroColor", filtroColor, 0, FILTROS.length - 1);
        volumen = (float) leerNumero(valores, "volumen", volumen, 0, 1);
        sensibilidad = leerNumero(valores, "sensibilidad", sensibilidad, 0.2, 3);
        brillo = (float) leerNumero(valores, "brillo", brillo, 0.6, 1.8);
    }

    public boolean guardar(Path archivo) {
        try {
            if (archivo.getParent() != null) {
                Files.createDirectories(archivo.getParent());
            }
            try (BufferedWriter escritor = Files.newBufferedWriter(archivo, StandardCharsets.UTF_8)) {
                escritor.write("altoContraste=" + altoContraste);
                escritor.newLine();
                escritor.write("sinJumpscares=" + sinJumpscares);
                escritor.newLine();
                escritor.write("subtitulos=" + subtitulos);
                escritor.newLine();
                escritor.write("filtroColor=" + filtroColor);
                escritor.newLine();
                escritor.write("volumen=" + volumen);
                escritor.newLine();
                escritor.write("sensibilidad=" + sensibilidad);
                escritor.newLine();
                escritor.write("brillo=" + brillo);
                escritor.newLine();
                escritor.write("pantallaCompleta=" + pantallaCompleta);
                escritor.newLine();
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static boolean leerBooleano(TablaHash<String, String> valores, String clave, boolean defecto) {
        String v = valores.obtener(clave);
        if (v == null) {
            return defecto;
        }
        if (v.equalsIgnoreCase("true")) {
            return true;
        }
        if (v.equalsIgnoreCase("false")) {
            return false;
        }
        return defecto;
    }

    private static double leerNumero(TablaHash<String, String> valores, String clave, double defecto, double min, double max) {
        String v = valores.obtener(clave);
        if (v == null) {
            return defecto;
        }
        try {
            double d = Double.parseDouble(v);
            if (Double.isNaN(d)) {
                return defecto;
            }
            return Math.max(min, Math.min(max, d));
        } catch (NumberFormatException e) {
            return defecto;
        }
    }

    public boolean altoContraste() {
        return altoContraste;
    }

    public void altoContraste(boolean v) {
        altoContraste = v;
    }

    public boolean sinJumpscares() {
        return sinJumpscares;
    }

    public void sinJumpscares(boolean v) {
        sinJumpscares = v;
    }

    public boolean subtitulos() {
        return subtitulos;
    }

    public void subtitulos(boolean v) {
        subtitulos = v;
    }

    public int filtroColor() {
        return filtroColor;
    }

    public void filtroColor(int v) {
        filtroColor = Math.floorMod(v, FILTROS.length);
    }

    public float volumen() {
        return volumen;
    }

    public void volumen(float v) {
        volumen = Math.max(0f, Math.min(1f, v));
    }

    public double sensibilidad() {
        return sensibilidad;
    }

    public void sensibilidad(double v) {
        sensibilidad = Math.max(0.2, Math.min(3, v));
    }

    public float brillo() {
        return brillo;
    }

    public void brillo(float v) {
        brillo = Math.max(0.6f, Math.min(1.8f, v));
    }

    public boolean pantallaCompleta() {
        return pantallaCompleta;
    }

    public void pantallaCompleta(boolean v) {
        pantallaCompleta = v;
    }
}
