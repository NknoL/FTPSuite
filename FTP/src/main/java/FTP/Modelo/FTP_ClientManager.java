package FTP.Modelo;

import org.apache.commons.net.ftp.*;
import java.io.*;

/*
 * Lógica de conexión y operaciones FTP del cliente.
 * //--- Nkno :? ---
 */

public class FTP_ClientManager {
    private final FTPClient ftp;
    private String currentDirectory = "/";
    private boolean conectado = false;

    public FTP_ClientManager() {
        ftp = new FTPClient();
    }

    public boolean conectar(String servidor, int puerto, String usuario, String password) throws IOException {
        ftp.setConnectTimeout(10000);
        ftp.setDataTimeout(30000);
        ftp.connect(servidor, puerto);
        if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
            ftp.disconnect();
            return false;
        }
        boolean login = ftp.login(usuario, password);
        if (!login) return false;
        ftp.enterLocalPassiveMode();
        ftp.setFileType(FTP.BINARY_FILE_TYPE);
        conectado = true;
        currentDirectory = ftp.printWorkingDirectory();
        return true;
    }

    public void desconectar() throws IOException {
        if (ftp.isConnected()) {
            ftp.logout();
            ftp.disconnect();
        }
        conectado = false;
    }

    public boolean isConectado() {
        return conectado && ftp.isConnected();
    }

    public String getDirectorioActual() throws IOException {
        currentDirectory = ftp.printWorkingDirectory();
        return currentDirectory;
    }

    public FTPFile[] listarArchivos() throws IOException {
        return ftp.listFiles();
    }

    public FTPFile[] listarArchivos(String path) throws IOException {
        return ftp.listFiles(path);
    }

    public boolean cambiarDirectorio(String dir) throws IOException {
        boolean ok = ftp.changeWorkingDirectory(dir);
        if (ok) currentDirectory = ftp.printWorkingDirectory();
        return ok;
    }

    public boolean cambiarAlPadre() throws IOException {
        boolean ok = ftp.changeToParentDirectory();
        if (ok) currentDirectory = ftp.printWorkingDirectory();
        return ok;
    }

    public boolean archivoExisteRemoto(String nombre) throws IOException {
        FTPFile[] files = ftp.listFiles(nombre);
        return files != null && files.length > 0;
    }

    public boolean subirArchivo(File archivo, ProgressListener listener) throws IOException {
        if (!ftp.isConnected()) throw new IOException("No hay conexión FTP activa");
        boolean success = false;
        try (InputStream input = new FileInputStream(archivo);
             OutputStream os = ftp.storeFileStream(archivo.getName())) {

            if (os == null) throw new IOException("No se pudo iniciar la transferencia");

            byte[] buffer = new byte[1024 * 1024];
            long total = archivo.length();
            long enviados = 0;
            int leidos;
            while ((leidos = input.read(buffer)) != -1) {
                os.write(buffer, 0, leidos);
                enviados += leidos;
                if (listener != null && total > 0) {
                    listener.onProgress((int) (enviados * 100 / total));
                }
            }
            os.flush();
            input.close();
            os.close();
            success = ftp.completePendingCommand();
        }
        if (listener != null) listener.onProgress(100);
        return success;
    }

    public boolean descargarArchivo(String nombreRemoto, File destino, ProgressListener listener) throws IOException {
        if (!ftp.isConnected()) throw new IOException("No hay conexión FTP activa");
        FTPFile[] files = ftp.listFiles(nombreRemoto);
        long total = (files != null && files.length > 0) ? files[0].getSize() : 0;
        boolean success = false;
        InputStream input = ftp.retrieveFileStream(nombreRemoto);
        if (input == null) throw new IOException("No se pudo abrir el stream remoto para descarga");
        try (OutputStream output = new FileOutputStream(destino)) {
            byte[] buffer = new byte[1024 * 1024];
            long recibidos = 0;
            int leidos;
            while ((leidos = input.read(buffer)) != -1) {
                output.write(buffer, 0, leidos);
                recibidos += leidos;
                if (listener != null && total > 0)
                    listener.onProgress((int) (recibidos * 100 / total));
            }
            output.flush();
            input.close();
            success = ftp.completePendingCommand();
        }
        if (listener != null) listener.onProgress(100);
        return success;
    }

    public boolean eliminarArchivo(String nombreRemoto) throws IOException {
        if (!ftp.isConnected()) throw new IOException("No hay conexión FTP activa");
        return ftp.deleteFile(nombreRemoto);
    }

    public boolean eliminarDirectorio(String nombreDir) throws IOException {
        if (!ftp.isConnected()) throw new IOException("No hay conexión FTP activa");
        return ftp.removeDirectory(nombreDir);
    }

    public boolean crearDirectorio(String nombreDir) throws IOException {
        if (!ftp.isConnected()) throw new IOException("No hay conexión FTP activa");
        return ftp.makeDirectory(nombreDir);
    }

    public boolean renombrarArchivo(String nombreViejo, String nombreNuevo) throws IOException {
        if (!ftp.isConnected()) throw new IOException("No hay conexión FTP activa");
        return ftp.rename(nombreViejo, nombreNuevo);
    }

    // Listener de progreso para transferencias
    public interface ProgressListener {
        void onProgress(int percent);
    }
}
//--- Nkno :?) ---


//--------------------------------------------------------------------
// FTP_ClientManager.java
/*
Por Nkno :?) | https://github.com/NknoL/FTPSuite
No es para solo copiar y pegar, estudia y modifícalo a tu gusto.
*/
//--------------------------------------------------------------------
