package org.dau.ide.main;

import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.EventListener;

@Configuration(proxyBeanMethods = false)
@ComponentScan(basePackages = "org.dau.ide.main")
public class MainConf {

  private final Stage primaryStage;

  public MainConf(@Autowired(required = false) Stage primaryStage) {
    this.primaryStage = primaryStage;
    this.primaryStage.setMaximized(true);
  }

  @Bean
  @Main
  public Stage primaryStage() {
    return primaryStage;
  }

  @EventListener
  public void onStart(ContextStartedEvent event) {
    primaryStage.show();
  }
}
