package org.dau.ui.schematic.model

import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import org.dau.classpath.Dependency
import org.dau.ui.utils.Encoders
import org.dau.ui.utils.Xmls
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.xml.sax.SAXException
import java.io.IOException
import java.io.UncheckedIOException
import java.lang.Thread.currentThread
import java.net.URI
import java.nio.file.Files
import java.nio.file.Files.newDirectoryStream
import java.nio.file.NotDirectoryException
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicInteger
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import javax.xml.transform.OutputKeys
import javax.xml.transform.Result
import javax.xml.transform.TransformerException
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class FxProject {

  val id: String
  val name = SimpleStringProperty(this, "name")
  val schemas = FXCollections.observableSet<FxSchema>()
  val repositories = FXCollections.observableSet<URI>()
  val dependencies = FXCollections.observableSet<Dependency>()

  private constructor(id: String) {
    this.id = id
    this.name.set(id)
  }

  constructor() {
    this.id = Encoders.generateId(this)
    this.name.set("Project " + COUNTER.incrementAndGet())
  }

  fun toXml(doc: Document): Element {
    val el = doc.createElement("project")
    el.setAttribute("name", name.get())
    return el
  }

  private fun save(result: Result) {
    try {
      val dbf = DocumentBuilderFactory.newDefaultInstance()
      val db = dbf.newDocumentBuilder()
      val doc = db.newDocument()
      val root = doc.createElement("project")
      doc.appendChild(root)

      root.setAttribute("name", name.get())

      val reposElement = doc.createElement("repositories")
      root.appendChild(reposElement)
      repositories.forEach { repo ->
        val repoElement = doc.createElement("repository")
        repoElement.textContent = repo.toString()
        reposElement.appendChild(repoElement)
      }

      val dependenciesElement = doc.createElement("dependencies")
      root.appendChild(dependenciesElement)
      dependencies.forEach { dep ->
        val depElement = doc.createElement("dependency")
        depElement.setAttribute("group", dep.group)
        depElement.setAttribute("name", dep.name)
        depElement.setAttribute("version", dep.version)
        dependenciesElement.appendChild(depElement)
      }

      val tf = TransformerFactory.newDefaultInstance()
      tf.setAttribute("indent-number", 2)
      val t = tf.newTransformer()
      t.setOutputProperty(OutputKeys.STANDALONE, "yes")
      t.setOutputProperty(OutputKeys.ENCODING, "UTF-8")
      t.setOutputProperty(OutputKeys.INDENT, "yes")
      t.transform(DOMSource(doc), result)
    } catch (e: ParserConfigurationException) {
      throw IllegalStateException(e)
    } catch (e: TransformerException) {
      throw IllegalStateException(e)
    }

  }

  fun save(path: Path) {
    try {
      Files.newOutputStream(path.resolve("project.xml")).use { out -> save(StreamResult(out)) }
    } catch (e: IOException) {
      throw UncheckedIOException(e)
    }

    for (schema in schemas) {
      schema.save(path.resolve("schema-" + schema.id + ".xml"))
    }
  }

  private fun load0(path: Path) {
    try {
      val dbf = DocumentBuilderFactory.newDefaultInstance()
      val db = dbf.newDocumentBuilder()
      val doc = db.parse(path.toFile())
      val root = doc.documentElement

      name.set(root.getAttribute("name"))

      Xmls.elementsByTag(root, "repositories", "repository").forEach { e ->
        val uri = URI.create(e.textContent)
        repositories.add(uri)
      }

      Xmls.elementsByTag(root, "dependencies", "dependency").forEach { e ->
        val group = e.getAttribute("group")
        val name = e.getAttribute("name")
        val version = e.getAttribute("version")
        dependencies.add(Dependency(group, name, version))
      }
    } catch (e: IOException) {
      throw IllegalStateException(e)
    } catch (e: ParserConfigurationException) {
      throw IllegalStateException(e)
    } catch (e: SAXException) {
      throw IllegalStateException(e)
    }

  }

  override fun hashCode(): Int {
    return id.hashCode()
  }

  override fun equals(other: Any?): Boolean {
    return other is FxProject && other.id == id
  }

  override fun toString(): String {
    return name.get()
  }

  companion object {

    private val COUNTER = AtomicInteger()

    fun load(directory: Path): FxProject {
      val project = FxProject(directory.fileName.toString())
      try {
        newDirectoryStream(directory, "schema-*.xml").use { ds ->
          project.load0(directory.resolve("project.xml"))
          for (file in ds) {
            val schema = FxSchema.load(file, currentThread().contextClassLoader)
            project.schemas.add(schema)
          }
        }
      } catch (ignore: NotDirectoryException) {
      } catch (e: IOException) {
        throw UncheckedIOException(e)
      }

      return project
    }

    fun isProject(path: Path): Boolean {
      return Files.isDirectory(path) && Files.isRegularFile(path.resolve("project.xml"))
    }
  }
}
