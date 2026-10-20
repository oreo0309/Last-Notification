package juego.pantallas;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import juego.Juego;
import juego.interfaz.Dibujo;
import juego.interfaz.Estilo;

public class PantallaNombre extends Pantalla {

    private static final int MAXIMO = 14;

    private final StringBuilder nombre = new StringBuilder();
    private String error;
    private double tiempoError;

    public PantallaNombre(Juego juego) {
        super("PantallaNombre", juego);
    }

    @Override
    public void entrar() {
        String anterior = juego.jugador().nombre();
        if (anterior != null && !anterior.equals("Sebas")) {
            nombre.append(anterior);
        }
    }

    @Override
    public void actualizar(double dt) {
        tiempo += dt;
        if (tiempoError > 0) {
            tiempoError -= dt;
        }
    }

    @Override
    public void salir() {
    }

    public static String validar(String texto) {
        String limpio = texto.trim();
        if (limpio.length() < 2) {
            return "El nombre debe tener al menos 2 letras.";
        }
        if (limpio.length() > MAXIMO) {
            return "El nombre puede tener máximo " + MAXIMO + " caracteres.";
        }
        for (char c : limpio.toCharArray()) {
            if (!Character.isLetter(c) && c != ' ') {
                return "Usa solo letras y espacios (sin números ni símbolos).";
            }
        }
        return null;
    }

    @Override
    public void dibujar(Graphics2D g) {
        g.setColor(new Color(6, 6, 10));
        g.fillRect(0, 0, 1280, 720);
        Dibujo.suavizado(g);
        g.setFont(Estilo.fuente(Font.BOLD, 34));
        g.setColor(Estilo.texto());
        Dibujo.centrado(g, "¿Cómo te llamas?", 640, 170);
        g.setFont(Estilo.fuente(Font.PLAIN, 16));
        g.setColor(Estilo.textoSuave());
        Dibujo.centrado(g, "Tu nombre aparecerá en el juego... y alguien lo va a usar.", 640, 205);
        int x = 640 - 230;
        int y = 250;
        Dibujo.rectRedondo(g, x, y, 460, 64, 18, Estilo.fondoTarjeta());
        g.setColor(tiempoError > 0 ? Estilo.negativo() : Estilo.acento());
        g.setStroke(new BasicStroke(2f));
        g.drawRoundRect(x, y, 460, 64, 18, 18);
        g.setFont(Estilo.fuente(Font.BOLD, 28));
        g.setColor(Estilo.texto());
        String texto = nombre.toString();
        boolean cursor = ((int) (tiempo * 2)) % 2 == 0;
        Dibujo.centrado(g, texto + (cursor ? "|" : " "), 640, y + 43);
        if (tiempoError > 0 && error != null) {
            g.setFont(Estilo.fuente(Font.BOLD, 15));
            g.setColor(Estilo.negativo());
            Dibujo.centrado(g, error, 640, y + 96);
        }
        int py = 410;
        Dibujo.panel(g, 240, py, 800, 170, 18);
        g.setFont(Estilo.fuente(Font.BOLD, 16));
        g.setColor(Estilo.advertencia());
        g.drawString("Antes de empezar", 266, py + 34);
        g.setFont(Estilo.fuente(Font.PLAIN, 15));
        g.setColor(Estilo.texto());
        Dibujo.parrafo(g, "Este juego tiene sustos repentinos, oscuridad y sonidos inquietantes. Se juega mejor con audífonos. "
                + "Si prefieres una experiencia más tranquila, activa la reducción de sustos y destellos.", 266, py + 46, 748, 1.15f);
        g.setFont(Estilo.fuente(Font.BOLD, 15));
        boolean suave = juego.configuracion().sinJumpscares();
        g.setColor(suave ? Estilo.positivo() : Estilo.textoSuave());
        g.drawString("[F2] Reducción de sustos: " + (suave ? "ACTIVADA" : "desactivada")
                + "      [F3] Subtítulos: " + (juego.configuracion().subtitulos() ? "SÍ" : "NO"), 266, py + 146);
        g.setFont(Estilo.fuente(Font.PLAIN, 14));
        g.setColor(Estilo.textoSuave());
        Dibujo.centrado(g, "ENTER para comenzar  ·  ESC para volver", 640, 650);
    }

    @Override
    public void alPresionarTecla(int codigo, char caracter) {
        if (codigo == KeyEvent.VK_ESCAPE) {
            juego.cambiarPantalla(new PantallaMenu(juego));
            return;
        }
        if (codigo == KeyEvent.VK_ENTER) {
            String err = validar(nombre.toString());
            if (err != null) {
                error = err;
                tiempoError = 3;
                juego.gestorAudio().reproducir("error", 0.5f);
                return;
            }
            String limpio = nombre.toString().trim();
            limpio = limpio.substring(0, 1).toUpperCase() + limpio.substring(1);
            juego.gestorAudio().reproducir("menu_elegir", 0.4f);
            juego.nuevaPartida(limpio);
            return;
        }
        if (codigo == KeyEvent.VK_BACK_SPACE) {
            if (nombre.length() > 0) {
                nombre.setLength(nombre.length() - 1);
            }
            return;
        }
        if (codigo == KeyEvent.VK_F2 || codigo == KeyEvent.VK_F3) {
            alternarOpcion(codigo);
            return;
        }
        if (caracter != '\0' && !Character.isISOControl(caracter)) {
            if (nombre.length() >= MAXIMO) {
                error = "Máximo " + MAXIMO + " caracteres.";
                tiempoError = 2;
                return;
            }
            if (!Character.isLetter(caracter) && caracter != ' ') {
                error = "Solo se permiten letras y espacios.";
                tiempoError = 2;
                juego.gestorAudio().reproducir("error", 0.4f);
                return;
            }
            nombre.append(caracter);
        }
    }

    private void alternarOpcion(int codigo) {
        if (codigo == KeyEvent.VK_F2) {
            juego.configuracion().sinJumpscares(!juego.configuracion().sinJumpscares());
        } else {
            juego.configuracion().subtitulos(!juego.configuracion().subtitulos());
        }
        juego.configuracion().aplicar(juego);
        juego.guardarConfiguracion();
        juego.gestorAudio().reproducir("interruptor", 0.5f);
    }
}
