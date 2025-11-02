package FTP.Controlador;

import org.apache.commons.net.ftp.FTPFile;
import FTP.Modelo.Server_History;
import FTP.Modelo.FTP_ClientManager;
import javax.swing.JOptionPane;
import java.io.File;
import FTP.Interfaz.ViewsInterface;

/*
 * Controlador principal del cliente FTP.
 * //--- Nkno :? ---
 */

public class FTP_ClientController {
    private final ViewsInterface vista;
    private final FTP_ClientManager ftpManager;
    private final Server_History historialServidores = new Server_History();

    public FTP_ClientController(ViewsInterface vista, FTP_ClientManager ftpManager) {
        this.vista = vista;
        this.ftpManager = ftpManager;
    }

    // Acceso al historial (favoritos)
    public Server_History getHistorialServidores() {
        return historialServidores;
    }

    // Conectar a servidor FTP
    public void conectar(String host, int puerto, String usuario, String pass) {
        new Thread(() -> {
            vista.actualizarEstado("Conectando a " + host + ":" + puerto);
            try {
                boolean ok = ftpManager.conectar(host, puerto, usuario, pass);
                if (ok) {
                    vista.actualizarEstado("Conectado a " + host);
                    vista.habilitarControles(true);
                    listarArchivos();
                    vista.log("Conectado exitosamente.");
                } else {
                    vista.actualizarEstado("Falló al conectar");
                    vista.mostrarError("No se pudo conectar a FTP: " + host);
                    vista.habilitarControles(false);
                }
            } catch (Exception e) {
                vista.actualizarEstado("Error de conexión");
                vista.mostrarError("Error conectando: " + e.getMessage());
                vista.habilitarControles(false);
            }
        }).start();
    }

    public void desconectar() {
        new Thread(() -> {
            try {
                ftpManager.desconectar();
                vista.actualizarEstado("Desconectado");
                vista.habilitarControles(false);
                vista.limpiarLista();
                vista.log("Desconectado del servidor.");
            } catch (Exception e) {
                vista.mostrarError("Error al desconectar: " + e.getMessage());
            }
        }).start();
    }

    // Sube archivo al servidor
    public void subirArchivo(File archivo) {
        new Thread(() -> {
            vista.actualizarEstado("Subiendo: " + archivo.getName());
            try {
                boolean ok = ftpManager.subirArchivo(archivo, percent -> vista.actualizarProgreso(percent));
                if (ok) {
                    vista.log("Archivo subido: " + archivo.getName());
                    listarArchivos();
                } else {
                    vista.log("No se pudo subir: " + archivo.getName());
                }
            } catch (Exception e) {
                vista.mostrarError("Error subida: " + e.getMessage());
                vista.actualizarEstado("Error en subida");
            } finally {
                vista.actualizarProgreso(0);
                vista.actualizarEstado("Conectado");
                vista.habilitarControles(true);
            }
        }).start();
    }

    // Descarga archivo del servidor
    public void descargarArchivo(String nombreRemoto, File destino) {
        new Thread(() -> {
            vista.actualizarEstado("Descargando: " + nombreRemoto);
            try {
                boolean ok = ftpManager.descargarArchivo(nombreRemoto, destino, percent -> vista.actualizarProgreso(percent));
                if (ok) {
                    vista.log("Descargado: " + nombreRemoto);
                } else {
                    vista.log("No se pudo descargar: " + nombreRemoto);
                }
            } catch (Exception e) {
                vista.mostrarError("Error descarga: " + e.getMessage());
                vista.actualizarEstado("Error en descarga");
            } finally {
                vista.actualizarProgreso(0);
                vista.actualizarEstado("Conectado");
                vista.habilitarControles(true);
            }
        }).start();
    }

    // Navegación y CRUD
    public void listarArchivos() {
        new Thread(() -> {
            try {
                String dir = ftpManager.getDirectorioActual();
                FTPFile[] archivos = ftpManager.listarArchivos();
                vista.mostrarArchivos(archivos, dir);
            } catch (Exception e) {
                vista.mostrarError("Error listando: " + e.getMessage());
            }
        }).start();
    }

    public void entrarDirectorio(String nombre) {
        new Thread(() -> {
            try {
                boolean ok = ftpManager.cambiarDirectorio(nombre);
                if (ok) listarArchivos();
                else vista.mostrarError("No se pudo abrir directorio: " + nombre);
            } catch (Exception e) { vista.mostrarError("Error: " + e.getMessage()); }
        }).start();
    }

    public void retrocederDirectorio() {
        new Thread(() -> {
            try {
                boolean ok = ftpManager.cambiarAlPadre();
                if (ok) listarArchivos();
                else vista.mostrarError("No se pudo retroceder");
            } catch (Exception e) { vista.mostrarError("Error: " + e.getMessage()); }
        }).start();
    }

    public void eliminarArchivo(String nombre, boolean esDir) {
        new Thread(() -> {
            try {
                boolean ok = esDir ?
                        ftpManager.eliminarDirectorio(nombre) :
                        ftpManager.eliminarArchivo(nombre);
                if (ok) {
                    vista.log((esDir ? "Directorio" : "Archivo") + " eliminado: " + nombre);
                    listarArchivos();
                } else {
                    vista.mostrarError("No se pudo eliminar: " + nombre);
                }
            } catch (Exception e) { vista.mostrarError("Error eliminando: " + e.getMessage()); }
        }).start();
    }

    public void crearDirectorio() {
        String nombre = JOptionPane.showInputDialog(null, "Nuevo nombre de directorio", "Crear directorio", JOptionPane.PLAIN_MESSAGE);
        if (nombre == null || nombre.trim().isEmpty()) return;
        new Thread(() -> {
            try {
                boolean ok = ftpManager.crearDirectorio(nombre.trim());
                if (ok) { vista.log("Directorio creado: " + nombre); listarArchivos(); }
                else vista.mostrarError("No se pudo crear el directorio");
            } catch (Exception e) { vista.mostrarError("Error creando directorio: " + e.getMessage()); }
        }).start();
    }

    public void renombrarArchivo(String nombreViejo) {
        // Detecta si el archivo tiene extensión
        int idx = nombreViejo.lastIndexOf('.');
        String nombreBase = (idx > 0) ? nombreViejo.substring(0, idx) : nombreViejo;
        String extension   = (idx > 0) ? nombreViejo.substring(idx) : "";

        String nombreNuevoBase = JOptionPane.showInputDialog(
            null, 
            "Nuevo nombre para: " + nombreViejo, 
            "Renombrar", 
            JOptionPane.PLAIN_MESSAGE
        );

        if (nombreNuevoBase == null || nombreNuevoBase.trim().isEmpty()) return;
        String nombreNuevo = nombreNuevoBase.trim() + extension; // reconstruye nombre completo

        new Thread(() -> {
            try {
                boolean ok = ftpManager.renombrarArchivo(nombreViejo, nombreNuevo);
                if (ok) { vista.log("Renombrado a: " + nombreNuevo); listarArchivos(); }
                else vista.mostrarError("No se pudo renombrar");
            } catch (Exception e) { vista.mostrarError("Error renombrando: " + e.getMessage()); }
        }).start();
    }
}

//--- Nkno :?) ---


//--------------------------------------------------------------------
// FTP_ClientController.java
/*
Por Nkno :?) | https://github.com/NknoL/FTPSuite
No es para solo copiar y pegar, estudia y modifícalo a tu gusto.
*/
//--------------------------------------------------------------------
