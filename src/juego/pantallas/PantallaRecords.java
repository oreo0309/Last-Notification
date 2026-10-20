package juego.pantallas;

import estructuras.ArbolBinarioBusqueda;
import estructuras.ListaEnlazada;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import juego.Juego;
import juego.interfaz.Dibujo;
import juego.interfaz.Estilo;

public class PantallaRecords extends Pantalla {

    private ListaEnlazada<ArbolBinarioBusqueda.Par<Integer, String>> mejores;

    public PantallaRecords(Juego juego) {
        super("PantallaRecords", juego);
    }

    @Override
    public void entrar() {
        mejores = juego.registro().mejores(8);
    }

    @Override
    public void actualizar(double dt) {
        tiempo += dt;
    }

    @Override
    public void salir() {
    }

    @Override
    public void dibujar(Graphics2D g) {
        g.setColor(new Color(8, 8, 14));
        g.fillRect(0, 0, 1280, 720);
        Dibujo.suavizado(g);
        g.setFont(Estilo.fuente(Font.BOLD, 38));
        g.setColor(Estilo.texto());
        Dibujo.centrado(g, "Récords", 640, 110);
        g.setFont(Estilo.fuente(Font.PLAIN, 15));
        g.setColor(Estilo.textoSuave());
        Dibujo.centrado(g, "Ordenados con un árbol binario de búsqueda (recorrido de mayor a menor).", 640, 145);
        Dibujo.panel(g, 290, 180, 700, 440, 18);
        if (mejores.estaVacia()) {
            g.setFont(Estilo.fuente(Font.ITALIC, 18));
            g.setColor(Estilo.textoSuave());
            Dibujo.centrado(g, "Todavía no hay partidas terminadas. ¡Sé el primero!", 640, 400);
        }
        int y = 232;
        int puesto = 1;
        for (ArbolBinarioBusqueda.Par<Integer, String> p : mejores) {
            g.setFont(Estilo.fuente(Font.BOLD, 22));
            g.setColor(puesto == 1 ? Estilo.advertencia() : Estilo.acento());
            g.drawString(puesto + ".", 330, y);
            g.setColor(Estilo.texto());
            g.setFont(Estilo.fuente(Font.PLAIN, 20));
            g.drawString(p.valor(), 380, y);
            g.setFont(Estilo.mono(Font.BOLD, 22));
            String pts = String.valueOf(p.clave());
            g.drawString(pts, 950 - g.getFontMetrics().stringWidth(pts), y);
            y += 48;
            puesto++;
        }
        g.setFont(Estilo.fuente(Font.PLAIN, 14));
        g.setColor(Estilo.textoSuave());
        Dibujo.centrado(g, "ESC o ENTER para volver", 640, 680);
    }

    @Override
    public void alPresionarTecla(int codigo, char caracter) {
        if (codigo == KeyEvent.VK_ESCAPE || codigo == KeyEvent.VK_ENTER) {
            juego.desapilarPantalla();
        }
    }

    @Override
    public void alHacerClic(int x, int y, int boton) {
        juego.desapilarPantalla();
    }
}
