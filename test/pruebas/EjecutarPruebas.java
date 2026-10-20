package pruebas;

public final class EjecutarPruebas {

    private EjecutarPruebas() {
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Pruebas de Última Notificación");
        PruebasEstructuras.ejecutar();
        PruebasLogica.ejecutar();
        int fallidas = Prueba.resumen();
        System.exit(fallidas == 0 ? 0 : 1);
    }
}
