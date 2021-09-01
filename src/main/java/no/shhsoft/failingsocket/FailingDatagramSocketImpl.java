package no.shhsoft.failingsocket;

import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.net.DatagramSocketImpl;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketException;

/**
 * @author <a href="mailto:shh@thathost.com">Sverre H. Huseby</a>
 */
final class FailingDatagramSocketImpl
extends DatagramSocketImpl {

    private static Constructor<?> defaultDatagramSocketImplConstructor;
    private static Method createMethod;
    private static Method getTTLMethod;
    private static Method getTimeToLiveMethod;
    private static Method setTTLMethod;
    private static Method setTimeToLiveMethod;
    private static Method bindMethod;
    private static Method closeMethod;
    private static Method connectMethod;
    private static Method getOptionMethod;
    private static Method setOptionMethod;
    private static Method getLocalPortMethod;
    private static Method getFileDescriptorMethod;
    private static Method disconnectMethod;
    private static Method sendMethod;
    private static Method receiveMethod;
    private static Method peekDataMethod;
    private static Method peekMethod;
    private static Method leaveGroupMethod;
    private static Method leaveMethod;
    private static Method joinGroupMethod;
    private static Method joinMethod;
    private final DatagramSocketImpl sock;
    private InetSocketAddress[] endpoints;

    static {
        try {
            final Class<?> clazz = Class.forName("java.net.PlainDatagramSocketImpl");
            defaultDatagramSocketImplConstructor = clazz.getDeclaredConstructor();
            defaultDatagramSocketImplConstructor.setAccessible(true);

            createMethod = ReflectionUtils.findMethod(clazz, "create", null);
            getTTLMethod = ReflectionUtils.findMethod(clazz, "getTTL", null);
            getTimeToLiveMethod = ReflectionUtils.findMethod(clazz, "getTimeToLive", null);
            setTTLMethod = ReflectionUtils.findMethod(clazz, "setTTL",
                                                      new Class[] { byte.class });
            setTimeToLiveMethod = ReflectionUtils.findMethod(clazz, "setTimeToLive",
                                                             new Class[] { int.class });
            bindMethod = ReflectionUtils.findMethod(clazz, "bind",
                                                    new Class[] { int.class, InetAddress.class });
            closeMethod = ReflectionUtils.findMethod(clazz, "close", null);
            connectMethod = ReflectionUtils.findMethod(clazz, "connect",
                                                       new Class[] { InetAddress.class, int.class });
            getOptionMethod = ReflectionUtils.findMethod(clazz, "getOption",
                                                         new Class[] { int.class });
            setOptionMethod = ReflectionUtils.findMethod(clazz, "setOption",
                                                         new Class[] { int.class, Object.class });
            getLocalPortMethod = ReflectionUtils.findMethod(clazz, "getLocalPort", null);
            getFileDescriptorMethod = ReflectionUtils.findMethod(clazz, "getFileDescriptor", null);
            disconnectMethod = ReflectionUtils.findMethod(clazz, "disconnect", null);
            sendMethod = ReflectionUtils.findMethod(clazz, "send",
                                                    new Class[] { DatagramPacket.class });
            receiveMethod = ReflectionUtils.findMethod(clazz, "receive",
                                                       new Class[] { DatagramPacket.class });
            peekDataMethod = ReflectionUtils.findMethod(clazz, "peekData",
                                                        new Class[] { DatagramPacket.class });
            peekMethod = ReflectionUtils.findMethod(clazz, "peek",
                                                    new Class[] { InetAddress.class });
            leaveGroupMethod = ReflectionUtils.findMethod(clazz, "leaveGroup",
                                                          new Class[] { SocketAddress.class,
                                                                        NetworkInterface.class });
            leaveMethod = ReflectionUtils.findMethod(clazz, "leave",
                                                     new Class[] { InetAddress.class,
                                                                   NetworkInterface.class });
            joinGroupMethod = ReflectionUtils.findMethod(clazz, "joinGroup",
                                                         new Class[] { SocketAddress.class,
                                                                       NetworkInterface.class });
            joinMethod = ReflectionUtils.findMethod(clazz, "join",
                                                    new Class[] { InetAddress.class,
                                                                  NetworkInterface.class });
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void ignore() {
    }

    private Object invokeMethod(final Method method, final Object[] args) {
        try {
            return ReflectionUtils.invokeMethod(sock, method, args);
        } catch (final InvocationTargetException e) {
            throw new RuntimeException(e);
        }
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

    private void fail(final SocketException e)
    throws SocketException {
        try {
            invokeMethodThrowingIOException(closeMethod, null);
        } catch (final Throwable t) {
            ignore();
        }
        throw e;
    }

    @Override
    protected void bind(final int lport, final InetAddress laddr)
    throws SocketException {
        invokeMethodThrowingSocketException(bindMethod, new Object[] { Integer.valueOf(lport),
                                                                       laddr });
    }

    @Override
    protected void close() {
        invokeMethod(closeMethod, null);
    }

    @Override
    protected void connect(final InetAddress address, final int port)
    throws SocketException {
        endpoints = EndpointUtils.createEndpoints(address, port);
        invokeMethodThrowingSocketException(connectMethod, new Object[] { address,
                                                                          Integer.valueOf(port) });
        if (FailingSocket.isConnectFailure(endpoints)) {
            fail(new SocketException("failing on purpose"));
        }
    }

    @Override
    protected void create()
    throws SocketException {
        invokeMethodThrowingSocketException(createMethod, null);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected byte getTTL()
    throws IOException {
        return ((Byte) invokeMethodThrowingIOException(getTTLMethod, null)).byteValue();
    }

    @Override
    protected int getTimeToLive()
    throws IOException {
        return ((Integer) invokeMethodThrowingIOException(getTimeToLiveMethod, null)).intValue();
    }

    @Override
    protected void join(final InetAddress inetaddr)
    throws IOException {
        invokeMethodThrowingIOException(joinMethod, new Object[] { inetaddr });
    }

    @Override
    protected void joinGroup(final SocketAddress mcastaddr, final NetworkInterface netIf)
    throws IOException {
        invokeMethodThrowingIOException(joinGroupMethod, new Object[] { mcastaddr, netIf });
    }

    @Override
    protected void leave(final InetAddress inetaddr)
    throws IOException {
        invokeMethodThrowingIOException(leaveMethod, new Object[] { inetaddr });
    }

    @Override
    protected void leaveGroup(final SocketAddress mcastaddr, final NetworkInterface netIf)
    throws IOException {
        invokeMethodThrowingIOException(leaveGroupMethod, new Object[] { mcastaddr, netIf });
    }

    @Override
    protected int peek(final InetAddress i)
    throws IOException {
        return ((Integer) invokeMethodThrowingIOException(peekMethod,
                                                          new Object[] { i })).intValue();
    }

    @Override
    protected int peekData(final DatagramPacket p)
    throws IOException {
        return ((Integer) invokeMethodThrowingIOException(peekDataMethod,
                                                          new Object[] { p })).intValue();
    }

    @Override
    protected void receive(final DatagramPacket p)
    throws IOException {
        invokeMethodThrowingIOException(receiveMethod, new Object[] { p });
        if (FailingSocket.isReadFailure(EndpointUtils.createEndpoints(p.getAddress(),
                                                                      p.getPort()))) {
            fail(new SocketException("failing on purpose"));
        }
    }

    @Override
    protected void send(final DatagramPacket p)
    throws IOException {
        invokeMethodThrowingIOException(sendMethod, new Object[] { p });
        if (FailingSocket.isWriteFailure(EndpointUtils.createEndpoints(p.getAddress(),
                                                                       p.getPort()))) {
            fail(new SocketException("failing on purpose"));
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void setTTL(final byte ttl)
    throws IOException {
        invokeMethodThrowingIOException(setTTLMethod, new Object[] { Byte.valueOf(ttl) });
    }

    @Override
    protected void setTimeToLive(final int ttl)
    throws IOException {
        invokeMethodThrowingIOException(setTimeToLiveMethod, new Object[] { Integer.valueOf(ttl) });
    }

    @Override
    protected void disconnect() {
        invokeMethod(disconnectMethod, null);
    }

    @Override
    protected FileDescriptor getFileDescriptor() {
        return (FileDescriptor) invokeMethod(getFileDescriptorMethod, null);
    }

    @Override
    protected int getLocalPort() {
        return ((Integer) invokeMethod(getLocalPortMethod, null)).intValue();
    }

    public FailingDatagramSocketImpl(final DatagramSocketImpl wrappedSocketImpl) {
        sock = wrappedSocketImpl;
    }

    public static DatagramSocketImpl createDefaultDatagramSocketImpl() {
        try {
            return (DatagramSocketImpl) defaultDatagramSocketImplConstructor.newInstance();
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
