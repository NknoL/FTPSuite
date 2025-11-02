package FTP.Interfaz;

import FTP.Controlador.FTP_ClientController;
import FTP.Modelo.Server_BackupGenerator;
import FTP.Modelo.FTP_ClientManager;
import org.apache.commons.net.ftp.FTPFile;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/*
 * Vista principal del cliente FTP.
 * //--- Nkno :? ---
 */
public class FTP_ClientView extends JFrame implements ViewsInterface {

    private final JTextPane areaLog = new JTextPane();
    private final JLabel lblEstado = new JLabel("Desconectado");
    private final JButton btnConectar = new JButton("Conectar");
    private final JButton btnDesconectar = new JButton("Desconectar");
    private final JButton btnSubir = new JButton("📤 Subir");
    private final JButton btnAtras = new JButton("⬅ Atrás");
    private final JButton btnEliminar = new JButton("🗑 Eliminar");
    private final JButton btnCrearDir = new JButton("📁+ Crear Dir");
    private final JButton btnRenombrar = new JButton("✏ Renombrar");
    private final JProgressBar barraProgreso = new JProgressBar(0, 100);
    private final JList<String> listaArchivos;
    private final DefaultListModel<String> modeloLista = new DefaultListModel<>();
    private final JPanel panelBotones;
    private final FTP_ClientController controller;
    private FTPFile[] archivosActuales;

