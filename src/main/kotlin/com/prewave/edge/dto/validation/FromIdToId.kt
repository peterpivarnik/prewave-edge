package com.prewave.edge.dto.validation

import com.prewave.edge.dto.CreateEdgeDto
import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.annotation.AnnotationTarget.VALUE_PARAMETER
import kotlin.reflect.KClass

/**
 * Annotation to be used for validating fromId and toId.
 */
@Constraint(validatedBy = [FromIdToIdValidator::class])
@Target(VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class FromIdToId(val message: String = "Parameters fromId, and toId must be different!",
                            val groups: Array<KClass<*>> = [],
                            val payload: Array<KClass<out Payload>> = [])

/**
 * Validator to validate fromId and toId.
 */
class FromIdToIdValidator : ConstraintValidator<FromIdToId, CreateEdgeDto> {
    override fun initialize(fromIdToId: FromIdToId) {
    }

    override fun isValid(createEdgeDto: CreateEdgeDto?, cxt: ConstraintValidatorContext): Boolean {
        return createEdgeDto?.fromId != createEdgeDto?.toId
    }
}
