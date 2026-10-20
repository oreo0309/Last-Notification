package juego.feed;

import static juego.IndicadorBienestar.ANSIEDAD;
import static juego.IndicadorBienestar.AUTOESTIMA;
import static juego.IndicadorBienestar.RELACIONES;
import static juego.IndicadorBienestar.SUENO;

import java.util.Random;
import juego.TipoDecision;

public class FabricaNotificacion extends FabricaContenido {

    private static final String[][] COMUNES = {
        {"eco", "Tienes 5 publicaciones nuevas que te pueden gustar", "1"},
        {"@juli.m", "te mencionó en un comentario", "2"},
        {"Curso 10B", "12 mensajes nuevos", "1"},
        {"eco", "Tu racha de 47 días termina pronto", "2"},
        {"@valen.rios", "subió una historia nueva", "1"},
        {"eco", "A 3 amigos les gustó una publicación", "1"},
        {"@mateo_sk8", "te envió un video", "2"},
        {"eco", "¿Ya viste lo que está en tendencia?", "1"},
        {"@sofi.dibuja", "reaccionó a tu historia", "1"},
        {"Curso 10B", "Alguien preguntó por la tarea de mañana", "2"}
    };

    private final FabricaEventoTerror fabricaEventos;
    private int ultimaComun = -1;

    public FabricaNotificacion(Random azar, GeneradorImagenes imagenes, FabricaEventoTerror fabricaEventos) {
        super(azar, imagenes);
        this.fabricaEventos = fabricaEventos;
    }

    @Override
    public ElementoFeed crear() {
        double probPerturbadora = Math.max(0, Math.min(0.75, (tension - 38) / 60.0 + (noche - 1) * 0.12));
        if (azar.nextDouble() < probPerturbadora) {
            return perturbadora();
        }
        int i;
        do {
            i = azar.nextInt(COMUNES.length);
        } while (i == ultimaComun);
        ultimaComun = i;
        String[] c = COMUNES[i];
        return new Notificacion(c[0], c[1], Integer.parseInt(c[2]), true);
    }

    private Notificacion perturbadora() {
        int opcion = azar.nextInt(noche >= 2 ? 6 : 4);
        switch (opcion) {
            case 0:
                return new Notificacion("@tu_seguidor", "está viendo tu perfil", 4, false)
                        .disparando(new EventoTerror(EventoTerror.SUSURRO, 2));
            case 1:
                return new Notificacion("@nadie", "¿por qué no duermes?", 5, false)
                        .disparando(new EventoTerror(EventoTerror.GLITCH, 3));
            case 2:
                return new Notificacion("@tu_seguidor", "está en el pasillo", 5, false)
                        .disparando(new EventoTerror(EventoTerror.APARICION, 3));
            case 3:
                return new Notificacion("@tu_seguidor", "le dio me gusta a tu foto de perfil", 4, false)
                        .disparando(new EventoTerror(EventoTerror.PARPADEO, 2));
            case 4:
                return new Notificacion("@tu_seguidor", "date la vuelta", 5, false)
                        .disparando(new EventoTerror(EventoTerror.PRESENCIA, 4));
            default:
                return new Notificacion("@tu_seguidor", "te etiquetó en una foto", 5, false)
                        .disparando(fabricaEventos.crearFoto());
        }
    }

