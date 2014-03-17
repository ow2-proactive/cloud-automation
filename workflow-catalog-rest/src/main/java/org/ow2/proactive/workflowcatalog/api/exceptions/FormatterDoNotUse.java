package org.ow2.proactive.workflowcatalog.api.exceptions;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public final class FormatterDoNotUse {

    public static String stackTraceToString(Throwable aThrowable) {
        Writer result = null;
        PrintWriter printWriter = null;
        try {
            result = new StringWriter();
            printWriter = new PrintWriter(result);
            aThrowable.printStackTrace(printWriter);
        } finally {
            if (printWriter != null)
                printWriter.close();
            if (result != null)
                try {
                    result.close();
                } catch (Exception e) {
                    //was not able to produce the String representing the exception
                    System.out.println("Could not get the stacktrace for the following ex: ");
                    aThrowable.printStackTrace();
                    System.out
                            .println("An exception occured while constructing the String representation of the exception above: ");
                    e.printStackTrace();
                }
        }
        return result.toString();
    }

    private Exception rebuildException(ExceptionBean json) throws IllegalArgumentException,
            InstantiationException, IllegalAccessException, InvocationTargetException {

        Throwable serverException = json.getThrowable();
        String exceptionClassName = json.getClassName();
        String errMsg = json.getMessage();

        if (errMsg == null) {
            errMsg = "An error has occurred.";
        }

        if (serverException != null && exceptionClassName != null) {
            Class<?> exceptionClass = toClass(exceptionClassName);
            if (exceptionClass != null) {
                // wrap the exception serialized in JSON inside an
                // instance of
                // the server exception class
                Constructor<?> constructor = getConstructor(exceptionClass, Throwable.class);
                if (constructor != null) {
                    return (Exception) constructor.newInstance(serverException);
                }
                constructor = getConstructor(exceptionClass, String.class);
                if (constructor != null) {
                    Exception built = (Exception) constructor.newInstance(errMsg);
                    built.setStackTrace(serverException.getStackTrace());
                    return built;
                }
            }
        }

        Exception built = new Exception(errMsg);
        if (serverException != null) {
            built.setStackTrace(serverException.getStackTrace());
        }
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
