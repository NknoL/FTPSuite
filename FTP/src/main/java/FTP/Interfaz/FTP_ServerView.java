package FTP.Interfaz;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.*;
import java.nio.file.*;
import java.util.List;
import FTP.Controlador.FTP_ServerController;
import FTP.Modelo.UserPrefs;
import FTP.Modelo.UserHistory;

/*
 * Vista principal del servidor FTP.
 * //--- Nkno :? ---
 */

public class FTP_ServerView extends JFrame {
    private final JTextField campoPuerto = new JTextField("2221");
    private final JTextField campoRoot = new JTextField("C:/FTP");
    private final JButton btnSeleccionarCarpeta = new JButton("Elegir carpeta");
    private final JTextField campoUsuario = new JTextField();
    private final JPasswordField campoPassword = new JPasswordField();
    private final JLabel lblEstado = new JLabel("Detenido");
    private final JLabel lblIP = new JLabel("IP: " + getLocalIPAddress());
    private final JButton btnRefrescarIP = new JButton("Refrescar IP");
    private final JTextArea areaLog = new JTextArea();
    private final JButton btnIniciar = new JButton("Iniciar Servidor");
    private final JButton btnDetener = new JButton("Detener Servidor");
    private final JButton btnAgregarUsuario = new JButton("Agregar usuario");
    private final JButton btnEliminarUsuario = new JButton("Eliminar usuario");
    private FTP_ServerController controller;
    private final UserHistory usuarioHistory = new UserHistory();
    private final DefaultListModel<UserPrefs> modeloUsuarios = new DefaultListModel<>();
    private final JList<UserPrefs> listaUsuariosVisibles = new JList<>(modeloUsuarios);

