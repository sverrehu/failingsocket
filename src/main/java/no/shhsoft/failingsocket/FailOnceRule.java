package no.shhsoft.failingsocket;

import java.net.InetSocketAddress;

/**
 * @author <a href="mailto:shh@thathost.com">Sverre H. Huseby</a>
 */
public final class FailOnceRule
implements FailRule {

    private boolean mustFail = true;

    @Override
    public boolean shouldFail(final InetSocketAddress address) {
        if (mustFail) {
            mustFail = false;
            return true;
        }
        return false;
    }

}
