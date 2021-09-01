package no.shhsoft.failingsocket;

import static org.junit.Assert.fail;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * @author <a href="mailto:shh@thathost.com">Sverre H. Huseby</a>
 */
public abstract class AbstractFailingSocketTest {

    protected static void ignore() {
    }

    protected final void assertCorrectException(final Exception e) {
        if (!"failing on purpose".equals(e.getMessage())) {
            fail("got the wrong exception");
        }
    }

    @BeforeClass
    public static void beforeAbstractFailingSocketTest() {
        FailingSocket.install();
    }

    @AfterClass
    public static void afterAbstractFailingSocketTest() {
        FailingSocket.disableAllFailures();
        FailingSocket.deinstall();
    }

    @Before
    public final void before() {
        FailingSocket.disableAllFailures();
    }

}
