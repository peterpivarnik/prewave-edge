package com.prewave.edge.exception

/**
 * Exception to be thrown in case cyclic edges to be created.
 */
class CyclicEdgesException(override val message: String, val from: Int, val to: Int) : EdgeException(message) {

}
