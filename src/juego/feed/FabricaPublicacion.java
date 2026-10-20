package juego.feed;

import static juego.IndicadorBienestar.ANSIEDAD;
import static juego.IndicadorBienestar.AUTOESTIMA;
import static juego.IndicadorBienestar.CONCENTRACION;
import static juego.IndicadorBienestar.RELACIONES;
import static juego.IndicadorBienestar.SUENO;

import estructuras.Cola;
import estructuras.ListaEnlazada;
import java.util.Random;
import java.util.function.Supplier;
import juego.Decision;
import juego.TipoDecision;

public class FabricaPublicacion extends FabricaContenido {

    private static class Plantilla {
        final String clave;
        final TipoPost tipo;
        final int nocheMinima;
        final int pesoBase;
        final Supplier<Publicacion> constructor;

        Plantilla(String clave, TipoPost tipo, int nocheMinima, int pesoBase, Supplier<Publicacion> constructor) {
            this.clave = clave;
            this.tipo = tipo;
            this.nocheMinima = nocheMinima;
            this.pesoBase = pesoBase;
            this.constructor = constructor;
        }
    }

    private final ListaEnlazada<Plantilla> plantillas = new ListaEnlazada<>();
    private final Cola<String> recientes = new Cola<>();

    public FabricaPublicacion(Random azar, GeneradorImagenes imagenes) {
        super(azar, imagenes);
        registrarPlantillas();
    }

    @Override
    public ElementoFeed crear() {
        int total = 0;
        int[] pesos = new int[plantillas.tamano()];
        int i = 0;
        for (Plantilla p : plantillas) {
            int peso = peso(p);
            pesos[i++] = peso;
            total += peso;
        }
        if (total <= 0) {
            return plantillas.primero().constructor.get();
        }
        int r = azar.nextInt(total);
        i = 0;
        for (Plantilla p : plantillas) {
            r -= pesos[i++];
            if (r < 0) {
                recordar(p.clave);
                return p.constructor.get().conClave(p.clave);
            }
        }
        return plantillas.ultimo().constructor.get();
    }

    public Publicacion crearEspecial(String clave) {
        for (Plantilla p : plantillas) {
            if (p.clave.equals(clave)) {
                recordar(clave);
                return p.constructor.get().conClave(clave);
            }
        }
        return null;
    }

    private int peso(Plantilla p) {
        if (noche < p.nocheMinima || estaReciente(p.clave)) {
            return 0;
        }
        int peso = p.pesoBase;
        switch (p.tipo) {
            case PERTURBADOR:
                peso = tension < 40 && noche == 1 ? 0 : (int) (p.pesoBase * (tension / 25.0 + noche - 1));
                break;
            case COMPARACION:
            case ACOSO:
                peso += noche * 2;
                break;
            case BIENESTAR:
                peso += tension / 15;
                break;
            case AMIGO:
            case MEME:
                peso = Math.max(1, peso - tension / 20);
                break;
            default:
                break;
        }
        return Math.max(0, peso);
    }

    private boolean estaReciente(String clave) {
        int n = recientes.tamano();
        boolean encontrado = false;
        for (int i = 0; i < n; i++) {
            String c = recientes.desencolar();
            if (c.equals(clave)) {
                encontrado = true;
            }
            recientes.encolar(c);
        }
        return encontrado;
    }

    private void recordar(String clave) {
        recientes.encolar(clave);
        while (recientes.tamano() > 9) {
            recientes.desencolar();
        }
    }

    public void olvidarRecientes() {
        recientes.limpiar();
    }

    private void registrar(String clave, TipoPost tipo, int nocheMinima, int peso, Supplier<Publicacion> constructor) {
        plantillas.agregar(new Plantilla(clave, tipo, nocheMinima, peso, constructor));
    }

