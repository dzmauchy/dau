package org.dau.ui.schematic.layout.model;

import org.dau.ui.schematic.IdEntity;

import java.util.stream.Stream;

public interface Block extends IdEntity {
  Layout getSchema();
  double getX();
  double getY();
  double getW();
  double getH();
  BlockInput getInput(String id);
  BlockOutput getOutput(String id);
  Stream<? extends BlockInput> getInputs();
  Stream<? extends BlockOutput> getOutputs();
}
