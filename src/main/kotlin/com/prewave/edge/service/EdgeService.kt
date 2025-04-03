package com.prewave.edge.service

import com.prewave.edge.dto.CreateEdgeDto
import com.prewave.edge.dto.EdgeResponseDto
import com.prewave.edge.exception.EntityNotFoundException
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.generated.tables.Edge
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Service providing functionality for edge db table.
 */
@Service
@Transactional
class EdgeService(private val dslContext: DSLContext) {

    fun createEdge(createEdgeDto: CreateEdgeDto): Int {
        return dslContext.insertInto(Edge.EDGE)
            .set(Edge.EDGE.FROM_ID, createEdgeDto.fromId)
            .set(Edge.EDGE.TO_ID, createEdgeDto.toId)
            .execute()
    }

    fun getAllEdges(): List<EdgeResponseDto> {
        return dslContext.select()
            .from(Edge.EDGE)
            .fetch()
            .map { toEdgeResponseDto(it) }
    }

    fun findById(edgeId: Int): EdgeResponseDto {
        val fetchOne = dslContext.select()
            .from(Edge.EDGE)
            .where(Edge.EDGE.ID.eq(edgeId))
            .fetchOne()

        if (fetchOne != null) {
            return toEdgeResponseDto(fetchOne)
        } else throw EntityNotFoundException("Edge not found")
    }

    private fun toEdgeResponseDto(edgeRecord: Record): EdgeResponseDto {
        return EdgeResponseDto(
            edgeRecord.get(Edge.EDGE.ID),
            edgeRecord.get(Edge.EDGE.FROM_ID),
            edgeRecord.get(Edge.EDGE.TO_ID)
        )
    }

    fun deleteById(edgeId: Int) {
        dslContext.deleteFrom(Edge.EDGE)
            .where(Edge.EDGE.ID.eq(edgeId))
            .execute()
    }
}
