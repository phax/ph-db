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
package com.helger.db.api.supplementary.tools;

import java.io.File;
import java.nio.charset.StandardCharsets;

import org.jspecify.annotations.NonNull;
import org.slf4j.LoggerFactory;

import com.helger.base.enforce.ValueEnforcer;
import com.helger.base.string.StringHelper;
import com.helger.base.string.StringReplace;
import com.helger.base.version.Version;
import com.helger.collection.commons.ICommonsList;
import com.helger.collection.hierarchy.visit.DefaultHierarchyVisitorCallback;
import com.helger.collection.hierarchy.visit.EHierarchyVisitorReturn;
import com.helger.db.api.mysql.EMySQLConnectionProperty;
import com.helger.io.file.SimpleFileIO;
import com.helger.xml.microdom.IMicroDocument;
import com.helger.xml.microdom.IMicroElement;
import com.helger.xml.microdom.IMicroNode;
import com.helger.xml.microdom.serialize.MicroReader;
import com.helger.xml.microdom.util.MicroVisitor;
import com.helger.xml.serialize.read.SAXReaderSettings;

/**
 * Helper tool that reads a local copy of
 * http://dev.mysql.com/doc/connector-j/6.0/en/connector-j-reference- configuration-properties.html
 * and creates the content of the {@link EMySQLConnectionProperty} enum.
 *
 * @author Philip Helger
 */
public final class MainMySQLPropertyExtractor
{
  public static void main (final String [] args)
  {
    final File aFile = new File ("src/test/resources/mysql/config-properties-6.0.html");
    String sContent = SimpleFileIO.getFileAsString (aFile, StandardCharsets.UTF_8);
    for (final EHTMLEntity e : EHTMLEntity.values ())
      sContent = StringReplace.replaceAll (sContent, e.getEntityReference (), e.getCharString ());
    // Remove all self-closed tags without a trailing "/>"
    sContent = sContent.replaceAll ("<(meta|col)\\s[^>]+>", "");
    // Remove unquoted links
    sContent = sContent.replaceAll ("src=\"[^\"]+\"", "");

    final SAXReaderSettings aSRS = new SAXReaderSettings ();
    final IMicroDocument aDoc = MicroReader.readMicroXML (sContent, aSRS);
    ValueEnforcer.notNull (aDoc, "Document");

    final StringBuilder ret = new StringBuilder ();
    MicroVisitor.visit (aDoc, new DefaultHierarchyVisitorCallback <IMicroNode> ()
    {
      @Override
      @NonNull
      public EHierarchyVisitorReturn onItemBeforeChildren (final IMicroNode aItem)
      {
        if (aItem.isElement ())
        {
          final IMicroElement aElement = (IMicroElement) aItem;
          if (aElement.hasTagName ("table") && "informaltable".equals (aElement.getAttributeValue ("class")))
          {
            final IMicroElement aTBody = aElement.getFirstChildElement ("tbody");
            aTBody.forAllChildElements (IMicroElement.filterNamespaceURIAndName (null, "tr"), aTR -> {
              final IMicroElement aTD = aTR.getFirstChildElement ("td");
              final ICommonsList <IMicroElement> aPs = aTD.getAllChildElements ("p");
              if (aPs.size () >= 3 && aPs.size () <= 4)
              {
                final String sName = aPs.get (0).getTextContentTrimmed ();
                final String sDescription = aPs.get (1).getTextContentTrimmed ();
                String sDefault;
                String sSince;
                if (aPs.size () == 3)
                {
                  // No default
                  sDefault = null;
                  sSince = aPs.get (2).getTextContentTrimmed ();
                }
                else
                {
                  // With default
                  sDefault = aPs.get (2).getTextContentTrimmed ();
                  sDefault = sDefault.replaceAll ("Default:\\s+", "");
                  sSince = aPs.get (3).getTextContentTrimmed ();
                }
                sSince = StringHelper.trimStart (sSince, "Since version: ");
                if (sSince.equals ("all versions"))
                  sSince = null;

                final Version aSince = sSince == null ? null : Version.parse (sSince);
                if (aSince != null && aSince.getQualifier () != null)
                  throw new IllegalStateException ("Unexpected version: " + sSince);

                String sIdentifier = sName;
                sIdentifier = StringReplace.replaceAll (sIdentifier, '.', '_');

                // Build output
                ret.append ("/** ").append (sDescription);
                if (aSince != null)
                  ret.append ("\n * @since MySQL ").append (sSince);
                ret.append ("\n */\n").append (sIdentifier).append (" (\"").append (sName).append ("\", ");
                if (sDefault != null)
                  ret.append ('"').append (sDefault).append ('"');
                else
                  ret.append ("null");
                ret.append (", ");
                if (aSince != null)
                  ret.append ("new Version (")
                     .append (aSince.getMajor ())
                     .append (", ")
                     .append (aSince.getMinor ())
                     .append (", ")
                     .append (aSince.getMicro ())
                     .append (")");
                else
                  ret.append ("null");
                ret.append ("),\n");
              }
              else
                throw new IllegalStateException ("Illegal number of cell P's: " + aPs);
            });
          }
        }
        // Always continue
        return EHierarchyVisitorReturn.CONTINUE;
      }
    });
    ret.setLength (ret.length () - 2);
    ret.append (";\n");
    LoggerFactory.getLogger ("dummy").info (ret.toString ());
  }
}
