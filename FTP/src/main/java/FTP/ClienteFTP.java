package FTP;

//--------------------------------------------------------------------
// ClienteFTP.java
/*
 * Cliente FTP en Java - Lanzador de la aplicación.
 * Por Nkno :?) | https://github.com/NknoL/FTPSuite
 * No es para solo copiar y pegar, estudia y modifícalo a tu gusto.
 */
//--------------------------------------------------------------------

import FTP.Interfaz.FTP_ClientView;
import javax.swing.SwingUtilities;

public class ClienteFTP {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new FTP_ClientView().setVisible(true);
        });
    }
}
//--- Nkno :?) ---
