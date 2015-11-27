/*
 * Together Relational Objects
 * Copyright (C) 2011 Together Teamsolutions Co., Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses
 */
 package org.enhydra.xml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;



/**
 * @author Tweety
 *
 * A class for manipulating the entire xml file (reading, writing...).
 *
 * @version 1.0
 */
public class XMLDocumentFactory {

   private static String[] properties = {
            "method",
            "version",
            "encoding",
            "omit-xml-declaration",
            "standalone",
            "doctype-public",
            "doctype-system",
            "indent",
            "media-type"
   };


   private static int METHOD = 0;
   private static int VERSION = 1;
   private static int ENCODING = 2;
   private static int OMIT_XML_DECLARATION = 3;
   private static int STANDALONE = 4;
   private static int DOCTYPE_PUBLIC = 5;
   private static int DOCTYPE_SYSTEM = 6;
   private static int CDATA_SECTION_ELEMENTS = 7;
   private static int INDENT = 8;
   private static int MEDIA_TYPE = 9;

	/**
	 * xml file name.
	 */
	private String fileName;


	/**
	 * Constructs an empty <code>XMLDocumentFactory</code>
	 */
	public XMLDocumentFactory() {
	}


	/**
	 * Constructs a <code>XMLDocumentFactory</code> with the given
	 * xml file name as <code>String</code>
	 */
	public XMLDocumentFactory(String fileName) {
		this.fileName = fileName;
	}


	/**
	 * Returns xml file name.
	 *
	 * @return xml file name.
	 */
	public String getFileName() {
		return this.fileName;
	}


	/**
	 * Parses xml file with the given name and creates <code>Document</code>.
	 *
	 * @param fileName xml file name.
	 *
	 * @return document.
	 */
	public static Document parse(String fileName) {
          DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         try {
           DocumentBuilder builder = factory.newDocumentBuilder();
           builder.setErrorHandler(new UtilErrorHandler());
           Document doc = builder.parse(new File(fileName).toURI().toString());
           return doc;
         } catch (SAXParseException e) {
           e.printStackTrace();
         } catch (ParserConfigurationException e) {
           e.printStackTrace();
         } catch (IOException e) {
           e.printStackTrace();
         } catch (SAXException e) {
           e.printStackTrace();
         }
          return null;

          //OLD with apache xerces
//		DOMParser parser = new DOMParser();
//		try {
//			parser.parse(fileName);
//			return parser.getDocument();
//		} catch (SAXException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		return null;
	}


	/**
	 * Parses xml file and creates creates <code>Document</code>.
	 */
	public Document parse() {
          DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
          try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setErrorHandler(new UtilErrorHandler());
            Document doc = builder.parse(fileName);
            return doc;
          } catch (SAXParseException e) {
            e.printStackTrace();
          } catch (ParserConfigurationException e) {
            e.printStackTrace();
          } catch (IOException e) {
            e.printStackTrace();
          } catch (SAXException e) {
            e.printStackTrace();
          }
          return null;

          //OLD with apache xerces
//		DOMParser parser = new DOMParser();
//		try {
//			parser.parse(this.fileName);
//			return parser.getDocument();
//		} catch (SAXException e) {
//			System.err.println("SAXException - bad xml format");
//		} catch (IOException e) {
//		}
//		return null;
	}


	/**
	 * Serializes node with all subnodes to the xml file with the given name,
	 * and with the <code>Properties</code> of the xml declaration.
	 *
	 * @param node root node of the document.
	 * @param fileName xml file name
	 * @param prop <code>Properties</code> of the xml declaration.
	 */
	public static void serialize(Node node, String fileName, Properties prop) {
		String out = "<?xml version=\"1.0\"?>";
		StringBuffer outBuffer = new StringBuffer(out);
      File file = new File(fileName);

      //serialize xml declaration
      if (prop != null) {
    	  outBuffer.append("<?xml");
         String str = "";
         for (int i=0; i<properties.length; i++) {
            str = (String)prop.get(properties[i]);
            if (str != null)
            	outBuffer.append(" "+properties[i]+"=\""+str+"\"");
         }
         outBuffer.append("?>");
      }
      out = outBuffer.toString();
      //serialize document
      try {
         FileOutputStream outStream = new FileOutputStream(file);
         out += node.toString();
         outStream.write(out.getBytes());
         outStream.close();
      } catch(Exception e) {
         System.err.println("Error serializing file");
      }
	}


	/**
	 * Serializes node with all subnodes to the xml file
	 * with the default <code>Properties</code> of the xml declaration.
	 *
	 * @param node root node of the document.
	 */
	public void serialize(Node node) {

		//TODO: NAPRAVITI I SERIALIZE ZA XML DECLARATION !!!!

	    File file = new File(fileName);
        try {
            FileOutputStream outStream = new FileOutputStream(file);
            outStream.write(node.toString().getBytes());
            outStream.close();
        } catch(Exception e) {
        	System.err.println("Error serializing file");
        }
	}

     static class UtilErrorHandler implements ErrorHandler
  {

    // throw SAXException for fatal errors
    public void fatalError( SAXParseException exception ) throws SAXException
    {
      throw new SAXException(exception);
    }

    public void error( SAXParseException errorException ) throws SAXException
    {
      throw new SAXException(errorException);
    }

    // print any warnings
    public void warning( SAXParseException warningError ) throws SAXException
    {
      System.err.println("[Validation : Warning] URI = " + warningError.getMessage());
    }
  }



}
