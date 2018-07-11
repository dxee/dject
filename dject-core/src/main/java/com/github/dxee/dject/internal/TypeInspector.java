package com.github.dxee.dject.internal;

import com.google.common.base.Supplier;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Utility class for class, field and method-based introspection.
 *
 * @author elandau
 */
public class TypeInspector {
    /**
     * visitor interface for introspection processing
     */
    public interface TypeVisitor {

        boolean visit(Field field);

        boolean visit(Method method);

        boolean visit(Class<?> clazz);
    }

    public static <R, V extends Supplier<R> & TypeVisitor> R accept(Class<?> type, V visitor) {
        accept(type, (TypeVisitor) visitor);
        return visitor.get();
    }

    public static void accept(Class<?> type, TypeVisitor visitor) {
        if (doAccept(type, visitor)) {
            // check these only once at the top level 
            for (Class<?> iface : type.getInterfaces()) {
                if (!doAccept(iface, visitor)) {
                    break;
                }
            }
        }
    }

    private static boolean doAccept(Class<?> type, TypeVisitor visitor) {
        if (type == null) {
            return false;
        }

        boolean continueVisit = visitor.visit(type);
        if (continueVisit) {
            for (final Field field : type.getDeclaredFields()) {
                continueVisit = visitor.visit(field);
                if (!continueVisit) {
                    break;
                }
            }

            if (continueVisit) {
                for (final Method method : type.getDeclaredMethods()) {
                    continueVisit = visitor.visit(method);
                    if (!continueVisit) {
                        break;
                    }
                }

                if (continueVisit) {
                    continueVisit = doAccept(type.getSuperclass(), visitor);
                }
            }
        }
        return continueVisit;
    }

}
