package juego.motor;

public interface MapaRender {

    int ancho();

    int alto();

    double altura(int x, int y);

    boolean esPuerta(int x, int y);

    double aperturaPuerta(int x, int y);

    boolean puertaVertical(int x, int y);

    Textura texturaPuerta(int x, int y);

    Textura texturaMuro(int x, int y, int desdeX, int desdeY);

    Textura texturaLado(int x, int y);

    Textura texturaTope(int x, int y);

    Textura texturaPiso(int x, int y);

    Textura texturaTecho(int x, int y);

    boolean bloqueaLuz(int x, int y);
}
