package br.edu.ui;

import br.edu.network.ClienteTCP;
import br.edu.network.ClienteUDP;
import java.awt.*;
import javax.swing.*;

public class ClienteSwing extends JFrame {

    private JTextField txtHost;
    private JTextField txtPorta;
    private JButton btnDescobrir;
    private JButton btnListar;
    private JButton btnBaixar;
    private JList<String> listaArquivos;
    private DefaultListModel<String> listaModel;
    private JTextArea areaLogs;
    private static final String PASTA_DOWNLOADS = "downloads";

    public ClienteSwing() {
        setTitle("Cliente de Arquivos");
        setSize(850, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        txtHost = new JTextField("localhost", 12);
        txtPorta = new JTextField("5000", 5);
        btnDescobrir = new JButton("Descobrir servidor");
        btnListar = new JButton("Listar arquivos");
        btnBaixar = new JButton("Baixar arquivo");
        listaModel = new DefaultListModel<>();
        listaArquivos = new JList<>(listaModel);
        listaArquivos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        areaLogs = new JTextArea();
        areaLogs.setEditable(false);
        JPanel painelTopo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        painelTopo.add(new JLabel("Host:"));
        painelTopo.add(txtHost);
        painelTopo.add(new JLabel("Porta:"));
        painelTopo.add(txtPorta);
        painelTopo.add(new JLabel("5000 = TCP | 6000 = UDP"));
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.LEFT));
        painelBotoes.add(btnDescobrir);
        painelBotoes.add(btnListar);
        painelBotoes.add(btnBaixar);
        JPanel painelSuperior = new JPanel(new BorderLayout());
        painelSuperior.add(painelTopo, BorderLayout.NORTH);
        painelSuperior.add(painelBotoes, BorderLayout.SOUTH);
        JPanel painelArquivos = new JPanel(new BorderLayout());
        painelArquivos.setBorder(
                BorderFactory.createTitledBorder("Arquivos disponíveis no servidor"));
        painelArquivos.add(new JScrollPane(listaArquivos), BorderLayout.CENTER);
        JPanel painelLogs = new JPanel(new BorderLayout());
        painelLogs.setBorder(BorderFactory.createTitledBorder("Logs da comunicação"));
        painelLogs.add(new JScrollPane(areaLogs), BorderLayout.CENTER);
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, painelArquivos, painelLogs);
        splitPane.setDividerLocation(280);
        add(painelSuperior, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
        btnDescobrir.addActionListener(e -> descobrirServidor());
        btnListar.addActionListener(e -> listarArquivos());
        btnBaixar.addActionListener(e -> baixarArquivoSelecionado());
    }

    private int obterPorta() {
        return Integer.parseInt(txtPorta.getText().trim());
    }

    private String obterHost() {
        return txtHost.getText().trim();
    }

    private boolean isTCP() {
        return obterPorta() == 5000;
    }

    private boolean isUDP() {
        return obterPorta() == 6000;
    }

    private ClienteTCP criarClienteTCP() {
        return new ClienteTCP(obterHost(), obterPorta());
    }

    private ClienteUDP criarClienteUDP() {
        return new ClienteUDP(obterHost(), obterPorta());
    }

    private void descobrirServidor() {
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                if (isTCP()) {
                    adicionarLog("Tentando descobrir servidor via TCP na porta 5000...");
                    ClienteTCP clienteTCP = criarClienteTCP();
                    clienteTCP.listarArquivos();
                    return "Servidor TCP ativo.";
                } else if (isUDP()) {
                    adicionarLog("Tentando descobrir servidor via UDP na porta 6000...");
                    ClienteUDP clienteUDP = criarClienteUDP();
                    return clienteUDP.descobrirServidor();
                } else {
                    throw new Exception("Porta inválida. Use 5000 para TCP ou 6000 para UDP.");
                }
            }

            @Override
            protected void done() {
                try {
                    String resposta = get();
                    adicionarLog("Resposta do servidor: " + resposta);
                    JOptionPane.showMessageDialog(ClienteSwing.this, resposta);
                } catch (Exception e) {
                    adicionarLog("Servidor não encontrado: " + e.getMessage());
                    JOptionPane.showMessageDialog(ClienteSwing.this, "Servidor não encontrado.");
                }
            }
        };
        worker.execute();
    }

    private void listarArquivos() {
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                if (isTCP()) {
                    adicionarLog("Listando arquivos via TCP...");
                    ClienteTCP clienteTCP = criarClienteTCP();
                    return clienteTCP.listarArquivos();
                } else if (isUDP()) {
                    adicionarLog("Listando arquivos via UDP...");
                    ClienteUDP clienteUDP = criarClienteUDP();
                    return clienteUDP.listarArquivos();
                } else {
                    throw new Exception("Porta inválida. Use 5000 ou 6000.");
                }
            }

            @Override
            protected void done() {
                try {
                    String resposta = get();
                    listaModel.clear();
                    if (resposta == null || resposta.isBlank()) {
                        adicionarLog("Nenhum arquivo encontrado.");
                        return;
                    }
                    String[] linhas = resposta.split("\n");
                    for (String linha : linhas) {
                        if (!linha.isBlank()) {
                            String[] partes = linha.split(";");
                            if (partes.length >= 1) {
                                listaModel.addElement(partes[0]);
                            }
                        }
                    }
                    adicionarLog("Arquivos carregados na lista.");
                } catch (Exception e) {
                    adicionarLog("Erro ao listar arquivos: " + e.getMessage());
                    JOptionPane.showMessageDialog(ClienteSwing.this, e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void baixarArquivoSelecionado() {
        String arquivoSelecionado = listaArquivos.getSelectedValue();
        if (arquivoSelecionado == null) {
            JOptionPane.showMessageDialog(this, "Selecione um arquivo.");
            return;
        }
        if (!isTCP()) {
            JOptionPane.showMessageDialog(this, "Download disponível apenas na porta TCP 5000.");
            return;
        }
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                adicionarLog("Baixando arquivo via TCP: " + arquivoSelecionado);
                ClienteTCP clienteTCP = criarClienteTCP();
                clienteTCP.baixarArquivo(arquivoSelecionado, PASTA_DOWNLOADS);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    adicionarLog("Download concluído: " + arquivoSelecionado);
                    adicionarLog("Arquivo salvo na pasta downloads.");
                    JOptionPane.showMessageDialog(ClienteSwing.this, "Download concluído.");
                } catch (Exception e) {
                    adicionarLog("Erro no download: " + e.getMessage());
                    JOptionPane.showMessageDialog(ClienteSwing.this, "Erro no download.");
                }
            }
        };
        worker.execute();
    }

    private void adicionarLog(String mensagem) {
        SwingUtilities.invokeLater(() -> {
            areaLogs.append(mensagem + "\n");
        });
    }
}
