package com.prewave.edge.exception

class CyclicEdgesException(override val message: String, val from: Int, val to: Int) : EdgeException(message) {

}
