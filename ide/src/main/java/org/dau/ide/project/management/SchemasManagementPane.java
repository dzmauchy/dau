package org.dau.ide.project.management;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.SetChangeListener;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.util.converter.DefaultStringConverter;
import org.dau.ide.l10n.Localization;
import org.dau.ide.project.ProjectSchemas;
import org.dau.ui.icons.IconFactory;
import org.dau.ui.schematic.fx.model.FxProject;
import org.dau.ui.schematic.fx.model.FxSchema;
import org.dau.ui.utils.TableColumnBuilder;
import org.kordamp.ikonli.ionicons4.Ionicons4IOS;
import org.kordamp.ikonli.material.Material;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import static org.dau.di.Builder.with;

@Component
@Order(1)
@Qualifier("management")
public class SchemasManagementPane extends TitledPane {

  private final FxProject project;
  private final SetChangeListener<FxSchema> schemaSetChangeListener = this::onUpdateSchemas;
  private final ObservableList<FxSchema> schemas;
  private final TableView<FxSchema> table;
  private final ToolBar toolBar = new ToolBar();
  private final ProjectSchemas projectSchemas;

  public SchemasManagementPane(FxProject project, ProjectSchemas projectSchemas) {
    textProperty().bind(Localization.binding("Schemas"));
    setGraphic(IconFactory.icon(Ionicons4IOS.LIST, 16));
    this.project = project;
    this.projectSchemas = projectSchemas;
    this.project.schemas.addListener(schemaSetChangeListener);
    this.schemas = FXCollections.observableArrayList(project.schemas);
    this.table = new TableView<>(schemas);
    this.table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
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
    this.table.getColumns().add(new TableColumnBuilder<FxSchema, HBox>("Actions")
      .width(400)
      .value(f -> {
        var box = new HBox(
          with(new Button(),
            b -> b.setGraphic(IconFactory.icon(Material.OPEN_IN_NEW, 16)),
            b -> b.setOnAction(ev -> projectSchemas.addSchema(f.getValue()))
          )
        );
        box.setAlignment(Pos.CENTER);
        return new SimpleObjectProperty<>(box);
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
    toolBar.getItems().addAll(
      with(new Button(),
        b -> b.setGraphic(IconFactory.icon(Ionicons4IOS.ADD, 20)),
        b -> b.setOnAction(ev -> {
          var schema = new FxSchema();
          schema.name.set("New schema");
          project.schemas.add(schema);
        })
      ),
      with(new Button(),
        b -> b.setGraphic(IconFactory.icon(Ionicons4IOS.REMOVE, 20)),
        b -> b.setOnAction(ev -> {
          var item = table.getSelectionModel().getSelectedItem();
          project.schemas.remove(item);
        }),
        b -> b.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull())
      )
    );
    toolBar.getItems().add(new Separator(Orientation.VERTICAL));
    toolBar.getItems().add(with(new Button(),
      b -> b.setGraphic(IconFactory.icon(Material.SORT, 20)),
      b -> b.setOnAction(ev -> table.sort())
    ));
    toolBar.getItems().add(new Separator(Orientation.VERTICAL));
    toolBar.getItems().add(with(new Button(),
      b -> b.setGraphic(IconFactory.icon(Material.OPEN_IN_NEW, 20)),
      b -> b.setOnAction(ev -> projectSchemas.addSchema(table.getSelectionModel().getSelectedItem())),
      b -> b.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull())
    ));
  }
}
