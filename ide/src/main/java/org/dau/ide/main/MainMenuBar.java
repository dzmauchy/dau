package org.dau.ide.main;

import javafx.scene.control.MenuBar;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import static org.dau.ide.action.FxActions.fillMenuBar;

@Component
public final class MainMenuBar extends MenuBar {

  @EventListener
  public void onRefresh(ContextRefreshedEvent event) {
    fillMenuBar(event.getApplicationContext(), this, MainQualifier.class);
  }
}
