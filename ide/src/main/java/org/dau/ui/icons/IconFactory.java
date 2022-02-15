package org.dau.ui.icons;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

import java.util.concurrent.ConcurrentHashMap;

import static java.util.Objects.requireNonNull;

public class IconFactory {

  private static final ConcurrentHashMap<String, Image> CACHE = new ConcurrentHashMap<>(32, 0.25f);

  public static Image image(String path, int size) {
    return CACHE.computeIfAbsent(path, p -> {
      var cl = Thread.currentThread().getContextClassLoader();
      var url = requireNonNull(cl.getResource(path), () -> "No icon at" + path);
      return size == 0
        ? new Image(url.toString(), false)
        : new Image(url.toString(), size, size, true, true, false);
    });
  }

  public static Image image(String path) {
    return image(path, 0);
  }

  public static ImageView icon(String path, int size) {
    return new ImageView(image(path, size));
  }

  public static ImageView icon(String path) {
    return icon(path, 0);
  }
}
