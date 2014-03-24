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

    private String message;
    private String className;
    private Throwable throwable;

    public ExceptionBean() { }

    public ExceptionBean(Throwable e) {
        this.message = e.getMessage();
        this.className = e.getClass().getName();
        this.throwable = e;
    }

    public String getClassName() {
        return className;
    }

    public String getMessage() {
        return message;
    }

    public Throwable getThrowable() {
        return throwable;
    }

}
