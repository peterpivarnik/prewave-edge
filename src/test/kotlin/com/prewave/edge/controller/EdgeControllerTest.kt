package com.prewave.edge.controller

import com.prewave.edge.dto.CreateEdgeDto
import com.prewave.edge.dto.EdgeResponseDto
import com.prewave.edge.dto.TreeResponse
import com.prewave.edge.service.EdgeService
import io.restassured.RestAssured
import io.restassured.RestAssured.given
import io.restassured.http.ContentType.JSON
import io.restassured.parsing.Parser
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.CoreMatchers.equalTo
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.server.LocalServerPort
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers()
@SpringBootTest(webEnvironment = RANDOM_PORT)
class EdgeControllerTest {

    companion object {
        @JvmStatic
        var postgres: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:16-alpine")

        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            postgres.start()
        }

        @JvmStatic
        @AfterAll
        fun afterAll() {
            postgres.stop()
        }
    }

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private val edgeService: EdgeService? = null

    @BeforeEach
    fun beforeEach() {
        RestAssured.defaultParser = Parser.JSON
        edgeService?.deleteAll()
    }

    @Test
    fun shouldCreateEdgeDto() {
        val request = CreateEdgeDto(1, 2)

        val edgeResponse: EdgeResponseDto = given()
            .port(port)
            .accept(JSON)
            .contentType(JSON)
            .body(request)
            .`when`()
            .post("/edge")
            .then()
            .statusCode(201)
            .extract()
            .`as`(EdgeResponseDto::class.java)

        assertThat(edgeResponse).isNotNull
        assertThat(edgeResponse.id).isNotNull()
        assertThat(edgeResponse.fromId).isEqualTo(1)
        assertThat(edgeResponse.toId).isEqualTo(2)

        val edge = edgeService?.findById(edgeResponse.id)
        assertThat(edge).isNotNull
    }

    @Test
    fun shouldReturnBadRequestWhenFromIdEqualsToId() {
        val request = CreateEdgeDto(2, 2)

        given()
            .port(port)
            .accept(JSON)
            .contentType(JSON)
            .body(request)
            .`when`()
            .post("/edge")
            .then()
            .statusCode(400)
            .body("title", equalTo("Bad Request"))
            .body("status", equalTo(400))
            .body("instance", equalTo("/edge"))
            .body("validationMessage", equalTo("Parameters fromId, and toId must be different!"))
    }

    @Test
    fun shouldReturnConflictWhenCyclicEdgesToBeCreated() {
        edgeService?.createEdge(CreateEdgeDto(1, 2))
        val request = CreateEdgeDto(2, 1)

        given()
            .port(port)
            .accept(JSON)
            .contentType(JSON)
            .body(request)
            .`when`()
            .post("/edge")
            .then()
            .statusCode(409)
            .body("message", equalTo("Edge creation not possible due to cyclic edges!"))
            .body("from", equalTo(2))
            .body("to", equalTo(1))
    }

    @Test
    fun shouldReturnBadRequestWhenEdgeAlreadyExists() {
        edgeService?.createEdge(CreateEdgeDto(1, 2))
        val request = CreateEdgeDto(1, 2)

        given()
            .port(port)
            .accept(JSON)
            .contentType(JSON)
            .body(request)
            .`when`()
            .post("/edge")
            .then()
            .statusCode(400)
            .body("message",
                  equalTo("ERROR: duplicate key value violates unique constraint \"unq_edge_from_id_to_id\"\n  "
                          + "Detail: Key (from_id, to_id)=(1, 2) already exists."))
            .body("constraint", equalTo("unq_edge_from_id_to_id"))
            .body("detail", equalTo("Key (from_id, to_id)=(1, 2) already exists."))
    }

    @Test
    fun shouldGetAllEdges() {
        edgeService?.createEdge(CreateEdgeDto(1, 2))
        edgeService?.createEdge(CreateEdgeDto(1, 3))

        val edges = given()
            .port(port)
            .accept(JSON)
            .contentType(JSON)
            .`when`()
            .get("/edge")
            .then()
            .statusCode(200)
            .extract()
            .`as`(Array<EdgeResponseDto>::class.java)

        assertThat(edges).hasSize(2)
    }

    @Test
    fun shouldFindEdgeById() {
        val edge = edgeService?.createEdge(CreateEdgeDto(1, 3))

        val foundEdge = given()
            .port(port)
            .accept(JSON)
            .contentType(JSON)
            .`when`()
            .get("/edge/${edge?.id}")
            .then()
            .statusCode(200)
            .extract()
            .`as`(EdgeResponseDto::class.java)

        assertThat(foundEdge).isNotNull
        assertThat(foundEdge.id).isEqualTo(edge?.id)
    }

    @Test
    fun shouldThrowNotFoundExceptionWhenRequestedEdgeDoNotExists() {
        given()
            .port(port)
            .accept(JSON)
            .contentType(JSON)
            .`when`()
            .get("/edge/654")
            .then()
            .statusCode(404)
            .body("entityName", equalTo("EDGE"))
            .body("exceptionMessage", equalTo("Edge with id=654 not found!"))
    }

    @Test
    fun shouldDeleteEdgeById() {
        val edge = edgeService?.createEdge(CreateEdgeDto(1, 3))

        given()
            .port(port)
            .accept(JSON)
            .contentType(JSON)
            .`when`()
            .delete("/edge/${edge?.id}")
            .then()
            .statusCode(204)

        val allEdges = edgeService?.getAllEdges()
        assertThat(allEdges).isEmpty()
    }

    @Test
    fun shouldReturnTree() {
        edgeService?.createEdge(CreateEdgeDto(1, 2))
        edgeService?.createEdge(CreateEdgeDto(2, 3))
        edgeService?.createEdge(CreateEdgeDto(3, 4))
        edgeService?.createEdge(CreateEdgeDto(3, 5))

        val tree = given()
            .port(port)
            .accept(JSON)
            .contentType(JSON)
            .`when`()
            .get("/tree/1")
            .then()
            .statusCode(200)
            .extract()
            .`as`(TreeResponse::class.java)


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