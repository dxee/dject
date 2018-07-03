package com.github.dxee.dject.lifecycle;

import com.github.dxee.dject.Dject;
import com.github.dxee.dject.TestSupport;
import org.junit.Test;
import org.mockito.Mockito;

public class AutoCloseableTest {
    public static class TestAutoCloseable implements AutoCloseable {
        @Override
        public void close() throws Exception {
        }
    }

    /**
     * try block will auto close injector, close injector in the try body will cause inject close two times,
     * but the closeable resource should only close once
     * @throws Exception
     */
    @Test
    public void onlyCloseOnceForAnAutoCloseableInstance() throws Exception {
        final AutoCloseable autoCloseable = Mockito.mock(TestAutoCloseable.class);
        try(Dject injector = TestSupport.inject(autoCloseable)) {
            injector.close();
        }

        Mockito.verify(autoCloseable, Mockito.times(1)).close();
    }
}