    public Notificacion crearEspecial(String clave) {
        switch (clave) {
            case "mama":
                Notificacion mama = new Notificacion("Mamá", "¿Ya estás acostado? Mañana madrugas. Estoy de turno en el hospital, llego a las 7. Te quiero.", 5, false).comoMensaje();
                mama.agregarDecision(d(TipoDecision.RESPONDER_CALMA, "Responder: \"Ya me voy a dormir. Te quiero, ma.\"")
                        .efecto(RELACIONES, 12).efecto(ANSIEDAD, -6).bandera("mama_respondida")
                        .orientacion("Responder con calma fortalece tus relaciones."));
                mama.agregarDecision(d(TipoDecision.RESPONDER_IMPULSIVO, "Responder: \"ya sé, deja de escribirme\"")
                        .efecto(RELACIONES, -10).efecto(ANSIEDAD, 5).bandera("mama_respondida")
                        .orientacion("Responder de mal genio por cansancio puede herir a quien te quiere."));
                mama.agregarDecision(d(TipoDecision.IGNORAR, "Dejarla en visto").efecto(RELACIONES, -8).efecto(ANSIEDAD, 3)
                        .orientacion("Dejar en visto a quien se preocupa por ti también pesa."));
                return mama.conClave("mama");
            case "mama2":
                Notificacion mama2 = new Notificacion("Mamá", "Hoy llego tarde otra vez. Cierra bien la puerta y duerme temprano, que mañana tienes examen.", 5, false).comoMensaje();
                mama2.agregarDecision(d(TipoDecision.RESPONDER_CALMA, "Responder: \"Tranquila, ya casi me duermo\"")
                        .efecto(RELACIONES, 10).efecto(ANSIEDAD, -5).bandera("mama_respondida")
                        .orientacion("Un mensaje corto y tranquilo basta para que alguien se quede en paz."));
                mama2.agregarDecision(d(TipoDecision.IGNORAR, "Leerlo y no responder").efecto(RELACIONES, -6));
                return mama2.conClave("mama2");
            case "seguidor_sigue":
                return new Notificacion("@tu_seguidor", "comenzó a seguirte", 3, false).conClave("seguidor_sigue");
            case "wifi":
                return new Notificacion("@tu_seguidor", "se fue la luz... pero el wifi sigue funcionando. ¿Verdad, {nombre}?", 5, false)
                        .disparando(new EventoTerror(EventoTerror.GLITCH, 2)).conClave("wifi");
            case "router_apagado":
                return new Notificacion("@tu_seguidor", "¿creías que eso me iba a desconectar?", 5, false)
                        .disparando(new EventoTerror(EventoTerror.PARPADEO, 3)).conClave("router_apagado");
            case "bateria":
                return new Notificacion("Sistema", "Batería baja: 20%. La linterna consume batería.", 3, false).conClave("bateria");
            case "llamada":
                return new Notificacion("Número desconocido", "Llamada entrante...", 5, false)
                        .disparando(new EventoTerror(EventoTerror.LLAMADA, 4)).conClave("llamada");
            case "juli_buenas":
                Notificacion juli = new Notificacion("@juli.m", "¿Sigues despierto? Yo ya me voy a dormir, mañana hablamos :)", 2, true).comoMensaje();
                juli.agregarDecision(d(TipoDecision.RESPONDER_CALMA, "Responder: \"Yo también. ¡Buenas noches!\"")
                        .efecto(RELACIONES, 6).efecto(SUENO, 2).orientacion("Despedirte a tiempo también es cuidar la amistad."));
                juli.agregarDecision(d(TipoDecision.VER_MAS, "Proponerle seguir hablando un rato más")
                        .efecto(RELACIONES, 2).efecto(SUENO, -5).minutos(20).orientacion("Las conversaciones también pueden esperar a mañana."));
                return juli.conClave("juli_buenas");
            case "apagar":
                Notificacion apagar = new Notificacion("eco", "¿Seguro que quieres desconectarte? Tienes 99+ notificaciones sin leer. Te vas a perder de todo.", 5, false).comoMensaje();
                apagar.agregarDecision(d(TipoDecision.APAGAR_TELEFONO, "Apagar el teléfono")
                        .efecto(ANSIEDAD, -10).efecto(AUTOESTIMA, 6).bandera("telefono_apagado")
                        .orientacion("No te pierdes de nada importante por descansar."));
                apagar.agregarDecision(d(TipoDecision.UN_MOMENTO_MAS, "Solo un momento más...")
                        .efecto(ANSIEDAD, 15).efecto(SUENO, -10).bandera("un_momento_mas"));
                return apagar.conClave("apagar");
            default:
                return null;
        }
    }
}
