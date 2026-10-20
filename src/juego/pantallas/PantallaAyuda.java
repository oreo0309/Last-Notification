package juego.pantallas;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import juego.Juego;
import juego.interfaz.Dibujo;
import juego.interfaz.Estilo;

public class PantallaAyuda extends Pantalla {

    private static final String[] TITULOS = {"¿De qué se trata?", "Controles", "Tu bienestar", "El Seguidor", "Consejos reales", "Créditos"};

    private static final String[][] CONTENIDO = {
        {
            "Eres un adolescente que pasa tres noches solo en casa. Tu mamá trabaja de noche en el hospital.",
            "Cada noche, tu teléfono te ofrece publicaciones, mensajes y notificaciones de \"eco\", una red social ficticia. "
            + "Cada decisión que tomas cambia tu sueño, autoestima, ansiedad, concentración y relaciones.",
            "Si caes en hábitos poco saludables (seguir desplazándote, compararte, responder con rabia), la tensión sube, "
            + "la interfaz se distorsiona y la casa se vuelve cada vez más inquietante.",
            "Cumple los objetivos de cada noche y logra dormir antes de las 3:12 a. m.",
            "Las redes no son malas en sí mismas: lo que importa es cómo y cuándo las usas."
        },
        {
            "W A S D o flechas: caminar        Ratón o ← →: mirar        SHIFT: correr",
            "TAB o clic derecho: abrir / cerrar el teléfono",
            "Con el teléfono abierto:  W/S, ↑/↓ o rueda: desplazar el feed   ·   1, 2, 3 o clic: decidir   ·   M: No molestar",
            "E: interactuar (puertas, cama, escritorio, router...)        F: linterna del teléfono (gasta batería)",
            "J: mostrar/ocultar objetivos        I: mostrar/ocultar bienestar        ESC o P: pausa        F1: esta ayuda",
            "F11: pantalla completa        ESPACIO (en la cama): respirar"
        },
        {
            "Sueño: baja cuando te desvelas y cuando miras la pantalla de noche. Se recupera durmiendo.",
            "Autoestima: baja cuando te comparas con otros o te acosan; sube cuando tomas perspectiva y te cuidas.",
            "Ansiedad: sube con notificaciones, discusiones y contenido dañino. Aquí, más bajo es mejor.",
            "Concentración: baja con el scroll infinito y los grupos llenos de mensajes.",
            "Relaciones: sube cuando hablas con calma y con gente de confianza; baja si respondes con rabia o ignoras a quien te quiere.",
            "Cuanto peor está tu bienestar, más sube la tensión... y más cerca está El Seguidor."
        },
        {
            "@tu_seguidor no es una cuenta normal. Aparece cuando pasas demasiado tiempo conectado.",
            "Si lo miras, no se mueve. Mientras miras el teléfono, no lo estás mirando a él.",
            "Mantener la linterna sobre él unos segundos lo hace retroceder.",
            "Cerrar la app, silenciar notificaciones y tomar buenas decisiones bajan la tensión y lo alejan.",
            "Si te alcanza, pierdes tiempo de sueño y bienestar. A las 3:12 a. m., llega la última notificación."
        },
        {
            "Deja el teléfono fuera de la cama (y si puedes, fuera del cuarto) a la hora de dormir.",
            "Activa \"No molestar\" por la noche: casi nada es tan urgente.",
            "Lo que ves en redes es una selección: compararte con eso no es justo contigo.",
            "Si algo o alguien te hace daño en línea: repórtalo, bloquéalo y habla con un adulto de confianza.",
            "Desconfía de los desconocidos, los premios falsos y los retos peligrosos.",
            "Respirar despacio ayuda a calmar el cuerpo cuando la ansiedad sube."
        },
        {
            "Última Notificación · PowerGames",
            "Sebastian Diaz (gerente de proyecto) · Carlos Roman (frontend) · Jesus Pacheco (backend) · Samuel Sarmiento (diseño gráfico)",
            "Programación Orientada a Objetos · Universidad del Norte · Docente: Eduardo Angulo",
            "Hecho desde cero en Java (Swing/AWT y javax.sound), sin motores gráficos: render 3D por raycasting, "
            + "texturas y sonidos generados por código, y estructuras de datos propias.",
            "Patrones de diseño: State (pantallas) y Factory Method (publicaciones, notificaciones y eventos de terror)."
        }
    };

    private final boolean superpuesta;
    private int pagina;

    public PantallaAyuda(Juego juego, boolean superpuesta) {
        super("PantallaAyuda", juego);
        this.superpuesta = superpuesta;
    }

    @Override
    public void entrar() {
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
        g.setFont(Estilo.fuente(Font.BOLD, 14));
        int x = 70;
        for (int i = 0; i < TITULOS.length; i++) {
            boolean sel = i == pagina;
            int w = g.getFontMetrics().stringWidth(TITULOS[i]) + 28;
            Dibujo.rectRedondo(g, x, 40, w, 34, 17, sel ? Estilo.acento() : Estilo.fondoBoton());
            g.setColor(sel ? (Estilo.altoContraste() ? Color.BLACK : Color.WHITE) : Estilo.textoSuave());
            g.drawString(TITULOS[i], x + 14, 62);
            x += w + 10;
        }
        g.setFont(Estilo.fuente(Font.BOLD, 34));
        g.setColor(Estilo.texto());
        g.drawString(TITULOS[pagina], 70, 140);
        int y = 180;
        for (String linea : CONTENIDO[pagina]) {
            g.setColor(Estilo.acento());
            g.fillOval(72, y + 8, 8, 8);
            g.setFont(Estilo.fuente(Font.PLAIN, 19));
            g.setColor(Estilo.texto());
            y += Dibujo.parrafo(g, linea, 96, y, 1110, 1.2f) + 18;
        }
        g.setFont(Estilo.fuente(Font.PLAIN, 14));
        g.setColor(Estilo.textoSuave());
        Dibujo.centrado(g, "← / → para cambiar de página  ·  ESC o ENTER para volver", 640, 690);
    }

    @Override
    public void alPresionarTecla(int codigo, char caracter) {
        switch (codigo) {
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
                pagina = Math.floorMod(pagina - 1, TITULOS.length);
                break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
            case KeyEvent.VK_TAB:
                pagina = Math.floorMod(pagina + 1, TITULOS.length);
                break;
            case KeyEvent.VK_ESCAPE:
            case KeyEvent.VK_ENTER:
            case KeyEvent.VK_F1:
                volver();
                break;
            default:
                break;
        }
    }

    @Override
    public void alHacerClic(int x, int y, int boton) {
        if (y < 80) {
            java.awt.FontMetrics fm = new java.awt.image.BufferedImage(1, 1, java.awt.image.BufferedImage.TYPE_INT_RGB)
                    .createGraphics().getFontMetrics(Estilo.fuente(Font.BOLD, 14));
            int cx = 70;
            for (int i = 0; i < TITULOS.length; i++) {
                int w = fm.stringWidth(TITULOS[i]) + 28;
                if (x >= cx && x <= cx + w) {
                    pagina = i;
                    return;
                }
                cx += w + 10;
            }
        } else {
            pagina = Math.floorMod(pagina + 1, TITULOS.length);
        }
    }

    private void volver() {
        if (superpuesta || juego.pantallaActual() == this) {
            juego.desapilarPantalla();
        }
    }
}
