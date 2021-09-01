package no.shhsoft.failingsocket;

import java.net.InetSocketAddress;

/**
 * @author <a href="mailto:shh@thathost.com">Sverre H. Huseby</a>
 */
public interface FailRule {

    boolean shouldFail(InetSocketAddress endpoint);

}
