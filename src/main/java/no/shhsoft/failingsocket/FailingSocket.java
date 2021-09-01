package no.shhsoft.failingsocket;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:shh@thathost.com">Sverre H. Huseby</a>
 */
public final class FailingSocket {

    private static Map<InetSocketAddress, FailRule> connectFailures = new HashMap<InetSocketAddress, FailRule>();
    private static Map<InetSocketAddress, FailRule> readFailures = new HashMap<InetSocketAddress, FailRule>();
    private static Map<InetSocketAddress, FailRule> writeFailures = new HashMap<InetSocketAddress, FailRule>();
    private static Set<String> nonFailingClasses = new HashSet<String>();
    public static final int ANY_PORT = 0;

    private FailingSocket() {
    }

    private static void enableFailure(final Map<InetSocketAddress, FailRule> map,
                                      final InetSocketAddress endpoint, final FailRule rule) {
        synchronized (map) {
            map.put(endpoint, rule);
        }
    }

    private static void disableFailure(final Map<InetSocketAddress, FailRule> map,
                                       final InetSocketAddress endpoint) {
        synchronized (map) {
            map.remove(endpoint);
        }
    }

    private static boolean isFailure(final Map<InetSocketAddress, FailRule> map,
                                     final InetSocketAddress endpoint) {
        synchronized (map) {
            FailRule rule = map.get(endpoint);
            if (rule != null && rule.shouldFail(endpoint)) {
                return true;
            }
            final InetSocketAddress anyPortEndpoint = new InetSocketAddress(endpoint.getAddress(), ANY_PORT);
            rule = map.get(anyPortEndpoint);
            if (rule != null && rule.shouldFail(endpoint)) {
                return true;
            }
            return false;
        }
    }

    private static boolean isFailure(final Map<InetSocketAddress, FailRule> map,
                                     final InetSocketAddress[] endpoints) {
        for (final InetSocketAddress endpoint : endpoints) {
            if (isFailure(map, endpoint)) {
                return true;
            }
        }
        return false;
    }

    static boolean isConnectFailure(final InetSocketAddress[] endpoints) {
        return isFailure(connectFailures, endpoints);
    }

    static boolean isReadFailure(final InetSocketAddress[] endpoints) {
        return isFailure(readFailures, endpoints);
    }

    static boolean isWriteFailure(final InetSocketAddress[] endpoints) {
        return isFailure(writeFailures, endpoints);
    }

    public static synchronized void install() {
        FailingSocketImplFactory.install();
    }

    public static synchronized void deinstall() {
        FailingSocketImplFactory.deinstall();
    }

    public static void enableConnectFailure(final InetAddress address, final int port,
                                            final FailRule rule) {
        enableFailure(connectFailures, new InetSocketAddress(address, port), rule);
    }

    public static void enableConnectFailure(final InetAddress address, final int port) {
        enableConnectFailure(address, port, new FailAlwaysRule());
    }

    public static void disableConnectFailure(final InetAddress address, final int port) {
        disableFailure(connectFailures, new InetSocketAddress(address, port));
    }

    public static void enableReadFailure(final InetAddress address, final int port,
                                         final FailRule rule) {
        enableFailure(readFailures, new InetSocketAddress(address, port), rule);
    }

    public static void enableReadFailure(final InetAddress address, final int port) {
        enableReadFailure(address, port, new FailAlwaysRule());
    }

    public static void disableReadFailure(final InetAddress address, final int port) {
        disableFailure(readFailures, new InetSocketAddress(address, port));
    }

    public static void enableWriteFailure(final InetAddress address, final int port,
                                          final FailRule rule) {
        enableFailure(writeFailures, new InetSocketAddress(address, port), rule);
    }

    public static void enableWriteFailure(final InetAddress address, final int port) {
        enableWriteFailure(address, port, new FailAlwaysRule());
    }

    public static void disableWriteFailure(final InetAddress address, final int port) {
        disableFailure(writeFailures, new InetSocketAddress(address, port));
    }

    public static void disableAllFailures() {
        connectFailures.clear();
        readFailures.clear();
        writeFailures.clear();
    }

    public static void addNonFailingClass(final String className) {
        nonFailingClasses.add(className);
    }

    public static void removeNonFailingClass(final String className) {
        nonFailingClasses.remove(className);
    }

    public static boolean isNonFailingClass(final String className) {
        return nonFailingClasses.contains(className);
    }

}
