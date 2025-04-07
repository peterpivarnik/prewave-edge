package com.prewave.edge.service

import com.prewave.edge.dto.CreateEdgeDto
import com.prewave.edge.exception.CyclicEdgesException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
@SpringBootTest(webEnvironment = RANDOM_PORT)
class EdgeServiceTest {

    companion object {
        @JvmStatic
        var postgres: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:16-alpine")

        @JvmStatic
        @BeforeAll fun beforeAll() {
            postgres.start()
        }

        @JvmStatic
        @AfterAll fun afterAll() {
            postgres.stop()
        }
    }

    @Autowired
    private val edgeService: EdgeService? = null

    @BeforeEach fun beforeEach() {
        edgeService?.deleteAll()
    }

    @Test fun shouldCreateEdge() {
        var allEdges = edgeService?.getAllEdges()
        assertThat(allEdges).isEmpty()

        val edgeId = edgeService?.createEdge(CreateEdgeDto(1, 2))

        assertThat(edgeId).isNotNull()
        allEdges = edgeService?.getAllEdges()
        assertThat(allEdges).hasSize(1)
        assertThat(allEdges!!.first().id).isEqualTo(edgeId)
    }

    @Test fun shouldCyclicEdgesExceptionDuringEdgeCreation() {
        val allEdges = edgeService?.getAllEdges()
        assertThat(allEdges).isEmpty()

        edgeService?.createEdge(CreateEdgeDto(1, 2))

        val cyclicEdgesException =
            assertThrows(CyclicEdgesException::class.java) { edgeService!!.createEdge(CreateEdgeDto(2, 1)) }
        assertThat(cyclicEdgesException.message).isEqualTo("Edge creation not possible due to cyclic edges!")
    }

    @Test fun shouldReturnAllEdges() {
        edgeService?.createEdge(CreateEdgeDto(1, 2))
        edgeService?.createEdge(CreateEdgeDto(1, 3))
        edgeService?.createEdge(CreateEdgeDto(1, 4))
        edgeService?.createEdge(CreateEdgeDto(1, 5))

        val allEdges = edgeService?.getAllEdges()

        assertThat(allEdges).hasSize(4)
    }

    @Test fun shouldReturnEdgeById() {
        val edgeId = edgeService?.createEdge(CreateEdgeDto(1, 2))

        val edge = edgeService?.findById(edgeId!!)

        assertThat(edge).isNotNull()
        assertThat(edge!!.id).isEqualTo(edgeId)
        assertThat(edge.fromId).isEqualTo(1)
        assertThat(edge.toId).isEqualTo(2)
    }

    @Test fun shouldDeleteEdge() {
        val edgeId = edgeService?.createEdge(CreateEdgeDto(1, 2))
        var allEdges = edgeService?.getAllEdges()
        assertThat(allEdges).hasSize(1)

        edgeService?.deleteById(edgeId!!)

        allEdges = edgeService?.getAllEdges()
        assertThat(allEdges).isEmpty()
    }

    @Test fun shouldGetTree() {
        edgeService?.createEdge(CreateEdgeDto(1, 2))
        edgeService?.createEdge(CreateEdgeDto(2, 3))
        edgeService?.createEdge(CreateEdgeDto(3, 4))
        edgeService?.createEdge(CreateEdgeDto(3, 5))

        val tree = edgeService?.getTree(1)

        assertThat(tree).isNotNull()
        assertThat(tree?.edgesWithChild).hasSize(1)
        assertThat(tree?.edgesWithChild?.first()?.fromId).isEqualTo(1)
        assertThat(tree?.edgesWithChild?.first()?.toId).isEqualTo(2)
        assertThat(tree?.edgesWithChild?.first()?.edgesWithChild).hasSize(1)
        assertThat(tree?.edgesWithChild?.first()?.edgesWithChild?.first()?.fromId).isEqualTo(2)
        assertThat(tree?.edgesWithChild?.first()?.edgesWithChild?.first()?.toId).isEqualTo(3)
        assertThat(tree?.edgesWithChild?.first()?.edgesWithChild?.first()?.edgesWithChild).hasSize(2)
        assertThat(tree?.edgesWithChild?.first()?.edgesWithChild?.first()?.edgesWithChild?.map { edge -> edge.fromId })
            .containsAll(listOf(3))
        assertThat(tree?.edgesWithChild?.first()?.edgesWithChild?.first()?.edgesWithChild?.map { edge -> edge.toId })
            .containsAll(listOf(4, 5))
        assertThat(tree?.edgesWithChild?.first()?.edgesWithChild?.first()?.edgesWithChild?.get(0)?.edgesWithChild).isEmpty()
        assertThat(tree?.edgesWithChild?.first()?.edgesWithChild?.first()?.edgesWithChild?.get(1)?.edgesWithChild).isEmpty()
    }
}