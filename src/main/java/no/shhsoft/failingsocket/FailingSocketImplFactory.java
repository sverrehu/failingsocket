package no.shhsoft.failingsocket;

import java.lang.reflect.Field;
import java.net.DatagramSocketImpl;
import java.net.DatagramSocketImplFactory;
import java.net.SocketImpl;
import java.net.SocketImplFactory;

/**
 * @author <a href="mailto:shh@thathost.com">Sverre H. Huseby</a>
 */
final class FailingSocketImplFactory
implements SocketImplFactory, DatagramSocketImplFactory {

    private static final FailingSocketImplFactory INSTANCE = new FailingSocketImplFactory();
    private static Field socketFactoryField;
    private static SocketImplFactory origSocketImplFactory;
    private static Field datagramSocketFactoryField;
    private static DatagramSocketImplFactory origDatagramSocketImplFactory;

    static {
        try {
            final Class<?> socketClass = Class.forName("java.net.Socket");
            socketFactoryField = socketClass.getDeclaredField("factory");
            socketFactoryField.setAccessible(true);
            final Class<?> datagramSocketClass = Class.forName("java.net.DatagramSocket");
            datagramSocketFactoryField = datagramSocketClass.getDeclaredField("factory");
            datagramSocketFactoryField.setAccessible(true);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean shouldHaveFailingSocket() {
        final StackTraceElement[] elements = new Throwable().getStackTrace();
        for (final StackTraceElement element : elements) {
            if (FailingSocket.isNonFailingClass(element.getClassName())) {
                return false;
            }
        }
        return true;
    }

    private FailingSocketImplFactory() {
    }

    static FailingSocketImplFactory getInstance() {
        return INSTANCE;
    }

    public static synchronized void install() {
        try {
            final SocketImplFactory oldSocketImplFactory
                = (SocketImplFactory) socketFactoryField.get(null);
            if (oldSocketImplFactory != INSTANCE) {
                origSocketImplFactory = oldSocketImplFactory;
                socketFactoryField.set(null, INSTANCE);
            }
            final DatagramSocketImplFactory oldDatagramSocketImplFactory
                = (DatagramSocketImplFactory) datagramSocketFactoryField.get(null);
            if (oldDatagramSocketImplFactory != INSTANCE) {
                origDatagramSocketImplFactory = oldDatagramSocketImplFactory;
                datagramSocketFactoryField.set(null, INSTANCE);
            }
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static synchronized void deinstall() {
        try {
            socketFactoryField.set(null, origSocketImplFactory);
            datagramSocketFactoryField.set(null, origDatagramSocketImplFactory);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public SocketImpl createSocketImpl() {
        SocketImpl wrappedSocketImpl;
        if (origSocketImplFactory != null) {
            wrappedSocketImpl = origSocketImplFactory.createSocketImpl();
        } else {
            wrappedSocketImpl = FailingSocketImpl.createDefaultSocketImpl();
        }
        if (!shouldHaveFailingSocket()) {
            return wrappedSocketImpl;
        }
        return new FailingSocketImpl(wrappedSocketImpl);
    }

    @Override
    public DatagramSocketImpl createDatagramSocketImpl() {
        DatagramSocketImpl wrappedSocketImpl;
        if (origDatagramSocketImplFactory != null) {
            wrappedSocketImpl = origDatagramSocketImplFactory.createDatagramSocketImpl();
        } else {
            wrappedSocketImpl = FailingDatagramSocketImpl.createDefaultDatagramSocketImpl();
        }
        if (!shouldHaveFailingSocket()) {
            return wrappedSocketImpl;
        }
        return new FailingDatagramSocketImpl(wrappedSocketImpl);
    }

}
