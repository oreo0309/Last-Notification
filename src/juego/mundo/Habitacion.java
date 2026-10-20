package juego.mundo;

public enum Habitacion {
    CUARTO("Tu cuarto", "papel_cuarto", "alfombra"),
    PASILLO("Pasillo", "papel_pasillo", "madera"),
    BANO("Baño", "azulejo_bano", "baldosa_bano"),
    PADRES("Cuarto de mamá", "papel_padres", "alfombra"),
    SALA("Sala", "pared_sala", "piso_sala"),
    COCINA("Cocina", "azulejo_cocina", "baldosa_cocina");

    private final String nombre;
    private final String muro;
    private final String piso;

    Habitacion(String nombre, String muro, String piso) {
        this.nombre = nombre;
        this.muro = muro;
        this.piso = piso;
    }

    public String nombre() {
        return nombre;
    }

    public String muro() {
        return muro;
    }

    public String piso() {
        return piso;
    }
}
