package org.dau.ide.project

import javafx.beans.value.ChangeListener
import org.dau.di.Ctx
import org.dau.ide.schema.SchemaConf
import org.dau.ide.schema.SchemaTab
import org.dau.ui.schematic.model.FxSchema
import org.springframework.beans.factory.ObjectFactory
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextClosedEvent
import org.springframework.stereotype.Component
import java.util.function.Supplier

@Component
class ProjectSchemas(private val ctx: Ctx, private val tabs: ObjectFactory<ProjectTabs>) {

  fun addSchema(schema: FxSchema) {
    if (tabs.getObject().tabs.stream().anyMatch { it is SchemaTab && it.schema.id == schema.id }) {
      return
    }
    val newCtx = Ctx(ctx, schema.name.get())
    val ih = ChangeListener<String> { _, _, nv -> newCtx.displayName = nv }
    schema.name.addListener(ih)
    newCtx.addApplicationListener(ApplicationListener { _: ContextClosedEvent -> schema.name.removeListener(ih) })
    newCtx.registerBean(SchemaConf::class.java, Supplier { SchemaConf(schema) })
    newCtx.refresh()
    newCtx.start()
  }
}
