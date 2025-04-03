package com.prewave.edge.dto

data class EdgeWithChild(var id: Int, var fromId: Int, var toId: Int, var edgesWithChild: List<EdgeWithChild>) {

}
