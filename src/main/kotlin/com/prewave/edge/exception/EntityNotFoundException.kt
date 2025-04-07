package com.prewave.edge.exception

/**
 * Exception to be thrown in case entity not found.
 */
class EntityNotFoundException(val entityName: String, override val message: String) : EdgeException(message) {
}