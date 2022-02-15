package org.dau.ui.schematic.layout.model;

import org.dau.ui.schematic.IdEntity;

public interface BlockInput extends IdEntity, BlockElement {
  double getConnectionPoint();
}
