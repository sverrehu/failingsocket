package no.shhsoft.failingsocket;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;

/**
 * @author <a href="mailto:shh@thathost.com">Sverre H. Huseby</a>
 */
final class EndpointUtils {

    private EndpointUtils() {
    }

    public static InetSocketAddress[] createEndpoints(final InetAddress address, final int port) {
        return new InetSocketAddress[] { new InetSocketAddress(address, port) };
    }

    public static InetSocketAddress[] createEndpoints(final SocketAddress address) {
        if (!(address instanceof InetSocketAddress)) {
            System.err.println("don't know how to handle " + address.getClass().getName()
                               + ": letting it through");
            return new InetSocketAddress[] {};
        }
        final InetAddress ia = ((InetSocketAddress) address).getAddress();
        final int p = ((InetSocketAddress) address).getPort();
        return new InetSocketAddress[] { new InetSocketAddress(ia, p) };
    }

    public static InetSocketAddress[] createEndpoints(final String host, final int port) {
        try {
            final InetAddress[] addresses = InetAddress.getAllByName(host);
            final InetSocketAddress[] endpoints = new InetSocketAddress[addresses.length];
            for (int q = 0; q < endpoints.length; q++) {
                endpoints[q] = new InetSocketAddress(addresses[q], port);
            }
            return endpoints;
        } catch (final UnknownHostException e) {
            System.err.println("unable to look up IP address for `" + host
                               + "': letting it through");
            return new InetSocketAddress[] {};
        }
    }

}
