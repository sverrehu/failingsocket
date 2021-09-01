package no.shhsoft.failingsocket;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketImpl;

import no.shhsoft.test.net.AbstractTcpDaemon;
import no.shhsoft.test.net.TcpEchoDaemon;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author <a href="mailto:shh@thathost.com">Sverre H. Huseby</a>
 */
public final class FailingSocketTest
extends AbstractFailingSocketTest {

    private static TcpEchoDaemon tcpEchoDaemon;
    private final InetAddress testAddress;
    private final int testPort;

    private Socket createSocket()
    throws Exception {
        final Socket socket = new Socket(testAddress, testPort);
        socket.setSoTimeout(2000);
        return socket;
    }

    private void closeSocket(final Socket socket) {
        if (socket == null) {
            return;
        }
        try {
            socket.close();
        } catch (final IOException e) {
            ignore();
        }
    }

    public FailingSocketTest()
    throws Exception {
        testAddress = InetAddress.getLocalHost();
        testPort = tcpEchoDaemon.getListenPort();
    }

    @BeforeClass
    public static void beforeClass() {
        FailingSocket.addNonFailingClass(AbstractTcpDaemon.class.getName());
        FailingSocket.addNonFailingClass(TcpEchoDaemon.class.getName());
        tcpEchoDaemon = new TcpEchoDaemon();
        tcpEchoDaemon.start();
    }

    @AfterClass
    public static void afterClass() {
        tcpEchoDaemon.stop();
    }

    @Test
    public void shouldBeAbleToRunWithoutFailing()
    throws Exception {
        /* just to verify that there is in fact a service to talk to. */
        final Socket socket = createSocket();
        try {
            socket.getOutputStream().write('x');
            assertEquals('x', socket.getInputStream().read());
        } finally {
            closeSocket(socket);
        }
    }

    @Test
    public void shouldFailOnConnect()
    throws Exception {
        FailingSocket.enableConnectFailure(testAddress, testPort);
        try {
            createSocket();
            fail("didn't get expected exception");
        } catch (final Exception e) {
            assertCorrectException(e);
        }
    }

    @Test
    public void shouldFailOnPeriodicConnect()
    throws Exception {
        FailingSocket.enableConnectFailure(testAddress, testPort, new FailPeriodicallyRule(3));
        for (int q = 0; q < 5; q++) {
            closeSocket(createSocket());
            closeSocket(createSocket());
            Socket sock = null;
            try {
                sock = createSocket();
                fail("didn't get expected exception");
            } catch (final Exception e) {
                assertCorrectException(e);
            } finally {
                closeSocket(sock);
            }
        }
    }

    @Test
    public void shouldFailOnWrite()
    throws Exception {
        FailingSocket.enableWriteFailure(testAddress, testPort);
        Socket sock = null;
        try {
            sock = createSocket();
            sock.getOutputStream().write('x');
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
        final Socket sock = createSocket();
        try {
            final OutputStream out = sock.getOutputStream();
            for (int q = 0; q < 5; q++) {
                out.write('x');
                out.write('x');
                try {
                    out.write('x');
                    fail("didn't get expected exception");
                } catch (final Exception e) {
                    assertCorrectException(e);
                }
            }
        } finally {
            closeSocket(sock);
        }
    }

    @Test
    public void shouldFailOnRead()
    throws Exception {
        FailingSocket.enableReadFailure(testAddress, testPort);
        Socket sock = null;
        try {
            sock = createSocket();
            sock.getOutputStream().write('x');
            sock.getInputStream().read();
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
        final Socket sock = createSocket();
        try {
            final OutputStream out = sock.getOutputStream();
            final InputStream in = sock.getInputStream();
            for (int q = 0; q < 5; q++) {
                out.write('x');
                assertEquals('x', in.read());
                out.write('x');
                assertEquals('x', in.read());
                out.write('x');
                try {
                    in.read();
                    fail("didn't get expected exception");
                } catch (final Exception e) {
                    assertCorrectException(e);
                }
            }
        } finally {
            closeSocket(sock);
        }
    }

    @Test
    public void shouldSetNonFailingClass()
    throws Exception {
        try {
            FailingSocket.addNonFailingClass(getClass().getName());
            SocketImpl socketImpl = FailingSocketImplFactory.getInstance().createSocketImpl();
            assertTrue("wasn't able to disable creation of failing sockets for this class",
                       socketImpl.getClass() != FailingSocketImpl.class);
            FailingSocket.removeNonFailingClass(getClass().getName());
            socketImpl = FailingSocketImplFactory.getInstance().createSocketImpl();
            assertTrue("wasn't able to reenable creation of failing sockets for this class",
                       socketImpl.getClass() == FailingSocketImpl.class);
        } finally {
            FailingSocket.removeNonFailingClass(getClass().getName());
        }
    }

}
