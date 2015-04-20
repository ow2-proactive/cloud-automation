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


package org.ow2.proactive.workflowcatalog;

import org.apache.log4j.Logger;
import org.ow2.proactive.workflowcatalog.utils.scheduling.JobCreationException;
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
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;


public class Workflow {

    private static final Logger logger = Logger.getLogger(Workflow.class.getName());
    private long lastModification;
    private File jobFile;
    private String jobContent;
    private String name;
    private Map<String, String> genericInfo;
    private Map<String, String> variables;

    public Workflow(File jobFile) {
        this.jobFile = jobFile;
        this.jobContent = null;
        this.name = jobFile.getName();
        lastModification = jobFile.lastModified();
        genericInfo = new HashMap<String, String>();
        variables = new HashMap<String, String>();
    }

    public Workflow(String jobName, String jobContent) {
        this.jobFile = null;
        this.jobContent = jobContent;
        this.name = jobName;
        lastModification = 0;
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
            if (jobFile != null) {
                lastModification = jobFile.lastModified();
            }
        } catch (JobParsingException e) {
            logger.warn("Error updating: " + e.getMessage());
        }
    }

    public boolean hasChanged() {
        return (jobFile != null && jobFile.lastModified() != lastModification);
    }

    public File configure(Map<String, String> attributes)
            throws IOException, JobParsingException, TransformerException, JobCreationException {
        Document doc = getJobDocument();
        setJobElementsInDocument(doc, "variables", attributes);
        verifyAllElementsInDocumentAreSet(doc, "variables");
        setJobElementsInDocument(doc, "genericInformation", attributes);
        verifyAllElementsInDocumentAreSet(doc, "genericInformation");

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

    private NodeList getNodeList(Document doc, String tagName) {
        NodeList tag = doc.getElementsByTagName(tagName);
        Node node = tag.item(0);
        if (node == null)
            return null;
        return node.getChildNodes();
    }

    private void verifyAllElementsInDocumentAreSet(Document doc, String tagName) throws JobCreationException {
        List<String> notDefined = new ArrayList<String>();

        NodeList vars = getNodeList(doc,tagName);

        if (vars == null)
            return;

        for (int n = 0; n < vars.getLength(); n++) {
            if (vars.item(n).getNodeType() == Node.ELEMENT_NODE) {
                Element var = (Element) vars.item(n);
                String varName = var.getAttributes().getNamedItem("name").getNodeValue();
                Node varValue = var.getAttributes().getNamedItem("value");
                if (varValue == null) {
                    notDefined.add(varName);
                }
            }
        }
        if (!notDefined.isEmpty())
            throw new JobCreationException("Some '" + tagName + "' are not defined: " + notDefined);
    }

    private void setJobElementsInDocument(Document doc, String tagName, Map<String,
            String> attributes) {

        NodeList vars = getNodeList(doc,tagName);

        if (vars == null)
            return;

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
            logger.warn("Cannot extract " + tagName + " from " + getName() + ": " + e.getMessage());
        }
    }

    private Document getJobDocument() throws JobParsingException {
        Document doc;
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            if (jobFile != null) {
                doc = docBuilder.parse(jobFile);
            } else if (jobContent != null) {
                doc = docBuilder.parse(new ByteArrayInputStream(jobContent.getBytes("UTF-8")));
            } else {
                throw new IllegalStateException("No job provided");
            }
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
