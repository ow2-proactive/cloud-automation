package org.ow2.proactive.brokering;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class Workflow {
    private static final Logger logger = Logger.getLogger(Catalog.class.getName());
    private File job;
    private long lastModification;
    private Map<String, String> genericInfo;
    private Map<String, String> variables;

    public Workflow(File job) {
        this.job = job;
        lastModification = job.lastModified();
        genericInfo = new HashMap<String, String>();
        variables = new HashMap<String, String>();
    }

    public static void fillElements(Document doc, String tagName, Map<String, String> elements) {
        try {
            NodeList vars = doc.getElementsByTagName(tagName).item(0).getChildNodes();
            for (int n = 0; n < vars.getLength(); n++) {
                Node var = vars.item(n);
                if (var.getNodeType() == 1) {
                    String key = var.getAttributes().getNamedItem("name").getNodeValue();
                    Node valueNode = var.getAttributes().getNamedItem("value");
                    String value = null;
                    if (valueNode != null) {
                        value = valueNode.getNodeValue();
                    }
                    elements.put(key, value);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public String toString() {
        return job.getName();
    }

    public void update() {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(job);

            fillElements(doc, "genericInformation", genericInfo);
            fillElements(doc, "variables", variables);

            lastModification = job.lastModified();

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean hasChanged() {
        return job.lastModified() != lastModification;
    }

    /**
     * Only checks compliance with the generic informations.
     *
     * @param category
     * @param operation
     * @param attributes
     * @return
     */
    public boolean isCompliant(String category, String operation, String action, Map<String, String> attributes) {
        // Generic Informations 'category' and 'operation' must contains given 'category' and 'operation' values
        if (!contains(category, genericInfo.get("category")) || !contains(operation, genericInfo.get("operation"))) {
            logger.debug(job.getName() + " : Wrong category or operation");
            return false;
        }

        // If an action is given, it must be present in the Generic information
        if (action != null && !contains(action, genericInfo.get("action"))) {
            logger.debug(job.getName() + " : Wrong action (" + action + "/" + genericInfo.get("action") + ")");
            return false;
        }

        // Request attributes which are in Generic Informations must matches (contains) their values
        for (String attributeKey : attributes.keySet()) {
            if (genericInfo.containsKey(attributeKey)
                    && !contains(attributes.get(attributeKey), genericInfo.get(attributeKey))) {
                logger.debug(job.getName() + " : Wrong value for " + attributeKey);
                return false;
            }
        }
        return true;
    }

    public boolean isDeepCompliant(Map<String, String> attributes) {
        // Check that empty template variables (variables without 'value' attribute) are in the request attributes
        for (String variableName : variables.keySet()) {
            if (variables.get(variableName) == null && !attributes.containsKey(variableName)) {
                return false;
            }
        }
        return true;
    }

    private boolean contains(String value, String set) {
        // Coma separated values, case and spaces are ignored
        String[] values = set.replaceAll(" ", "").toLowerCase().split(",");
        Arrays.sort(values);
        if (value == null || Arrays.binarySearch(values, value.toLowerCase()) < 0) {
            return false;
        }
        return true;
    }

    public File configure(Map<String, String> attributes)
            throws IOException, ParserConfigurationException, SAXException, TransformerException {
        // Load the job template
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(job);

        // Set job variables from attributes
        NodeList vars = doc.getElementsByTagName("variables").item(0).getChildNodes();
        for (int n = 0; n < vars.getLength(); n++) {
            if (vars.item(n).getNodeType() == Node.ELEMENT_NODE) {
                Element var = (Element) vars.item(n);
                String varName = var.getAttributes().getNamedItem("name").getNodeValue();
                if (attributes.get(varName) != null) {
                    var.setAttribute("value", attributes.get(varName));
                }
            }
        }

        // Set proper Generic informations from attributes
        NodeList gis = doc.getElementsByTagName("genericInformation").item(0).getChildNodes();
        for (int n = 0; n < gis.getLength(); n++) {
            if (gis.item(n).getNodeType() == Node.ELEMENT_NODE) {
                Element gi = (Element) gis.item(n);
                String giName = gi.getAttributes().getNamedItem("name").getNodeValue();
                if (attributes.get(giName) != null) {
                    gi.setAttribute("value", attributes.get(giName));
                }
            }
        }

        // Create output Job file
        File jobFile = File.createTempFile("broker_tempjob_", ".xml");
        //jobFile.deleteOnExit(); // TODO Remove comment after debugging
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        StreamResult result = new StreamResult(jobFile);
        DOMSource source = new DOMSource(doc);
        transformer.transform(source, result);

        return jobFile;
    }
}