    private Publicacion pub(String autor, String texto, TipoPost tipo, int impacto, String imagen, Decision... decisiones) {
        Publicacion p = new Publicacion(autor, texto, tipo, impacto);
        if (imagen != null) {
            p.conImagen(imagenes.obtener(imagen));
        }
        p.conReacciones(entre(3, 2400), entre(0, 140), entre(1, 59));
        for (Decision d : decisiones) {
            p.agregarDecision(d);
        }
        return p;
    }

    private void registrarPlantillas() {
        registrar("partido", TipoPost.AMIGO, 1, 8, () -> pub("@juli.m", "¡Mañana partido a las 4! ¿Quién se apunta?", TipoPost.AMIGO, 0, "cancha",
                d(TipoDecision.RESPONDER_CALMA, "Comentar: \"¡Cuenta conmigo!\"").efecto(RELACIONES, 6).efecto(ANSIEDAD, -2).minutos(2)
                        .orientacion("Usar las redes para quedar con tus amigos es un buen uso."),
                d(TipoDecision.DAR_ME_GUSTA, "Dar me gusta").efecto(RELACIONES, 2).minutos(1)));
        registrar("dibujo", TipoPost.AMIGO, 1, 8, () -> pub("@sofi.dibuja", "Terminé este dibujo después de tres semanas. Me da pena mostrarlo, pero aquí va.", TipoPost.AMIGO, 0, "dibujo",
                d(TipoDecision.RESPONDER_CALMA, "Comentar: \"¡Te quedó increíble!\"").efecto(RELACIONES, 7).efecto(AUTOESTIMA, 2).minutos(2)
                        .orientacion("Un comentario amable también te hace sentir bien a ti."),
                d(TipoDecision.DAR_ME_GUSTA, "Dar me gusta").efecto(RELACIONES, 2).minutos(1)));
        registrar("skate", TipoPost.AMIGO, 1, 7, () -> pub("@mateo_sk8", "Truco nuevo desbloqueado. (Me caí 40 veces antes, no pregunten).", TipoPost.AMIGO, 0, "skate",
                d(TipoDecision.DAR_ME_GUSTA, "Reírte y dar me gusta").efecto(RELACIONES, 3).efecto(ANSIEDAD, -1).minutos(1),
                d(TipoDecision.RESPONDER_CALMA, "Comentar: \"¡Enséñame ese truco!\"").efecto(RELACIONES, 5).minutos(2)));
        registrar("abuela", TipoPost.AMIGO, 1, 6, () -> pub("@abuela.carmen", "¿Así se publica? Hola a todos los amigos de mi nieto. Duerman temprano.", TipoPost.AMIGO, -2, "flores",
                d(TipoDecision.RESPONDER_CALMA, "Responderle con cariño").efecto(RELACIONES, 8).efecto(ANSIEDAD, -3).minutos(2)
                        .orientacion("Tu abuela tiene razón: dormir bien también es cuidarte."),
                d(TipoDecision.DAR_ME_GUSTA, "Dar me gusta").efecto(RELACIONES, 3).minutos(1)));

        registrar("playa", TipoPost.COMPARACION, 1, 8, () -> pub("@valen.rios", "Otra semana perfecta en la playa. Algunos sí sabemos vivir. #vidaperfecta", TipoPost.COMPARACION, 4, "playa",
                d(TipoDecision.COMPARARSE, "Ver todas sus fotos de vacaciones").efecto(AUTOESTIMA, -8).efecto(ANSIEDAD, 5).minutos(6).tension(4)
                        .orientacion("Lo que ves en redes es una selección. Nadie publica sus días malos."),
                d(TipoDecision.REFLEXIONAR, "Recordar que es solo una parte de su vida").efecto(AUTOESTIMA, 5).efecto(ANSIEDAD, -3).minutos(1)
                        .orientacion("Bien pensado: compararte con una foto no es justo contigo."),
                d(TipoDecision.SILENCIAR, "Silenciar esta cuenta").efecto(AUTOESTIMA, 4).minutos(1)
                        .orientacion("Silenciar cuentas que te hacen sentir mal es una forma de cuidarte.")));
        registrar("productivo", TipoPost.COMPARACION, 1, 6, () -> pub("@nico.productivo", "5:00 a. m. Ya entrené, estudié y medité. ¿Y tú qué has hecho hoy?", TipoPost.COMPARACION, 4, "amanecer_gym",
                d(TipoDecision.COMPARARSE, "Sentirte mal por no hacer lo mismo").efecto(AUTOESTIMA, -7).efecto(ANSIEDAD, 4).minutos(2).tension(3),
                d(TipoDecision.REFLEXIONAR, "Recordar que cada quien tiene su ritmo").efecto(AUTOESTIMA, 4).efecto(ANSIEDAD, -2).minutos(1)
                        .orientacion("Tu valor no depende de qué tan productivo te ves en internet."),
                d(TipoDecision.BLOQUEAR, "Dejar de seguirlo").efecto(AUTOESTIMA, 3).minutos(1)));
        registrar("fiesta", TipoPost.COMPARACION, 1, 7, () -> pub("@fiesta.10b", "La mejor fiesta del año. ¡Gracias a todos los que vinieron!", TipoPost.COMPARACION, 5, "fiesta",
                d(TipoDecision.VER_MAS, "Revisar quién fue y por qué no te invitaron").efecto(AUTOESTIMA, -9).efecto(ANSIEDAD, 6).efecto(RELACIONES, -2).minutos(7).tension(5)
                        .orientacion("Revisar cada foto alimenta la sensación de quedarte por fuera."),
                d(TipoDecision.HABLAR_CONFIANZA, "Escribirle a un amigo para planear algo juntos").efecto(RELACIONES, 7).efecto(AUTOESTIMA, 3).minutos(3)
                        .orientacion("Convertir esa sensación en un plan real es mejor que mirar fotos."),
                d(TipoDecision.CERRAR_APP, "Cerrar la app").efecto(SUENO, 3).efecto(ANSIEDAD, -3)));
        registrar("notas", TipoPost.COMPARACION, 1, 5, () -> pub("@top.notas", "Saqué 5.0 en todo el semestre sin estudiar jaja", TipoPost.COMPARACION, 3, "notas",
                d(TipoDecision.COMPARARSE, "Compararte con tus notas").efecto(AUTOESTIMA, -6).efecto(CONCENTRACION, -3).efecto(ANSIEDAD, 5).minutos(3).tension(3),
                d(TipoDecision.REFLEXIONAR, "Pensar en tu propio progreso").efecto(AUTOESTIMA, 3).efecto(CONCENTRACION, 2).minutos(1)
                        .orientacion("Medirte con tu propio avance es más justo y más útil.")));

        registrar("anonimo", TipoPost.ACOSO, 1, 6, () -> pub("@anonimo_77", "Comentó tu foto: \"jajaja qué ridículo te ves\"", TipoPost.ACOSO, 6, null,
                d(TipoDecision.RESPONDER_IMPULSIVO, "Responder con rabia").efecto(RELACIONES, -5).efecto(ANSIEDAD, 9).efecto(SUENO, -2).minutos(5).tension(6)
                        .orientacion("Responder con rabia suele alargar la pelea y empeorar cómo te sientes."),
                d(TipoDecision.REPORTAR, "Reportar y bloquear").efecto(ANSIEDAD, -6).efecto(AUTOESTIMA, 4).minutos(1)
                        .orientacion("Reportar y bloquear es valiente. No tienes que aguantar el acoso."),
                d(TipoDecision.HABLAR_CONFIANZA, "Contárselo a alguien de confianza").efecto(RELACIONES, 6).efecto(ANSIEDAD, -8).minutos(2)
                        .orientacion("Hablar con alguien de confianza ayuda. No estás solo con esto.")));
        registrar("sticker", TipoPost.ACOSO, 2, 6, () -> pub("@grupo.10b", "Alguien subió un sticker tuyo editado al grupo. 23 personas reaccionaron.", TipoPost.ACOSO, 7, null,
                d(TipoDecision.VER_MAS, "Ver todas las reacciones").efecto(AUTOESTIMA, -8).efecto(ANSIEDAD, 8).minutos(5).tension(6),
                d(TipoDecision.REPORTAR, "Reportar el sticker y salir del grupo por hoy").efecto(ANSIEDAD, -5).efecto(AUTOESTIMA, 3).minutos(1)
                        .orientacion("Alejarte de un espacio que te hace daño es una decisión sana."),
                d(TipoDecision.HABLAR_CONFIANZA, "Contarle a un adulto de confianza").efecto(RELACIONES, 5).efecto(ANSIEDAD, -6).minutos(2)
                        .orientacion("Un adulto de confianza puede ayudarte a frenar el ciberacoso.")));
        registrar("troll", TipoPost.ACOSO, 1, 5, () -> pub("@troll.xd", "Respondió a tu comentario: \"nadie te preguntó\"", TipoPost.ACOSO, 4, null,
                d(TipoDecision.RESPONDER_IMPULSIVO, "Discutir hasta tener la razón").efecto(ANSIEDAD, 7).efecto(SUENO, -4).efecto(CONCENTRACION, -3).minutos(10).tension(5)
                        .orientacion("Las discusiones en línea rara vez terminan. Tu descanso vale más."),
                d(TipoDecision.BLOQUEAR, "Ignorar y bloquear").efecto(ANSIEDAD, -4).minutos(1)
                        .orientacion("No tienes que responderle a todo el mundo.")));

        registrar("reto", TipoPost.RETO_PELIGROSO, 1, 3, () -> pub("@trend.max", "#RetoNoDormir: aguanta 48 horas sin dormir y súbelo. ¿Te atreves?", TipoPost.RETO_PELIGROSO, 4, "reto",
                d(TipoDecision.COMPARTIR, "Compartirlo con tus amigos").efecto(SUENO, -4).efecto(ANSIEDAD, 4).efecto(RELACIONES, -2).minutos(2).tension(4)
                        .orientacion("Los retos que ponen en riesgo tu salud no son un juego."),
                d(TipoDecision.REPORTAR, "Reportar contenido peligroso").efecto(ANSIEDAD, -3).efecto(AUTOESTIMA, 3).minutos(1).bandera("reto_reportado")
                        .orientacion("Reportar retos peligrosos protege a otras personas también."),
                d(TipoDecision.IGNORAR, "Ignorarlo")));

        registrar("desconocido", TipoPost.DESCONOCIDO, 1, 5, () -> pub("@usuario_8812", "Solicitud de mensaje: \"Hola, vi tus fotos. ¿Me pasas tu número?\"", TipoPost.DESCONOCIDO, 5, null,
                d(TipoDecision.RESPONDER_IMPULSIVO, "Responderle").efecto(ANSIEDAD, 6).minutos(3).tension(6)
                        .orientacion("Nunca compartas datos personales con desconocidos."),
                d(TipoDecision.REPORTAR, "Bloquear y reportar").efecto(ANSIEDAD, -5).efecto(AUTOESTIMA, 2).minutos(1)
                        .orientacion("Bien hecho: bloquear a desconocidos te protege."),
                d(TipoDecision.IGNORAR, "Ignorar la solicitud").efecto(ANSIEDAD, -1)));
        registrar("premio", TipoPost.DESCONOCIDO, 1, 4, () -> pub("@premios.eco", "¡FELICIDADES! Ganaste un celular nuevo. Ingresa tu contraseña para reclamarlo.", TipoPost.DESCONOCIDO, 2, "premio",
                d(TipoDecision.VER_MAS, "Ingresar tus datos").efecto(ANSIEDAD, 10).efecto(AUTOESTIMA, -3).minutos(3).tension(5)
                        .orientacion("Ninguna app real te pide la contraseña por mensaje. Era una estafa."),
                d(TipoDecision.REPORTAR, "Reportarlo como estafa").efecto(ANSIEDAD, -2).efecto(CONCENTRACION, 2).minutos(1)
                        .orientacion("Bien visto: desconfiar de premios falsos te protege.")));

        registrar("grupo", TipoPost.GRUPO, 1, 7, () -> pub("@curso.10b", "48 mensajes nuevos en \"Curso 10B\"", TipoPost.GRUPO, 3, null,
                d(TipoDecision.VER_MAS, "Leerlos todos ahora").efecto(CONCENTRACION, -6).efecto(SUENO, -4).efecto(ANSIEDAD, 3).minutos(10)
                        .orientacion("No necesitas ponerte al día con todo esta noche."),
                d(TipoDecision.DEJAR_PARA_MANANA, "Leerlos mañana").efecto(ANSIEDAD, -2).efecto(SUENO, 2)
                        .orientacion("Dejar las conversaciones para mañana protege tu sueño."),
                d(TipoDecision.SILENCIAR, "Silenciar el grupo esta noche").efecto(ANSIEDAD, -4).minutos(1)));
        registrar("racha", TipoPost.GRUPO, 1, 6, () -> pub("@eco", "¡Tu racha con @juli.m termina en 1 hora! No la pierdas.", TipoPost.GRUPO, 3, null,
                d(TipoDecision.VER_MAS, "Mandar cualquier cosa para no perderla").efecto(SUENO, -3).efecto(ANSIEDAD, 3).minutos(3)
                        .orientacion("Las rachas están diseñadas para que vuelvas a la app."),
                d(TipoDecision.REFLEXIONAR, "Dejar que se pierda: la amistad no es un número").efecto(ANSIEDAD, -3).efecto(AUTOESTIMA, 2)
                        .orientacion("Tu amistad no depende de una racha.")));

        registrar("bienestar", TipoPost.BIENESTAR, 1, 4, () -> pub("@eco.bienestar", "Llevas un buen rato aquí. Descansar también es cuidarte.", TipoPost.BIENESTAR, -3, "bienestar",
                d(TipoDecision.CERRAR_APP, "Cerrar la app").efecto(SUENO, 4).efecto(ANSIEDAD, -4)
                        .orientacion("Cerrar la app a tiempo es difícil... y muy sano."),
                d(TipoDecision.SILENCIAR, "Activar No molestar").efecto(SUENO, 2).efecto(ANSIEDAD, -3).bandera("no_molestar")
                        .orientacion("Silenciar las notificaciones de noche protege tu sueño.")));
        registrar("consejo", TipoPost.BIENESTAR, 1, 4, () -> pub("@psico.joven", "Tip: dejar el celular lejos de la cama mejora la calidad del sueño.", TipoPost.BIENESTAR, -2, null,
                d(TipoDecision.REFLEXIONAR, "Guardar el consejo").efecto(CONCENTRACION, 2).efecto(SUENO, 1).minutos(1),
                d(TipoDecision.DAR_ME_GUSTA, "Dar me gusta").minutos(1)));

        registrar("meme", TipoPost.MEME, 1, 7, () -> pub("@memes.u", "Yo diciendo \"solo un video más\" a las 3 a. m.", TipoPost.MEME, 0, "meme",
                d(TipoDecision.DAR_ME_GUSTA, "Reírte y seguir").efecto(ANSIEDAD, -1).minutos(2),
                d(TipoDecision.RESPONDER_CALMA, "Mandárselo a un amigo").efecto(RELACIONES, 3).minutos(2)));
        registrar("gato", TipoPost.MEME, 1, 7, () -> pub("@gatitos24", "Este gato juzgándote por seguir despierto.", TipoPost.MEME, 0, "gato",
                d(TipoDecision.DAR_ME_GUSTA, "Dar me gusta").efecto(ANSIEDAD, -1).minutos(1),
                d(TipoDecision.CERRAR_APP, "El gato tiene razón: cerrar la app").efecto(SUENO, 4).efecto(ANSIEDAD, -3)
                        .orientacion("Hasta el gato lo sabe.")));

        registrar("noticia", TipoPost.NOTICIA_FALSA, 1, 5, () -> pub("@noticias.ya", "ÚLTIMA HORA: lo que nadie te quiere contar. Mira el video antes de que lo borren.", TipoPost.NOTICIA_FALSA, 5, "noticia",
                d(TipoDecision.VER_MAS, "Ver el video completo").efecto(ANSIEDAD, 6).efecto(CONCENTRACION, -3).minutos(7).tension(4)
                        .orientacion("Los titulares alarmistas buscan atraparte, no informarte."),
                d(TipoDecision.REFLEXIONAR, "Verificar en una fuente confiable").efecto(CONCENTRACION, 3).efecto(ANSIEDAD, -2).minutos(2)
                        .orientacion("Verificar antes de creer o compartir es pensar con cabeza fría."),
                d(TipoDecision.REPORTAR, "Reportar como información falsa").efecto(ANSIEDAD, -2).minutos(1)));

        registrar("seguidor_cansado", TipoPost.PERTURBADOR, 1, 4, () -> pub("@tu_seguidor", "Te ves cansado, {nombre}.", TipoPost.PERTURBADOR, 8, "oscura_nombre",
                d(TipoDecision.REPORTAR, "Reportar la cuenta").efecto(ANSIEDAD, 2)
                        .orientacion("No se pudo reportar: la cuenta @tu_seguidor no existe."),
                d(TipoDecision.VER_MAS, "Seguir mirando").efecto(ANSIEDAD, 6).tension(6),
                d(TipoDecision.CERRAR_APP, "Cerrar la app").efecto(ANSIEDAD, -2).efecto(SUENO, 2)));
        registrar("nadie", TipoPost.PERTURBADOR, 1, 4, () -> pub("@nadie", "sigues aquí", TipoPost.PERTURBADOR, 8, "oscura",
                d(TipoDecision.VER_MAS, "Responder: \"¿quién eres?\"").efecto(ANSIEDAD, 7).tension(8)
                        .orientacion("Nadie respondió. Pero aparece \"escribiendo...\""),
                d(TipoDecision.CERRAR_APP, "Cerrar la app").efecto(ANSIEDAD, -2)));
        registrar("tu_publicacion", TipoPost.PERTURBADOR, 2, 3, () -> pub("@" + usuarioJugador(), "no puedo dormir", TipoPost.PERTURBADOR, 9, null,
                d(TipoDecision.REPORTAR, "Eliminar la publicación").efecto(ANSIEDAD, 3)
                        .orientacion("Ya no está. Pero alguien alcanzó a verla."),
                d(TipoDecision.VER_MAS, "Ver quién le dio me gusta").efecto(ANSIEDAD, 8).tension(8)
                        .orientacion("Le gustó a 1 persona: @tu_seguidor")));
        registrar("recuerdo", TipoPost.PERTURBADOR, 2, 3, () -> pub("@eco.recuerdos", "Recuerdo de hace 1 año: tú, a las 3:12 a. m.", TipoPost.PERTURBADOR, 8, "estatica",
                d(TipoDecision.VER_MAS, "Ver el recuerdo").efecto(ANSIEDAD, 6).tension(6)
                        .orientacion("El video solo muestra tu cuarto a oscuras. Y algo que respira."),
                d(TipoDecision.CERRAR_APP, "Cerrar la app").efecto(ANSIEDAD, -2)));
    }

    private String usuarioJugador() {
        String limpio = nombreJugador.toLowerCase().replaceAll("[^a-z0-9ñáéíóú_.]", "");
        return limpio.isEmpty() ? "tu_cuenta" : limpio;
    }
}
