package FTP.Modelo;

import java.util.*;
import java.io.File;
import java.nio.file.*;

/*
 * Historial de usuarios FTP configurados localmente.
 * //--- Nkno :? ---
 */

public class UserHistory {
    // Carpeta de datos dinámica (al lado del ejecutable)
    public static final String RUTA_DATA = System.getProperty("user.dir") + File.separator + "data" + File.separator;
    public static final String ARCHIVO_USUARIOS = RUTA_DATA + "usuarios_ftp.txt";

    private final LinkedHashSet<String> historial = new LinkedHashSet<>();

    // Agrega usuario al historial (sin duplicados)
    public void agregar(UserPrefs usuario) {
        historial.add(usuario.toPrefsString());
    }

    // Devuelve lista de usuarios actuales
    public List<UserPrefs> listar() {
        List<UserPrefs> lista = new ArrayList<>();
        for (String s : historial) {
            try { lista.add(UserPrefs.fromPrefsString(s)); }
            catch (Exception ignored) {}
        }
        return lista;
    }

    //Elimina un usuario
    public void eliminar(String usuario, String dir) {
        historial.removeIf(s -> {
            String[] campos = s.split("\\|", -1);
            return campos.length >= 3 && campos[0].equals(usuario) && campos[2].equals(dir);
        });
    }

    // Guarda historial en archivo
    public void guardarArchivo() throws Exception {
        Path carpeta = Paths.get(RUTA_DATA);
        if (!Files.exists(carpeta)) Files.createDirectories(carpeta);
        Files.write(Paths.get(ARCHIVO_USUARIOS), historial);
    }

    // Carga historial desde archivo
    public void cargarArchivo() throws Exception {
        historial.clear();
        Path archivo = Paths.get(ARCHIVO_USUARIOS);
        if (Files.exists(archivo)) {
            historial.addAll(Files.readAllLines(archivo));
        }
    }
}
//--- Nkno :?) ---


//--------------------------------------------------------------------
// UsuarioHistory.java
/*
Por Nkno :?) | https://github.com/NknoL/FTPSuite
No es para solo copiar y pegar, estudia y modifícalo a tu gusto.
*/
//--------------------------------------------------------------------
