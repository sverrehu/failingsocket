package no.shhsoft.failingsocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketImpl;

/**
 * @author <a href="mailto:shh@thathost.com">Sverre H. Huseby</a>
 */
final class FailingSocketImpl
extends SocketImpl {

    private static Constructor<?> defaultSocketImplConstructor;
    private static Method sendUrgentDataMethod;
    private static Method listenMethod;
    private static Method getOutputStreamMethod;
    private static Method getInputStreamMethod;
    private static Method acceptMethod;
    private static Method availableMethod;
    private static Method bindMethod;
    private static Method closeMethod;
    private static Method connect1Method;
    private static Method connect2Method;
    private static Method connect3Method;
    private static Method createMethod;
    private static Method getOptionMethod;
    private static Method setOptionMethod;
    private final SocketImpl sock;
    private InetSocketAddress[] endpoints;

    static {
        try {
            final Class<?> clazz = Class.forName("java.net.SocksSocketImpl");
            defaultSocketImplConstructor = clazz.getDeclaredConstructor();
            defaultSocketImplConstructor.setAccessible(true);

            acceptMethod = ReflectionUtils.findMethod(clazz, "accept",
                                                      new Class[] { SocketImpl.class });
            availableMethod = ReflectionUtils.findMethod(clazz, "available", null);
            bindMethod = ReflectionUtils.findMethod(clazz, "bind",
                                                    new Class[] { InetAddress.class, int.class });
            closeMethod = ReflectionUtils.findMethod(clazz, "close", null);
            connect1Method = ReflectionUtils.findMethod(clazz, "connect",
                                                        new Class[] { String.class, int.class });
            connect2Method = ReflectionUtils.findMethod(clazz, "connect",
                                                        new Class[] { InetAddress.class, int.class });
            connect3Method = ReflectionUtils.findMethod(clazz, "connect",
                                                        new Class[] { SocketAddress.class, int.class });
            createMethod = ReflectionUtils.findMethod(clazz, "create",
                                                      new Class[] { boolean.class });
            getInputStreamMethod = ReflectionUtils.findMethod(clazz, "getInputStream", null);
            getOutputStreamMethod = ReflectionUtils.findMethod(clazz, "getOutputStream", null);
            listenMethod = ReflectionUtils.findMethod(clazz, "listen", new Class[] { int.class });
            sendUrgentDataMethod = ReflectionUtils.findMethod(clazz, "sendUrgentData",
                                                              new Class[] { int.class });
            getOptionMethod = ReflectionUtils.findMethod(clazz, "getOption",
                                                         new Class[] { int.class });
            setOptionMethod = ReflectionUtils.findMethod(clazz, "setOption",
                                                         new Class[] { int.class, Object.class });
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void ignore() {
    }

    private Object invokeMethodThrowingIOException(final Method method, final Object[] args)
    throws IOException {
        try {
            return ReflectionUtils.invokeMethod(sock, method, args);
        } catch (final InvocationTargetException e) {
            if (e.getCause() != null && e.getCause() instanceof IOException) {
                throw (IOException) e.getCause();
            }
            throw new RuntimeException(e);
        }
    }

    private Object invokeMethodThrowingSocketException(final Method method, final Object[] args)
    throws SocketException {
        try {
            return ReflectionUtils.invokeMethod(sock, method, args);
        } catch (final InvocationTargetException e) {
            if (e.getCause() != null && e.getCause() instanceof SocketException) {
                throw (SocketException) e.getCause();
            }
            throw new RuntimeException(e);
        }
    }

    private void fail(final IOException e)
    throws IOException {
        try {
            invokeMethodThrowingIOException(closeMethod, null);
        } catch (final Throwable t) {
            ignore();
        }
        throw e;
    }

    @Override
    protected void accept(final SocketImpl s)
    throws IOException {
        invokeMethodThrowingIOException(acceptMethod, new Object[] { s });
    }

    @Override
    protected int available()
    throws IOException {
        return ((Integer) invokeMethodThrowingIOException(availableMethod, null)).intValue();
    }

    @Override
    protected void bind(final InetAddress host, final int port)
    throws IOException {
        invokeMethodThrowingIOException(bindMethod, new Object[] { host, Integer.valueOf(port) });
    }

    @Override
    protected void close()
    throws IOException {
        invokeMethodThrowingIOException(closeMethod, null);
    }

    @Override
    protected void connect(final String host, final int port)
    throws IOException {
        endpoints = EndpointUtils.createEndpoints(host, port);
        invokeMethodThrowingIOException(connect1Method, new Object[] { host,
                                                                       Integer.valueOf(port) });
        if (FailingSocket.isConnectFailure(endpoints)) {
            fail(new ConnectException("failing on purpose"));
        }
    }

    @Override
    protected void connect(final InetAddress address, final int port)
    throws IOException {
        endpoints = EndpointUtils.createEndpoints(address, port);
        invokeMethodThrowingIOException(connect2Method, new Object[] { address,
                                                                       Integer.valueOf(port) });
        if (FailingSocket.isConnectFailure(endpoints)) {
            fail(new ConnectException("failing on purpose"));
        }
    }

    @Override
    protected void connect(final SocketAddress address, final int timeout)
    throws IOException {
        endpoints = EndpointUtils.createEndpoints(address);
        invokeMethodThrowingIOException(connect3Method, new Object[] { address,
                                                                       Integer.valueOf(timeout) });
        if (FailingSocket.isConnectFailure(endpoints)) {
            fail(new ConnectException("failing on purpose"));
        }
    }

    @Override
    protected void create(final boolean stream)
    throws IOException {
        invokeMethodThrowingIOException(createMethod, new Object[] { Boolean.valueOf(stream) });
    }

    @Override
    protected InputStream getInputStream()
    throws IOException {
        final InputStream wrappedStream = (InputStream) invokeMethodThrowingIOException(getInputStreamMethod, null);
        if (wrappedStream == null) {
            return null;
        }
        return new FailingInputStream(wrappedStream, endpoints);
    }

    @Override
    protected OutputStream getOutputStream()
    throws IOException {
        final OutputStream wrappedStream = (OutputStream) invokeMethodThrowingIOException(getOutputStreamMethod, null);
        if (wrappedStream == null) {
            return null;
        }
        return new FailingOutputStream(wrappedStream, endpoints);
    }

    @Override
    protected void listen(final int backlog)
    throws IOException {
        invokeMethodThrowingIOException(listenMethod, new Integer[] { Integer.valueOf(backlog) });
    }

    @Override
    protected void sendUrgentData(final int data)
    throws IOException {
        invokeMethodThrowingIOException(sendUrgentDataMethod,
                                        new Integer[] { Integer.valueOf(data) });
    }

    public FailingSocketImpl(final SocketImpl wrappedSocketImpl) {
        sock = wrappedSocketImpl;
    }

    public static SocketImpl createDefaultSocketImpl() {
        try {
            return (SocketImpl) defaultSocketImplConstructor.newInstance();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object getOption(final int optID)
    throws SocketException {
        return invokeMethodThrowingSocketException(getOptionMethod,
                                                   new Object[] { Integer.valueOf(optID) });
    }

    @Override
    public void setOption(final int optID, final Object value)
    throws SocketException {
        invokeMethodThrowingSocketException(setOptionMethod, new Object[] { Integer.valueOf(optID),
                                                                            value });
    }

}
