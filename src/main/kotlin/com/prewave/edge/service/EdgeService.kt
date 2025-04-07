package com.prewave.edge.service

import com.prewave.edge.dto.CreateEdgeDto
import com.prewave.edge.dto.EdgeResponseDto
import com.prewave.edge.dto.EdgeWithChild
import com.prewave.edge.dto.TreeResponse
import com.prewave.edge.exception.CyclicEdgesException
import com.prewave.edge.exception.EntityNotFoundException
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.Record3
import org.jooq.Result
import org.jooq.generated.tables.Edge.EDGE
import org.jooq.impl.DSL.*
import org.jooq.impl.SQLDataType.INTEGER
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Service providing functionality for edge db table.
 */
@Service
@Transactional
class EdgeService(private val dslContext: DSLContext) {

    /**
     * Creates edge.
     */
    fun createEdge(createEdgeDto: CreateEdgeDto): Int? {
        val edgesForTree = getEdgesForTree(createEdgeDto.toId)
        val filteredEdges = edgesForTree.filter { edge -> edge.get(EDGE.TO_ID) == createEdgeDto.fromId }
        if (filteredEdges.isNotEmpty()) {
            throw CyclicEdgesException("Edge creation not possible due to cyclic edges!",
                                       createEdgeDto.fromId,
                                       createEdgeDto.toId)
        }
        val edgeIdRecord = dslContext.insertInto(EDGE)
            .set(EDGE.FROM_ID, createEdgeDto.fromId)
            .set(EDGE.TO_ID, createEdgeDto.toId)
            .returningResult(EDGE.ID)
            .fetchOne()

        if (edgeIdRecord != null) {
            return edgeIdRecord.get(EDGE.ID)
        } else throw EntityNotFoundException(EDGE.name, "Edge not found")
    }

    /**
     * List all edges.
     */
    fun getAllEdges(): List<EdgeResponseDto> {
        return dslContext.select()
            .from(EDGE)
            .fetch()
            .map { toEdgeResponseDto(it) }
    }

    /**
     * Get edge by edgeId.
     */
    fun findById(edgeId: Int): EdgeResponseDto {
        val fetchOne = dslContext.select()
            .from(EDGE)
            .where(EDGE.ID.eq(edgeId))
            .fetchOne()

        if (fetchOne != null) {
            return toEdgeResponseDto(fetchOne)
        } else throw EntityNotFoundException(EDGE.name, "Edge with id=$edgeId not found!")
    }

    private fun toEdgeResponseDto(edgeRecord: Record): EdgeResponseDto {
        return EdgeResponseDto(edgeRecord.get(EDGE.ID),
                               edgeRecord.get(EDGE.FROM_ID),
                               edgeRecord.get(EDGE.TO_ID))
    }

    /**
     * Delete edge by edgeId.
     */
    fun deleteById(edgeId: Int) {
        dslContext.deleteFrom(EDGE)
            .where(EDGE.ID.eq(edgeId))
            .execute()
    }

    /**
     * Get tree of edges by fromId.
     */
    fun getTree(fromId: Int): TreeResponse {
        val edges = getEdgesForTree(fromId)
        return TreeResponse(createEdgesWithChild(edges, fromId))
    }

    private fun getEdgesForTree(fromId: Int): Result<Record3<Int, Int, Int>> {
        val cte = name("Tree")
            .fields(EDGE.ID.name, EDGE.FROM_ID.name, EDGE.TO_ID.name)
            .`as`(select(EDGE.ID, EDGE.FROM_ID, EDGE.TO_ID)
                      .from(EDGE)
                      .where(EDGE.FROM_ID.eq(fromId))
                      .unionAll(select(EDGE.ID, EDGE.FROM_ID, EDGE.TO_ID)
                                    .from(EDGE)
                                    .join(table("Tree")
                                              .`as`("Tree"))
                                    .on(EDGE.FROM_ID.eq(field("Tree.TO_ID", INTEGER)))))
        val edges = dslContext.withRecursive(cte)
            .selectFrom(cte)
            .fetch()
        return edges
    }

    private fun createEdgesWithChild(edges: List<Record3<Int, Int, Int>>, fromId: Int): List<EdgeWithChild> {
        val filtered = edges.filter { edge -> edge.get(EDGE.FROM_ID).equals(fromId) }
        return filtered.parallelStream()
            .map { edge ->
                EdgeWithChild(edge.get(EDGE.ID), edge.get(EDGE.FROM_ID), edge.get(EDGE.TO_ID),
                              createEdgesWithChild(edges.minus(filtered.toSet()), edge.get(EDGE.TO_ID)))
            }
            .toList()
    }

    /**
     * Delete all from edge table. For test purposes only!
     */
    fun deleteAll() {
        dslContext.deleteFrom(EDGE)
            .execute()
    }
}
