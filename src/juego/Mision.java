package juego;

public class Mision {

    private final String objetivo;
    private final String bandera;
    private final String pista;
    private boolean cumplida;

    public Mision(String objetivo, String bandera, String pista) {
        this.objetivo = objetivo;
        this.bandera = bandera;
        this.pista = pista;
    }

    public boolean evaluar(Juego juego) {
        if (!cumplida && juego.bandera(bandera)) {
            cumplida = true;
            return true;
        }
        return false;
    }

    public void reiniciar() {
        cumplida = false;
    }

    public String objetivo() {
        return objetivo;
    }

    public String bandera() {
        return bandera;
    }

    public String pista() {
        return pista;
    }

    public boolean cumplida() {
        return cumplida;
    }
}
