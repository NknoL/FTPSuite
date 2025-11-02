package FTP.Modelo;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.WritePermission;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import java.util.Collections;
import java.util.List;

/*
 * Lógica de manejo del servidor FTP y sus usuarios.
 * //--- Nkno :? ---
 */

public class FTP_ServerManager {
    private FtpServer server;

    // Inicia el servidor FTP con la lista de usuarios
    public void iniciar(int puerto, List<UserPrefs> usuarios) throws Exception {
        FtpServerFactory factory = new FtpServerFactory();
        ListenerFactory listenerFactory = new ListenerFactory();
        listenerFactory.setPort(puerto);
        factory.addListener("default", listenerFactory.createListener());

        PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
        org.apache.ftpserver.ftplet.UserManager userManager = userManagerFactory.createUserManager();

        for (UserPrefs u : usuarios) {
            BaseUser user = new BaseUser();
            user.setName(u.usuario);
            user.setPassword(u.pass);
            user.setHomeDirectory(u.dir);
            user.setAuthorities(Collections.singletonList(new WritePermission()));
            userManager.save(user);
        }
        factory.setUserManager(userManager);

        server = factory.createServer();
        server.start();
    }

    // Detiene el servidor FTP
    public void detener() {
        if (server != null && !server.isStopped()) server.stop();
    }

    // Indica si el servidor está iniciado
    public boolean isIniciado() {
        return server != null && !server.isStopped();
    }
}
//--- Nkno :?) ---


//--------------------------------------------------------------------
// FTP_ServerManager.java
/*
Por Nkno :?) | https://github.com/NknoL/FTPSuite
No es para solo copiar y pegar, estudia y modifícalo a tu gusto.
*/
//--------------------------------------------------------------------
