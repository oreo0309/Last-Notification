package juego;

public enum TipoFinal {
    DESCONECTADO("Desconectado",
            "Apagaste el teléfono. La casa quedó en silencio y, por primera vez en días, dormiste de verdad.",
            "Desconectarte no te hizo perderte de nada importante. Mañana vas a estar ahí para lo que sí importa."),
    AL_LIMITE("Al límite",
            "Apagaste el teléfono, pero llegaste agotado. El Seguidor se fue... por ahora.",
            "Lograste cortar a tiempo, aunque tus hábitos te pasaron factura. Pequeños cambios cada noche hacen la diferencia."),
    AHORA_ERES_CONTENIDO("Ahora eres contenido",
            "\"Solo un momento más.\" La pantalla te devolvió la mirada. Tu última publicación ya tiene 3.120 me gusta.",
            "El \"solo un momento más\" es justo lo que estas apps buscan. Tú tienes el control: puedes cerrar la app."),
    ULTIMA_NOTIFICACION("La última notificación",
            "Son las 3:12 a. m. Sigues despierto. Y ya no estás solo.",
            "Perder horas de sueño en el teléfono afecta tu ánimo, tu concentración y tus relaciones. Inténtalo de nuevo: esta vez, desconéctate a tiempo.");

    private final String titulo;
    private final String relato;
    private final String reflexion;

    TipoFinal(String titulo, String relato, String reflexion) {
        this.titulo = titulo;
        this.relato = relato;
        this.reflexion = reflexion;
    }

    public String titulo() {
        return titulo;
    }

    public String relato() {
        return relato;
    }

    public String reflexion() {
        return reflexion;
    }

    public boolean positivo() {
        return this == DESCONECTADO || this == AL_LIMITE;
    }
}
