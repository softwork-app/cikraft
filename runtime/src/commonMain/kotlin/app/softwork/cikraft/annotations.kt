package app.softwork.cikraft

import kotlinx.serialization.StringFormat
import kotlin.reflect.KClass

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION)
public annotation class ScriptEntry

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER)
public annotation class Body(val first: KClass<out StringFormat>, vararg val other: KClass<out StringFormat>)

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS)
public annotation class ContentType(val value: String, val parameters: Array<String> = [])

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER)
public annotation class Property(val name: String = "")

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.VALUE_PARAMETER)
public annotation class Parameter(val name: String = "")

/**
 * Injected header value of the header with the given [name].
 * See [SAP Cloud Integration header](https://help.sap.com/docs/cloud-integration/sap-cloud-integration/headers-and-exchange-properties-provided-by-integration-framework?locale=en-US&q=headers)
 * for predefined headers.
 * Custom headers are also possible, but could be null if the header is not present in the request.
 */
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER)
public annotation class Header(val name: String = "")

// Not @StatusCode due to https://youtrack.jetbrains.com/issue/KT-57012
public const val STATUS_CODE_HEADER: String = "CamelHttpResponseCode"

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.PROPERTY)
public annotation class DynamicHeaders

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.VALUE_PARAMETER)
public annotation class Password

/**
 * SAP CI header which is always part of every HTTP response.
 */
public const val SAP_MESSAGE_PROCESSING_LOG_ID_HEADER: String = "sap_messageprocessinglogid"

/**
 * Set the current log level for this processing.
 * Allowed values: INFO, NONE, DEBUG, ERROR
 */
public const val SAP_MESSAGE_PROCESSING_LOG_LEVEL: String = "SAP_MessageProcessingLogLevel"
