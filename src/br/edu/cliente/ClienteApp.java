package br.edu.cliente;

import br.edu.cliente.ui.ClienteSwing;
import javax.swing.SwingUtilities;

public class ClienteApp {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ClienteSwing tela = new ClienteSwing();
            tela.setVisible(true);
        });
    }
}
