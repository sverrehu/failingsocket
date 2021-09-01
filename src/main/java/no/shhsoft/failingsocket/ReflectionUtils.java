package no.shhsoft.failingsocket;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author <a href="mailto:shh@thathost.com">Sverre H. Huseby</a>
 */
final class ReflectionUtils {

    private ReflectionUtils() {
    }

    public static Method findMethod(final Class<?> clazz, final String name,
                                    final Class<?>[] paramTypes) {
        Method method;
        Class<?> searchClass = clazz;
        try {
            for (;;) {
                try {
                    method = searchClass.getDeclaredMethod(name, paramTypes);
                } catch (final NoSuchMethodException e) {
                    method = null;
                }
                if (method != null) {
                    break;
                }
                if (searchClass.equals(Object.class)) {
                    break;
                }
                searchClass = searchClass.getSuperclass();
            }
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
        if (method == null) {
            throw new RuntimeException("method `" + name + "' not found.");
        }
        method.setAccessible(true);
        return method;
    }

    public static Object invokeMethod(final Object object, final Method method, final Object[] args)
    throws InvocationTargetException {
        try {
            return method.invoke(object, args);
        } catch (final IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (final IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
