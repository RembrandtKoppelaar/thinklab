/**
 * XMLDocument.java
 * ----------------------------------------------------------------------------------
 * 
 * Copyright (C) 2008 www.integratedmodelling.org
 * Created: Jan 17, 2008
 *
 * ----------------------------------------------------------------------------------
 * This file is part of Thinklab.
 * 
 * Thinklab is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Thinklab is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with the software; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 * ----------------------------------------------------------------------------------
 * 
 * @copyright 2008 www.integratedmodelling.org
 * @author    Ferdinando Villa (fvilla@uvm.edu)
 * @author    Ioannis N. Athanasiadis (ioannis@athanasiadis.info)
 * @date      Jan 17, 2008
 * @license   http://www.gnu.org/licenses/gpl.txt GNU General Public License v3
 * @link      http://www.integratedmodelling.org
 **/
package org.integratedmodelling.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.xerces.parsers.DOMParser;
import org.integratedmodelling.thinklab.exception.ThinklabIOException;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Trivial helper class to wrap the read/write interface for an XML file without having to remember too much. 
 * Non-ADD people need not apply.
 * 
 * Write operations will fail if the document has been read from a non-writable source (a 
 * write protected file or a remote URL).
 * 
 * @author Ferdinando Villa
 */
public class XMLDocument {
	
	DOMParser parser;
	Document  dom;
	String namespace = null;
	boolean   isWritable = false;
	boolean   needsWrite = false;
	File      docFile = null;
	Element  root = null;
	
	public XMLDocument(String rootNode) throws ParserConfigurationException {
	
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance ();
		DocumentBuilder docBuilder = factory.newDocumentBuilder();
		dom = docBuilder.newDocument();
		root = dom.createElement(rootNode);
		dom.appendChild(root);

	}
	
	public XMLDocument(File f) throws ThinklabIOException {
		createFromFile(f,null);
	}
	
	public XMLDocument(File f, String namespace) throws ThinklabIOException {
		createFromFile(f, namespace);
	}
	
	public Node createNode(String tag, Node parent) {
		
		Node ret = null;
		ret = dom.createElement(tag);		
		parent.appendChild(ret);
		return ret;
	}
		
	public void addNamespace(String ns, String uri) {

//		if (uri.endsWith("#")) {
//			uri = uri.substring(0, uri.length()-1);
//		}

		Attr ret = dom.createAttributeNS(uri, ns);		
		root.setAttributeNode(ret);
	}
	
	public void addAttribute(Node parent, String aName, String aValue) {
		
		if (parent instanceof Element) {
			Attr ret = dom.createAttribute(aName);		
			ret.setValue(aValue);
			((Element)parent).setAttributeNode(ret);
		}
	}
	
	
	private void createFromFile(File f, String namespace) throws ThinklabIOException {
		
		this.namespace = namespace;
		this.docFile = f;
		
		if (!f.exists()) {
						
			/* create XML document */
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(namespace != null);
			try {
				dom = factory.newDocumentBuilder().newDocument();
			} catch (ParserConfigurationException e) {
				/* why in the world should this happen? */
			}
			isWritable = new File(f.getParent()).canWrite();
		}
		else {
			
			isWritable = f.canWrite();
			
			FileInputStream is;
			try {
				is = new FileInputStream(f);
			} catch (FileNotFoundException e) {
				throw new ThinklabIOException(e);
			}
			
			parser = new DOMParser();
			try {
				parser.setFeature("http://xml.org/sax/features/namespaces", true);
				parser.parse(new InputSource(is));	
			} catch (Exception e) {
				throw new ThinklabIOException(e);
			}
			dom = parser.getDocument();
		}
	}
	
	protected void finalize() throws ThinklabIOException {
		if (needsWrite)
			flush();
	}
	
	public XMLDocument(URL u) throws SAXException, IOException {
		
		parser = new DOMParser();
		parser.setFeature("http://xml.org/sax/features/namespaces", true);
		parser.parse(u.toString());
		dom = parser.getDocument();
	}
	
	public XMLDocument(InputStream is) throws SAXException, IOException {
		parser = new DOMParser();
		parser.setFeature("http://xml.org/sax/features/namespaces", true);
		parser.parse(new InputSource(is));	
		dom = parser.getDocument();
	}
	

	public Element root() {
		return root == null ? dom.getDocumentElement() : root;
	}
	
	public void flush() throws ThinklabIOException {
		
		Transformer transformer = null;
		try {
			transformer = TransformerFactory.newInstance().newTransformer();
		} catch (Exception e) {
			throw new ThinklabIOException(e);
		}
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		StreamResult result = new StreamResult(docFile);
		DOMSource source = new DOMSource(dom);
		try {
			transformer.transform(source, result);
		} catch (TransformerException e) {
			throw new ThinklabIOException(e);
		}
	}

