package org.dau.ide.schema

import org.dau.runtime.Block

@Block("Demo block")
class SchemaDemoBlock(sync: String, name: String, code: Int, len: Double, angle: Double, speed: Double) {

  fun distance(): Double {
    return 0.0
  }

  fun out(): Double {
    return 0.0
  }

  fun delta(): Double {
    return 0.0
  }
}
