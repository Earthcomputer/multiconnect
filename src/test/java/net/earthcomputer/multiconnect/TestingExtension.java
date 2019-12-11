package net.earthcomputer.multiconnect;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.launch.knot.Knot;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class TestingExtension implements BeforeAllCallback, InvocationInterceptor {

    private ClassLoader knotClassLoader;

    @Override
    public void beforeAll(ExtensionContext context) {
        System.setProperty("fabric.development", "true");
        System.setProperty("fabric.loader.entrypoint", "net.earthcomputer.multiconnect.TestingDummyMain");
        Knot knot = new Knot(EnvType.CLIENT, null);
        knot.init(new String[0]);
        knotClassLoader = knot.getClassLoader();
    }

    @Override
    public <T> T interceptTestClassConstructor(Invocation<T> invocation, ReflectiveInvocationContext<Constructor<T>> invocationContext, ExtensionContext extensionContext) throws Throwable {
        redirectInvocation(invocationContext);
        return invocation.proceed();
    }

    @Override
    public void interceptBeforeAllMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        redirectInvocation(invocationContext);
        invocation.proceed();
    }

    @Override
    public void interceptBeforeEachMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        redirectInvocation(invocationContext);
        invocation.proceed();
    }

    @Override
    public void interceptTestMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        redirectInvocation(invocationContext);
        invocation.proceed();
    }

    @Override
    public <T> T interceptTestFactoryMethod(Invocation<T> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        redirectInvocation(invocationContext);
        return invocation.proceed();
    }

    @Override
    public void interceptTestTemplateMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        redirectInvocation(invocationContext);
        invocation.proceed();
    }

    @Override
    public void interceptAfterEachMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        redirectInvocation(invocationContext);
        invocation.proceed();
    }

    @Override
    public void interceptAfterAllMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        redirectInvocation(invocationContext);
        invocation.proceed();
    }

    private void redirectInvocation(ReflectiveInvocationContext<?> context) {
        Thread.currentThread().setContextClassLoader(knotClassLoader);
        Executable method = context.getExecutable();
        try {
            Class<?> newClass = knotClassLoader.loadClass(method.getDeclaringClass().getName());
            if (context.getTarget().isPresent() && !newClass.isInstance(context.getTarget().get()))
                throw new AssertionError("Cannot match source class "
                        + context.getTarget().get().getClass().getName()
                        + " (ClassLoader " + context.getTarget().get().getClass().getClassLoader().getClass().getName()
                        + ") to new class " + newClass.getName()
                        + " (ClassLoader " + newClass.getClassLoader().getClass().getName() + ")");
            if (method instanceof Method) {
                method = newClass.getDeclaredMethod(method.getName(), method.getParameterTypes());
            } else {
                method = newClass.getDeclaredConstructor(method.getParameterTypes());
            }
            method.setAccessible(true);
            Field methodField = null;
            for (Field field : context.getClass().getDeclaredFields()) {
                if (Executable.class.isAssignableFrom(field.getType())) {
                    methodField = field;
                    break;
                }
            }
            if (methodField == null)
                throw new AssertionError("Could not find method field of class " + context.getClass().getName());
            methodField.setAccessible(true);
            methodField.set(context, method);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }
}
