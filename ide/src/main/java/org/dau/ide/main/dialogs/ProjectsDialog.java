package org.dau.ide.main.dialogs;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TableView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.dau.di.PrototypeComponent;
import org.dau.ide.main.MainDirectories;
import org.dau.ide.main.MainQualifier;
import org.dau.ui.schematic.fx.model.FxProject;
import org.dau.ui.utils.TableColumnBuilder;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.dau.ide.l10n.Localization.binding;

@PrototypeComponent
public class ProjectsDialog extends Dialog<Path> {

  private final SimpleObjectProperty<Path> path = new SimpleObjectProperty<>(this, "path");
  private final TableView<Path> table = new TableView<>();

  public ProjectsDialog(@MainQualifier Stage stage, MainDirectories directories) throws Exception {
    initOwner(stage);
    initModality(Modality.WINDOW_MODAL);
    initStyle(StageStyle.UTILITY);
    getDialogPane().getButtonTypes().setAll(ButtonType.APPLY, ButtonType.CANCEL);
    initTable(directories);
    getDialogPane().setContent(table);
    titleProperty().bind(binding("Projects"));
    setResultConverter(param -> switch (param.getButtonData()) {
      case APPLY -> path.get();
      case CANCEL_CLOSE -> null;
      default -> throw new IllegalStateException();
    });
    setResizable(false);
  }

  public void initTable(MainDirectories directories) throws Exception {
    table.getColumns().add(
      new TableColumnBuilder<Path, String>("Id")
        .value(f -> new SimpleStringProperty(f.getValue().getFileName().toString()))
        .width(100)
        .build()
    );
    table.getColumns().add(
      new TableColumnBuilder<Path, String>("Name")
        .value(f -> new SimpleStringProperty(FxProject.load(f.getValue()).name.get()))
        .width(300)
        .build()
    );
    try (var ds = Files.newDirectoryStream(directories.homeDir)) {
      for (var path : ds) {
        if (FxProject.isProject(path)) {
          table.getItems().add(path);
        }
      }
    }
    path.bind(table.getSelectionModel().selectedItemProperty());
  }
}
