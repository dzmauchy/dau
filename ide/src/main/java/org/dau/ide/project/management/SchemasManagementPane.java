package org.dau.ide.project.management;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.SetChangeListener;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.util.converter.DefaultStringConverter;
import org.dau.di.B;
import org.dau.ide.l10n.Localization;
import org.dau.ui.icons.IconFactory;
import org.dau.ui.schematic.fx.model.FxProject;
import org.dau.ui.schematic.fx.model.FxSchema;
import org.dau.ui.utils.TableColumnBuilder;
import org.kordamp.ikonli.ionicons4.Ionicons4IOS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
@Qualifier("management")
public class SchemasManagementPane extends TitledPane {

  private final FxProject project;
  private final SetChangeListener<FxSchema> schemaSetChangeListener = this::onUpdateSchemas;
  private final ObservableList<FxSchema> schemas;
  private final TableView<FxSchema> table;
  private final ToolBar toolBar = new ToolBar();

  public SchemasManagementPane(FxProject project) {
    textProperty().bind(Localization.binding("Schemas"));
    setGraphic(IconFactory.icon(Ionicons4IOS.LIST, 16));
    this.project = project;
    this.project.schemas.addListener(schemaSetChangeListener);
    this.schemas = FXCollections.observableArrayList(project.schemas);
    this.table = new TableView<>(schemas);
    this.table.getColumns().add(new TableColumnBuilder<FxSchema, String>("Id")
      .width(128)
      .value(f -> new SimpleStringProperty(f.getValue().id))
      .build()
    );
    this.table.getColumns().add(new TableColumnBuilder<FxSchema, String>("Name")
      .width(400)
      .value(f -> f.getValue().name)
      .with(c -> c.setSortType(TableColumn.SortType.ASCENDING))
      .with(c -> c.setEditable(true))
      .cell(c -> new TextFieldTableCell<>(new DefaultStringConverter()))
      .build()
    );
    this.table.getColumns().add(new TableColumnBuilder<FxSchema, Number>("Blocks")
      .width(128)
      .value(f -> f.getValue().blockCount())
      .with(c -> c.setEditable(false))
      .cell(c -> {
        var cell = new TextFieldTableCell<FxSchema, Number>();
        cell.setAlignment(Pos.BASELINE_RIGHT);
        cell.setEditable(false);
        return cell;
      })
      .build()
    );
    this.table.setEditable(true);
    this.table.getSortOrder().add(table.getColumns().get(1));
    setContent(new BorderPane(table, toolBar, null, null, null));
  }

  private void onUpdateSchemas(SetChangeListener.Change<? extends FxSchema> change) {
    if (change.wasRemoved()) {
      var e = change.getElementRemoved();
      schemas.removeIf(s -> s.id.equals(e.id));
    }
    if (change.wasAdded()) {
      var e = change.getElementAdded();
      schemas.add(e);
    }
  }

  @EventListener
  public void onClose(ContextClosedEvent event) {
    project.schemas.removeListener(schemaSetChangeListener);
  }

  @Autowired
  public void initToolbar() {
    toolBar.getItems().add(B.with(new Button(),
      b -> b.setGraphic(IconFactory.icon(Ionicons4IOS.ADD, 20)),
      b -> b.setOnAction(ev -> {
        var schema = new FxSchema();
        schema.name.set("New schema");
        project.schemas.add(schema);
      })
    ));
  }
}
