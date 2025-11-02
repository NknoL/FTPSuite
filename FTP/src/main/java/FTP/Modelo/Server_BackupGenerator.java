package FTP.Modelo;

/*
 * Representa un servidor FTP favorito para conexiones rápidas.
 * //--- Nkno :? ---
 */
public class Server_BackupGenerator {
    public String nombre;
    public String host;
    public int puerto;
    public String usuario;
    public String pass;

    public Server_BackupGenerator(String nombre, String host, int puerto, String usuario, String pass) {
        this.nombre = nombre;
        this.host = host;
        this.puerto = puerto;
        this.usuario = usuario;
        this.pass = pass;
    }

    // Convierte a formato cadena para almacenaje en historial.
    public String toPrefsString() {
        return nombre + "|" + host + "|" + puerto + "|" + usuario + "|" + pass;
    }

    // Reconstruye desde cadena de historial.
    public static Server_BackupGenerator fromPrefsString(String s) {
        String[] p = s.split("\\|", -1);
        if (p.length < 5) throw new IllegalArgumentException("No hay suficientes campos en: " + s);
        return new Server_BackupGenerator(p[0], p[1], Integer.parseInt(p[2]), p[3], p[4]);
    }

    @Override
    public String toString() {
        return nombre + " (" + host + ":" + puerto + ") [" + usuario + "]";
    }
}
//--- Nkno :?) ---


//--------------------------------------------------------------------
// Server_BackupGenerator.java
/*
Por Nkno :?) | https://github.com/NknoL/FTPSuite
No es para solo copiar y pegar, estudia y modifícalo a tu gusto.
*/
//--------------------------------------------------------------------
