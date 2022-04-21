package org.dau.ide.schema

import org.springframework.beans.factory.annotation.Qualifier
import kotlin.annotation.AnnotationTarget.*

@Qualifier("schemaUI")
@Target(ANNOTATION_CLASS, FUNCTION, CLASS, VALUE_PARAMETER)
annotation class SchemaQualifier
