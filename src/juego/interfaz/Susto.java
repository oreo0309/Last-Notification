package juego.interfaz;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.Random;

public final class Susto {

    private static final int ANCHO = 1280;
    private static final int ALTO = 720;
    private static BufferedImage lienzo;

    private Susto() {
    }

    public static void dibujar(Graphics2D g, double progreso, String nombre, Random r) {
        if (lienzo == null) {
            lienzo = new BufferedImage(ANCHO, ALTO, BufferedImage.TYPE_INT_ARGB);
        }
        Graphics2D c = lienzo.createGraphics();
        c.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        c.setComposite(AlphaComposite.Src);
        c.setColor(new Color(0, 0, 0, 255));
        c.fillRect(0, 0, ANCHO, ALTO);
        c.setComposite(AlphaComposite.SrcOver);
        double llegada = Math.min(1, progreso / 0.18);
        double escala = 0.55 + llegada * 0.75 + progreso * 0.25;
        c.translate(ANCHO / 2.0 + (r.nextDouble() - 0.5) * 30 * (1 - progreso * 0.5),
                ALTO / 2.0 + 40 + (r.nextDouble() - 0.5) * 24 * (1 - progreso * 0.5));
        c.scale(escala, escala);
        c.rotate(0.12 + (r.nextDouble() - 0.5) * 0.05);
        cara(c, progreso, r);
        c.dispose();

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, ANCHO, ALTO);
        int franja = 0;
        while (franja < ALTO) {
            int alto = 8 + r.nextInt(60);
            int desp = r.nextDouble() < 0.35 ? (int) ((r.nextDouble() - 0.5) * 90) : 0;
            int y1 = Math.min(ALTO, franja + alto);
            g.drawImage(lienzo, desp, franja, ANCHO + desp, y1, 0, franja, ANCHO, y1, null);
            franja = y1;
        }
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f));
        g.drawImage(tenir(lienzo, true), -14, 0, null);
        g.drawImage(tenir(lienzo, false), 14, 0, null);
        g.setComposite(AlphaComposite.SrcOver);

        g.setPaint(new RadialGradientPaint(new Point2D.Double(ANCHO / 2.0, ALTO / 2.0), 760,
                new float[]{0.35f, 1f}, new Color[]{new Color(0, 0, 0, 0), new Color(90, 0, 8, 210)}));
        g.fillRect(0, 0, ANCHO, ALTO);
        for (int i = 0; i < 18; i++) {
            int y = r.nextInt(ALTO);
            g.setColor(new Color(255, 255, 255, r.nextInt(40)));
            g.fillRect(0, y, ANCHO, 1 + r.nextInt(3));
        }
        for (int i = 0; i < 1400; i++) {
            int v = r.nextInt(200);
            g.setColor(new Color(v, v, v, 70));
            g.fillRect(r.nextInt(ANCHO), r.nextInt(ALTO), 2, 2);
        }
        if (progreso > 0.3) {
            g.setFont(Estilo.fija("SansSerif", Font.BOLD, 46));
            String texto = "TE VEO, " + nombre.toUpperCase();
            int tw = g.getFontMetrics().stringWidth(texto);
            g.setColor(new Color(240, 240, 240));
            Dibujo.textoGlitch(g, texto, (ANCHO - tw) / 2 + (int) ((r.nextDouble() - 0.5) * 12), 92, 1.0, r);
        }
        if (progreso < 0.07) {
            g.setColor(new Color(200, 0, 20, (int) (200 * (1 - progreso / 0.07))));
            g.fillRect(0, 0, ANCHO, ALTO);
        }
    }

    private static BufferedImage tenido;

    private static BufferedImage tenir(BufferedImage origen, boolean rojo) {
        if (tenido == null) {
            tenido = new BufferedImage(ANCHO, ALTO, BufferedImage.TYPE_INT_ARGB);
        }
        Graphics2D t = tenido.createGraphics();
        t.setComposite(AlphaComposite.Src);
        t.drawImage(origen, 0, 0, null);
        t.setComposite(AlphaComposite.SrcAtop);
        t.setColor(rojo ? new Color(255, 0, 40, 160) : new Color(0, 220, 255, 140));
        t.fillRect(0, 0, ANCHO, ALTO);
        t.dispose();
        return tenido;
    }

    private static void cara(Graphics2D g, double progreso, Random r) {
        Path2D silueta = new Path2D.Double();
        silueta.moveTo(0, -350);
        silueta.curveTo(125, -350, 175, -230, 172, -70);
        silueta.curveTo(170, 60, 140, 200, 85, 330);
        silueta.curveTo(50, 400, -50, 400, -85, 330);
        silueta.curveTo(-140, 200, -170, 60, -172, -70);
        silueta.curveTo(-175, -230, -125, -350, 0, -350);
        silueta.closePath();
        g.setPaint(new RadialGradientPaint(new Point2D.Double(-10, -110), 430,
                new float[]{0f, 0.4f, 0.75f, 1f},
                new Color[]{new Color(168, 172, 176), new Color(122, 126, 132), new Color(46, 46, 54), new Color(4, 4, 6)}));
        g.fill(silueta);
        g.setPaint(new GradientPaint(-172, 0, new Color(0, 0, 0, 200), -60, 0, new Color(0, 0, 0, 0)));
        g.fill(silueta);
        g.setPaint(new GradientPaint(172, 0, new Color(0, 0, 0, 200), 60, 0, new Color(0, 0, 0, 0)));
        g.fill(silueta);
        g.setPaint(new GradientPaint(0, 120, new Color(0, 0, 0, 0), 0, 390, new Color(0, 0, 0, 230)));
        g.fill(silueta);
        sombra(g, -120, 120, 70, 140, 140);
        sombra(g, 120, 120, 70, 140, 140);
        g.setColor(new Color(70, 72, 80, 70));
        g.setStroke(new BasicStroke(1.6f));
        for (int i = 0; i < 9; i++) {
            double x0 = -60 + i * 15 + (r.nextDouble() - 0.5) * 4;
            g.drawLine((int) x0, -330, (int) (x0 + (r.nextDouble() - 0.5) * 30), -250 + r.nextInt(30));
        }
        double abrir = Math.min(1, progreso * 2.5);
        cuenca(g, -64, -52, 1.0 + abrir * 0.2, -0.18, r);
        cuenca(g, 66, -40, 0.92 + abrir * 0.2, 0.22, r);
        sombra(g, 0, 40, 26, 70, 120);
        g.setPaint(new RadialGradientPaint(new Point2D.Double(6, -232), 34,
                new float[]{0f, 0.3f, 1f}, new Color[]{new Color(255, 70, 80), new Color(210, 10, 26), new Color(210, 10, 26, 0)}));
        g.fill(new Ellipse2D.Double(-28, -266, 68, 68));
        g.setColor(new Color(255, 210, 210));
        g.fill(new Ellipse2D.Double(1, -237, 10, 10));
        double apertura = 6 + abrir * 85;
        Path2D boca = new Path2D.Double();
        boca.moveTo(-150, 178);
        boca.curveTo(-80, 168 - abrir * 6, 80, 168 - abrir * 6, 150, 182);
        boca.curveTo(90, 182 + apertura, -90, 182 + apertura, -150, 178);
        boca.closePath();
        g.setPaint(new RadialGradientPaint(new Point2D.Double(0, 178 + apertura * 0.4), (float) (150 + apertura),
                new float[]{0f, 0.6f, 1f}, new Color[]{new Color(2, 0, 2), new Color(18, 2, 6), new Color(40, 30, 36, 0)}));
        g.fill(new Ellipse2D.Double(-185, 140, 370, 80 + apertura * 1.4));
        g.setColor(new Color(3, 1, 3));
        g.fill(boca);
        g.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(10, 8, 12, 180));
        g.drawLine(-150, 178, -185, 150);
        g.drawLine(150, 182, 182, 156);
    }

    private static void sombra(Graphics2D g, double cx, double cy, double rx, double ry, int alfa) {
        g.setPaint(new RadialGradientPaint(new Point2D.Double(cx, cy), (float) Math.max(rx, ry),
                new float[]{0f, 1f}, new Color[]{new Color(0, 0, 0, alfa), new Color(0, 0, 0, 0)}));
        g.fill(new Ellipse2D.Double(cx - rx, cy - ry, rx * 2, ry * 2));
    }

    private static void cuenca(Graphics2D g, double cx, double cy, double escala, double inclinacion, Random r) {
        java.awt.geom.AffineTransform t = g.getTransform();
        g.translate(cx, cy);
        g.rotate(inclinacion);
        double w = 50 * escala;
        double h = 64 * escala;
        g.setPaint(new RadialGradientPaint(new Point2D.Double(0, 0), (float) (h * 1.6),
                new float[]{0f, 0.45f, 1f}, new Color[]{new Color(0, 0, 0, 250), new Color(10, 8, 12, 200), new Color(30, 30, 36, 0)}));
        g.fill(new Ellipse2D.Double(-w * 1.6, -h * 1.5, w * 3.2, h * 3.0));
        Path2D ojo = new Path2D.Double();
        ojo.moveTo(-w, 4);
        ojo.curveTo(-w * 0.5, -h * 0.75, w * 0.5, -h * 0.7, w, -2);
        ojo.curveTo(w * 0.55, h * 0.8, -w * 0.55, h * 0.75, -w, 4);
        ojo.closePath();
        g.setColor(new Color(1, 0, 2));
        g.fill(ojo);
        double px = (r.nextDouble() - 0.5) * 6;
        double py = -4 + (r.nextDouble() - 0.5) * 6;
        g.setPaint(new RadialGradientPaint(new Point2D.Double(px, py), 14,
                new float[]{0f, 1f}, new Color[]{new Color(255, 255, 255, 200), new Color(255, 255, 255, 0)}));
        g.fill(new Ellipse2D.Double(px - 14, py - 14, 28, 28));
        g.setColor(Color.WHITE);
        g.fill(new Ellipse2D.Double(px - 3, py - 3, 6, 6));
        g.setTransform(t);
    }

    public static void dibujarSuave(Graphics2D g, double progreso, String texto) {
        int alfa = (int) (Math.min(1, progreso * 1.5) * 200);
        g.setColor(new Color(60, 0, 8, alfa));
        g.fillRect(0, 0, ANCHO, ALTO);
        if (progreso > 0.3) {
            g.setFont(Estilo.fuente(Font.BOLD, 26));
            g.setColor(new Color(255, 255, 255, (int) (Math.min(1, (progreso - 0.3) * 2) * 255)));
            Dibujo.centrado(g, texto, ANCHO / 2, 370);
        }
    }
}
