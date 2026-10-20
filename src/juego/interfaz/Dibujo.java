package juego.interfaz;

import estructuras.ListaEnlazada;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Random;

public final class Dibujo {

    private static final String GLIFOS = "#%&@?!/\\|_~*+=<>";

    private Dibujo() {
    }

    public static void suavizado(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    }

    public static ListaEnlazada<String> envolver(FontMetrics fm, String texto, int ancho) {
        ListaEnlazada<String> lineas = new ListaEnlazada<>();
        if (texto == null) {
            return lineas;
        }
        for (String parrafo : texto.split("\n", -1)) {
            String[] palabras = parrafo.split(" ");
            StringBuilder actual = new StringBuilder();
            for (String palabra : palabras) {
                String prueba = actual.length() == 0 ? palabra : actual + " " + palabra;
                if (fm.stringWidth(prueba) <= ancho || actual.length() == 0) {
                    actual.setLength(0);
                    actual.append(prueba);
                } else {
                    lineas.agregar(actual.toString());
                    actual.setLength(0);
                    actual.append(palabra);
                }
            }
            lineas.agregar(actual.toString());
        }
        return lineas;
    }

    public static int parrafo(Graphics2D g, String texto, int x, int y, int ancho, float interlineado) {
        FontMetrics fm = g.getFontMetrics();
        int alto = Math.round(fm.getHeight() * interlineado);
        int cy = y + fm.getAscent();
        int total = 0;
        for (String linea : envolver(fm, texto, ancho)) {
            g.drawString(linea, x, cy);
            cy += alto;
            total += alto;
        }
        return total;
    }

    public static int medirParrafo(Graphics2D g, Font fuente, String texto, int ancho, float interlineado) {
        FontMetrics fm = g.getFontMetrics(fuente);
        return envolver(fm, texto, ancho).tamano() * Math.round(fm.getHeight() * interlineado);
    }

    public static void centrado(Graphics2D g, String texto, int cx, int y) {
        FontMetrics fm = g.getFontMetrics();
        g.drawString(texto, cx - fm.stringWidth(texto) / 2, y);
    }

    public static int parrafoCentrado(Graphics2D g, String texto, int cx, int y, int ancho, float interlineado) {
        FontMetrics fm = g.getFontMetrics();
        int alto = Math.round(fm.getHeight() * interlineado);
        int cy = y;
        for (String linea : envolver(fm, texto, ancho)) {
            g.drawString(linea, cx - fm.stringWidth(linea) / 2, cy);
            cy += alto;
        }
        return cy - y;
    }

    public static void sombra(Graphics2D g, String texto, int x, int y, Color color) {
        Color c = g.getColor();
        g.setColor(new Color(0, 0, 0, 180));
        g.drawString(texto, x + 2, y + 2);
        g.setColor(color != null ? color : c);
        g.drawString(texto, x, y);
        g.setColor(c);
    }

    public static void panel(Graphics2D g, int x, int y, int w, int h, int radio) {
        g.setColor(Estilo.fondoPanel());
        g.fill(new RoundRectangle2D.Float(x, y, w, h, radio, radio));
        g.setColor(Estilo.borde());
        g.setStroke(new BasicStroke(Estilo.altoContraste() ? 2f : 1f));
        g.draw(new RoundRectangle2D.Float(x + 0.5f, y + 0.5f, w - 1, h - 1, radio, radio));
    }

    public static void rectRedondo(Graphics2D g, double x, double y, double w, double h, double r, Color relleno) {
        g.setColor(relleno);
        g.fill(new RoundRectangle2D.Double(x, y, w, h, r, r));
    }

