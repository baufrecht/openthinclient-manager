package org.openthinclient.advisor.swing;

import com.google.common.base.Strings;
import org.openthinclient.advisor.cVerwaltung;
import org.openthinclient.manager.util.http.config.NetworkConfiguration;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;

/**
 * GUI zum Setzen der Proxyeinstellungen.
 *
 * @author Benedikt Diehl
 */
public class ProxyConfigurationDialog extends JDialog {

  private final NetworkConfiguration.ProxyConfiguration proxyConfiguration;
  private javax.swing.JButton jBtnProxyAbort;
  private javax.swing.JButton jBtnProxyOK;
  private javax.swing.JCheckBox jCBoxProxy;
  private javax.swing.JLabel jLbProxyhost;
  private javax.swing.JLabel jLbProxyport;
  private javax.swing.JTextField jTxtProxyHost;
  private javax.swing.JTextField jTxtProxyPort;

  public ProxyConfigurationDialog(NetworkConfiguration.ProxyConfiguration proxyConfiguration) {
    this.proxyConfiguration = proxyConfiguration;
    initComponents();
    setModal(true);
    centerGUI(this);
    readProxySettings();
  }

  public NetworkConfiguration.ProxyConfiguration getProxyConfiguration() {
    return proxyConfiguration;
  }

  private void readProxySettings() {

    jCBoxProxy.setSelected(proxyConfiguration.isEnabled());
    updateEnabledState();
    jTxtProxyHost.setText(proxyConfiguration.getHost());
    jTxtProxyPort.setText(String.valueOf(proxyConfiguration.getPort()));
  }

  /**
   * update the enabled and disabled state of the host and port text fields.
   */
  private void updateEnabledState() {
    jTxtProxyHost.setEnabled(jCBoxProxy.isSelected());
    jTxtProxyPort.setEnabled(jCBoxProxy.isSelected());
  }

  private boolean validateAndSave() {

    final String hostText = jTxtProxyHost.getText();
    final String proxyPortText = jTxtProxyPort.getText();

    if (!jCBoxProxy.isSelected()) {
      // shortcut, there is no need to validate the given values for host and proxy
      proxyConfiguration.setEnabled(false);
      return true;
    }

    if (Strings.isNullOrEmpty(hostText)) {
      showError(jTxtProxyHost, "No hostname has been provided");
      return false;
    }

    if (Strings.isNullOrEmpty(proxyPortText)) {
      showError(jTxtProxyPort, "No port has been provided");
      return false;
    }

    final int proxyPort;
    try {
      proxyPort = Integer.parseInt(proxyPortText);
    } catch (NumberFormatException e) {
      showError(jTxtProxyPort, "Given port is not a number");
      return false;
    }

    proxyConfiguration.setEnabled(true);
    proxyConfiguration.setHost(hostText);
    proxyConfiguration.setPort(proxyPort);
    return true;

  }

  protected void showError(JComponent component, String message) {

    JOptionPane.showMessageDialog(this, message, "Incorrect input", JOptionPane.ERROR_MESSAGE);

  }

  /**
   * Zentriert die Oberfl??che in der Bildschirmmitte.
   */
  private void centerGUI(Window gui) {
    Dimension dm = Toolkit.getDefaultToolkit().getScreenSize();
    double width = dm.getWidth();
    double height = dm.getHeight();
    double xPosition = (width / 2 - gui.getWidth() / 2);
    double yPosition = (height / 2 - gui.getHeight() / 2);
    gui.setLocation((int) xPosition, (int) yPosition);
  }

  /**
   * Schlie??t das Fenster jFrProxy.
   * Alle ??nderungen werden verworfen und die Programmoberfl??che wird wieder Freigegeben.
   * Mit dieser Methode wird der Button jBtnProxyAbort und der X Button abgefangen.
   */
  private void Abbruch() {
    this.dispose();
  }

  /**
   * This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    jLbProxyhost = new javax.swing.JLabel();
    jTxtProxyHost = new javax.swing.JTextField();
    jLbProxyport = new javax.swing.JLabel();
    jCBoxProxy = new javax.swing.JCheckBox();
    jTxtProxyPort = new javax.swing.JTextField();
    jBtnProxyOK = new javax.swing.JButton();
    jBtnProxyAbort = new javax.swing.JButton();

    setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
    setTitle("Proxy settings");
    setResizable(false);
    addWindowListener(new java.awt.event.WindowAdapter() {
      public void windowClosing(java.awt.event.WindowEvent evt) {
        formWindowClosing(evt);
      }
    });

    jLbProxyhost.setText("Proxyhost");

    jLbProxyport.setText("Proxyport");

    jCBoxProxy.setText("Use proxy");
    jCBoxProxy.addActionListener(e -> updateEnabledState());

    jBtnProxyOK.setText("OK");
    jBtnProxyOK.addActionListener(e -> {
      if (validateAndSave()) {
        cVerwaltung.setProxySettings(proxyConfiguration);
        this.dispose();
      }
    });

    jBtnProxyAbort.setText("Cancel");
    jBtnProxyAbort.addActionListener(e -> Abbruch());

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                            .addGap(144, 144, 144)
                            .addComponent(jCBoxProxy)
                            .addContainerGap(187, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                            .addGap(68, 68, 68)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(layout.createSequentialGroup()
                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(jLbProxyhost)
                                                    .addComponent(jLbProxyport))
                                            .addGap(25, 25, 25))
                                    .addComponent(jBtnProxyOK))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 99, Short.MAX_VALUE)
                                            .addComponent(jBtnProxyAbort)
                                            .addGap(100, 100, 100))
                                    .addGroup(layout.createSequentialGroup()
                                            .addGap(56, 56, 56)
                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                                    .addComponent(jTxtProxyPort, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 124, Short.MAX_VALUE)
                                                    .addComponent(jTxtProxyHost, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 124, Short.MAX_VALUE))
                                            .addContainerGap(84, Short.MAX_VALUE))))
    );
    layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(jCBoxProxy)
                            .addGap(20, 20, 20)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLbProxyhost)
                                    .addComponent(jTxtProxyHost, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGap(18, 18, 18)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLbProxyport)
                                    .addComponent(jTxtProxyPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 31, Short.MAX_VALUE)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jBtnProxyOK)
                                    .addComponent(jBtnProxyAbort))
                            .addContainerGap())
    );

    pack();
  }

  /**
   * Dient dazu den X Button der Oberfl??che jFrProxy abzufangen.
   * Die Beendigung der kompletten Anwendung wird verhindert.
   * Stattdessen wird die Methode Abbruch aufgerufen.
   */
  private void formWindowClosing(java.awt.event.WindowEvent evt) {

    this.Abbruch();
  }

  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
      final ProxyConfigurationDialog configDialog = new ProxyConfigurationDialog(new NetworkConfiguration.ProxyConfiguration());
      configDialog.setVisible(true);

      final NetworkConfiguration.ProxyConfiguration config = configDialog.getProxyConfiguration();

      System.out.println(config.isEnabled());
      System.out.println(config.getHost());
      System.out.println(config.getPort());
    });
  }
}
