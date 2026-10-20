package juego;

import estructuras.TablaHash;

public class Decision {

    private final TipoDecision tipo;
    private final String texto;
    private final TablaHash<String, Integer> efectos = new TablaHash<>(8);
    private String orientacion;
    private String bandera;
    private int minutos;
    private int tension;

    public Decision(TipoDecision tipo, String texto) {
        this.tipo = tipo;
        this.texto = texto;
    }

    public Decision efecto(String indicador, int delta) {
        efectos.poner(indicador, efectos.obtenerOPorDefecto(indicador, 0) + delta);
        return this;
    }

    public Decision minutos(int minutos) {
        this.minutos = minutos;
        return this;
    }

    public Decision tension(int tension) {
        this.tension = tension;
        return this;
    }

    public Decision orientacion(String texto) {
        this.orientacion = texto;
        return this;
    }

    public Decision bandera(String bandera) {
        this.bandera = bandera;
        return this;
    }

    public void aplicarEfecto(IndicadorBienestar indicador) {
        indicador.modificar(efectos);
    }

    public int impacto() {
        int total = 0;
        for (String clave : IndicadorBienestar.TODOS) {
            int d = efectos.obtenerOPorDefecto(clave, 0);
            total += IndicadorBienestar.esInverso(clave) ? -d : d;
        }
        return total - tension / 2;
    }

    public int efectoEn(String indicador) {
        return efectos.obtenerOPorDefecto(indicador, 0);
    }

    public TablaHash<String, Integer> efectos() {
        return efectos;
    }

    public TipoDecision tipo() {
        return tipo;
    }

    public String texto() {
        return texto;
    }

    public String orientacion() {
        return orientacion;
    }

    public String bandera() {
        return bandera;
    }

    public int minutos() {
        return minutos;
    }

    public int tension() {
        return tension;
    }

    @Override
    public String toString() {
        return texto;
    }
}
