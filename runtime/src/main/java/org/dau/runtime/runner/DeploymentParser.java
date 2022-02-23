package org.dau.runtime.runner;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;

public final class DeploymentParser {

  private DeploymentParser() {}

  public static Deployment parse(Document document) {
    var root = document.getDocumentElement();

    var name = root.getAttribute("name");
    if (name.isEmpty()) {
      throw new IllegalStateException("'name' attribute is not found in the deployment descriptor");
    }

    var units = Xmls.elements(root, "units", "unit")
      .map(e -> {
        var unitName = e.getAttribute("name");
        if (unitName.isEmpty()) {
          throw new IllegalStateException("'name' attribute of unit " + e + " is not defined");
        }
        var entryPoint = e.getAttribute("entry-point");
        if (entryPoint.isEmpty()) {
          throw new IllegalStateException("'entry-point' attribute of unit " + e + " is not defined");
        }

        return new DeploymentUnit(unitName, entryPoint);
      })
      .toList();

    var repositories = Xmls.elements(root, "repositories", "repository")
      .map(e -> {
        var uriText = e.getAttribute("uri");
        if (uriText.isEmpty()) {
          throw new IllegalStateException("'uri' attribute of repository " + e + " is not defined");
        }

        try {
          return new Repository(new URI(uriText));
        } catch (Throwable x) {
          throw new IllegalStateException("'uri' of repository " + e + " is invalid");
        }
      })
      .toList();

    var libraries = Xmls.elements(root, "libraries", "library")
      .map(e -> {
        var group = e.getAttribute("group");
        if (group.isEmpty()) {
          throw new IllegalStateException("'group' of library " + e + " is not defined");
        }
        var artifact = e.getAttribute("name");
        if (artifact.isEmpty()) {
          throw new IllegalStateException("'name' of library " + e + " is not defined");
        }
        var version = e.getAttribute("version");
        if (version.isEmpty()) {
          throw new IllegalStateException("'version' of library " + e + " is not defined");
        }
        var classifier = e.getAttribute("classifier");
        return new Library(group, artifact, version, classifier);
      })
      .toList();

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
