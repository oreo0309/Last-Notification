package juego;

import estructuras.ArbolBinarioBusqueda;
import estructuras.ListaEnlazada;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;

public class RegistroPuntajes {

    private final Path archivo;
    private final ArbolBinarioBusqueda<Integer, String> arbol = new ArbolBinarioBusqueda<>();

    public RegistroPuntajes(Path archivo) {
        this.archivo = archivo;
        cargar();
    }

    private void cargar() {
        if (!Files.exists(archivo)) {
            return;
        }
        try (BufferedReader lector = Files.newBufferedReader(archivo, StandardCharsets.UTF_8)) {
            String linea;
            while ((linea = lector.readLine()) != null) {
                String[] partes = linea.split(";");
                if (partes.length < 3) {
                    continue;
                }
                try {
                    int puntaje = Integer.parseInt(partes[1].trim());
                    arbol.insertar(puntaje, partes[0].trim() + " · " + partes[2].trim());
                } catch (NumberFormatException e) {
                    continue;
                }
            }
        } catch (IOException e) {
            return;
        }
    }

    public void agregar(String nombre, int puntaje, TipoFinal tipoFinal) {
        String limpio = nombre.replace(";", " ");
        arbol.insertar(puntaje, limpio + " · " + tipoFinal.titulo());
        try {
            if (archivo.getParent() != null) {
                Files.createDirectories(archivo.getParent());
            }
            try (BufferedWriter escritor = Files.newBufferedWriter(archivo, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                escritor.write(limpio + ";" + puntaje + ";" + tipoFinal.titulo() + ";" + LocalDate.now());
                escritor.newLine();
            }
        } catch (IOException e) {
            return;
        }
    }

    public ListaEnlazada<ArbolBinarioBusqueda.Par<Integer, String>> mejores(int cantidad) {
        return arbol.mayoresPrimero(cantidad);
    }

    public int cantidad() {
        return arbol.tamano();
    }
}
