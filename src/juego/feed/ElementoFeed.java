package juego.feed;

import estructuras.ListaEnlazada;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import juego.Decision;
import juego.interfaz.ContextoVisual;

public interface ElementoFeed {

    int mostrar(Graphics2D g, int x, int y, int ancho, ContextoVisual contexto);

    int medirAlto(Graphics2D g, int ancho, ContextoVisual contexto);

    ListaEnlazada<Decision> decisiones();

    ListaEnlazada<Rectangle> zonasDecision();

    boolean resuelto();

    void resolver(Decision decision);

    Decision decisionTomada();

    boolean visto();

    void marcarVisto();

    int impactoAnsiedad();

    String autor();
}
