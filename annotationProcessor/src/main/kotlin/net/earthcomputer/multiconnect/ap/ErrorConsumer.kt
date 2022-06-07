package net.earthcomputer.multiconnect.ap

import javax.lang.model.element.Element

interface ErrorConsumer {
    fun report(message: String, element: Element)
}
