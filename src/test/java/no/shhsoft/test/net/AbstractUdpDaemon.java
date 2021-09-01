package no.shhsoft.test.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;

/**
 * @author <a href="mailto:shh@thathost.com">Sverre H. Huseby</a>
 */
public abstract class AbstractUdpDaemon
extends AbstractDaemon {

    private DatagramSocket socket;

    @Override
    protected final void setup() {
        try {
            socket = new DatagramSocket(getListenPort());
            /* since the accept call cannot be interrupted, we need to let the socket
             * time out periodically to be able to check if the loop should terminate.
             * not really good; should use NIO instead.  later, maybe. */
            socket.setSoTimeout(500);
            socket.setReuseAddress(true);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract void handlePacket(DatagramPacket packet, DatagramSocket socket)
    throws IOException;

    @Override
    public final void run() {
        while (!isDone()) {
            try {
                final byte[] buffer = new byte[65536];
                final DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                handlePacket(packet, socket);
            } catch (final SocketTimeoutException e) {
                ignore();
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }
        socket.close();
    }

}
