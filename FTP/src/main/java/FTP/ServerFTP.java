package FTP;

//--------------------------------------------------------------------
// ServerFTP.java
/*
 * Servidor FTP en Java - Lanzador de la aplicación server.
 * Por Nkno :?) | https://github.com/NknoL/FTPSuite
 * No es para solo copiar y pegar, estudia y modifícalo a tu gusto.
 */
//--------------------------------------------------------------------

import FTP.Interfaz.FTP_ServerView;
import FTP.Modelo.FTP_ServerManager;
import FTP.Controlador.FTP_ServerController;

import javax.swing.SwingUtilities;

public class ServerFTP {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FTP_ServerView vista = new FTP_ServerView();
            FTP_ServerManager manager = new FTP_ServerManager();
            FTP_ServerController controller = new FTP_ServerController(vista, manager);
            vista.setController(controller);
            vista.setVisible(true);
        });
    }
}
//--- Nkno :?) ---
