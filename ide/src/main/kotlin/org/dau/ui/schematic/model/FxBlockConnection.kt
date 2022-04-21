package org.dau.ui.schematic.model

import org.w3c.dom.Document
import org.w3c.dom.Element

data class FxBlockConnection(val out: FxBlock.Output, val inp: FxBlock.Input) {

    fun toXml(doc: Document): Element {
        val el = doc.createElement("connection")
        el.setAttribute("out-block-id", out.block.id.toString())
        el.setAttribute("out", out.id)
        el.setAttribute("in-block-id", inp.block.id.toString())
        el.setAttribute("in", inp.id)
        return el
    }

    override fun toString(): String {
        return "Conn($out->$inp)"
    }
}
