package org.dau.ide.main

import javafx.stage.Stage
import org.dau.di.Init
import org.dau.ui.icons.IconFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.event.ContextStartedEvent
import org.springframework.context.event.EventListener

@Configuration(proxyBeanMethods = false)
@ComponentScan
@Import(Init::class)
open class MainConf(@param:Autowired(required = false) private val primaryStage: Stage) {

  @Bean
  @MainQualifier
  open fun primaryStage(): Stage {
    return primaryStage.apply { isMaximized = true }
  }

  @EventListener
  fun onStart(event: ContextStartedEvent) {
    primaryStage.icons.add(IconFactory.image("icons/schema.png"))
    primaryStage.title = "IDE"
    primaryStage.show()
  }
}
