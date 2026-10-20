package juego;

import estructuras.ColaPrioridad;
import estructuras.ListaEnlazada;

public class Noche {

    public static final int TOTAL = 3;

    private final int numero;
    private final String dia;
    private final int horaInicio;
    private final double bateriaInicial;
    private final String introduccion;
    private final ListaEnlazada<Mision> misiones = new ListaEnlazada<>();
    private final ColaPrioridad<EventoProgramado> programados =
            new ColaPrioridad<>((a, b) -> Integer.compare(b.minuto(), a.minuto()));

    private Noche(int numero, String dia, int horaInicio, double bateriaInicial, String introduccion) {
        this.numero = numero;
        this.dia = dia;
        this.horaInicio = horaInicio;
        this.bateriaInicial = bateriaInicial;
        this.introduccion = introduccion;
    }

    public static Noche crear(int numero) {
        Noche n;
        switch (numero) {
            case 1:
                n = new Noche(1, "Lunes", 23 * 60 + 5, 64,
                        "Tu mamá tiene turno de noche en el hospital. Estás solo en casa. Mañana hay clase a las 6:30.\n"
                        + "Solo vas a revisar el teléfono un momento antes de dormir.");
                n.mision("Responde el mensaje de mamá", "mama_respondida", "Abre el teléfono (TAB) y busca su mensaje.");
                n.mision("Deja el teléfono cargando en el escritorio", "telefono_cargando", "Acércate al escritorio y pulsa E.");
                n.mision("Acuéstate a dormir", "dormir", "Ve a tu cama y pulsa E.");
                n.programar(3, "mama");
                n.programar(16, "seguidor_sigue");
                n.programar(40, "golpes_ventana");
                n.programar(70, "pasos_pasillo");
                n.programar(95, "parpadeo");
                n.programar(120, "juli_buenas");
                n.programar(150, "susurro");
                break;
            case 2:
                n = new Noche(2, "Martes", 23 * 60 + 20, 52,
                        "Mañana tienes examen de matemáticas. Alguien dejó abierta la puerta de tu cuarto.\n"
                        + "@tu_seguidor sigue apareciendo en tus notificaciones.");
                n.mision("Activa \"No molestar\" (tecla M con el teléfono abierto)", "no_molestar", "Abre el teléfono con TAB y pulsa M.");
                n.mision("Reporta el reto peligroso #RetoNoDormir", "reto_reportado", "Desplázate por el feed hasta encontrarlo.");
                n.mision("Cierra la puerta de tu cuarto", "puerta_cerrada", "Acércate a la puerta y pulsa E.");
                n.mision("Deja el teléfono cargando y duerme", "dormir", "Escritorio (E) y luego la cama (E).");
                n.programar(2, "reto");
                n.programar(5, "mama2");
                n.programar(9, "anonimo");
                n.programar(24, "foto");
                n.programar(42, "golpes_padres");
                n.programar(64, "tv");
                n.programar(88, "susurro");
                n.programar(110, "parpadeo");
                n.programar(135, "presencia");
                break;
            default:
                n = new Noche(3, "Miércoles", 23 * 60 + 35, 41,
                        "Hay tormenta. La luz se va justo cuando terminas de cenar.\n"
                        + "El teléfono sigue conectado a internet. Eso no debería ser posible.");
                n.mision("Apaga el router de la sala", "router_apagado", "La sala está al final del pasillo. Usa la linterna (F).");
                n.mision("Vuelve a tu cuarto", "volver_cuarto", "Regresa por el pasillo hasta tu cuarto.");
                n.mision("Apaga el teléfono", "telefono_apagado", "Abre el teléfono cuando estés en tu cuarto.");
                n.programar(1, "apagon");
                n.programar(4, "wifi");
                n.programar(14, "caza");
                n.programar(22, "tv");
                n.programar(40, "llamada");
                n.programar(60, "susurro");
                n.programar(80, "golpes_padres");
                break;
        }
        return n;
    }

    private void mision(String objetivo, String bandera, String pista) {
        misiones.agregar(new Mision(objetivo, bandera, pista));
    }

    private void programar(int minuto, String clave) {
        programados.insertar(new EventoProgramado(horaInicio + minuto, clave));
    }

    public EventoProgramado siguienteListo(int minutoActual) {
        if (!programados.estaVacia() && programados.ver().minuto() <= minutoActual) {
            return programados.extraer();
        }
        return null;
    }

    public int numero() {
        return numero;
    }

    public String dia() {
        return dia;
    }

    public int horaInicio() {
        return horaInicio;
    }

    public double bateriaInicial() {
        return bateriaInicial;
    }

    public String introduccion() {
        return introduccion;
    }

    public ListaEnlazada<Mision> misiones() {
        return misiones;
    }
}
