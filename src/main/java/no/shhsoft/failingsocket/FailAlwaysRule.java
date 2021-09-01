package no.shhsoft.failingsocket;

import java.net.InetSocketAddress;

/**
 * @author <a href="mailto:shh@thathost.com">Sverre H. Huseby</a>
 */
public final class FailAlwaysRule
implements FailRule {

    @Override
    public boolean shouldFail(final InetSocketAddress address) {
        return true;
    }

}
