package FTP.Modelo;

/*
 * Representa un usuario FTP para almacenar en historial/configuración.
 * //--- Nkno :? ---
 */
public class UserPrefs {
    public String usuario;
    public String pass;
    public String dir;

    public UserPrefs(String usuario, String pass, String dir) {
        this.usuario = usuario;
        this.pass = pass;
        this.dir = dir;
    }

    // Convierte a formato cadena para almacenaje en historial.
    public String toPrefsString() {
        return usuario + "|" + pass + "|" + dir;
    }

    // Reconstruye desde cadena de historial.
    public static UserPrefs fromPrefsString(String s) {
        String[] p = s.split("\\|", -1);
        if (p.length < 3) throw new IllegalArgumentException("No hay suficientes campos en: " + s);
        return new UserPrefs(p[0], p[1], p[2]);
    }

    @Override
    public String toString() {
        return usuario + " (" + dir + ")";
    }
}
//--- Nkno :?) ---


//--------------------------------------------------------------------
// UsuarioPrefs.java
/*
Por Nkno :?) | https://github.com/NknoL/FTPSuite
No es para solo copiar y pegar, estudia y modifícalo a tu gusto.
*/
//--------------------------------------------------------------------
