package no.shhsoft.test.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author <a href="mailto:shh@thathost.com">Sverre H. Huseby</a>
 */
public final class TcpEchoDaemon
extends AbstractTcpDaemon {

    @Override
    protected void handleCommunication(final InputStream in, final OutputStream out)
    throws IOException {
        for (;;) {
            final int c = in.read();
            if (c < 0) {
                break;
            }
            out.write(c);
        }
    }

    @Override
    public int getListenPort() {
        return 10007;
    }

    public static void main(final String[] args) {
        new TcpEchoDaemon().start().join();
    }

}
