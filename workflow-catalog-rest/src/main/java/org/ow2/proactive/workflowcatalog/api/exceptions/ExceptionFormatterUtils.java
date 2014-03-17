package org.ow2.proactive.workflowcatalog.api.exceptions;

import javax.ws.rs.core.Response;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.jboss.resteasy.specimpl.ResponseBuilderImpl;
import static javax.ws.rs.core.Response.ResponseBuilder;

public class ExceptionFormatterUtils {

    public static Response createResponse(Exception exception) {
        ResponseBuilder builder = new ResponseBuilderImpl();
        return builder
                .status(555)
                .entity(new ExceptionBean(exception))
                .build();
    }

    public static Throwable createException(Response response) {
        ExceptionBean entity = response.readEntity(ExceptionBean.class);
        return rebuildThrowableFromBean(entity);
    }

    private static Throwable rebuildThrowableFromBean(ExceptionBean bean) {
        Throwable serverException = bean.getThrowable();
        String exceptionClassName = bean.getClassName();
        String errMsg = (bean.getMessage()==null?"An error has occurred":bean.getMessage());

        if (serverException == null || exceptionClassName == null)
            throw new RuntimeException("Bad exception bean");

        Class<?> exceptionClass = toClass(exceptionClassName);

        if (exceptionClass == null)
            throw new RuntimeException("Cannot find class: " + exceptionClassName);

        Constructor<?> constructor = getConstructor(exceptionClass, String.class, Throwable.class);

        if (constructor == null)
            throw new RuntimeException("Cannot find constructor: " + exceptionClassName);

        Throwable built = null;
        try {
            built = (Throwable) constructor.newInstance(errMsg, serverException);
        } catch (InstantiationException e) {
            throw new RuntimeException("Cannot instantiate: " + exceptionClassName, e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot access constructor: " + exceptionClassName, e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Cannot invoke constructor: " + exceptionClassName, e);
        }

        built.setStackTrace(serverException.getStackTrace());

        return built;

    }

    private static Class<?> toClass(String className) {
        Class<?> clazz = null;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            // returns NULL
        }
        return clazz;
    }

    private static Constructor<?> getConstructor(Class<?> clazz, Class<?>... paramTypes) {
        Constructor<?> ctor = null;
        try {
            ctor = clazz.getConstructor(paramTypes);
        } catch (NoSuchMethodException e) {
            // returns NULL
        }
        return ctor;
    }

}

