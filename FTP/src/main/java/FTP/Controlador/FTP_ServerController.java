package FTP.Controlador;

import FTP.Interfaz.FTP_ServerView;
import FTP.Modelo.FTP_ServerManager;
import FTP.Modelo.UserPrefs;
import java.util.List;

/*
 * Controlador principal del servidor FTP.
 * //--- Nkno :? ---
 */

public class FTP_ServerController {
    private final FTP_ServerView vista;
    private final FTP_ServerManager manager;

    public FTP_ServerController(FTP_ServerView vista, FTP_ServerManager manager) {
        this.vista = vista;
        this.manager = manager;
    }

    // Inicia el servidor con una lista de usuarios
    public void iniciarServidor(int puerto, List<UserPrefs> usuarios, FTP_ServerView vista) {
        try {
            manager.iniciar(puerto, usuarios);
            vista.actualizarEstadoServidor("Iniciado", puerto);
            vista.log("[OK] Servidor FTP iniciado.");
        } catch (Exception ex) {
            vista.actualizarEstadoServidor("Error", puerto);
            vista.log("[ERROR] " + ex.getMessage());
        }
    }

    // Inicia el servidor usando la vista por defecto
    public void iniciarServidor(int puerto, List<UserPrefs> usuarios) {
        iniciarServidor(puerto, usuarios, vista);
    }

    // Detiene el servidor FTP
    public void detenerServidor() {
        manager.detener();
        vista.actualizarEstadoServidor("Detenido", 0);
        vista.log("[OK] Servidor detenido.");
    }
}
//--- Nkno :?) ---


//--------------------------------------------------------------------
// FTP_ServerController.java
/*
Por Nkno :?) | https://github.com/NknoL/FTPSuite
No es para solo copiar y pegar, estudia y modifícalo a tu gusto.
*/
//--------------------------------------------------------------------
