package com.prewave.edge.controller

import com.prewave.edge.dto.CreateEdgeDto
import com.prewave.edge.dto.EdgeResponseDto
import com.prewave.edge.dto.validation.FromIdToId
import com.prewave.edge.dto.TreeResponse
import com.prewave.edge.service.EdgeService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI

/**
 * Rest controller for http resource /edge
 */
@RestController
class EdgeController(var edgeService: EdgeService) {

    /**
     * Creates edge.
     */
    @PostMapping(value = ["/edge"])
    fun createEdge(@RequestBody @FromIdToId createEdgeDto: CreateEdgeDto): ResponseEntity<EdgeResponseDto> {
        val edgeId = edgeService.createEdge(createEdgeDto)
        return ResponseEntity.created(URI.create("http://localhost:8080/edge/${edgeId}"))
            .build()
    }

    /**
     * Get all edges.
     */
    @GetMapping(value = ["/edge"])
    fun getAllEdges(): ResponseEntity<List<EdgeResponseDto>> {
        val allEdges = edgeService.getAllEdges()
        return ResponseEntity.ok(allEdges)
    }

    /**
     * Get edge by edgeId.
     */
    @GetMapping(value = ["/edge/{edgeId}"])
    fun getEdge(@PathVariable edgeId: Int): ResponseEntity<EdgeResponseDto> {
        val edge: EdgeResponseDto = edgeService.findById(edgeId)
        return ResponseEntity.ok(edge)
    }

    /**
     * Delete edge by edgeId.
     */
    @DeleteMapping(value = ["/edge/{edgeId}"])
    fun deleteEdge(@PathVariable edgeId: Int): ResponseEntity<Void> {
        edgeService.deleteById(edgeId)
        return ResponseEntity.noContent().build()
    }

    /**
     * Get tree of all edges.
     */
    @GetMapping(value = ["/tree/{fromId}"])
    fun getTree(@PathVariable fromId: Int): ResponseEntity<TreeResponse> {
        val tree = edgeService.getTree(fromId)
        return ResponseEntity.ok(tree)
    }
}