    public FTP_ClientView() {
        setTitle("Cliente FTP - POO ");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 650);
        setLocationRelativeTo(null);
        setResizable(true);
        setLayout(new BorderLayout(8, 8));
        panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        panelBotones.add(btnConectar);
        panelBotones.add(btnDesconectar);
        panelBotones.add(btnSubir);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnRenombrar);
        panelBotones.add(btnCrearDir);
        panelBotones.add(btnAtras);

        JPanel panelEstado = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        JLabel lblTextoEstado = new JLabel("Estado:");
        panelEstado.add(lblTextoEstado);
        panelEstado.add(lblEstado);

        JPanel top = new JPanel(new BorderLayout(8, 8));
        top.add(panelBotones, BorderLayout.CENTER);
        top.add(panelEstado, BorderLayout.EAST);

        listaArchivos = new JList<>(modeloLista);
        listaArchivos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaArchivos.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        JScrollPane scrollLista = new JScrollPane(listaArchivos);
        scrollLista.setBorder(BorderFactory.createTitledBorder("📂 Archivos en servidor"));
        scrollLista.setPreferredSize(new Dimension(700, 400));

        // Log 
        areaLog.setEditable(false);
        areaLog.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        JScrollPane scrollLog = new JScrollPane(areaLog);
        scrollLog.setBorder(BorderFactory.createTitledBorder("📋 Log de operaciones"));
        scrollLog.setPreferredSize(new Dimension(700, 140)); 

        // Panel central: lista + log fijo 
        JPanel panelCentral = new JPanel();
        panelCentral.setLayout(new BorderLayout(0, 6));
        panelCentral.add(scrollLista, BorderLayout.CENTER);
        panelCentral.add(scrollLog, BorderLayout.SOUTH);

        add(top, BorderLayout.NORTH);
        add(panelCentral, BorderLayout.CENTER);
        add(barraProgreso, BorderLayout.SOUTH);

        ((JComponent) getContentPane()).setBorder(new EmptyBorder(10, 10, 10, 10));

        FTP_ClientManager ftpManager = new FTP_ClientManager();
        controller = new FTP_ClientController(this, ftpManager);

        configurarEventos();
        habilitarControles(false);
        actualizarEstado("Desconectado");
    }

    private void ocultarAccionesTransferencia(boolean ocultar) {
        btnAtras.setVisible(!ocultar);
        btnEliminar.setVisible(!ocultar);
        btnCrearDir.setVisible(!ocultar);
        btnRenombrar.setVisible(!ocultar);
        panelBotones.revalidate();
        panelBotones.repaint();
    }

    private void configurarEventos() {
        btnConectar.addActionListener(e -> {
            JTextField campoServidor = new JTextField("localhost");
            JTextField campoPuerto = new JTextField("2221");
            JTextField campoUsuario = new JTextField("anonymous");
            JPasswordField campoPass = new JPasswordField();
            JButton btnGuardarServer = new JButton("⭐ Guardar servidor");
            DefaultListModel<Server_BackupGenerator> modeloFav = new DefaultListModel<>();
            try { controller.getHistorialServidores().cargarArchivo(); }
            catch(Exception ex) { mostrarError("No se pudo cargar favoritos: "+ex.getMessage()); }
            List<Server_BackupGenerator> favoritos = controller.getHistorialServidores().listar();
            for(Server_BackupGenerator s : favoritos) modeloFav.addElement(s);
            JList<Server_BackupGenerator> listaFav = new JList<>(modeloFav);
            listaFav.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            JScrollPane scrollFav = new JScrollPane(listaFav);
            scrollFav.setBorder(BorderFactory.createTitledBorder("⭐ Favoritos"));
            JButton btnBorrarFav = new JButton("Eliminar favorito");
            btnBorrarFav.setEnabled(false);
            listaFav.addListSelectionListener(ev -> {
                boolean sel = !listaFav.isSelectionEmpty();
                btnBorrarFav.setEnabled(sel);
            });
            btnBorrarFav.addActionListener(ev -> {
                Server_BackupGenerator fav = listaFav.getSelectedValue();
                if (fav == null) return;
                int opt = JOptionPane.showConfirmDialog(FTP_ClientView.this,
                    "¿Seguro que deseas eliminar este favorito?\n" + fav,
                    "Eliminar favorito",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
                );
                if (opt != JOptionPane.YES_OPTION) return;
                try {
                    favoritos.remove(fav);
                    controller.getHistorialServidores().eliminar(fav.nombre);
                    controller.getHistorialServidores().guardarArchivo();
                    modeloFav.removeElement(fav);
                    JOptionPane.showMessageDialog(FTP_ClientView.this, "Servidor eliminado de favoritos.");
                } catch(Exception exc) {
                    mostrarError("Error al eliminar: " + exc.getMessage());
                }
            });
            btnGuardarServer.addActionListener(ev -> {
                String servidor = campoServidor.getText().trim();
                String usuario = campoUsuario.getText().trim();
                String pass = new String(campoPass.getPassword()).trim();
                int puerto;
                try { puerto = Integer.parseInt(campoPuerto.getText().trim()); }
                catch (NumberFormatException ex2) { mostrarError("Puerto inválido. Debe ser un número."); return; }
                if (servidor.isEmpty()) { mostrarError("Servidor vacío."); return; }
                Server_BackupGenerator favorito = new Server_BackupGenerator(servidor + ":" + puerto, servidor, puerto, usuario, pass );
                controller.getHistorialServidores().agregar(favorito);
                try {
                    controller.getHistorialServidores().guardarArchivo();
                    modeloFav.addElement(favorito);
                    JOptionPane.showMessageDialog(FTP_ClientView.this, "Servidor guardado en favoritos.");
                } catch (Exception ex3) { mostrarError("Error guardando historial: " + ex3.getMessage()); }
            });
            JPanel panelDatos = new JPanel(new GridLayout(0, 2, 4, 4));
            panelDatos.add(new JLabel("Servidor:"));
            panelDatos.add(campoServidor);
            panelDatos.add(new JLabel("Puerto:"));
            panelDatos.add(campoPuerto);
            panelDatos.add(new JLabel("Usuario:"));
            panelDatos.add(campoUsuario);
            panelDatos.add(new JLabel("Contraseña:"));
            panelDatos.add(campoPass);
            JPanel panelGuardar = new JPanel(new FlowLayout(FlowLayout.CENTER));
            panelGuardar.add(btnGuardarServer);
            JPanel panelFavoritosBotones = new JPanel(new FlowLayout(FlowLayout.CENTER));
            panelFavoritosBotones.add(btnBorrarFav);
            JPanel panelFavoritosTotal = new JPanel(new BorderLayout());
            panelFavoritosTotal.add(scrollFav, BorderLayout.CENTER);
            panelFavoritosTotal.add(panelFavoritosBotones, BorderLayout.SOUTH);
            JPanel panelDialogo = new JPanel();
            panelDialogo.setLayout(new BoxLayout(panelDialogo, BoxLayout.Y_AXIS));
            panelDialogo.add(panelDatos);
            panelDialogo.add(panelGuardar);
            panelDialogo.add(panelFavoritosTotal);
            JButton btnConectarDialog = new JButton("Conectar");
            JButton btnCancelarDialog = new JButton("Cancelar");
            JPanel panelAcciones = new JPanel(new FlowLayout(FlowLayout.CENTER));
            panelAcciones.add(btnConectarDialog);
            panelAcciones.add(btnCancelarDialog);
            panelDialogo.add(panelAcciones);
            JDialog dialog = new JDialog(this, "Conexión FTP", true);
            dialog.setContentPane(panelDialogo);
            dialog.pack();
            dialog.setLocationRelativeTo(this);
            btnConectarDialog.addActionListener(ev -> {
                Server_BackupGenerator favoritoSeleccionado = listaFav.getSelectedValue();
                if (favoritoSeleccionado != null) {
                    controller.conectar(favoritoSeleccionado.host, favoritoSeleccionado.puerto,
                                       favoritoSeleccionado.usuario, favoritoSeleccionado.pass);
                } else {
                    String servidor = campoServidor.getText().trim();
                    String usuario = campoUsuario.getText().trim();
                    String pass = new String(campoPass.getPassword()).trim();
                    int puerto;
                    try { puerto = Integer.parseInt(campoPuerto.getText().trim()); }
                    catch (NumberFormatException ex1) { mostrarError("Puerto inválido. Debe ser un número entero."); return; }
                    if (servidor.isEmpty()) { mostrarError("Debe ingresar un servidor FTP válido."); return; }
                    controller.conectar(servidor, puerto, usuario, pass);
                }
                dialog.dispose();
            });
            btnCancelarDialog.addActionListener(ev -> dialog.dispose());
            dialog.setVisible(true);
        });

        btnDesconectar.addActionListener(e -> {
            controller.desconectar();
            limpiarLista();
            actualizarEstado("Desconectado");
            habilitarControles(false);
        });

        btnSubir.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File archivo = fileChooser.getSelectedFile();
                ocultarAccionesTransferencia(true);
                controller.subirArchivo(archivo);
            }
        });

        btnAtras.addActionListener(e -> controller.retrocederDirectorio());

        btnEliminar.addActionListener(e -> {
            int index = listaArchivos.getSelectedIndex();
            if (index >= 0 && archivosActuales != null && index < archivosActuales.length) {
                FTPFile archivo = archivosActuales[index];
                String tipo = archivo.isDirectory() ? "directorio" : "archivo";
                int opt = JOptionPane.showConfirmDialog(
                    this,
                    "¿Seguro que deseas eliminar el " + tipo + "?\n" + archivo.getName(),
                    "Confirmar eliminación",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
                );
                if (opt != JOptionPane.YES_OPTION) return;
                controller.eliminarArchivo(archivo.getName(), archivo.isDirectory());
            } else {
                mostrarError("Selecciona un archivo o directorio para eliminar");
            }
        });


        btnCrearDir.addActionListener(e -> controller.crearDirectorio());

        btnRenombrar.addActionListener(e -> {
            int index = listaArchivos.getSelectedIndex();
            if (index >= 0 && archivosActuales != null && index < archivosActuales.length) {
                FTPFile archivo = archivosActuales[index];
                controller.renombrarArchivo(archivo.getName());
            } else { mostrarError("Selecciona un archivo o directorio para renombrar"); }
        });

        listaArchivos.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && archivosActuales != null) {
                    int index = listaArchivos.locationToIndex(e.getPoint());
                    if (index >= 0 && index < archivosActuales.length) {
                        FTPFile sel = archivosActuales[index];
                        if (!sel.isDirectory()) {
                            JFileChooser fc = new JFileChooser();
                            fc.setSelectedFile(new File(sel.getName()));
                            if (fc.showSaveDialog(FTP_ClientView.this) == JFileChooser.APPROVE_OPTION) {
                                ocultarAccionesTransferencia(true);
                                controller.descargarArchivo(sel.getName(), fc.getSelectedFile());
                            }
                        } else {
                            controller.entrarDirectorio(sel.getName());
                        }
                    }
                }
            }
        });

        JPopupMenu menuContextual = new JPopupMenu();
        JMenuItem itemDescargar = new JMenuItem("⬇ Descargar");
        JMenuItem itemEliminar = new JMenuItem("🗑 Eliminar");
        JMenuItem itemRenombrar = new JMenuItem("✏ Renombrar");
        itemDescargar.addActionListener(e -> {
            int index = listaArchivos.getSelectedIndex();
            if (index >= 0 && archivosActuales != null && index < archivosActuales.length) {
                FTPFile archivo = archivosActuales[index];
                if (!archivo.isDirectory()) {
                    JFileChooser fc = new JFileChooser();
                    fc.setSelectedFile(new File(archivo.getName()));
                    if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                        ocultarAccionesTransferencia(true);
                        controller.descargarArchivo(archivo.getName(), fc.getSelectedFile());
                    }
                }
            }
        });
        itemEliminar.addActionListener(e -> btnEliminar.doClick());
        itemRenombrar.addActionListener(e -> btnRenombrar.doClick());
        menuContextual.add(itemDescargar);
        menuContextual.add(itemEliminar);
        menuContextual.add(itemRenombrar);

        listaArchivos.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) { if (e.isPopupTrigger()) mostrarMenu(e); }
            @Override
            public void mouseReleased(MouseEvent e) { if (e.isPopupTrigger()) mostrarMenu(e); }
            private void mostrarMenu(MouseEvent e) {
                int index = listaArchivos.locationToIndex(e.getPoint());
                if (index >= 0) {
                    listaArchivos.setSelectedIndex(index);
                    menuContextual.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }

    @Override
    public void log(String mensaje) { log(mensaje, TipoLog.NORMAL); }

    @Override
    public void log(String mensaje, TipoLog tipo) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            SimpleAttributeSet attrs = new SimpleAttributeSet();
            switch (tipo) {
                case ERROR: StyleConstants.setForeground(attrs, Color.RED); break;
                case ADVERTENCIA: StyleConstants.setForeground(attrs, new Color(240,150,0)); break;
                case ESPECIAL: StyleConstants.setForeground(attrs, new Color(0,120,255)); break;
                default: StyleConstants.setForeground(attrs, Color.BLACK); break;
            }
            StyledDocument doc = areaLog.getStyledDocument();
            try {
                doc.insertString(doc.getLength(),
                    "[" + timestamp + "] " + mensaje + "\n",
                    attrs
                );
                areaLog.setCaretPosition(doc.getLength());
            } catch (BadLocationException ex) { /* Ignora */ }
        });
    }

    @Override
    public void mostrarError(String mensaje) {
        log(mensaje, TipoLog.ERROR);
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE));
    }

    @Override
    public void mostrarConfirmacion(String mensaje) {
        log(mensaje, TipoLog.ESPECIAL);
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, mensaje, "Confirmación", JOptionPane.INFORMATION_MESSAGE));
    }

    @Override
    public void mostrarInformacion(String mensaje) {
        log(mensaje, TipoLog.ADVERTENCIA);
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, mensaje, "Información", JOptionPane.INFORMATION_MESSAGE));
    }

    @Override
    public void actualizarEstado(String estado) {
        SwingUtilities.invokeLater(() -> {
            lblEstado.setText(estado);
            String st = estado.toLowerCase();
            if (st.contains("conectado") && !st.contains("desconectado")) {
                lblEstado.setForeground(new Color(0, 180, 60));
            } else if (st.contains("conectando")) {
                lblEstado.setForeground(new Color(50, 120, 240));
            } else if (st.contains("error") || st.contains("desconectado") || st.contains("falló")) {
                lblEstado.setForeground(new Color(200, 0, 0));
            } else if (st.contains("subiendo") || st.contains("descargando") || st.contains("eliminando")) {
                lblEstado.setForeground(new Color(240, 150, 0));
            } else {
                lblEstado.setForeground(Color.DARK_GRAY);
            }
        });
    }

    @Override
    public void habilitarControles(boolean habilitar) {
        SwingUtilities.invokeLater(() -> {
            btnDesconectar.setEnabled(habilitar);
            btnSubir.setEnabled(habilitar);
            btnConectar.setEnabled(!habilitar);
            ocultarAccionesTransferencia(false);
        });
    }

    private String formatSize(long bytes) {
        if (bytes >= 1024L*1024*1024) return String.format("%.2f GB", bytes / (1024.0*1024*1024));
        else if (bytes >= 1024*1024) return String.format("%.2f MB", bytes / (1024.0*1024));
        else if (bytes >= 1024) return String.format("%.1f KB", bytes / 1024.0);
        else return bytes + " bytes";
    }

    @Override
    public void mostrarArchivos(FTPFile[] archivos, String directorioActual) {
        SwingUtilities.invokeLater(() -> {
            modeloLista.clear();
            archivosActuales = archivos;
            if (archivos != null && archivos.length > 0) {
                for (FTPFile f : archivos) {
                    String icono = f.isDirectory() ? "📁" : "📄";
                    String tamaño = f.isDirectory() ? "" : " (" + formatSize(f.getSize()) + ")";
                    modeloLista.addElement(String.format("%s %-40s%s", icono, f.getName(), tamaño));
                }
                log("Listados " + archivos.length + " elementos en: " + directorioActual);
            } else {
                modeloLista.addElement("📂 Directorio vacío");
                log("Directorio vacío: " + directorioActual);
            }
        });
    }

    @Override
    public void limpiarLista() {
        SwingUtilities.invokeLater(() -> { modeloLista.clear(); archivosActuales = null; });
    }

    @Override
    public void actualizarProgreso(int progreso) {
        SwingUtilities.invokeLater(() -> barraProgreso.setValue(progreso));
    }

    @Override
    public void transferenciaPausada(String archivo) { log("Transferencia pausada: " + archivo, TipoLog.ADVERTENCIA); }
    @Override
    public void transferenciaReanudada(String archivo) { log("Transferencia reanudada: " + archivo, TipoLog.ESPECIAL); }
    @Override
    public void transferenciaCancelada(String archivo) { log("Transferencia cancelada: " + archivo, TipoLog.ERROR); }
    @Override
    public void conexionPerdida() {
        log("Conexión perdida con el servidor FTP.", TipoLog.ERROR);
        actualizarEstado("Desconectado");
        habilitarControles(false);
    }
    @Override
    public void tiempoEsperaExcedido() { log("Tiempo de espera excedido en operación.", TipoLog.ADVERTENCIA); }
    @Override
    public void transferenciaCompleta(String archivo) {
        log("Transferencia completa: " + archivo, TipoLog.ESPECIAL);
        barraProgreso.setValue(0);
        ocultarAccionesTransferencia(false);
    }
    @Override
    public void transferenciaError(String archivo, String error) {
        log("Error al transferir " + archivo + ": " + error, TipoLog.ERROR);
        barraProgreso.setValue(0);
        ocultarAccionesTransferencia(false);
    }
    @Override
    public void mostrarHistorialOperaciones(String[] operaciones) {
        log("Historial de operaciones:", TipoLog.ESPECIAL);
        for (String op : operaciones) log("[HIST] " + op, TipoLog.NORMAL);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FTP_ClientView().setVisible(true));
    }
}
//--- Nkno :?) ---
