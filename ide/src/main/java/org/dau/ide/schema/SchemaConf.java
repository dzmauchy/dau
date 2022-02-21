package org.dau.ide.schema;

import org.dau.ui.schematic.fx.model.FxSchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ComponentScan
public class SchemaConf {

  public final FxSchema schema;

  public SchemaConf(@Autowired(required = false) FxSchema schema) {
    this.schema = schema;
  }

  @Bean
  public FxSchema schema() {
    return schema;
  }
}
