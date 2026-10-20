package juego.pantallas;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import juego.interfaz.Estilo;

public final class Medalla {

    private Medalla() {
    }

    public static void dibujar(Graphics2D g, double cx, double cy, double radio, float alfa) {
        if (alfa <= 0) {
            return;
        }
        g.setPaint(new RadialGradientPaint(new Point2D.Double(cx - radio * 0.3, cy - radio * 0.3), (float) (radio * 1.4),
                new float[]{0f, 0.6f, 1f},
                new Color[]{Estilo.conAlfa(new Color(255, 240, 170), alfa), Estilo.conAlfa(new Color(232, 180, 60), alfa),
                    Estilo.conAlfa(new Color(150, 100, 20), alfa)}));
        g.fill(new Ellipse2D.Double(cx - radio, cy - radio, radio * 2, radio * 2));
        g.setColor(Estilo.conAlfa(new Color(120, 80, 10), alfa));
        g.setStroke(new BasicStroke((float) Math.max(1, radio * 0.1)));
        g.draw(new Ellipse2D.Double(cx - radio * 0.8, cy - radio * 0.8, radio * 1.6, radio * 1.6));
        Path2D estrella = new Path2D.Double();
        for (int i = 0; i < 10; i++) {
            double ang = -Math.PI / 2 + i * Math.PI / 5;
            double r = (i % 2 == 0) ? radio * 0.58 : radio * 0.25;
            double x = cx + Math.cos(ang) * r;
            double y = cy + Math.sin(ang) * r;
            if (i == 0) {
                estrella.moveTo(x, y);
            } else {
                estrella.lineTo(x, y);
            }
        }
        estrella.closePath();
        g.setColor(Estilo.conAlfa(new Color(255, 252, 230), alfa));
        g.fill(estrella);
    }
}
