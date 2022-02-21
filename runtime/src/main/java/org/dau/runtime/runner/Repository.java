package org.dau.runtime.runner;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public record Repository(URI uri) {

  public URI jarURI(Library library) {
    var groupParts = library.group().split("\\.");
    var groupPath = String.join("/", groupParts);
    var uriText = groupPath +
      "/" + library.name() +
      "/" + library.version() +
      "/" + library.name() + "-" + library.version();
    if (library.classifier().isEmpty()) {
      return uri.resolve(uriText);
    } else {
      return uri.resolve(uriText + "-" + library.classifier());
    }
  }

  public URL jarURL(Library library) {
    var uri = jarURI(library);
    try {
      return uri.toURL();
    } catch (MalformedURLException e) {
      throw new IllegalArgumentException(uri.toString(), e);
    }
  }

  public Optional<URL> existingURL(Library library) {
    var url = jarURL(library);
    return exists(url) ? Optional.of(url) : Optional.empty();
  }

  private boolean exists(URL url) {
    try {
      return switch (url.getProtocol()) {
        case "file" -> {
          var file = Path.of(url.toURI());
          yield Files.exists(file);
        }
        case "http", "https" -> {
          var conn = (HttpURLConnection) url.openConnection();
          conn.setDoInput(false);
          conn.setRequestMethod("HEAD");
          conn.setInstanceFollowRedirects(true);
          conn.setReadTimeout(10_000);
          conn.setConnectTimeout(10_000);
          conn.setUseCaches(false);
          conn.connect();
          yield conn.getResponseCode() == HttpURLConnection.HTTP_OK;
        }
        case "ftp" -> {
          var conn = url.openConnection();
          conn.setUseCaches(false);
          conn.setConnectTimeout(10_000);
          conn.setReadTimeout(10_000);
          conn.setDoInput(true);
          conn.connect();
          try (var ignore = conn.getInputStream()) {
            yield true;
          } catch (FileNotFoundException e) {
            yield false;
          }
        }
        default -> throw new IllegalArgumentException("Unsupported URL protocol: " + url);
      };
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    } catch (URISyntaxException e) {
      throw new IllegalStateException(e);
    }
  }
}