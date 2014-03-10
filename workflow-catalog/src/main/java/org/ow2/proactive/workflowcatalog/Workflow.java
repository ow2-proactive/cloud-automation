package org.ow2.proactive.workflowcatalog;

import org.apache.log4j.Logger;
import org.ow2.proactive.workflowcatalog.utils.scheduling.JobParsingException;
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
import java.util.*;


public class Workflow {

    private static final Logger logger = Logger.getLogger(Workflow.class.getName());
    private long lastModification;
    private File job;
    private String name;
    private Map<String, String> genericInfo;
    private Map<String, String> variables;

    public Workflow(File job) {
        this.job = job;
        this.name = job.getName();
        lastModification = job.lastModified();
        genericInfo = new HashMap<String, String>();
        variables = new HashMap<String, String>();
    }

    public Workflow() {
        genericInfo = new HashMap<String, String>();
        variables = new HashMap<String, String>();
    }

    public Map<String, String> getVariables() {
        return new HashMap<String, String>(variables);
    }

    public Map<String, String> getGenericInformation() {
        return new HashMap<String, String>(genericInfo);
    }

    public synchronized boolean containsGenericInfo(String key) {
        return genericInfo.containsKey(key);
    }

    public synchronized boolean containsVariable(String key) {
        return variables.containsKey(key);
    }

    public synchronized String getGenericInformation(String key) {
        return genericInfo.get(key);
    }

    public synchronized String getVariable(String key) {
        return variables.get(key);
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return "['" + name + "', '" + variables + "', '" + genericInfo + "']";
    }

    public synchronized void update() {
        try {
            Document doc = getJobDocument();
            extractElementsFromDocument(doc, "genericInformation", genericInfo);
            extractElementsFromDocument(doc, "variables", variables);
            lastModification = job.lastModified();
            name = job.getName();
        } catch (JobParsingException e) {
            logger.warn("Error updating: " + e.getMessage());
        }
    }

    public boolean hasChanged() {
        return job.lastModified() != lastModification;
    }

    public File configure(Map<String, String> attributes)
            throws IOException, JobParsingException, TransformerException {
        Document doc = getJobDocument();
        setJobElementsInDocument(doc, "variables", attributes);
        setJobElementsInDocument(doc, "genericInformation", attributes);
        File jobFile = buildJobFile(doc);
        logger.debug(String.format("Job file: %s", jobFile));
        return jobFile;
    }

    private File buildJobFile(Document doc) throws IOException, TransformerException {
        File jobFile = File.createTempFile("broker_tempjob_", ".xml");
        jobFile.deleteOnExit();
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        StreamResult result = new StreamResult(jobFile);
        DOMSource source = new DOMSource(doc);
        transformer.transform(source, result);
        return jobFile;
    }

    private void setJobElementsInDocument(Document doc, String tagName, Map<String,
            String> attributes) {
        NodeList vars = doc.getElementsByTagName(tagName).item(0).getChildNodes();
        for (int n = 0; n < vars.getLength(); n++) {
            if (vars.item(n).getNodeType() == Node.ELEMENT_NODE) {
                Element var = (Element) vars.item(n);
                String varName = var.getAttributes().getNamedItem("name").getNodeValue();
                if (attributes.get(varName) != null) {
                    var.setAttribute("value", attributes.get(varName));
                }
            }
        }
    }

    private void extractElementsFromDocument(Document doc, String tagName, Map<String,
            String> elements) {
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
            logger.warn("Error extracting: " + getName() + ", " + e.getMessage());
        }
    }

    private Document getJobDocument() throws JobParsingException {
        Document doc;
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            doc = docBuilder.parse(job);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new JobParsingException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return doc;
    }

}
