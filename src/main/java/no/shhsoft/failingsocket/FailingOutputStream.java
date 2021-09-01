package no.shhsoft.failingsocket;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.SocketException;

/**
 * @author <a href="mailto:shh@thathost.com">Sverre H. Huseby</a>
 */
final class FailingOutputStream
extends OutputStream {

    private final OutputStream wrappedStream;
    private final InetSocketAddress[] endpoints;

    private void failConditionally()
    throws IOException {
        if (FailingSocket.isWriteFailure(endpoints)) {
            throw new SocketException("failing on purpose");
        }
    }

    public FailingOutputStream(final OutputStream wrappedStream, final InetSocketAddress[] endpoints) {
        this.wrappedStream = wrappedStream;
        this.endpoints = endpoints;
    }

    @Override
    public void close()
    throws IOException {
        wrappedStream.close();
    }

    @Override
    public void flush()
    throws IOException {
        wrappedStream.flush();
        failConditionally();
    }

    @Override
    public void write(final byte[] b, final int off, final int len)
    throws IOException {
        wrappedStream.write(b, off, len);
        failConditionally();
    }

    @Override
    public void write(final byte[] b)
    throws IOException {
        wrappedStream.write(b);
        failConditionally();
    }

    @Override
    public void write(final int b)
    throws IOException {
        wrappedStream.write(b);
        failConditionally();
    }

}
