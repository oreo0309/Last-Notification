package juego.feed;

import estructuras.ColaPrioridad;
import estructuras.ListaEnlazada;
import java.util.Random;

public class Feed {

    private final ListaEnlazada<ElementoFeed> elementos = new ListaEnlazada<>();
    private int nivelScroll;
    private final ColaPrioridad<Notificacion> pendientes =
            new ColaPrioridad<>((a, b) -> Integer.compare(a.urgencia(), b.urgencia()));
    private final GeneradorImagenes imagenes = new GeneradorImagenes();
    private final FabricaPublicacion fabricaPublicacion;
    private final FabricaEventoTerror fabricaEventoTerror;
    private final FabricaNotificacion fabricaNotificacion;
    private final FabricaContenido fabricaMensajesTerror;
    private final Random azar;
    private int noche = 1;
    private int tension;
    private String nombreJugador = "";
    private int vistosSeguidos;

    public Feed(Random azar) {
        this.azar = azar;
        fabricaPublicacion = new FabricaPublicacion(azar, imagenes);
        fabricaEventoTerror = new FabricaEventoTerror(azar, imagenes);
        fabricaNotificacion = new FabricaNotificacion(azar, imagenes, fabricaEventoTerror);
        fabricaMensajesTerror = new FabricaContenido(azar, imagenes) {
            @Override
            public ElementoFeed crear() {
                return fabricaEventoTerror.crearMensaje();
            }
        };
    }

    public void reiniciar(int noche, String nombreJugador) {
        this.noche = noche;
        this.nombreJugador = nombreJugador;
        this.tension = 0;
        elementos.limpiar();
        pendientes.limpiar();
        nivelScroll = 0;
        vistosSeguidos = 0;
        fabricaPublicacion.olvidarRecientes();
        configurarFabricas();
    }

    public void actualizarTension(int tension) {
        this.tension = tension;
        configurarFabricas();
    }

    private void configurarFabricas() {
        FabricaContenido[] fabricas = {fabricaPublicacion, fabricaEventoTerror, fabricaNotificacion, fabricaMensajesTerror};
        for (FabricaContenido f : fabricas) {
            f.configurar(noche, tension, nombreJugador);
        }
    }

    public void generarContenido(int cantidad) {
        for (int i = 0; i < cantidad; i++) {
            FabricaContenido fabrica = elegirFabrica();
            elementos.agregar(fabrica.crear());
        }
        if (!elementos.estaVacia()) {
            elementos.obtener(nivelScroll).marcarVisto();
        }
    }

    private FabricaContenido elegirFabrica() {
        double probTerror = Math.max(0, (tension - 50) / 140.0 + (noche - 1) * 0.04);
        if (azar.nextDouble() < probTerror) {
            return fabricaMensajesTerror;
        }
        return fabricaPublicacion;
    }

    public boolean hacerScroll(int direccion) {
        if (elementos.estaVacia()) {
            generarContenido(4);
        }
        int anterior = nivelScroll;
        nivelScroll = Math.max(0, Math.min(elementos.tamano() - 1, nivelScroll + direccion));
        if (nivelScroll >= elementos.tamano() - 3) {
            generarContenido(3);
        }
        if (nivelScroll == anterior) {
            return false;
        }
        ElementoFeed actual = elementos.obtener(nivelScroll);
        if (!actual.visto()) {
            actual.marcarVisto();
            vistosSeguidos++;
            return true;
        }
        return false;
    }

    public ElementoFeed enfocado() {
        if (elementos.estaVacia()) {
            return null;
        }
        return elementos.obtener(Math.min(nivelScroll, elementos.tamano() - 1));
    }

    public void insertarEnFoco(ElementoFeed elemento) {
        insertarEn(nivelScroll, elemento);
        elemento.marcarVisto();
    }

    public void insertarEn(int indice, ElementoFeed elemento) {
        indice = Math.max(0, Math.min(elementos.tamano(), indice));
        if (indice == elementos.tamano()) {
            elementos.agregar(elemento);
            return;
        }
        ListaEnlazada<ElementoFeed> nueva = new ListaEnlazada<>();
        int i = 0;
        for (ElementoFeed e : elementos) {
            if (i == indice) {
                nueva.agregar(elemento);
            }
            nueva.agregar(e);
            i++;
        }
        elementos.limpiar();
        for (ElementoFeed e : nueva) {
            elementos.agregar(e);
        }
    }

    public void encolarNotificacion(Notificacion n) {
        if (n != null) {
            pendientes.insertar(n);
        }
    }

    public Notificacion siguienteNotificacion() {
        return pendientes.estaVacia() ? null : pendientes.extraer();
    }

    public boolean hayNotificaciones() {
        return !pendientes.estaVacia();
    }

    public int indiceDe(ElementoFeed e) {
        return elementos.indiceDe(e);
    }

    public void enfocar(int indice) {
        if (!elementos.estaVacia()) {
            nivelScroll = Math.max(0, Math.min(elementos.tamano() - 1, indice));
        }
    }

    public void reiniciarRacha() {
        vistosSeguidos = 0;
    }

    public int vistosSeguidos() {
        return vistosSeguidos;
    }

    public ListaEnlazada<ElementoFeed> elementos() {
        return elementos;
    }

    public int nivelScroll() {
        return nivelScroll;
    }

    public FabricaPublicacion fabricaPublicacion() {
        return fabricaPublicacion;
    }

    public FabricaNotificacion fabricaNotificacion() {
        return fabricaNotificacion;
    }

    public FabricaEventoTerror fabricaEventoTerror() {
        return fabricaEventoTerror;
    }

    public GeneradorImagenes imagenes() {
        return imagenes;
    }

    public int noche() {
        return noche;
    }

    public int tension() {
        return tension;
    }
}
