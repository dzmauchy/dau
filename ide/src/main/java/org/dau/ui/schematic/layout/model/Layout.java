package org.dau.ui.schematic.layout.model;

import java.util.stream.Stream;

public interface Layout {
  Block getBlock(String id);
  Stream<? extends Block> getBlocks();
  Stream<? extends BlockConnection> getConnections();
  Stream<? extends BlockConnection> getInputConnection(String id);
  Stream<? extends BlockConnection> getOutputConnections(String id);
  Stream<? extends BlockInput> getOutputConnections(String blockId, String outputId);
  Stream<? extends BlockOutput> getInputConnections(String blockId, String inputId);
}
