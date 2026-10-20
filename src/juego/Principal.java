package juego;

import java.awt.GraphicsEnvironment;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import juego.motor.Entrada;
import juego.motor.Ventana;

public final class Principal {

    private static Ventana ventana;

    private Principal() {
    }

    public static void main(String[] args) {
        if (GraphicsEnvironment.isHeadless()) {
            System.err.println("Última Notificación necesita una pantalla para ejecutarse.");
            return;
        }
        Thread.setDefaultUncaughtExceptionHandler((hilo, error) -> {
            error.printStackTrace();
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null,
                    "Ocurrió un error grave y el juego debe cerrarse:\n" + error,
                    "Última Notificación", JOptionPane.ERROR_MESSAGE));
        });
        Entrada entrada = new Entrada();
        try {
            SwingUtilities.invokeAndWait(() -> ventana = new Ventana("Última Notificación", entrada));
        } catch (InterruptedException | InvocationTargetException e) {
            Thread.currentThread().interrupt();
            return;
        }
        Juego juego = new Juego(ventana, entrada);
        Thread bucle = new Thread(juego::ejecutar, "bucle-juego");
        bucle.start();
    }
}