    public static void barra(Graphics2D g, int x, int y, int w, int h, double valor, Color color) {
        valor = Math.max(0, Math.min(1, valor));
        g.setColor(Estilo.altoContraste() ? new Color(60, 60, 60) : new Color(255, 255, 255, 30));
        g.fill(new RoundRectangle2D.Float(x, y, w, h, h, h));
        g.setColor(color);
        g.fill(new RoundRectangle2D.Float(x, y, (float) (w * valor), h, h, h));
        if (Estilo.altoContraste()) {
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(1f));
            g.draw(new RoundRectangle2D.Float(x, y, w, h, h, h));
        }
    }

    public static String corromper(String texto, double nivel, long semilla) {
        if (nivel <= 0.01 || texto == null) {
            return texto;
        }
        Random r = new Random(semilla);
        StringBuilder sb = new StringBuilder(texto.length());
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            double p = r.nextDouble();
            if (c != ' ' && p < nivel * 0.22) {
                sb.append(GLIFOS.charAt(r.nextInt(GLIFOS.length())));
            } else if (p < nivel * 0.35) {
                sb.append(Character.isUpperCase(c) ? Character.toLowerCase(c) : Character.toUpperCase(c));
            } else if (p < nivel * 0.4 && Character.isLetter(c)) {
                sb.append(c).append(c);
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static void textoGlitch(Graphics2D g, String texto, int x, int y, double nivel, Random r) {
        Color original = g.getColor();
        if (nivel > 0.05) {
            int d = (int) Math.round(nivel * 4) + 1;
            g.setColor(new Color(255, 30, 60, 150));
            g.drawString(texto, x - d + r.nextInt(2), y);
            g.setColor(new Color(40, 220, 255, 150));
            g.drawString(texto, x + d - r.nextInt(2), y + (r.nextFloat() < nivel * 0.3 ? 1 : 0));
        }
        g.setColor(original);
        g.drawString(texto, x, y);
    }

    public static void corazon(Graphics2D g, double x, double y, double tam, Color color) {
        Path2D p = new Path2D.Double();
        double s = tam;
        p.moveTo(x + s / 2, y + s * 0.95);
        p.curveTo(x - s * 0.35, y + s * 0.35, x + s * 0.12, y - s * 0.25, x + s / 2, y + s * 0.22);
        p.curveTo(x + s * 0.88, y - s * 0.25, x + s * 1.35, y + s * 0.35, x + s / 2, y + s * 0.95);
        g.setColor(color);
        g.fill(p);
    }

    public static void burbuja(Graphics2D g, double x, double y, double tam, Color color) {
        g.setColor(color);
        g.setStroke(new BasicStroke(1.6f));
        g.draw(new RoundRectangle2D.Double(x, y, tam, tam * 0.75, tam * 0.4, tam * 0.4));
        Path2D cola = new Path2D.Double();
        cola.moveTo(x + tam * 0.25, y + tam * 0.72);
        cola.lineTo(x + tam * 0.15, y + tam);
        cola.lineTo(x + tam * 0.45, y + tam * 0.74);
        g.draw(cola);
    }

    public static void luna(Graphics2D g, double x, double y, double tam, Color color) {
        g.setColor(color);
        java.awt.geom.Area a = new java.awt.geom.Area(new Ellipse2D.Double(x, y, tam, tam));
        a.subtract(new java.awt.geom.Area(new Ellipse2D.Double(x + tam * 0.35, y - tam * 0.1, tam, tam)));
        g.fill(a);
    }

    public static void campana(Graphics2D g, double x, double y, double tam, Color color) {
        g.setColor(color);
        Path2D p = new Path2D.Double();
        p.moveTo(x + tam * 0.1, y + tam * 0.78);
        p.curveTo(x + tam * 0.22, y + tam * 0.6, x + tam * 0.16, y + tam * 0.1, x + tam * 0.5, y + tam * 0.1);
        p.curveTo(x + tam * 0.84, y + tam * 0.1, x + tam * 0.78, y + tam * 0.6, x + tam * 0.9, y + tam * 0.78);
        p.closePath();
        g.fill(p);
        g.fill(new Ellipse2D.Double(x + tam * 0.4, y + tam * 0.8, tam * 0.2, tam * 0.16));
    }

    public static void bateria(Graphics2D g, int x, int y, int w, int h, double nivel, Color color) {
        g.setColor(color);
        g.setStroke(new BasicStroke(1.4f));
        g.drawRoundRect(x, y, w, h, 3, 3);
        g.fillRect(x + w + 1, y + h / 3, 2, h / 3);
        Color relleno = nivel < 0.2 ? Estilo.peligro() : color;
        g.setColor(relleno);
        g.fillRect(x + 2, y + 2, (int) Math.max(0, (w - 3) * nivel), h - 3);
    }

    public static void wifi(Graphics2D g, double x, double y, double tam, Color color, boolean tachado) {
        g.setColor(color);
        g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < 3; i++) {
            double r = tam * (0.35 + i * 0.32);
            g.draw(new Arc2D.Double(x + tam / 2 - r, y + tam - r, r * 2, r * 2, 50, 80, Arc2D.OPEN));
        }
        g.fill(new Ellipse2D.Double(x + tam / 2 - 2, y + tam - 3, 4, 4));
        if (tachado) {
            g.setColor(Estilo.peligro());
            g.drawLine((int) x, (int) (y + tam), (int) (x + tam), (int) y);
        }
    }

    public static void ojo(Graphics2D g, double cx, double cy, double r, double mirarX, double mirarY) {
        g.setColor(new Color(225, 220, 210));
        g.fill(new Ellipse2D.Double(cx - r, cy - r * 0.6, r * 2, r * 1.2));
        g.setColor(new Color(150, 24, 30));
        double px = cx + mirarX * r * 0.35;
        double py = cy + mirarY * r * 0.2;
        g.fill(new Ellipse2D.Double(px - r * 0.45, py - r * 0.45, r * 0.9, r * 0.9));
        g.setColor(Color.BLACK);
        g.fill(new Ellipse2D.Double(px - r * 0.2, py - r * 0.2, r * 0.4, r * 0.4));
    }

    public static void logoEco(Graphics2D g, double x, double y, double tam, Color color) {
        g.setColor(color);
        g.setStroke(new BasicStroke((float) (tam * 0.09), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.fill(new Ellipse2D.Double(x + tam * 0.08, y + tam * 0.38, tam * 0.24, tam * 0.24));
        for (int i = 0; i < 3; i++) {
            double r = tam * (0.28 + i * 0.16);
            g.draw(new Arc2D.Double(x + tam * 0.2 - r, y + tam * 0.5 - r, r * 2, r * 2, -45, 90, Arc2D.OPEN));
        }
    }

    public static void avatar(Graphics2D g, int x, int y, int tam, String nombre, boolean ojo, double tiempo) {
        if (ojo) {
            g.setColor(new Color(8, 6, 8));
            g.fill(new Ellipse2D.Double(x, y, tam, tam));
            ojo(g, x + tam / 2.0, y + tam / 2.0, tam * 0.36, Math.sin(tiempo * 1.7), Math.cos(tiempo * 1.3));
            return;
        }
        int h = Math.abs(nombre.hashCode());
        Color c = Color.getHSBColor((h % 360) / 360f, 0.45f, Estilo.altoContraste() ? 0.9f : 0.7f);
        g.setColor(c);
        g.fill(new Ellipse2D.Double(x, y, tam, tam));
        String inicial = nombre.replace("@", "");
        inicial = inicial.isEmpty() ? "?" : inicial.substring(0, 1).toUpperCase();
        g.setColor(Color.WHITE);
        g.setFont(Estilo.fija("SansSerif", Font.BOLD, tam * 0.48f));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(inicial, x + (tam - fm.stringWidth(inicial)) / 2, y + (tam + fm.getAscent() - fm.getDescent()) / 2);
    }

    public static void check(Graphics2D g, double x, double y, double tam, Color color) {
        g.setColor(color);
        g.setStroke(new BasicStroke((float) (tam * 0.16), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Path2D p = new Path2D.Double();
        p.moveTo(x, y + tam * 0.55);
        p.lineTo(x + tam * 0.38, y + tam * 0.9);
        p.lineTo(x + tam, y + tam * 0.1);
        g.draw(p);
    }

    public static void cruz(Graphics2D g, double x, double y, double tam, Color color) {
        g.setColor(color);
        g.setStroke(new BasicStroke((float) (tam * 0.16), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine((int) x, (int) y, (int) (x + tam), (int) (y + tam));
        g.drawLine((int) (x + tam), (int) y, (int) x, (int) (y + tam));
    }

    public static void casilla(Graphics2D g, int x, int y, int tam, boolean marcada, Color color) {
        g.setColor(color);
        g.setStroke(new BasicStroke(1.6f));
        g.drawRoundRect(x, y, tam, tam, 4, 4);
        if (marcada) {
            check(g, x + 3, y + 3, tam - 6, Estilo.positivo());
        }
    }
}
