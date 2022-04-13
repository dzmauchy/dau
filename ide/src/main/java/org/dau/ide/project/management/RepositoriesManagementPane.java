package org.dau.ide.project.management;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.SetChangeListener;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import org.dau.ide.l10n.Localization;
import org.dau.ui.icons.IconFactory;
import org.dau.ui.schematic.fx.model.FxProject;
import org.kordamp.ikonli.ionicons4.Ionicons4IOS;
import org.kordamp.ikonli.material.Material;
import org.kordamp.ikonli.metrizeicons.MetrizeIcons;
import org.kordamp.ikonli.remixicon.RemixiconAL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.net.URI;

import static org.dau.di.Builder.with;

@Component
@Order(2)
@Qualifier("management")
public class RepositoriesManagementPane extends TitledPane {

  private final FxProject project;
  private final ObservableList<URI> repositories;
  private final SetChangeListener<URI> changeListener = this::onUpdateRepositories;
  private final ListView<URI> listView;
  private final ToolBar toolBar = new ToolBar();

  public RepositoriesManagementPane(FxProject project) {
    textProperty().bind(Localization.binding("Repositories"));
    setGraphic(IconFactory.icon(RemixiconAL.GIT_REPOSITORY_FILL, 16));
    this.project = project;
    this.repositories = FXCollections.observableArrayList(project.repositories);
    this.project.repositories.addListener(changeListener);
    this.listView = new ListView<>(repositories);
    this.listView.setCellFactory(v -> new ListCell<>() {
      @Override
      protected void updateItem(URI item, boolean empty) {
        super.updateItem(item, empty);
        if (item != null) {
          setText(item.toString());
          setGraphic(IconFactory.icon(MetrizeIcons.MET_LINK_URL, 18));
        }
      }
    });
    setContent(new BorderPane(listView, toolBar, null, null, null));
  }

  @EventListener
  public void onClose(ContextClosedEvent event) {
    project.repositories.removeListener(changeListener);
  }

  private void onUpdateRepositories(SetChangeListener.Change<? extends URI> change) {
    if (change.wasRemoved()) {
      repositories.remove(change.getElementRemoved());
    }
    if (change.wasAdded()) {
      repositories.add(change.getElementAdded());
    }
  }

  @Autowired
  public void initToolbar() {
    toolBar.getItems().addAll(
      with(new Button(),
        b -> b.setGraphic(IconFactory.icon(Ionicons4IOS.ADD_CIRCLE_OUTLINE, 20)),
        b -> b.setTooltip(with(new Tooltip("URL"))),
        b -> b.setOnAction(ev -> {
          var dlg = new TextInputDialog();
          dlg.initModality(Modality.APPLICATION_MODAL);
          dlg.initOwner(getScene().getWindow());
          dlg.setTitle("URL");
          dlg.setContentText("URL: ");
          dlg.getDialogPane().setPrefWidth(800);
          dlg.headerTextProperty().bind(Localization.binding("Repository URL"));
          dlg.showAndWait().ifPresent(url -> project.repositories.add(URI.create(url)));
        })
      )
    );
    toolBar.getItems().add(new Separator(Orientation.VERTICAL));
    toolBar.getItems().addAll(
      with(new Button(),
        b -> b.setGraphic(IconFactory.icon(Ionicons4IOS.REMOVE, 20)),
        b -> b.setTooltip(with(new Tooltip(), t -> t.textProperty().bind(Localization.binding("Remove")))),
        b -> b.disableProperty().bind(listView.getSelectionModel().selectedItemProperty().isNull()),
        b -> b.setOnAction(ev -> project.repositories.remove(listView.getSelectionModel().getSelectedItem()))
      ),
      with(new Button(),
        b -> b.setGraphic(IconFactory.icon(Material.CLEAR, 20)),
        b -> b.setTooltip(with(new Tooltip(), t -> t.textProperty().bind(Localization.binding("Clear")))),
        b -> b.disableProperty().bind(Bindings.isEmpty(listView.getItems())),
        b -> b.setOnAction(ev -> project.repositories.clear())
      )
    );
  }
}
