package org.dau.classpath

import javafx.util.Pair
import org.dau.runtime.Block
import org.dau.runtime.Factory
import java.lang.reflect.Constructor
import java.lang.reflect.Executable
import java.net.URL
import java.net.URLClassLoader
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.Arrays.sort
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentSkipListMap
import java.util.concurrent.ConcurrentSkipListSet
import java.util.function.BiConsumer
import java.util.function.Consumer
import java.util.function.Supplier
import java.util.jar.JarEntry
import java.util.jar.JarInputStream
import java.util.stream.Collectors
import java.util.stream.IntStream
import java.util.stream.Stream
import java.util.stream.StreamSupport

class Executables(vararg classes: Class<*>) {

  private val map = ConcurrentSkipListMap<Package, ConcurrentSkipListSet<Executable>>(PACKAGE_COMPARATOR)
  private val index = ConcurrentHashMap<BlockKey, Executable>(32, 0.25f)
  private val notFound = ConcurrentSkipListSet<String>()

  init {
    for (cl in classes) tryProcess(cl)
  }

  private fun tryProcess(cl: Class<*>) {
    if (cl.isAnnotationPresent(Factory::class.java)) {
      for (m in cl.methods) {
        if (m.isAnnotationPresent(Block::class.java)) {
          map.computeIfAbsent(cl.getPackage()) { ConcurrentSkipListSet(EXECUTABLE_COMPARATOR) }.add(m)
          index[BlockKey(m.declaringClass.name, m.name)] = m
        }
      }
    }
    if (cl.isAnnotationPresent(Block::class.java)) {
      val constructors = cl.constructors
      sort(constructors, CONSTRUCTOR_COMPARATOR)
      for (c in constructors) {
        map.computeIfAbsent(cl.getPackage()) { ConcurrentSkipListSet(EXECUTABLE_COMPARATOR) }.add(c)
        index[BlockKey(c.declaringClass.name, c.name)] = c
        return
      }
    }
  }

  fun notFound(): Stream<String> {
    return notFound.stream()
  }

  fun forEachNotFound(consumer: Consumer<String>) {
    notFound.forEach(consumer)
  }

  fun executables(): Stream<Pair<Package, Supplier<Stream<Executable>>>> {
    return map.entries.stream().map { (key, value) -> Pair(key, Supplier { value.stream() }) }
  }

  fun forEach(consumer: BiConsumer<Package, Consumer<Consumer<Executable>>>) {
    map.forEach { (p, s) -> consumer.accept(p, Consumer { s.forEach(it) }) }
  }

  fun hasNotFound(): Boolean {
    return !notFound.isEmpty()
  }

  fun getExecutable(className: String, name: String): Executable? {
    return index[BlockKey(className, name)]
  }

  fun getExecutable(key: BlockKey): Executable? {
    return index[key]
  }

  fun size(): Int {
    return index.size
  }

  private fun tryProcess(classLoader: URLClassLoader, name: String) {
    try {
      val cl = Class.forName(name, false, classLoader)
      tryProcess(cl)
    } catch (_: ClassNotFoundException) {
      notFound.add(name)
    }
  }

  companion object {

    private val CLASS_SUFFIX = ".class"
    private val PACKAGE_COMPARATOR = compareBy(Package::getName)
    private val EXECUTABLE_COMPARATOR = compareBy({ it.declaringClass.name }, Executable::getName)
    private val CONSTRUCTOR_COMPARATOR = compareByDescending<Constructor<*>> { it.parameterCount }

    private fun findExecutables(classLoader: URLClassLoader, map: Executables, url: URL) {
      if (url.path.endsWith(".jar")) {
        JarInputStream(url.openStream(), true).use { jis ->
          var je: JarEntry? = jis.nextJarEntry
          while (je != null) {
            if (je.isDirectory) {
              je = jis.nextJarEntry
              continue
            }
            if (!je.name.endsWith(CLASS_SUFFIX)) {
              je = jis.nextJarEntry
              continue
            }
            val fileName = je.name
            val fileNameWithoutExtension = fileName.substring(0, fileName.length - CLASS_SUFFIX.length)
            val className = fileNameWithoutExtension.replace('/', '.')
            map.tryProcess(classLoader, className)
            je = jis.nextJarEntry
          }
        }
      } else {
        val base = Path.of(url.toURI())
        Files.walk(base)
          .filter(Files::isRegularFile)
          .filter { f -> f.fileName.toString().endsWith(CLASS_SUFFIX) }
          .use { ds ->
            ds.forEach { f ->
              val relativePath = base.relativize(f)
              val fileName = IntStream.range(0, relativePath.nameCount)
                .mapToObj(relativePath::getName)
                .map(Path::toString)
                .collect(Collectors.joining("."))
              val className = fileName.substring(0, fileName.length - CLASS_SUFFIX.length)
              map.tryProcess(classLoader, className)
            }
          }
      }
    }

    fun findExecutables(classLoader: URLClassLoader): Executables {
      val map = Executables()
      val urls = classLoader.urLs
      val spl = Spliterators.spliterator<URL>(urls, Spliterator.IMMUTABLE or Spliterator.NONNULL or Spliterator.DISTINCT)
      StreamSupport.stream(spl, true).forEach { url ->
        try {
          findExecutables(classLoader, map, url)
        } catch (e: Throwable) {
          throw IllegalStateException(e)
        }
      }
      return map
    }
  }
}