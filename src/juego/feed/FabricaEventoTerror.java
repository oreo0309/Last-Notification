package juego.feed;

import static juego.IndicadorBienestar.ANSIEDAD;
import static juego.IndicadorBienestar.SUENO;

import java.util.Random;
import juego.TipoDecision;

public class FabricaEventoTerror extends FabricaContenido {

    public FabricaEventoTerror(Random azar, GeneradorImagenes imagenes) {
        super(azar, imagenes);
    }

    @Override
    public ElementoFeed crear() {
        String[] posibles;
        if (tension < 30) {
            posibles = new String[]{EventoTerror.PARPADEO, EventoTerror.PASOS, EventoTerror.SUSURRO, EventoTerror.TELEFONO_MESA};
        } else if (tension < 60) {
            posibles = new String[]{EventoTerror.PARPADEO, EventoTerror.PASOS, EventoTerror.SUSURRO, EventoTerror.GOLPES,
                EventoTerror.PUERTA, EventoTerror.GOLPES_VENTANA, EventoTerror.GLITCH, EventoTerror.MENSAJE};
        } else {
            posibles = new String[]{EventoTerror.APARICION, EventoTerror.PRESENCIA, EventoTerror.FOTO, EventoTerror.PORTAZO,
                EventoTerror.ESPEJO, EventoTerror.GLITCH, EventoTerror.GOLPES, EventoTerror.SUSURRO, EventoTerror.MENSAJE,
                noche >= 2 ? EventoTerror.CAZA : EventoTerror.APARICION};
        }
        String tipo = posibles[azar.nextInt(posibles.length)];
        if (EventoTerror.TV.equals(tipo) && noche < 2) {
            tipo = EventoTerror.PASOS;
        }
        int intensidad = 1 + tension / 25 + (noche - 1);
        switch (tipo) {
            case EventoTerror.FOTO:
                return crearFoto();
            case EventoTerror.MENSAJE:
                return crearMensaje();
            default:
                return new EventoTerror(tipo, intensidad);
        }
    }

    public EventoTerror crearFoto() {
        EventoTerror foto = new EventoTerror(EventoTerror.FOTO, 4)
                .conTexto("¿Por qué sigues despierto, {nombre}?")
                .enFeed(true);
        foto.agregarDecision(d(TipoDecision.REPORTAR, "Reportar la foto").efecto(ANSIEDAD, 3)
                .orientacion("No se pudo reportar. La foto se vuelve a publicar sola."));
        foto.agregarDecision(d(TipoDecision.CERRAR_APP, "Cerrar la app y mirar atrás").efecto(ANSIEDAD, -2).efecto(SUENO, 1));
        return foto;
    }

    public EventoTerror crearMensaje() {
        String[] textos = {
            "Sé que estás leyendo esto.",
            "No apagues la pantalla. Me gusta verte la cara iluminada.",
            "Cada vez que deslizas, me acerco un poco más.",
            "¿Escuchaste eso? Fui yo.",
            "Tu mamá llega a las 7. Faltan muchas horas."
        };
        EventoTerror mensaje = new EventoTerror(EventoTerror.MENSAJE, 3)
                .conTexto(textos[azar.nextInt(textos.length)])
                .enFeed(true);
        mensaje.agregarDecision(d(TipoDecision.VER_MAS, "Responder").efecto(ANSIEDAD, 8).tension(8)
                .orientacion("El mensaje se marca como \"visto\" antes de que lo envíes."));
        mensaje.agregarDecision(d(TipoDecision.CERRAR_APP, "Cerrar la app").efecto(ANSIEDAD, -2));
        return mensaje;
    }

    public EventoTerror crearTipo(String tipo, int intensidad) {
        if (EventoTerror.FOTO.equals(tipo)) {
            return crearFoto();
        }
        if (EventoTerror.MENSAJE.equals(tipo)) {
            return crearMensaje();
        }
        return new EventoTerror(tipo, intensidad);
    }
}
