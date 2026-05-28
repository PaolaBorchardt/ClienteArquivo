package br.edu.cliente.network;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ClienteTCP {

    private final String host;
    private final int porta;

    public ClienteTCP(String host, int porta) {
        this.host = host;
        this.porta = porta;
    }

    private Socket conectar() throws IOException {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress(host, porta), 3000);
        return socket;
    }

    public String listarArquivos() throws Exception {
        try (Socket socket = conectar(); DataOutputStream saida = new DataOutputStream(socket.getOutputStream()); DataInputStream entrada = new DataInputStream(socket.getInputStream())) {
            saida.writeUTF("LIST");
            saida.flush();
            String status = entrada.readUTF();
            if (!status.equalsIgnoreCase("OK")) {
                return "";
            }
            return entrada.readUTF();
        }
    }

    public void baixarArquivo(String nomeArquivo, String pastaDestino) throws Exception {
        try (Socket socket = conectar(); DataOutputStream saida = new DataOutputStream(socket.getOutputStream()); DataInputStream entrada = new DataInputStream(socket.getInputStream())) {
            saida.writeUTF("DOWNLOAD " + nomeArquivo);
            saida.flush();
            String status = entrada.readUTF();
            if (!status.equalsIgnoreCase("OK")) {
                String erro = entrada.readUTF();
                throw new Exception(erro);
            }
            String nomeRecebido = entrada.readUTF();
            long tamanho = entrada.readLong();
            File pasta = new File(pastaDestino);
            if (!pasta.exists()) {
                pasta.mkdirs();
            }
            File arquivoDestino = new File(pasta, nomeRecebido);
            try (FileOutputStream fos = new FileOutputStream(arquivoDestino)) {
                byte[] buffer = new byte[4096];
                long totalRecebido = 0;
                while (totalRecebido < tamanho) {
                    int bytesLidos = entrada.read(buffer);
                    if (bytesLidos == -1) {
                        break;
                    }
                    fos.write(buffer, 0, bytesLidos);
                    totalRecebido += bytesLidos;
                }
            }
        }
    }
}
