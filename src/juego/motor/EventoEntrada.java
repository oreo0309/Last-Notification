package juego.motor;

public class EventoEntrada {

    public enum Tipo {
        TECLA_PRESIONADA, TECLA_SOLTADA, CLIC, RUEDA, MOVIMIENTO
    }

    public final Tipo tipo;
    public final int codigo;
    public final char caracter;
    public final int x;
    public final int y;
    public final int valor;

    public EventoEntrada(Tipo tipo, int codigo, char caracter, int x, int y, int valor) {
        this.tipo = tipo;
        this.codigo = codigo;
        this.caracter = caracter;
        this.x = x;
        this.y = y;
        this.valor = valor;
    }
}
