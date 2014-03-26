package org.ow2.proactive.workflowcatalog.api.exceptions;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.specimpl.ResponseBuilderImpl;

import static javax.ws.rs.core.Response.ResponseBuilder;

public class ExceptionFormatterUtils {

    public static Response createResponse(Exception exception) {
        ResponseBuilder builder = new ResponseBuilderImpl();
        if (exception instanceof WebApplicationException) {
            return builder
              .status(((WebApplicationException) exception).getResponse().getStatusInfo())
              .entity(new ExceptionBean(exception))
              .build();
        } else {
            return builder
              .status(555)
              .entity(new ExceptionBean(exception))
              .build();
        }
    }

    public static Throwable createException(Response response) {
        ExceptionBean entity = response.readEntity(ExceptionBean.class);
        return rebuildThrowableFromBean(entity);
    }

    private static Throwable rebuildThrowableFromBean(ExceptionBean bean) {
        Throwable serverException = bean.getThrowable();
        String exceptionClassName = bean.getExceptionClassName();
        String errMsg = (bean.getExceptionMessage()==null?"An error has occurred":bean.getExceptionMessage());

        if (serverException == null || exceptionClassName == null)
            throw new RuntimeException("Bad exception bean");

        Class<?> exceptionClass = toClass(exceptionClassName);

        if (exceptionClass == null)
            throw new RuntimeException("Cannot find class: " + exceptionClassName);

        Constructor<?> constructorStr = getConstructor(exceptionClass, String.class);
        Constructor<?> constructorStrThr = getConstructor(exceptionClass, String.class, Throwable.class);

        if (constructorStrThr == null && constructorStr == null)
            throw new RuntimeException("Cannot find constructors: " + exceptionClassName);

        Throwable built = null;
        try {

            if (constructorStrThr != null)
                built = (Throwable) constructorStrThr.newInstance(errMsg, serverException);
            else
                built = (Throwable) constructorStr.newInstance(errMsg);

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