	/**
	 * Take a xml element and the tag name, look for the tag and get
	 * the text content 
	 * i.e for <employee><name>John</name></employee> xml snippet if
	 * the Element points to employee node and tagName is name I will return John  
	 * @param ele
	 * @param tagName
	 * @return the string value.
	 */
	public static String getTextValue(Element ele, String tagName) {
		String textVal = null;
		NodeList nl = ele.getElementsByTagName(tagName);
		if(nl != null && nl.getLength() > 0) {
			Element el = (Element)nl.item(0);
			textVal = el.getFirstChild().getNodeValue();
		}

		return textVal;
	}
	
	public static String getNodeValue(Node node) {
		StringBuffer buf = new StringBuffer();
		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node textChild = children.item(i);
			if (textChild.getNodeType() != Node.TEXT_NODE) {
				System.err.println("Mixed content! Skipping child element " + textChild.getNodeName());
				continue;
	        }
			buf.append(textChild.getNodeValue());
	       }
	     return buf.toString();
	}

	/**
	 * Calls getTextValue and returns a int value
	 * @param ele
	 * @param tagName
	 * @return the int value
	 */
	 public static int getIntValue(Element ele, String tagName) {
		//in production application you would catch the exception
		return Integer.parseInt(getTextValue(ele,tagName));
	}
	
	 /**
	  * Extract attribute string value from node.
	  * @param n the Node
	  * @param attr the attribute name
	  * @return attribute value, or null if not there.
	  */
	 public static String getAttributeValue(Node n, String attr) {

		 String ret = null;

		if (n.hasAttributes()) {
			Node nn = n.getAttributes().getNamedItem(attr);
			if (nn != null)
				ret = nn.getTextContent();
		}

		return ret;
	 }

	 /**
	  * Return the given attribute value or the given default parameter if not there.
	  * @param n
	  * @param attr
	  * @param defval
	  * @return
	  */
	 public static String getAttributeValue(Node n, String attr, String defval) {
		 String ret = getAttributeValue(n, attr);
		 return ret == null ? defval : ret;
	 }
	 
	public static Node getChildNode(Node nn, String string) {
		Node ret = null;
		for (Node n = nn.getFirstChild(); n != null; n = n.getNextSibling())
			if (n.getNodeName().equals(string)) {
				ret = n;
				break;
			}
		return ret;
	}
	
	public Collection<ProcessingInstruction> getProcessingInstructions() {
		
		ArrayList<ProcessingInstruction> ret = new ArrayList<ProcessingInstruction>();
		
		for (Node n = dom.getFirstChild(); n != null; n = n.getNextSibling())
			if (n.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE) {
				ret.add((ProcessingInstruction) n);
			}
		
		return ret;
	}

	public Node appendTextNode(String tag, String text, Node parent) {
		
		Node nn = createNode(tag, parent);
		nn.setTextContent(text);
		return nn;
	}

	public void addProcessingInstruction(String target, String data) {
		dom.createProcessingInstruction(target, data);
	}
	
	/**
	 * What this does should be obvious. How it does it is quite far from that.
	 * 
	 * FIXME by now I just hard-coded options such as pretty printing. Of course we
	 * want to pass or set them.
	 * 
	 * @param outfile
	 * @throws ThinklabIOException
	 */
	public void writeToFile(File outfile) throws ThinklabIOException {

		/* 
		 * if this looks like something I cut-and-pasted from a Java forum,
		 * it's just because it is.
		 */
		System.setProperty(
				DOMImplementationRegistry.PROPERTY, 
				"org.apache.xerces.dom.DOMImplementationSourceImpl"); 

		DOMImplementationRegistry registry = null; 
		DOMImplementation domImpl = null;
		try {
			
			registry = DOMImplementationRegistry.newInstance();
			domImpl = registry.getDOMImplementation("LS 3.0"); 
			
		} catch (Exception e) {
			throw new ThinklabIOException(e);
		} 
	
		DOMImplementationLS implLS = (DOMImplementationLS)domImpl; 
		LSSerializer dom3Writer = implLS.createLSSerializer(); 
		
		dom3Writer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE);
		LSOutput output = implLS.createLSOutput(); 
		
		OutputStream outputStream = null; 
		try {
			outputStream = new FileOutputStream(outfile);
		} catch (FileNotFoundException e) {
			throw new ThinklabIOException(e);
		} 
		output.setByteStream(outputStream); 
		output.setEncoding("UTF-8"); 
		
		dom3Writer.write(dom, output); 
	}
	 
}
