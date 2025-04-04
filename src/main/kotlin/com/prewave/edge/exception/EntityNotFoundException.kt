package com.prewave.edge.exception

class EntityNotFoundException(val entityName: String, override val message: String) : EdgeException(message) {
}