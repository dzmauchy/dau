package org.dau.runtime.runner;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public final class DeploymentParser {

  private DeploymentParser() {}

  public static Deployment parse(Document document) {
    var root = document.getDocumentElement();

    var name = root.getAttribute("name");
    if (name.isEmpty()) {
      throw new IllegalStateException("'name' attribute is not found in the deployment descriptor");
    }
    var units = new ArrayList<DeploymentUnit>();
    var repositories = new ArrayList<Repository>();
    var libraries = new ArrayList<Library>();

    var nodes = root.getChildNodes();
    for (int n = 0; n < nodes.getLength(); n++) {
      if (nodes.item(n) instanceof Element child) {
        switch (child.getTagName()) {
          case "units" -> {
            var children = child.getElementsByTagName("unit");
            for (int i = 0; i < children.getLength(); i++) {
              var e = (Element) children.item(i);

              var unitName = e.getAttribute("name");
              if (unitName.isEmpty()) {
                throw new IllegalStateException("'name' attribute of unit " + i + " is not defined");
              }
              var entryPoint = e.getAttribute("entry-point");
              if (entryPoint.isEmpty()) {
                throw new IllegalStateException("'entry-point' attribute of unit " + i + " is not defined");
              }
              units.add(new DeploymentUnit(unitName, entryPoint));
            }
          }
          case "repositories" -> {
            var children = child.getElementsByTagName("repository");
            for (int i = 0; i < children.getLength(); i++) {
              var e = (Element) children.item(i);

              var uriText = e.getAttribute("uri");
              if (uriText.isEmpty()) {
                throw new IllegalStateException("'uri' attribute of repository " + i + " is not defined");
              }

              try {
                repositories.add(new Repository(new URI(uriText)));
              } catch (Throwable x) {
                throw new IllegalStateException("'uri' of repository " + i + " is invalid");
              }
            }
          }
          case "libraries" -> {
            var children = child.getElementsByTagName("library");
            for (int i = 0; i < children.getLength(); i++) {
              var e = (Element) children.item(i);

              var group = e.getAttribute("group");
              if (group.isEmpty()) {
                throw new IllegalStateException("'group' of library " + i + " is not defined");
              }
              var artifact = e.getAttribute("name");
              if (artifact.isEmpty()) {
                throw new IllegalStateException("'name' of library " + i + " is not defined");
              }
              var version = e.getAttribute("version");
              if (version.isEmpty()) {
                throw new IllegalStateException("'version' of library " + i + " is not defined");
              }
              var classifier = e.getAttribute("classifier");
              libraries.add(new Library(group, artifact, version, classifier));
            }
          }
        }
      }
    }

    return new Deployment(
      name,
      List.copyOf(units),
      List.copyOf(repositories),
      List.copyOf(libraries)
    );
  }

  public static Deployment parse(InputStream inputStream) throws ParserConfigurationException, SAXException, IOException {
    var dbf = DocumentBuilderFactory.newDefaultInstance();
    var db = dbf.newDocumentBuilder();
    var doc = db.parse(inputStream);
    return parse(doc);
  }
}
