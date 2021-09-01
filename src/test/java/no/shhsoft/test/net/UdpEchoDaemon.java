package no.shhsoft.test.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * @author <a href="mailto:shh@thathost.com">Sverre H. Huseby</a>
 */
public final class UdpEchoDaemon
extends AbstractUdpDaemon {

    @Override
    protected void handlePacket(final DatagramPacket packet, final DatagramSocket socket)
    throws IOException {
        socket.send(packet);
    }

    @Override
    public int getListenPort() {
        return 10007;
    }

    public static void main(final String[] args) {
        new UdpEchoDaemon().start().join();
    }

}
