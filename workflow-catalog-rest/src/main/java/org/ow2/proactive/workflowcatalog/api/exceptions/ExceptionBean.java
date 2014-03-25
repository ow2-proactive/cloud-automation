package org.ow2.proactive.workflowcatalog.api.exceptions;

import java.io.Serializable;

import javax.ws.rs.Produces;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name = "exception")
@XmlAccessorType(XmlAccessType.FIELD)
@Produces("application/json")
public class ExceptionBean implements Serializable {

    private String exceptionMessage;
    private String exceptionClassName;
    private Throwable throwable;

    public ExceptionBean() { }

    public ExceptionBean(Throwable e) {
        this.exceptionMessage = e.getMessage();
        this.exceptionClassName = e.getClass().getName();
        this.throwable = e;
    }

    public String getExceptionClassName() {
        return exceptionClassName;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public Throwable getThrowable() {
        return throwable;
    }

}
