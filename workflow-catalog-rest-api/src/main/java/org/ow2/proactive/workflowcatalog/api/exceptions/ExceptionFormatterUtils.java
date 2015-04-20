/*
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 2013-2015 ActiveEon
 * 
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * $$ACTIVEEON_INITIAL_DEV$$
 */


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

