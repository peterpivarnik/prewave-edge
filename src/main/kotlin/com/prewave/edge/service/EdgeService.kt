package com.prewave.edge.service

import com.prewave.edge.dto.CreateEdgeDto
import com.prewave.edge.dto.EdgeResponseDto
import com.prewave.edge.exception.EntityNotFoundException
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.generated.public_.Sequences.SQ_ID
import org.jooq.generated.public_.tables.Edge
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Service providing functionality for edge db table.
 */
@Service
@Transactional
class EdgeService {

    @Autowired
    private val dslContext: DSLContext? = null

    fun createEdge(createEdgeDto: CreateEdgeDto?): Int {
        val id = SQ_ID.nextval()
        return dslContext!!.insertInto(Edge.EDGE)
            .values(id, createEdgeDto!!.fromId, createEdgeDto.toId)
            .execute()
    }

    fun getAllEdges(): List<EdgeResponseDto> {
        return dslContext!!.select()
            .from(Edge.EDGE)
            .fetch()
            .map { toEdgeResponseDto(it) }
    }

    fun getEdge(edgeId: Int?): EdgeResponseDto {
        val fetchOne = dslContext!!.select()
            .from(Edge.EDGE)
            .where(Edge.EDGE.ID.eq(edgeId))
            .fetchOne()

        if (fetchOne != null) {
            return toEdgeResponseDto(fetchOne)
        } else throw EntityNotFoundException("Edge not found")
    }

    private fun toEdgeResponseDto(fetchOne: Record): EdgeResponseDto {
        return EdgeResponseDto(
            fetchOne.get(Edge.EDGE.ID),
            fetchOne.get(Edge.EDGE.FROM_ID),
            fetchOne.get(Edge.EDGE.TO_ID)
        )
    }

    fun deleteById(edgeId: Int?) {
        dslContext!!.deleteFrom(Edge.EDGE)
            .where(Edge.EDGE.ID.eq(edgeId))
            .execute()
    }
}
