package no.shhsoft.failingsocket;

import java.net.InetSocketAddress;

/**
 * @author <a href="mailto:shh@thathost.com">Sverre H. Huseby</a>
 */
public final class FailFirstThenOkRule
implements FailRule {

    private int failuresLeft;

    public FailFirstThenOkRule(final int numFailures) {
        failuresLeft = numFailures;
    }

    @Override
    public boolean shouldFail(final InetSocketAddress address) {
        if (failuresLeft == 0) {
            return false;
        }
        --failuresLeft;
        return true;
    }

}
