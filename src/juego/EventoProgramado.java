package juego;

public class EventoProgramado {

    private final int minuto;
    private final String clave;

    public EventoProgramado(int minuto, String clave) {
        this.minuto = minuto;
        this.clave = clave;
    }

    public int minuto() {
        return minuto;
    }

    public String clave() {
        return clave;
    }
}
