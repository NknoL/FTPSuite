package FTP.Interfaz;

import org.apache.commons.net.ftp.FTPFile;

/*
 * Interfaz entre la vista y el controlador.
 * //--- Nkno :? ---
 */

public interface ViewsInterface {
    // Mensaje en el log
    void log(String mensaje);

    // Mensaje en el log con tipo de color/estilo
    void log(String mensaje, TipoLog tipo);

    // Muestra error
    void mostrarError(String mensaje);

    // Muestra confirmación
    void mostrarConfirmacion(String mensaje);

    // Muestra información general
    void mostrarInformacion(String mensaje);

    // Cambia estado visual con texto
    void actualizarEstado(String estado);

    // Habilita/deshabilita controles
    void habilitarControles(boolean habilitar);

    // Lista archivos FTP recibidos
    void mostrarArchivos(FTPFile[] archivos, String directorioActual);

    // Limpia la lista de archivos
    void limpiarLista();

    // Barra de progreso de transferencia
    void actualizarProgreso(int progreso);

    // Transferencia pausada
    void transferenciaPausada(String archivo);

    // Transferencia reanudada
    void transferenciaReanudada(String archivo);

    // Transferencia cancelada
    void transferenciaCancelada(String archivo);

    // Conexión perdida
    void conexionPerdida();

    // Tiempo de espera excedido
    void tiempoEsperaExcedido();

    // Transferencia completa
    void transferenciaCompleta(String archivo);

    // Error en transferencia
    void transferenciaError(String archivo, String error);

    // Muestra historial de operaciones
    void mostrarHistorialOperaciones(String[] operaciones);
}

//Enum para tipos de log.
enum TipoLog {
    NORMAL,
    ERROR,
    ADVERTENCIA,
    ESPECIAL
}

//--------------------------------------------------------------------
// ViewsInterface.java
/*
Por Nkno :?) | https://github.com/NknoL/FTPSuite
No es para solo copiar y pegar, estudia y modifícalo a tu gusto.
*/
//
//--------------------------------------------------------------------