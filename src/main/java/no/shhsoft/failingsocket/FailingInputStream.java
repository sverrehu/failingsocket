package no.shhsoft.failingsocket;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.SocketException;

/**
 * @author <a href="mailto:shh@thathost.com">Sverre H. Huseby</a>
 */
final class FailingInputStream
extends InputStream {

    private final InputStream wrappedStream;
    private final InetSocketAddress[] endpoints;

    private void failConditionally()
    throws IOException {
        if (FailingSocket.isReadFailure(endpoints)) {
            throw new SocketException("failing on purpose");
        }
    }

    public FailingInputStream(final InputStream wrappedStream, final InetSocketAddress[] endpoints) {
        this.wrappedStream = wrappedStream;
        this.endpoints = endpoints;
    }

    @Override
    public int available()
    throws IOException {
        return wrappedStream.available();
    }

    @Override
    public void close()
    throws IOException {
        wrappedStream.close();
    }

    @Override
    public synchronized void mark(final int readlimit) {
        wrappedStream.mark(readlimit);
    }

    @Override
    public boolean markSupported() {
        return wrappedStream.markSupported();
    }

    @Override
    public int read()
    throws IOException {
        final int ret = wrappedStream.read();
        failConditionally();
        return ret;
    }

    @Override
    public int read(final byte[] b, final int off, final int len)
    throws IOException {
        final int ret = wrappedStream.read(b, off, len);
        failConditionally();
        return ret;
    }

    @Override
    public int read(final byte[] b)
    throws IOException {
        final int ret = wrappedStream.read(b);
        failConditionally();
        return ret;
    }

    @Override
    public void reset()
    throws IOException {
        wrappedStream.reset();
        failConditionally();
    }

    @Override
    public long skip(final long n)
    throws IOException {
        final long ret = wrappedStream.skip(n);
        failConditionally();
        return ret;
    }

}
