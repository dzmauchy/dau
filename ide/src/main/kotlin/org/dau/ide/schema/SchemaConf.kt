package org.dau.ide.schema

import org.dau.ui.schematic.model.FxSchema
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
@ComponentScan
open class SchemaConf(@param:Autowired(required = false) val schema: FxSchema) {

  @Bean
  open fun schema(): FxSchema {
    return schema
  }
}
