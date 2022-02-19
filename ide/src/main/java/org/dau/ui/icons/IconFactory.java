package org.dau.ui.icons;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.concurrent.ConcurrentHashMap;

public class IconFactory {

  private static final ConcurrentHashMap<Key, Image> CACHE = new ConcurrentHashMap<>(32);

  public static Image image(String path, int size) {
    if (path == null) {
      return null;
    }
    return CACHE.computeIfAbsent(new Key(path, size), p -> {
      var cl = Thread.currentThread().getContextClassLoader();
      var url = cl.getResource(p.path);
      if (url == null) {
        return null;
      }
      return p.size == 0
        ? new Image(url.toString(), false)
        : new Image(url.toString(), p.size, p.size, true, true, false);
    });
  }

  public static Image image(String path) {
    return image(path, 0);
  }

  public static ImageView icon(String path, int size) {
    var image = image(path, size);
    return image == null ? null : new ImageView(image);
  }

  public static ImageView icon(String path) {
    return icon(path, 0);
  }

  public static FontIcon icon(Ikon ikon, Color color, int size) {
    var icon = new FontIcon(ikon);
    icon.setIconColor(color);
    if (size > 0) {
      icon.setIconSize(size);
    }
    return icon;
  }

  public static FontIcon icon(Ikon ikon, int size) {
    return icon(ikon, Color.LIGHTGRAY, size);
  }

  private record Key(String path, int size) {
  }
}
