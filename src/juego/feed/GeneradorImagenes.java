package juego.feed;

import estructuras.TablaHash;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.Random;

public class GeneradorImagenes {

    public static final int ANCHO = 240;
    public static final int ALTO = 150;

    private final TablaHash<String, BufferedImage> cache = new TablaHash<>();

    public BufferedImage obtener(String clave) {
        BufferedImage img = cache.obtener(clave);
        if (img == null) {
            img = generar(clave);
            cache.poner(clave, img);
        }
        return img;
    }

    private BufferedImage generar(String clave) {
        BufferedImage img = new BufferedImage(ANCHO, ALTO, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        Random r = new Random(clave.hashCode());
        switch (clave) {
            case "playa":
                playa(g, r);
                break;
            case "fiesta":
                fiesta(g, r);
                break;
            case "cancha":
                cancha(g);
                break;
            case "dibujo":
                dibujo(g, r);
                break;
            case "skate":
                skate(g);
                break;
            case "flores":
                flores(g, r);
                break;
            case "gato":
                gato(g);
                break;
            case "meme":
                meme(g);
                break;
            case "reto":
                reto(g, r);
                break;
            case "bienestar":
                bienestar(g, r);
                break;
            case "noticia":
                noticia(g, r);
                break;
            case "premio":
                premio(g, r);
                break;
            case "notas":
                notas(g);
                break;
            case "amanecer_gym":
                amanecerGym(g);
                break;
            case "oscura":
                oscura(g, r, "sigues aquí");
                break;
            case "oscura_nombre":
                oscura(g, r, "te veo");
                break;
            case "estatica":
                estatica(img, r);
                break;
            case "ojo":
                ojo(g);
                break;
            default:
                g.setPaint(new GradientPaint(0, 0, new Color(40, 40, 60), 0, ALTO, new Color(10, 10, 20)));
                g.fillRect(0, 0, ANCHO, ALTO);
                break;
        }
        g.dispose();
        return img;
    }

    private void cielo(Graphics2D g, Color arriba, Color abajo, int alto) {
        g.setPaint(new GradientPaint(0, 0, arriba, 0, alto, abajo));
        g.fillRect(0, 0, ANCHO, alto);
    }

    private void playa(Graphics2D g, Random r) {
        cielo(g, new Color(255, 140, 90), new Color(255, 210, 140), 90);
        g.setColor(new Color(255, 236, 170));
        g.fill(new Ellipse2D.Double(150, 52, 46, 46));
        g.setPaint(new GradientPaint(0, 88, new Color(40, 110, 170), 0, 118, new Color(20, 70, 130)));
        g.fillRect(0, 88, ANCHO, 30);
        g.setColor(new Color(255, 220, 160, 120));
        for (int i = 0; i < 8; i++) {
            g.fillRect(150 + r.nextInt(40), 92 + i * 3, 10 + r.nextInt(30), 1);
        }
        g.setColor(new Color(236, 206, 150));
        g.fillRect(0, 118, ANCHO, 32);
        g.setColor(new Color(40, 30, 30));
        g.fill(new Ellipse2D.Double(52, 92, 12, 12));
        g.fillRoundRect(50, 102, 16, 26, 6, 6);
        g.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(52, 108, 40, 96);
        g.drawLine(64, 108, 76, 96);
        g.drawLine(54, 126, 50, 142);
        g.drawLine(62, 126, 66, 142);
        g.setColor(new Color(30, 60, 40));
        g.fillRect(200, 60, 4, 60);
        for (int i = 0; i < 5; i++) {
            Path2D hoja = new Path2D.Double();
            hoja.moveTo(202, 60);
            hoja.quadTo(202 + Math.cos(i * 1.2) * 30, 40 + i * 2, 202 + Math.cos(i * 1.2) * 36, 64 + i * 3);
            g.setStroke(new BasicStroke(3));
            g.draw(hoja);
        }
    }

    private void fiesta(Graphics2D g, Random r) {
        g.setColor(new Color(16, 10, 30));
        g.fillRect(0, 0, ANCHO, ALTO);
        Color[] luces = {new Color(255, 60, 160), new Color(60, 200, 255), new Color(255, 220, 60), new Color(140, 80, 255)};
        for (int i = 0; i < 40; i++) {
            Color c = luces[r.nextInt(luces.length)];
            int t = 6 + r.nextInt(24);
            g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 60 + r.nextInt(100)));
            g.fill(new Ellipse2D.Double(r.nextInt(ANCHO), r.nextInt(90), t, t));
        }
        g.setColor(new Color(6, 4, 10));
        for (int i = 0; i < 9; i++) {
            int x = 10 + i * 26 + r.nextInt(8);
            int y = 92 + r.nextInt(16);
            g.fill(new Ellipse2D.Double(x, y, 16, 16));
            g.fillRoundRect(x - 4, y + 14, 24, 50, 10, 10);
            if (r.nextBoolean()) {
                g.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.drawLine(x + 2, y + 20, x - 6, y - 4);
            }
        }
    }

    private void cancha(Graphics2D g) {
        cielo(g, new Color(110, 170, 230), new Color(190, 220, 245), 60);
        g.setColor(new Color(60, 140, 70));
        g.fillRect(0, 60, ANCHO, 90);
        g.setColor(new Color(230, 240, 230));
        g.setStroke(new BasicStroke(2));
        g.drawLine(0, 100, ANCHO, 100);
        g.drawOval(90, 82, 60, 36);
        g.setColor(Color.WHITE);
        g.fillRect(10, 64, 4, 30);
        g.fillRect(10, 64, 40, 4);
        g.fillRect(46, 64, 4, 30);
        g.setColor(Color.WHITE);
        g.fill(new Ellipse2D.Double(160, 118, 16, 16));
        g.setColor(Color.BLACK);
        g.fill(new Ellipse2D.Double(165, 123, 6, 6));
    }

    private void dibujo(Graphics2D g, Random r) {
        g.setColor(new Color(236, 230, 214));
        g.fillRect(0, 0, ANCHO, ALTO);
        g.setColor(new Color(60, 60, 70));
        g.setStroke(new BasicStroke(1.4f));
        g.drawOval(80, 26, 80, 96);
        g.drawLine(100, 64, 112, 64);
        g.drawLine(128, 64, 140, 64);
        g.drawArc(104, 84, 32, 14, 200, 140);
        for (int i = 0; i < 60; i++) {
            int x = 60 + r.nextInt(120);
            g.setColor(new Color(60, 60, 70, 40));
            g.drawLine(x, 20 + r.nextInt(20), x + r.nextInt(30) - 15, 10 + r.nextInt(20));
        }
        g.setColor(new Color(200, 80, 90, 120));
        g.fill(new Ellipse2D.Double(92, 76, 14, 8));
        g.fill(new Ellipse2D.Double(134, 76, 14, 8));
    }

    private void skate(Graphics2D g) {
        cielo(g, new Color(150, 170, 190), new Color(200, 200, 200), 90);
        g.setColor(new Color(110, 110, 116));
        g.fillRect(0, 90, ANCHO, 60);
        g.setColor(new Color(90, 90, 96));
        Path2D rampa = new Path2D.Double();
        rampa.moveTo(140, 140);
        rampa.quadTo(200, 140, 230, 60);
        rampa.lineTo(240, 60);
        rampa.lineTo(240, 150);
        rampa.lineTo(140, 150);
        rampa.closePath();
        g.fill(rampa);
        g.setColor(new Color(30, 30, 40));
        g.fill(new Ellipse2D.Double(70, 40, 14, 14));
        g.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(77, 54, 80, 80);
        g.drawLine(80, 80, 66, 96);
        g.drawLine(80, 80, 96, 92);
        g.drawLine(78, 60, 60, 50);
        g.drawLine(78, 60, 98, 56);
        g.setColor(new Color(200, 60, 40));
        g.fillRoundRect(56, 98, 50, 6, 4, 4);
    }

    private void flores(Graphics2D g, Random r) {
        cielo(g, new Color(230, 240, 220), new Color(200, 225, 190), ALTO);
        for (int i = 0; i < 9; i++) {
            int x = 20 + r.nextInt(200);
            int y = 30 + r.nextInt(80);
            g.setColor(new Color(60, 120, 60));
            g.setStroke(new BasicStroke(2));
            g.drawLine(x, y, x + r.nextInt(10) - 5, 150);
            Color c = Color.getHSBColor(r.nextFloat(), 0.55f, 0.95f);
            g.setColor(c);
            for (int p = 0; p < 6; p++) {
                double a = p * Math.PI / 3;
                g.fill(new Ellipse2D.Double(x + Math.cos(a) * 7 - 5, y + Math.sin(a) * 7 - 5, 10, 10));
            }
            g.setColor(new Color(250, 210, 60));
            g.fill(new Ellipse2D.Double(x - 4, y - 4, 8, 8));
        }
    }

    private void gato(Graphics2D g) {
        g.setColor(new Color(24, 26, 40));
        g.fillRect(0, 0, ANCHO, ALTO);
        g.setColor(new Color(60, 70, 110));
        g.fillRect(40, 10, 160, 100);
        g.setColor(new Color(24, 26, 40));
        g.fillRect(118, 10, 4, 100);
        g.fillRect(40, 58, 160, 4);
        g.setColor(new Color(220, 220, 200));
        g.fill(new Ellipse2D.Double(150, 22, 18, 18));
        g.setColor(new Color(12, 12, 16));
        g.fill(new Ellipse2D.Double(90, 70, 60, 56));
        g.fill(new Ellipse2D.Double(98, 46, 44, 38));
        g.fillPolygon(new Polygon(new int[]{100, 108, 112}, new int[]{54, 30, 50}, 3));
        g.fillPolygon(new Polygon(new int[]{140, 132, 128}, new int[]{54, 30, 50}, 3));
        g.setColor(new Color(230, 210, 60));
        g.fill(new Ellipse2D.Double(108, 60, 9, 6));
        g.fill(new Ellipse2D.Double(124, 60, 9, 6));
        g.setColor(Color.BLACK);
        g.fillRect(111, 60, 2, 6);
        g.fillRect(127, 60, 2, 6);
        g.setColor(new Color(50, 40, 34));
        g.fillRect(0, 124, ANCHO, 26);
    }

    private void meme(Graphics2D g) {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, ANCHO, ALTO);
        g.setColor(new Color(20, 20, 30));
        g.fillRect(4, 4, 114, 142);
        g.fillRect(122, 4, 114, 142);
        g.setColor(new Color(130, 170, 230));
        g.fillRect(20, 50, 40, 60);
        g.setColor(new Color(250, 220, 190));
        g.fill(new Ellipse2D.Double(64, 70, 30, 30));
        g.setColor(Color.BLACK);
        g.fillRect(72, 82, 3, 3);
        g.fillRect(84, 82, 3, 3);
        g.setColor(new Color(250, 220, 190));
        g.fill(new Ellipse2D.Double(162, 64, 34, 34));
        g.setColor(Color.BLACK);
        g.fillRect(168, 76, 8, 2);
        g.fillRect(182, 76, 8, 2);
        g.setColor(new Color(80, 0, 0));
        g.fillRect(174, 88, 10, 3);
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        g.setColor(Color.WHITE);
        g.drawString("23:00", 40, 30);
        g.drawString("3:00 a. m.", 146, 30);
        g.setColor(new Color(110, 180, 255));
        g.fillRect(150, 100, 60, 40);
    }

    private void reto(Graphics2D g, Random r) {
        g.setColor(new Color(10, 10, 12));
        g.fillRect(0, 0, ANCHO, ALTO);
        for (int i = 0; i < 300; i++) {
            g.setColor(new Color(255, 255, 255, r.nextInt(30)));
            g.fillRect(r.nextInt(ANCHO), r.nextInt(ALTO), 1, 1);
        }
        g.setFont(new Font("SansSerif", Font.BOLD, 26));
        g.setColor(new Color(255, 40, 50));
        g.drawString("#RetoNoDormir", 22, 62);
        g.setFont(new Font("Monospaced", Font.BOLD, 40));
        g.setColor(Color.WHITE);
        g.drawString("48:00:00", 24, 112);
        g.setColor(new Color(255, 40, 50));
        g.fill(new Ellipse2D.Double(206, 16, 14, 14));
    }

    private void bienestar(Graphics2D g, Random r) {
        cielo(g, new Color(30, 40, 90), new Color(110, 90, 160), ALTO);
        for (int i = 0; i < 40; i++) {
            g.setColor(new Color(255, 255, 255, 60 + r.nextInt(150)));
            g.fillRect(r.nextInt(ANCHO), r.nextInt(100), 1, 1);
        }
        g.setColor(new Color(250, 240, 210));
        g.fill(new Ellipse2D.Double(160, 24, 40, 40));
        g.setColor(new Color(42, 50, 100));
        g.fill(new Ellipse2D.Double(172, 18, 40, 40));
        g.setFont(new Font("SansSerif", Font.BOLD, 20));
        g.setColor(Color.WHITE);
        g.drawString("Desconéctate.", 22, 110);
        g.setFont(new Font("SansSerif", Font.PLAIN, 14));
        g.drawString("Tu descanso también importa.", 22, 132);
    }

    private void noticia(Graphics2D g, Random r) {
        g.setColor(new Color(170, 20, 20));
        g.fillRect(0, 0, ANCHO, ALTO);
        g.setColor(new Color(255, 230, 0));
        g.fillRect(0, 100, ANCHO, 30);
        g.setFont(new Font("SansSerif", Font.BOLD, 28));
        g.setColor(Color.WHITE);
        g.drawString("¡¡URGENTE!!", 28, 60);
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        g.setColor(Color.BLACK);
        g.drawString("NO VAS A CREER LO QUE PASÓ", 14, 120);
        g.setColor(Color.WHITE);
        Polygon play = new Polygon(new int[]{108, 108, 136}, new int[]{70, 94, 82}, 3);
        g.fill(play);
    }

    private void premio(Graphics2D g, Random r) {
        cielo(g, new Color(255, 200, 40), new Color(255, 120, 20), ALTO);
        g.setColor(new Color(30, 30, 40));
        g.fillRoundRect(92, 20, 56, 104, 14, 14);
        g.setColor(new Color(110, 200, 255));
        g.fillRect(98, 30, 44, 82);
        g.setFont(new Font("SansSerif", Font.BOLD, 18));
        g.setColor(Color.WHITE);
        g.drawString("¡GANASTE!", 10, 40);
        g.drawString("CLIC AQUÍ", 152, 130);
        for (int i = 0; i < 16; i++) {
            g.setColor(Color.getHSBColor(r.nextFloat(), 0.8f, 1f));
            g.fillRect(r.nextInt(ANCHO), r.nextInt(ALTO), 4, 8);
        }
    }

    private void notas(Graphics2D g) {
        g.setColor(new Color(245, 245, 240));
        g.fillRect(0, 0, ANCHO, ALTO);
        g.setColor(new Color(40, 60, 120));
        g.setFont(new Font("SansSerif", Font.BOLD, 13));
        String[] materias = {"Matemáticas", "Física", "Español", "Química", "Inglés"};
        for (int i = 0; i < materias.length; i++) {
            g.drawString(materias[i], 20, 30 + i * 24);
            g.setColor(new Color(30, 150, 60));
            g.drawString("5.0", 190, 30 + i * 24);
            g.setColor(new Color(40, 60, 120));
        }
    }

    private void amanecerGym(Graphics2D g) {
        cielo(g, new Color(30, 30, 60), new Color(240, 150, 90), ALTO);
        g.setColor(new Color(20, 20, 26));
        g.fillRect(0, 110, ANCHO, 40);
        g.fillRect(60, 80, 120, 8);
        g.fill(new Ellipse2D.Double(50, 70, 20, 28));
        g.fill(new Ellipse2D.Double(170, 70, 20, 28));
        g.setFont(new Font("Monospaced", Font.BOLD, 22));
        g.setColor(Color.WHITE);
        g.drawString("05:00", 160, 30);
    }

    private void oscura(Graphics2D g, Random r, String texto) {
        g.setColor(new Color(4, 4, 6));
        g.fillRect(0, 0, ANCHO, ALTO);
        for (int i = 0; i < 600; i++) {
            int v = r.nextInt(40);
            g.setColor(new Color(v, v, v));
            g.fillRect(r.nextInt(ANCHO), r.nextInt(ALTO), 1, 1);
        }
        g.setFont(new Font("Serif", Font.ITALIC, 22));
        g.setColor(new Color(200, 200, 200));
        g.drawString(texto, (ANCHO - g.getFontMetrics().stringWidth(texto)) / 2, 84);
    }

    private void estatica(BufferedImage img, Random r) {
        for (int y = 0; y < ALTO; y++) {
            for (int x = 0; x < ANCHO; x++) {
                int v = r.nextInt(200);
                img.setRGB(x, y, (v << 16) | (v << 8) | v);
            }
        }
    }

    private void ojo(Graphics2D g) {
        g.setColor(new Color(6, 4, 6));
        g.fillRect(0, 0, ANCHO, ALTO);
        g.setColor(new Color(220, 214, 200));
        g.fill(new Ellipse2D.Double(50, 40, 140, 70));
        g.setColor(new Color(140, 20, 26));
        g.fill(new Ellipse2D.Double(92, 42, 56, 56));
        g.setColor(Color.BLACK);
        g.fill(new Ellipse2D.Double(108, 58, 24, 24));
        g.setColor(new Color(160, 30, 30));
        g.setStroke(new BasicStroke(1));
        for (int i = 0; i < 10; i++) {
            g.drawLine(54 + i * 3, 74, 80 + i * 2, 60 + i);
            g.drawLine(186 - i * 3, 74, 160 - i * 2, 60 + i);
        }
    }

    public static BufferedImage estaticaViva(Random r) {
        BufferedImage img = new BufferedImage(ANCHO / 2, ALTO / 2, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int v = r.nextInt(180);
                img.setRGB(x, y, (v << 16) | (v << 8) | v);
            }
        }
        return img;
    }
}
