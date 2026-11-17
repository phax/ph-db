/*
 * Copyright (C) 2014-2026 Philip Helger (www.helger.com)
 * philip[at]helger[dot]com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.helger.db.jpa.utils;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.annotation.concurrent.Immutable;
import com.helger.base.classloader.ClassLoaderHelper;
import com.helger.base.reflection.GenericReflection;
import com.helger.base.string.StringHelper;
import com.helger.io.resource.URLResource;
import com.helger.xml.microdom.IMicroDocument;
import com.helger.xml.microdom.IMicroElement;
import com.helger.xml.microdom.serialize.MicroReader;

/**
 * Utility class that scans all META-INF/persistence.xml files available, and checks if all
 * referenced classes are available :)
 *
 * @author Philip Helger
 */
@Immutable
public final class PersistenceXmlHelper
{
  /** The JPA configuration file relative to the classpath */
  public static final String PATH_PERSISTENCE_XML = "META-INF/persistence.xml";
  /** The namespace URI of JPA 1.0 and 2.x persistence.xml files */
  public static final String PERSISTENCE_NAMESPACE_URI = "http://java.sun.com/xml/ns/persistence";
  /** The namespace URI of Jakarta Persistence 3.x persistence.xml files */
  public static final String PERSISTENCE_NAMESPACE_URI_JAKARTA = "https://jakarta.ee/xml/ns/persistence";

  private static final Logger LOGGER = LoggerFactory.getLogger (PersistenceXmlHelper.class);

  private PersistenceXmlHelper ()
  {}

  /**
   * Read all {@value #PATH_PERSISTENCE_XML} files in the class path and check if the referenced
   * classes are in the classpath.
   *
   * @throws IllegalStateException
   *         in case of an error
   */
  public static void checkPersistenceXMLValidity ()
  {
    // Check if all persistence.xml files contain valid classes!
    try
    {
      int nCount = 0;
      final Enumeration <URL> aEnum = ClassLoaderHelper.getResources (ClassLoaderHelper.getDefaultClassLoader (),
                                                                      PATH_PERSISTENCE_XML);
      while (aEnum.hasMoreElements ())
      {
        nCount++;
        final URL aURL = aEnum.nextElement ();
        final IMicroDocument aDoc = MicroReader.readMicroXML (new URLResource (aURL));
        if (aDoc == null || aDoc.getDocumentElement () == null)
          throw new IllegalStateException ("No XML file: " + aURL);
        final IMicroElement eRoot = aDoc.getDocumentElement ();
        final String sNamespaceURI = eRoot.getNamespaceURI ();
        if (!PERSISTENCE_NAMESPACE_URI.equals (sNamespaceURI) &&
            !PERSISTENCE_NAMESPACE_URI_JAKARTA.equals (sNamespaceURI))
          throw new IllegalStateException ("Persistence file " +
                                           aURL +
                                           ": unsupported namespace URI '" +
                                           sNamespaceURI +
                                           "'");
        for (final IMicroElement ePU : eRoot.getAllChildElements (sNamespaceURI, "persistence-unit"))
          for (final IMicroElement eClass : ePU.getAllChildElements (sNamespaceURI, "class"))
          {
            final String sClass = eClass.getTextContent ();
            if (StringHelper.isBlank (sClass))
              throw new IllegalStateException ("Persistence file " + aURL + ": class name is missing!");
            GenericReflection.getClassFromName (sClass.trim ());
          }
      }
      LOGGER.info ("All " + PATH_PERSISTENCE_XML + " files are valid (count=" + nCount + ")");
    }
    catch (final IOException ex)
    {
      // From getResources
      throw new IllegalStateException ("Failed to read " + PATH_PERSISTENCE_XML + " file", ex);
    }
    catch (final ClassNotFoundException ex)
    {
      // If forName fails
      throw new IllegalStateException ("Failed to resolve class", ex);
    }
  }
}
