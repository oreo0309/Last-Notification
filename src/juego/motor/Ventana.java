package juego.motor;

import java.awt.AWTException;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Ventana {

    public static final int ANCHO = 1280;
    public static final int ALTO = 720;

    private final Entrada entrada;
    private final JFrame marco;
    private final Canvas lienzo;
    private final Cursor cursorInvisible;
    private volatile BufferStrategy estrategia;
    private Robot robot;
    private volatile boolean capturando;
    private volatile boolean abierta = true;
    private volatile boolean focoPerdido;
    private volatile double escala = 1;
    private volatile int desplazamientoX;
    private volatile int desplazamientoY;
    private boolean pantallaCompleta;

    public Ventana(String titulo, Entrada entrada) {
        this.entrada = entrada;
        marco = new JFrame(titulo);
        marco.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        marco.setBackground(Color.BLACK);
        marco.setIconImage(crearIcono());
        lienzo = new Canvas();
        lienzo.setBackground(Color.BLACK);
        lienzo.setPreferredSize(tamanoInicial());
        lienzo.setFocusTraversalKeysEnabled(false);
        lienzo.setIgnoreRepaint(true);
        marco.setFocusTraversalKeysEnabled(false);
        marco.add(lienzo);
        marco.pack();
        marco.setLocationRelativeTo(null);
        cursorInvisible = Toolkit.getDefaultToolkit().createCustomCursor(
                new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), new Point(0, 0), "invisible");
        try {
            robot = new Robot();
        } catch (AWTException | SecurityException e) {
            robot = null;
        }
        registrarEventos();
    }

    private static Dimension tamanoInicial() {
        try {
            Rectangle disponible = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
            double s = Math.min(1.0, Math.min((disponible.width - 30) / (double) ANCHO, (disponible.height - 50) / (double) ALTO));
            return new Dimension((int) (ANCHO * s), (int) (ALTO * s));
        } catch (Exception e) {
            return new Dimension(ANCHO, ALTO);
        }
    }

    private BufferedImage crearIcono() {
        BufferedImage icono = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = icono.createGraphics();
        g.setColor(new Color(16, 16, 20));
        g.fillRoundRect(7, 1, 18, 30, 6, 6);
        g.setColor(new Color(170, 200, 220));
        g.fillRect(9, 4, 14, 24);
        g.setColor(new Color(225, 30, 42));
        g.fillOval(18, 0, 12, 12);
        g.dispose();
        return icono;
    }

    private void registrarEventos() {
        marco.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                abierta = false;
            }
        });
        KeyAdapter teclado = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                char c = e.getKeyChar() == KeyEvent.CHAR_UNDEFINED ? '\0' : e.getKeyChar();
                entrada.presionar(e.getKeyCode(), c);
                if (e.getKeyCode() == KeyEvent.VK_TAB || e.getKeyCode() == KeyEvent.VK_SPACE) {
                    e.consume();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                entrada.soltar(e.getKeyCode());
            }
        };
        lienzo.addKeyListener(teclado);
        marco.addKeyListener(teclado);
        MouseAdapter raton = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lienzo.requestFocus();
                int boton = SwingUtilities.isRightMouseButton(e) ? 3 : 1;
                if (boton == 3) {
                    entrada.botonDerecho(true);
                }
                entrada.clic(aVirtualX(e.getX()), aVirtualY(e.getY()), boton);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    entrada.botonDerecho(false);
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                movimiento(e);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                movimiento(e);
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                entrada.rueda(e.getWheelRotation());
            }
        };
        lienzo.addMouseListener(raton);
        lienzo.addMouseMotionListener(raton);
        lienzo.addMouseWheelListener(raton);
        lienzo.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                entrada.soltarTodo();
                focoPerdido = true;
            }
        });
    }

    private void movimiento(MouseEvent e) {
        entrada.moverRaton(aVirtualX(e.getX()), aVirtualY(e.getY()));
        if (!capturando || robot == null || !marco.isFocused()) {
            return;
        }
        Point centro = centroEnPantalla();
        if (centro == null) {
            return;
        }
        int dx = e.getXOnScreen() - centro.x;
        int dy = e.getYOnScreen() - centro.y;
        if (dx == 0 && dy == 0) {
            return;
        }
        entrada.acumularMovimiento(dx, dy);
        robot.mouseMove(centro.x, centro.y);
    }

    private Point centroEnPantalla() {
        try {
            Point p = lienzo.getLocationOnScreen();
            return new Point(p.x + lienzo.getWidth() / 2, p.y + lienzo.getHeight() / 2);
        } catch (Exception e) {
            return null;
        }
    }

    private int aVirtualX(int x) {
        return (int) ((x - desplazamientoX) / escala);
    }

    private int aVirtualY(int y) {
        return (int) ((y - desplazamientoY) / escala);
    }

    public void mostrar() {
        marco.setVisible(true);
        lienzo.requestFocus();
        crearEstrategia();
    }

    private void crearEstrategia() {
        lienzo.createBufferStrategy(2);
        estrategia = lienzo.getBufferStrategy();
    }

    public void capturarRaton(boolean capturar) {
        if (capturar == capturando) {
            return;
        }
        capturando = capturar;
        SwingUtilities.invokeLater(() -> {
            lienzo.setCursor(capturar ? cursorInvisible : Cursor.getDefaultCursor());
            if (capturar && robot != null) {
                Point centro = centroEnPantalla();
                if (centro != null) {
                    robot.mouseMove(centro.x, centro.y);
                }
            }
        });
        entrada.consumirDX();
        entrada.consumirDY();
    }

    public boolean tieneCapturaRaton() {
        return robot != null;
    }

    public void dibujar(Consumer<Graphics2D> pintor) {
        BufferStrategy actual = estrategia;
        if (actual == null || !lienzo.isDisplayable()) {
            return;
        }
        try {
            dibujarCon(actual, pintor);
        } catch (IllegalStateException e) {
            estrategia = null;
            SwingUtilities.invokeLater(() -> {
                if (lienzo.isDisplayable() && estrategia == null) {
                    crearEstrategia();
                }
            });
        }
    }

    private void dibujarCon(BufferStrategy estrategia, Consumer<Graphics2D> pintor) {
        int w = Math.max(1, lienzo.getWidth());
        int h = Math.max(1, lienzo.getHeight());
        double s = Math.min(w / (double) ANCHO, h / (double) ALTO);
        escala = s;
        desplazamientoX = (int) ((w - ANCHO * s) / 2);
        desplazamientoY = (int) ((h - ALTO * s) / 2);
        do {
            do {
                Graphics2D g = (Graphics2D) estrategia.getDrawGraphics();
                try {
                    g.setColor(Color.BLACK);
                    g.fillRect(0, 0, w, h);
                    AffineTransform original = g.getTransform();
                    g.translate(desplazamientoX, desplazamientoY);
                    g.scale(s, s);
                    g.clipRect(0, 0, ANCHO, ALTO);
                    pintor.accept(g);
                    g.setTransform(original);
                } finally {
                    g.dispose();
                }
            } while (estrategia.contentsRestored());
            estrategia.show();
        } while (estrategia.contentsLost());
        Toolkit.getDefaultToolkit().sync();
    }

    public void pantallaCompleta(boolean activar) {
        if (activar == pantallaCompleta) {
            return;
        }
        pantallaCompleta = activar;
        SwingUtilities.invokeLater(() -> {
            marco.dispose();
            marco.setUndecorated(activar);
            if (activar) {
                marco.setExtendedState(JFrame.MAXIMIZED_BOTH);
            } else {
                marco.setExtendedState(JFrame.NORMAL);
                lienzo.setPreferredSize(tamanoInicial());
                marco.pack();
                marco.setLocationRelativeTo(null);
            }
            marco.setVisible(true);
            lienzo.requestFocus();
            crearEstrategia();
        });
    }

    public boolean esPantallaCompleta() {
        return pantallaCompleta;
    }

    public boolean consumirFocoPerdido() {
        boolean v = focoPerdido;
        focoPerdido = false;
        return v;
    }

    public boolean abierta() {
        return abierta;
    }

    public void solicitarCierre() {
        abierta = false;
    }

    public void cerrar() {
        SwingUtilities.invokeLater(marco::dispose);
    }
}
