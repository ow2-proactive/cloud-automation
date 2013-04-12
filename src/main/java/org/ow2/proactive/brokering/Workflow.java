package org.ow2.proactive.brokering;

import groovy.lang.GroovyClassLoader;
import org.w3c.dom.Document;
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
import java.util.logging.Level;
import java.util.logging.Logger;


public class Workflow {
    private static final Logger logger = Logger.getLogger(Catalog.class.getName());
    private File job;
    private long lastModification;
    private Map<String, String> genericInfo;
    private Map<String, String> variables;

    public Workflow(File job) {
        logger.setLevel(Level.ALL);
        this.job = job;
        lastModification = job.lastModified();
        genericInfo = new HashMap<String, String>();
        variables = new HashMap<String, String>();
    }

    public static void fillElements(Document doc, String tagName, Map<String, String> elements) {
        NodeList vars = doc.getElementsByTagName(tagName).item(0).getChildNodes();
        for (int n = 0; n < vars.getLength(); n++) {
            Node var = vars.item(n);
            if (var.getNodeType() == 1) {
                String key = var.getAttributes().getNamedItem("name").getNodeValue();
                String value = var.getAttributes().getNamedItem("value").getNodeValue();
                elements.put(key, value);
            }
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

    public boolean isValid(Attributes attributes) {
        logger.fine("Testing " + job.getName());
        Set<String> allKeys = new HashSet<String>();
        allKeys.addAll(genericInfo.keySet());
        allKeys.addAll(variables.keySet());

        logger.fine("workflow -> " + Arrays.toString(allKeys.toArray()));
        logger.fine("request  -> " + attributes);

        // First, check if keys matches
        if (allKeys.containsAll(attributes.keySet())) {
            // Then, check if generic information values matches
            for (String key : genericInfo.keySet()) {
                String value = genericInfo.get(key);
                logger.finer("GenericInfo:" + key + " = " + value + " contains? " + attributes.get(key));

                // Check that generic information contains corresponding value in the given map
                String[] values = value.split(",");
                Arrays.sort(values);
                if (Arrays.binarySearch(values, attributes.get(key).toLowerCase()) < 0) {
                    logger.fine("Failed: " + attributes.get(key) + " not in " + Arrays.toString(values));
                    return false;
                }
            }

            logger.fine("Success !");
            return true;
        }

        logger.fine("No match found");

        return false;
    }

    /**
     * Replaces variables and generic informations with given attributes
     *
     * @param attributes
     * @return
     */
    public File configure(Attributes attributes, Rules rules) throws ParserConfigurationException, IOException, SAXException, TransformerException {

        // Apply proper rules
        List<File> ruleFiles = rules.getMatchingRules(attributes);
        GroovyClassLoader gcl = new GroovyClassLoader();
        for (File file : ruleFiles) {
            try {
                Class clazz = gcl.parseClass(file);
                Rule rule = (Rule) clazz.newInstance();
                rule.apply(attributes);

            } catch (Throwable e) {
                logger.fine("Error when loading a rules file : " + file.getName());
            }
        }

        // Create output Job file
        File jobFile = File.createTempFile("broker_job_edit", ".xml");
        //jobFile.deleteOnExit(); // TODO Remove comment after debugging
        logger.fine("Temp File : " + jobFile.getAbsolutePath());

        // Modify the job variables
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(job);

        // Set Variable values
        NodeList vars = doc.getElementsByTagName("variables").item(0).getChildNodes();
        for (int n = 0; n < vars.getLength(); n++) {
            Node var = vars.item(n);
            if (var.getNodeType() == 1) {
                String varName = var.getAttributes().getNamedItem("name").getNodeValue();
                if (variables.containsKey(varName)) {
                    var.getAttributes().getNamedItem("value").setNodeValue(attributes.get(varName));
                }
            }
        }

        // Set Generic information values
        vars = doc.getElementsByTagName("genericInformation").item(0).getChildNodes();
        for (int n = 0; n < vars.getLength(); n++) {
            Node var = vars.item(n);
            if (var.getNodeType() == 1) {
                String varName = var.getAttributes().getNamedItem("name").getNodeValue();
                if (genericInfo.containsKey(varName)) {
                    var.getAttributes().getNamedItem("value").setNodeValue(attributes.get(varName));
                }
            }
        }


        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        StreamResult result = new StreamResult(jobFile);
        DOMSource source = new DOMSource(doc);
        transformer.transform(source, result);

        return jobFile;
    }
}
