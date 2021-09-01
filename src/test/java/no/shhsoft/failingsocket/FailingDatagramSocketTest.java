package no.shhsoft.failingsocket;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import no.shhsoft.test.net.AbstractUdpDaemon;
import no.shhsoft.test.net.UdpEchoDaemon;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author <a href="mailto:shh@thathost.com">Sverre H. Huseby</a>
 */
public final class FailingDatagramSocketTest
extends AbstractFailingSocketTest {

    private static UdpEchoDaemon udpEchoDaemon;
    private final InetAddress testAddress;
    private final int testPort;

    private void closeSocket(final DatagramSocket socket) {
        if (socket == null) {
            return;
        }
        socket.close();
    }

    private void sendString(final DatagramSocket socket, final String s)
    throws Exception {
        final DatagramPacket packet = new DatagramPacket(s.getBytes(), s.length());
        packet.setAddress(testAddress);
        packet.setPort(testPort);
        socket.send(packet);
    }

    private String receiveString(final DatagramSocket socket)
    throws Exception {
        final byte[] buffer = new byte[65536];
        final DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);
        return new String(packet.getData(), packet.getOffset(), packet.getLength());
    }

    public FailingDatagramSocketTest()
    throws Exception {
        testAddress = InetAddress.getLocalHost();
        testPort = udpEchoDaemon.getListenPort();
    }

    @BeforeClass
    public static void beforeClass() {
        FailingSocket.addNonFailingClass(AbstractUdpDaemon.class.getName());
        FailingSocket.addNonFailingClass(UdpEchoDaemon.class.getName());
        udpEchoDaemon = new UdpEchoDaemon();
        udpEchoDaemon.start();
    }

    @AfterClass
    public static void afterClass() {
        udpEchoDaemon.stop();
    }

    @Test
    public void shouldBeAbleToRunWithoutFailing()
    throws Exception {
        /* just to verify that there is in fact a service to talk to. */
        final DatagramSocket socket = new DatagramSocket();
        socket.setSoTimeout(1000);
        try {
            sendString(socket, "x");
            assertEquals("x", receiveString(socket));
        } finally {
            closeSocket(socket);
        }
    }

    @Test
    public void shouldFailOnWrite()
    throws Exception {
        FailingSocket.enableWriteFailure(testAddress, testPort);
        final DatagramSocket sock = new DatagramSocket();
        try {
            sendString(sock, "x");
            fail("didn't get expected exception");
        } catch (final Exception e) {
            assertCorrectException(e);
        } finally {
            closeSocket(sock);
        }
    }

    @Test
    public void shouldFailOnPeriodicWrite()
    throws Exception {
        FailingSocket.enableWriteFailure(testAddress, testPort, new FailPeriodicallyRule(3));
        for (int q = 0; q < 5; q++) {
            final DatagramSocket sock = new DatagramSocket();
            try {
                sendString(sock, "x");
                sendString(sock, "x");
                try {
                    sendString(sock, "x");
                    fail("didn't get expected exception");
                } catch (final Exception e) {
                    assertCorrectException(e);
                }
            } finally {
                closeSocket(sock);
            }
        }
    }

    @Test
    public void shouldFailOnRead()
    throws Exception {
        FailingSocket.enableReadFailure(testAddress, testPort);
        final DatagramSocket sock = new DatagramSocket();
        try {
            sendString(sock, "x");
            receiveString(sock);
            fail("didn't get expected exception");
        } catch (final Exception e) {
            assertCorrectException(e);
        } finally {
            closeSocket(sock);
        }
    }

    @Test
    public void shouldFailOnPeriodicRead()
    throws Exception {
        FailingSocket.enableReadFailure(testAddress, testPort, new FailPeriodicallyRule(3));
        for (int q = 0; q < 5; q++) {
            final DatagramSocket sock = new DatagramSocket();
            try {
                sendString(sock, "x");
                assertEquals("x", receiveString(sock));
                sendString(sock, "x");
                assertEquals("x", receiveString(sock));
                sendString(sock, "x");
                try {
                    receiveString(sock);
                    fail("didn't get expected exception");
                } catch (final Exception e) {
                    assertCorrectException(e);
                }
            } finally {
                closeSocket(sock);
            }
        }
    }

}
