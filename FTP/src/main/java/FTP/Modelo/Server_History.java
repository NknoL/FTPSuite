package FTP.Modelo;

import java.util.*;
import java.io.File;
import java.nio.file.*;

/*
 * Historial de servidores FTP favoritos para conexión rápida.
 * //--- Nkno :? ---
 */

public class Server_History {
    // Carpeta de datos dinámica (ubicada junto al ejecutable)
    public static final String RUTA_DATA = System.getProperty("user.dir") + File.separator + "data" + File.separator;
    public static final String ARCHIVO_SERVERS = RUTA_DATA + "servers.txt";

    private final LinkedHashSet<String> historial = new LinkedHashSet<>();

    public void agregar(Server_BackupGenerator servidor) {
        historial.add(servidor.toPrefsString());
    }

    public List<Server_BackupGenerator> listar() {
        List<Server_BackupGenerator> lista = new ArrayList<>();
        for (String s : historial) {
            try { lista.add(Server_BackupGenerator.fromPrefsString(s)); }
            catch (Exception ignored) {}
        }
        return lista;
    }

    public void guardarArchivo() throws Exception {
        Path carpeta = Paths.get(RUTA_DATA);
        if (!Files.exists(carpeta)) Files.createDirectories(carpeta);
        Files.write(Paths.get(ARCHIVO_SERVERS), historial);
    }
    
    public void eliminar(String nombre) {
        historial.removeIf(s -> {
            String[] campos = s.split("\\|", -1);
            return campos.length >= 1 && campos[0].equals(nombre);
        });
    }

    public void cargarArchivo() throws Exception {
        historial.clear();
        Path archivo = Paths.get(ARCHIVO_SERVERS);
        if (Files.exists(archivo)) {
            historial.addAll(Files.readAllLines(archivo));
        }
    }
}
//--- Nkno :?) ---


//--------------------------------------------------------------------
// Server_History.java
/*
Por Nkno :?) | https://github.com/NknoL/FTPSuite
No es para solo copiar y pegar, estudia y modifícalo a tu gusto.
*/
//--------------------------------------------------------------------
