package juego.pantallas;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import juego.ConfiguracionAccesibilidad;
import juego.Juego;
import juego.interfaz.Dibujo;
import juego.interfaz.Estilo;

public class PantallaConfiguracion extends Pantalla {

    private static final String[] OPCIONES = {
        "Subtítulos", "Alto contraste", "Reducción de sustos y destellos", "Filtro de color",
        "Volumen", "Sensibilidad del ratón", "Brillo", "Pantalla completa", "Volver"
    };

    private static final String[] DESCRIPCIONES = {
        "Muestra en texto los sonidos importantes: pasos, susurros, golpes, vibraciones.",
        "Textos más grandes, fondos sólidos y bordes visibles. También aclara un poco la casa.",
        "Cambia los sustos repentinos por versiones suaves y reduce los destellos de los relámpagos.",
        "Ajusta los colores de la interfaz para distintos tipos de daltonismo, o muestra el mundo en grises.",
        "Volumen general del juego.",
        "Qué tan rápido gira la cámara con el ratón.",
        "Ilumina la imagen si tu pantalla es oscura.",
        "Ocupa toda la pantalla (también con F11).",
        "Guarda los cambios y regresa."
    };

    private int seleccion;
    private final Rectangle[] zonas = new Rectangle[OPCIONES.length];

    public PantallaConfiguracion(Juego juego) {
        super("PantallaConfiguracion", juego);
    }

    @Override
    public void entrar() {
    }

    @Override
    public void actualizar(double dt) {
        tiempo += dt;
        int mx = juego.entrada().ratonX();
        int my = juego.entrada().ratonY();
        for (int i = 0; i < zonas.length; i++) {
            if (zonas[i] != null && zonas[i].contains(mx, my)) {
                seleccion = i;
            }
        }
    }

    @Override
    public void salir() {
        juego.guardarConfiguracion();
    }

    private String valor(int i) {
        ConfiguracionAccesibilidad c = juego.configuracion();
        switch (i) {
            case 0:
                return c.subtitulos() ? "Sí" : "No";
            case 1:
                return c.altoContraste() ? "Sí" : "No";
            case 2:
                return c.sinJumpscares() ? "Sí" : "No";
            case 3:
                return ConfiguracionAccesibilidad.FILTROS[c.filtroColor()];
            case 4:
                return Math.round(c.volumen() * 100) + "%";
            case 5:
                return String.format("%.1f", c.sensibilidad());
            case 6:
                return String.format("%.1f", c.brillo());
            case 7:
                return c.pantallaCompleta() ? "Sí" : "No";
            default:
                return "";
        }
    }

    private void cambiar(int i, int direccion) {
        ConfiguracionAccesibilidad c = juego.configuracion();
        switch (i) {
            case 0:
                c.subtitulos(!c.subtitulos());
                break;
            case 1:
                c.altoContraste(!c.altoContraste());
                break;
            case 2:
                c.sinJumpscares(!c.sinJumpscares());
                break;
            case 3:
                c.filtroColor(c.filtroColor() + (direccion == 0 ? 1 : direccion));
                break;
            case 4:
                c.volumen(c.volumen() + (direccion == 0 ? 0.1f : direccion * 0.1f));
                break;
            case 5:
                c.sensibilidad(c.sensibilidad() + (direccion == 0 ? 0.1 : direccion * 0.1));
                break;
            case 6:
                c.brillo(c.brillo() + (direccion == 0 ? 0.1f : direccion * 0.1f));
                break;
            case 7:
                c.pantallaCompleta(!c.pantallaCompleta());
                break;
            default:
                juego.desapilarPantalla();
                return;
        }
        c.aplicar(juego);
        juego.gestorAudio().reproducir("menu_mover", 0.4f);
    }

    @Override
    public void dibujar(Graphics2D g) {
        g.setColor(new Color(8, 8, 14));
        g.fillRect(0, 0, 1280, 720);
        Dibujo.suavizado(g);
        g.setFont(Estilo.fuente(Font.BOLD, 36));
        g.setColor(Estilo.texto());
        g.drawString("Configuración y accesibilidad", 90, 90);
        int y = 150;
        for (int i = 0; i < OPCIONES.length; i++) {
            boolean sel = i == seleccion;
            zonas[i] = new Rectangle(80, y - 30, 760, 44);
            if (sel) {
                Dibujo.rectRedondo(g, 80, y - 30, 760, 44, 12, Estilo.conAlfa(Estilo.acento(), 0.25f));
            }
            g.setFont(Estilo.fuente(sel ? Font.BOLD : Font.PLAIN, 21));
            g.setColor(sel ? Color.WHITE : Estilo.textoSuave());
            g.drawString(OPCIONES[i], 100, y);
            String v = valor(i);
            if (!v.isEmpty()) {
                g.setColor(sel ? Estilo.acento() : Estilo.texto());
                String texto = sel ? "◀  " + v + "  ▶" : v;
                g.drawString(texto, 820 - g.getFontMetrics().stringWidth(texto), y);
            }
            if (i == 4) {
                Dibujo.barra(g, 520, y - 12, 120, 8, juego.configuracion().volumen(), Estilo.acento());
            }
            y += 52;
        }
        Dibujo.panel(g, 880, 120, 330, 300, 16);
        g.setFont(Estilo.fuente(Font.PLAIN, 16));
        g.setColor(Estilo.texto());
        Dibujo.parrafo(g, DESCRIPCIONES[seleccion], 900, 140, 290, 1.25f);
        if (seleccion == 3) {
            g.setFont(Estilo.fuente(Font.BOLD, 15));
            Dibujo.rectRedondo(g, 900, 300, 130, 40, 10, Estilo.positivo());
            Dibujo.rectRedondo(g, 1050, 300, 130, 40, 10, Estilo.negativo());
            g.setColor(Color.BLACK);
            Dibujo.centrado(g, "+ positivo", 965, 326);
            Dibujo.centrado(g, "− negativo", 1115, 326);
        }
        g.setFont(Estilo.fuente(Font.PLAIN, 14));
        g.setColor(Estilo.textoSuave());
        Dibujo.centrado(g, "↑/↓ elegir  ·  ← / → o ENTER cambiar  ·  ESC volver", 640, 690);
    }

    @Override
    public void alPresionarTecla(int codigo, char caracter) {
        switch (codigo) {
            case KeyEvent.VK_UP:
            case KeyEvent.VK_W:
                seleccion = Math.floorMod(seleccion - 1, OPCIONES.length);
                break;
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_S:
                seleccion = Math.floorMod(seleccion + 1, OPCIONES.length);
                break;
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
                if (seleccion < OPCIONES.length - 1) {
                    cambiar(seleccion, -1);
                }
                break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
                if (seleccion < OPCIONES.length - 1) {
                    cambiar(seleccion, 1);
                }
                break;
            case KeyEvent.VK_ENTER:
            case KeyEvent.VK_SPACE:
                cambiar(seleccion, 0);
                break;
            case KeyEvent.VK_ESCAPE:
                juego.desapilarPantalla();
                break;
            default:
                break;
        }
    }

    @Override
    public void alHacerClic(int x, int y, int boton) {
        for (int i = 0; i < zonas.length; i++) {
            if (zonas[i] != null && zonas[i].contains(x, y)) {
                seleccion = i;
                cambiar(i, boton == 3 ? -1 : 0);
                return;
            }
        }
    }
}
