package com.prewave.edge.dto

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import org.springframework.stereotype.Component
import kotlin.annotation.AnnotationTarget.VALUE_PARAMETER
import kotlin.reflect.KClass

@Constraint(validatedBy = [FromIdToIdValidator::class])
@Target(VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class FromIdToId(val message: String = "Parameters fromId, and toId must be different!",
                            val groups: Array<KClass<*>> = [],
                            val payload: Array<KClass<out Payload>> = [])

@Component
class FromIdToIdValidator : ConstraintValidator<FromIdToId, CreateEdgeDto> {
    override fun initialize(fromIdToId: FromIdToId) {
    }

    override fun isValid(createEdgeDto: CreateEdgeDto?, cxt: ConstraintValidatorContext): Boolean {
        return createEdgeDto?.fromId != createEdgeDto?.toId
    }
}
