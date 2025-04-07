package com.prewave.edge.exception

import org.postgresql.util.PSQLException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus.*
import org.springframework.http.HttpStatusCode
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.context.request.WebRequest
import org.springframework.web.method.annotation.HandlerMethodValidationException
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

/**
 * Global exception handler for application exceptions.
 */
@ControllerAdvice
internal class GlobalExceptionHandler : ResponseEntityExceptionHandler() {

    @ResponseStatus(value = NOT_FOUND)
    @ExceptionHandler(EntityNotFoundException::class)
    fun handleEntityNotFoundException(exception: EntityNotFoundException, request: WebRequest): ResponseEntity<Any>? {
        val notFound = NotFoundError(exception.entityName, exception.message)
        return super.handleExceptionInternal(exception, notFound, HttpHeaders(), NOT_FOUND, request)
    }

    private data class NotFoundError(val entityName: String, val exceptionMessage: String)

    @ResponseStatus(value = BAD_REQUEST)
    @ExceptionHandler(PSQLException::class)
    fun handlePSQLException(exception: PSQLException, request: WebRequest): ResponseEntity<Any>? {
        val unique = UniqueError(exception.message,
                                 exception.serverErrorMessage?.constraint,
                                 exception.serverErrorMessage?.detail)
        return super.handleExceptionInternal(exception, unique, HttpHeaders(), BAD_REQUEST, request)
    }

    private data class UniqueError(val message: String?, val constraint: String?, val detail: String?)

    @ResponseStatus(value = CONFLICT)
    @ExceptionHandler(CyclicEdgesException::class)
    fun handleCyclicEdgesException(exception: CyclicEdgesException, request: WebRequest): ResponseEntity<Any>? {
        val cyclic = CyclicError(exception.message,
                                 exception.from,
                                 exception.to)
        return super.handleExceptionInternal(exception, cyclic, HttpHeaders(), CONFLICT, request)
    }

    private data class CyclicError(val message: String, val from: Int, val to: Int)

    override fun handleHandlerMethodValidationException(exception: HandlerMethodValidationException,
                                                        headers: HttpHeaders,
                                                        status: HttpStatusCode,
                                                        request: WebRequest): ResponseEntity<Any>? {
        val errorResponseEntity = super.handleHandlerMethodValidationException(exception, headers, status, request)
        val body: ProblemDetail = errorResponseEntity?.body as ProblemDetail
        exception.parameterValidationResults.stream()
            .map { result -> result.resolvableErrors }
            .map { errors -> errors.toList() }
            .flatMap { list -> list.stream() }
            .map { messageSourceResolvable -> messageSourceResolvable.defaultMessage }
            .forEach { message -> body.setProperty("validationMessage", message) }
        return errorResponseEntity
    }
}