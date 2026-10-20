package juego;

public enum TipoDecision {
    SEGUIR_DESPLAZANDO("Seguir desplazándote", -1),
    CERRAR_APP("Cerrar la app", 1),
    SILENCIAR("Silenciar", 1),
    REPORTAR("Reportar", 1),
    BLOQUEAR("Bloquear", 1),
    RESPONDER_CALMA("Responder con calma", 1),
    RESPONDER_IMPULSIVO("Responder impulsivamente", -1),
    IGNORAR("Ignorar", 0),
    COMPARARSE("Compararte", -1),
    REFLEXIONAR("Tomar perspectiva", 1),
    DAR_ME_GUSTA("Dar me gusta", 0),
    HABLAR_CONFIANZA("Hablar con alguien de confianza", 1),
    COMPARTIR("Compartir", -1),
    DEJAR_PARA_MANANA("Dejarlo para mañana", 1),
    VER_MAS("Ver más", -1),
    DESCANSAR("Descansar", 1),
    APAGAR_TELEFONO("Apagar el teléfono", 1),
    UN_MOMENTO_MAS("Solo un momento más", -1);

    private final String nombre;
    private final int valoracion;

    TipoDecision(String nombre, int valoracion) {
        this.nombre = nombre;
        this.valoracion = valoracion;
    }

    public String nombre() {
        return nombre;
    }

    public boolean saludable() {
        return valoracion > 0;
    }

    public boolean danina() {
        return valoracion < 0;
    }
}
