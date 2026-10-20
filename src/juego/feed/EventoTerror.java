package juego.feed;

import java.awt.Color;
import juego.interfaz.ContextoVisual;
import juego.interfaz.Estilo;

public class EventoTerror extends ElementoBase {

    public static final String PARPADEO = "PARPADEO";
    public static final String SUSURRO = "SUSURRO";
    public static final String PASOS = "PASOS";
    public static final String GOLPES = "GOLPES";
    public static final String GOLPES_VENTANA = "GOLPES_VENTANA";
    public static final String PUERTA = "PUERTA";
    public static final String PORTAZO = "PORTAZO";
    public static final String APARICION = "APARICION";
    public static final String PRESENCIA = "PRESENCIA";
    public static final String FOTO = "FOTO";
    public static final String GLITCH = "GLITCH";
    public static final String TV = "TV";
    public static final String ESPEJO = "ESPEJO";
    public static final String LLAMADA = "LLAMADA";
    public static final String CAZA = "CAZA";
    public static final String MENSAJE = "MENSAJE";
    public static final String TELEFONO_MESA = "TELEFONO_MESA";
    public static final String JUMPSCARE = "JUMPSCARE";
    public static final String JUMPSCARE_SUAVE = "JUMPSCARE_SUAVE";

    private final int intensidad;
    private final String tipo;
    private String texto = "";
    private boolean enFeed;
    private boolean suave;

    public EventoTerror(String tipo, int intensidad) {
        super("@tu_seguidor");
        this.tipo = tipo;
        this.intensidad = Math.max(1, Math.min(5, intensidad));
    }

    public EventoTerror conTexto(String texto) {
        this.texto = texto;
        return this;
    }

    public EventoTerror enFeed(boolean valor) {
        this.enFeed = valor;
        return this;
    }

    public EventoTerror versionSuave() {
        String nuevoTipo;
        switch (tipo) {
            case JUMPSCARE:
                nuevoTipo = JUMPSCARE_SUAVE;
                break;
            case PRESENCIA:
                nuevoTipo = SUSURRO;
                break;
            case PORTAZO:
                nuevoTipo = PUERTA;
                break;
            case LLAMADA:
                nuevoTipo = TELEFONO_MESA;
                break;
            default:
                nuevoTipo = tipo;
                break;
        }
        EventoTerror suaveEvento = new EventoTerror(nuevoTipo, Math.max(1, intensidad - 2));
        suaveEvento.texto = texto;
        suaveEvento.enFeed = enFeed;
        suaveEvento.imagen = imagen;
        suaveEvento.suave = true;
        for (juego.Decision d : decisiones) {
            suaveEvento.agregarDecision(d);
        }
        return suaveEvento;
    }

    @Override
    protected String cuerpo(ContextoVisual contexto) {
        return texto.replace("{nombre}", contexto.nombreJugador);
    }

    @Override
    protected boolean perturbador() {
        return true;
    }

    @Override
    protected Color fondo(ContextoVisual contexto) {
        if (Estilo.altoContraste()) {
            return Color.BLACK;
        }
        int pulso = (int) (8 + 6 * Math.sin(contexto.tiempo * 3));
        return new Color(18 + pulso, 4, 8);
    }

    @Override
    protected Color colorBorde(ContextoVisual contexto) {
        return Estilo.peligro();
    }

    @Override
    protected String subtitulo(ContextoVisual contexto) {
        return FOTO.equals(tipo) ? "publicó una foto · hace 0 segundos" : "hace 0 segundos";
    }

    @Override
    public int impactoAnsiedad() {
        return 3 + intensidad * 2;
    }

    public int intensidad() {
        return intensidad;
    }

    public String tipo() {
        return tipo;
    }

    public boolean enFeed() {
        return enFeed;
    }

    public boolean suave() {
        return suave;
    }

    public String texto() {
        return texto;
    }
}
