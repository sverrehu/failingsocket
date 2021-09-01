package no.shhsoft.failingsocket;

import java.net.InetSocketAddress;

/**
 * @author <a href="mailto:shh@thathost.com">Sverre H. Huseby</a>
 */
public final class FailPeriodicallyRule
implements FailRule {

    private final int interval;
    private long numCalls;

    public FailPeriodicallyRule(final int interval) {
        this.interval = interval;
    }

    @Override
    public boolean shouldFail(final InetSocketAddress endpoint) {
        ++numCalls;
        return numCalls % interval == 0;
    }

}