    public FTP_ServerView() {
        setTitle("Servidor FTP - POO");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(850, 550);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Panel superior de configuración
        JPanel panelConfig = new JPanel(new GridLayout(0, 2, 4, 4));
        panelConfig.setBorder(BorderFactory.createTitledBorder("Configuración"));

        panelConfig.add(new JLabel("Puerto:"));           panelConfig.add(campoPuerto);
        panelConfig.add(new JLabel("Directorio raíz:"));
        JPanel panelRuta = new JPanel(new BorderLayout());
        panelRuta.add(campoRoot, BorderLayout.CENTER);
        panelRuta.add(btnSeleccionarCarpeta, BorderLayout.EAST);
        panelConfig.add(panelRuta);
        panelConfig.add(new JLabel("Usuario:"));          panelConfig.add(campoUsuario);
        panelConfig.add(new JLabel("Contraseña:"));       panelConfig.add(campoPassword);

        panelConfig.add(new JLabel("IP Servidor:"));
        JPanel panelIp = new JPanel(new BorderLayout());
        panelIp.add(lblIP, BorderLayout.CENTER);
        panelIp.add(btnRefrescarIP, BorderLayout.EAST);
        panelConfig.add(panelIp);

        JPanel botonesInternos = new JPanel(new GridLayout(2, 2, 8, 8));
        botonesInternos.add(btnIniciar);
        botonesInternos.add(btnDetener);
        botonesInternos.add(btnAgregarUsuario);
        botonesInternos.add(btnEliminarUsuario);
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        panelBotones.add(botonesInternos);

        JPanel panelEstado = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelEstado.add(new JLabel("Estado:"));
        panelEstado.add(lblEstado);

        JPanel panelListaUsuarios = new JPanel(new BorderLayout());
        listaUsuariosVisibles.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollUsuarios = new JScrollPane(listaUsuariosVisibles);
        scrollUsuarios.setBorder(BorderFactory.createTitledBorder("Usuarios FTP"));
        panelListaUsuarios.add(scrollUsuarios, BorderLayout.CENTER);

        JPanel panelCentro = new JPanel(new BorderLayout());
        panelCentro.add(panelBotones, BorderLayout.CENTER);
        panelCentro.add(panelListaUsuarios, BorderLayout.EAST);

        areaLog.setEditable(false);
        areaLog.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        JScrollPane scrollLog = new JScrollPane(areaLog);
        scrollLog.setBorder(BorderFactory.createTitledBorder("Log del servidor"));
        scrollLog.setPreferredSize(new Dimension(0, 250));

        add(panelConfig, BorderLayout.NORTH);
        add(panelCentro, BorderLayout.CENTER);
        add(scrollLog, BorderLayout.SOUTH);
        add(panelEstado, BorderLayout.WEST);

        actualizarEstadoColor(lblEstado.getText());

        btnSeleccionarCarpeta.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int ok = chooser.showOpenDialog(this);
            if (ok == JFileChooser.APPROVE_OPTION) campoRoot.setText(chooser.getSelectedFile().getAbsolutePath());
        });

        btnRefrescarIP.addActionListener(e -> actualizarIP());

        btnIniciar.addActionListener(e -> {
            int puerto;
            try {
                puerto = Integer.parseInt(campoPuerto.getText().trim());
                puerto = sugerirPuertoDisponible(puerto);
                campoPuerto.setText(String.valueOf(puerto));
            } catch (NumberFormatException ex) {
                log("[ERROR] Puerto inválido");
                return;
            }
            actualizarIP();
            if (usuarioHistory.listar().isEmpty()) {
                log("[ERROR] Debes agregar al menos un usuario antes de iniciar el servidor.");
                JOptionPane.showMessageDialog(this, "Agrega al menos un usuario antes de iniciar.", "Sin usuarios", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (controller != null)
                controller.iniciarServidor(puerto, usuarioHistory.listar(), this);
            else
                log("[ERROR] No hay controlador asignado.");
        });

        btnDetener.addActionListener(e -> {
            if (controller != null)
                controller.detenerServidor();
            else
                log("[ERROR] No hay controlador asignado.");
        });

        btnAgregarUsuario.addActionListener(e -> {
            JTextField campoUser = new JTextField();
            JTextField campoPass = new JTextField();
            JTextField campoDir = new JTextField(campoRoot.getText());
            Object[] fields = {
                "Usuario:", campoUser,
                "Contraseña:", campoPass,
                "Directorio raíz:", campoDir
            };
            int opt = JOptionPane.showConfirmDialog(this, fields, "Agregar usuario FTP", JOptionPane.OK_CANCEL_OPTION);
            if (opt == JOptionPane.OK_OPTION) {
                UserPrefs nuevo = new UserPrefs(campoUser.getText(), campoPass.getText(), campoDir.getText());
                usuarioHistory.agregar(nuevo);
                guardarUsuarios();
                actualizaListaUsuariosVisual();
                log("[INFO] Usuario agregado: " + nuevo.usuario);
            }
        });

        btnEliminarUsuario.addActionListener(e -> {
            UserPrefs u = listaUsuariosVisibles.getSelectedValue();
            if (u == null) return;
            String clave = JOptionPane.showInputDialog(this, "Clave para eliminar usuario:");
            if ("2904".equals(clave)) {
                usuarioHistory.eliminar(u.usuario, u.dir);  // NUEVA línea
                guardarUsuarios();
                actualizaListaUsuariosVisual();
                log("[INFO] Usuario eliminado: " + u.usuario);
            } else {
                log("[ERROR] Clave incorrecta, no se elimina.");
            }
        });


        cargarUsuarios();
        actualizaListaUsuariosVisual();
        bloquearControles(false);
    }

    public void actualizarEstadoColor(String estado) {
        if ("Iniciado".equalsIgnoreCase(estado)) {
            lblEstado.setForeground(new Color(0, 128, 0)); // Verde
            btnIniciar.setEnabled(false);      
            btnDetener.setEnabled(true);       
        } else if ("Detenido".equalsIgnoreCase(estado)) {
            lblEstado.setForeground(Color.RED);
            btnIniciar.setEnabled(true);       
            btnDetener.setEnabled(false);      
        } else {
            lblEstado.setForeground(Color.BLUE);
        }
    }


    private void actualizaListaUsuariosVisual() {
        modeloUsuarios.clear();
        for (UserPrefs u : usuarioHistory.listar()) modeloUsuarios.addElement(u);
    }

    public void log(String mensaje) {
        SwingUtilities.invokeLater(() -> {
            areaLog.append(mensaje + "\n");
            areaLog.setCaretPosition(areaLog.getDocument().getLength());
        });
    }

    public void actualizarEstadoServidor(String estado, int puerto) {
        SwingUtilities.invokeLater(() -> {
            lblEstado.setText(estado);
            actualizarEstadoColor(estado);
            boolean iniciado = "Iniciado".equalsIgnoreCase(estado);
            bloquearControles(iniciado);
            if (iniciado) {
                log("[OK] Servidor iniciado en IP: " + getLocalIPAddress() +
                    " puerto: " + puerto +
                    "\nAccede por ftp://" + getLocalIPAddress() + ":" + puerto + "/");
            }
        });
    }

    public void setController(FTP_ServerController controller) {
        this.controller = controller;
    }

    public void actualizarEstadoServidor(String estado) {
        actualizarEstadoServidor(estado, Integer.parseInt(campoPuerto.getText().trim()));
    }

    public void actualizarIP() {
        lblIP.setText("IP: " + getLocalIPAddress());
    }

    public String getLocalIPAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "Desconocida";
        }
    }

    public void bloquearControles(boolean bloquear) {
        btnSeleccionarCarpeta.setEnabled(!bloquear);
        campoRoot.setEnabled(!bloquear);
        campoUsuario.setEnabled(!bloquear);
        campoPassword.setEnabled(!bloquear);
        campoPuerto.setEnabled(!bloquear);

        btnAgregarUsuario.setVisible(!bloquear);
        btnEliminarUsuario.setVisible(!bloquear);
    }

    public int sugerirPuertoDisponible(int puertoInicial) {
        int puerto = puertoInicial;
        while (!puertoDisponible(puerto) && puerto < 65535) {
            puerto++;
        }
        if (puerto != puertoInicial) log("[INFO] El puerto estaba ocupado, usando el " + puerto);
        return puerto;
    }
    private boolean puertoDisponible(int puerto) {
        try (java.net.ServerSocket socket = new java.net.ServerSocket(puerto)) {
            socket.setReuseAddress(true);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void guardarUsuarios() {
        try {
            usuarioHistory.guardarArchivo();
        } catch (Exception ex) {
            log("[ERROR] No se pudo guardar usuarios: " + ex.getMessage());
        }
    }

    private void cargarUsuarios() {
        try {
            usuarioHistory.cargarArchivo();
        } catch (Exception ex) {
            log("[ERROR] No se pudo cargar usuarios: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FTP_ServerView().setVisible(true));
    }
}
//--- Nkno :?) ---


//--------------------------------------------------------------------
// FTP_ServerView.java
/*
Por Nkno :?) | https://github.com/NknoL/FTPSuite
No es para solo copiar y pegar, estudia y modifícalo a tu gusto.
*/
//--------------------------------------------------------------------
