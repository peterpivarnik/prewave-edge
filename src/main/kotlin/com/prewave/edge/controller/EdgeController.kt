package com.prewave.edge.controller

import com.prewave.edge.dto.CreateEdgeDto
import com.prewave.edge.dto.EdgeResponseDto
import com.prewave.edge.dto.TreeResponse
import com.prewave.edge.service.EdgeService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus.CONFLICT
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI

/**
 * Rest controller for http resource /edge
 */
@RestController
class EdgeController {

    lateinit var edgeService: EdgeService

    @Autowired
    fun initialize(edgeService: EdgeService) {
        this.edgeService = edgeService
    }

    @PostMapping(value = ["/edge"])
    fun createEdge(@RequestBody createEdgeDto: CreateEdgeDto): ResponseEntity<Void> {
        val response: Int = edgeService.createEdge(createEdgeDto)
        if (response != 1) {
            return ResponseEntity.status(CONFLICT)
                .build()
        }
        return ResponseEntity.created(URI.create(""))
            .build()
    }

    @GetMapping(value = ["/edge"])
    fun getAllEdges(): ResponseEntity<List<EdgeResponseDto>> {
        val allEdges = edgeService.getAllEdges()
        return ResponseEntity.ok(allEdges)
    }

    @GetMapping(value = ["/edge/{edgeId}"])
    fun getEdge(@PathVariable edgeId: Int): ResponseEntity<EdgeResponseDto> {
        val edge: EdgeResponseDto = edgeService.findById(edgeId)
        return ResponseEntity.ok(edge)
    }

    @DeleteMapping(value = ["/edge/{edgeId}"])
    fun deleteEdge(@PathVariable edgeId: Int): ResponseEntity<Void> {
        edgeService.deleteById(edgeId)
        return ResponseEntity.noContent().build()
    }

    @GetMapping(value = ["/tree/{fromId}"])
    fun getTree(@PathVariable fromId: Int): ResponseEntity<TreeResponse> {
        val tree = edgeService.getTree(fromId)
        return ResponseEntity.ok(tree)
    }
}