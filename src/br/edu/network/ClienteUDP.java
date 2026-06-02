package br.edu.network;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

public class ClienteUDP {

    private final String host;
    private final int porta;

    public ClienteUDP(String host, int porta) {
        this.host = host;
        this.porta = porta;
    }

    public String descobrirServidor() throws Exception {
        return enviarMensagem("DISCOVER_SERVER");
    }

    public String listarArquivos() throws Exception {
        return enviarMensagem("LIST");
    }

    private String enviarMensagem(String mensagem) throws Exception {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(3000);
            byte[] dadosEnvio = mensagem.getBytes(StandardCharsets.UTF_8);
            DatagramPacket pacoteEnvio = new DatagramPacket(dadosEnvio, dadosEnvio.length, InetAddress.getByName(host),
                    porta);
            socket.send(pacoteEnvio);
            byte[] buffer = new byte[4096];
            DatagramPacket pacoteResposta = new DatagramPacket(buffer, buffer.length);
            socket.receive(pacoteResposta);
            return new String(pacoteResposta.getData(), 0, pacoteResposta.getLength(), StandardCharsets.UTF_8);
        }
    }
}
