package org.ow2.proactive.brokering;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;

public class Main {

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {

            File file = new File("configuration/config.xml");
            JAXBContext jaxbContext = JAXBContext.newInstance(Configuration.class);

            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            Configuration rules = (Configuration) jaxbUnmarshaller.unmarshal(file);
            System.out.println(rules);

        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

}
