package juego;

import estructuras.ListaEnlazada;

public enum Logro {
    A_TIEMPO("Desconexión a tiempo", "Te dormiste antes de la medianoche."),
    SUENO_REPARADOR("Sueño reparador", "Dormiste 7 horas o más."),
    GUARDIAN("Guardián del feed", "Reportaste contenido dañino."),
    BUENA_COMPANIA("Buena compañía", "Respondiste con calma o buscaste a alguien de confianza."),
    OJOS_PROPIOS("Ojos propios", "No te comparaste con nadie en toda la noche."),
    FRENO_SCROLL("Freno al scroll", "Viste menos de 6 publicaciones en la noche."),
    MODO_NOCHE("Modo noche", "Activaste \"No molestar\"."),
    FUERA_DE_ALCANCE("Fuera de su alcance", "El Seguidor no te alcanzó en toda la noche."),
    MENTE_EN_CALMA("Mente en calma", "Terminaste la noche con la ansiedad en 30 o menos.");

    private final String nombre;
    private final String descripcion;

    Logro(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public String nombre() {
        return nombre;
    }

    public String descripcion() {
        return descripcion;
    }

    public static ListaEnlazada<Logro> evaluarNoche(ListaEnlazada<Decision> decisionesNoche, int minutoDormir, int publicacionesNoche,
            int atrapadasNoche, boolean noMolestar, int ansiedadFinal) {
        ListaEnlazada<Logro> obtenidos = new ListaEnlazada<>();
        double horas = (Jugador.MINUTO_DESPERTAR - minutoDormir) / 60.0;
        if (minutoDormir < 1440) {
            obtenidos.agregar(A_TIEMPO);
        }
        if (horas >= 7) {
            obtenidos.agregar(SUENO_REPARADOR);
        }
        boolean reporto = decisionesNoche.contar(d -> d.tipo() == TipoDecision.REPORTAR) > 0;
        boolean social = decisionesNoche.contar(d -> d.tipo() == TipoDecision.RESPONDER_CALMA
                || d.tipo() == TipoDecision.HABLAR_CONFIANZA) > 0;
        boolean comparo = decisionesNoche.contar(d -> d.tipo() == TipoDecision.COMPARARSE) > 0;
        if (reporto) {
            obtenidos.agregar(GUARDIAN);
        }
        if (social) {
            obtenidos.agregar(BUENA_COMPANIA);
        }
        if (!comparo) {
            obtenidos.agregar(OJOS_PROPIOS);
        }
        if (publicacionesNoche < 6) {
            obtenidos.agregar(FRENO_SCROLL);
        }
        if (noMolestar) {
            obtenidos.agregar(MODO_NOCHE);
        }
        if (atrapadasNoche == 0) {
            obtenidos.agregar(FUERA_DE_ALCANCE);
        }
        if (ansiedadFinal <= 30) {
            obtenidos.agregar(MENTE_EN_CALMA);
        }
        return obtenidos;
    }
}
