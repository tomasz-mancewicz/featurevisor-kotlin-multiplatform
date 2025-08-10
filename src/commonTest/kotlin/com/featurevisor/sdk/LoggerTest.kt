package com.featurevisor.sdk

import com.featurevisor.sdk.Logger.LogLevel.*
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode.Companion.exactly
import kotlin.test.Test

class LoggerTest {

    private val mockLogMessage = "test message"
    private val mockLogDetails: Map<String, Any> = emptyMap()
    private val mockLogHandler: LogHandler = mock<LogHandler> {
        every { invoke(any(), any(), any()) } returns Unit
    }

    @Test
    fun `log DEBUG message when level DEBUG is set (hierarchical)`() {
        val systemUnderTest = Logger.createLogger(
            level = DEBUG,
            handle = mockLogHandler,
        )

        systemUnderTest.debug(mockLogMessage, mockLogDetails)

        verify(exactly(1)) {
            mockLogHandler.invoke(DEBUG, mockLogMessage, mockLogDetails)
        }
    }

    @Test
    fun `log INFO message when level DEBUG is set (hierarchical)`() {
        val systemUnderTest = Logger.createLogger(
            level = DEBUG, // DEBUG includes INFO
            handle = mockLogHandler,
        )

        systemUnderTest.info(mockLogMessage, mockLogDetails)

        verify(exactly(1)) {
            mockLogHandler.invoke(INFO, mockLogMessage, mockLogDetails)
        }
    }

    @Test
    fun `do not log DEBUG when level INFO is set`() {
        val systemUnderTest = Logger.createLogger(
            level = INFO, // INFO does not include DEBUG
            handle = mockLogHandler,
        )

        systemUnderTest.debug(mockLogMessage, mockLogDetails)

        verify(exactly(0)) {
            mockLogHandler.invoke(DEBUG, any(), any())
        }
    }

    @Test
    fun `log WARN and ERROR when level WARN is set`() {
        val systemUnderTest = Logger.createLogger(
            level = WARN,
            handle = mockLogHandler,
        )

        systemUnderTest.warn(mockLogMessage, mockLogDetails)
        systemUnderTest.error(mockLogMessage, mockLogDetails)

        verify(exactly(1)) {
            mockLogHandler.invoke(WARN, mockLogMessage, mockLogDetails)
        }
        verify(exactly(1)) {
            mockLogHandler.invoke(ERROR, mockLogMessage, mockLogDetails)
        }
    }

    @Test
    fun `do not log anything when level NONE is set`() {
        val systemUnderTest = Logger.createLogger(
            level = NONE,
            handle = mockLogHandler,
        )

        systemUnderTest.debug(mockLogMessage, mockLogDetails)
        systemUnderTest.info(mockLogMessage, mockLogDetails)
        systemUnderTest.warn(mockLogMessage, mockLogDetails)
        systemUnderTest.error(mockLogMessage, mockLogDetails)

        verify(exactly(0)) {
            mockLogHandler.invoke(any(), any(), any())
        }
    }

    @Test
    fun `level can be changed at runtime`() {
        val systemUnderTest = Logger.createLogger(
            level = ERROR,
            handle = mockLogHandler,
        )

        systemUnderTest.info(mockLogMessage, mockLogDetails)

        systemUnderTest.setLevel(DEBUG)

        systemUnderTest.info(mockLogMessage, mockLogDetails)

        verify(exactly(1)) {
            mockLogHandler.invoke(INFO, mockLogMessage, mockLogDetails)
        }
    }

    @Test
    fun `hierarchical levels work correctly`() {
        // Test DEBUG level (should log everything)
        val debugLogger = Logger.createLogger(level = DEBUG, handle = mockLogHandler)

        debugLogger.debug("test", null)
        debugLogger.info("test", null)
        debugLogger.warn("test", null)
        debugLogger.error("test", null)

        verify(exactly(4)) {
            mockLogHandler.invoke(any(), any(), any())
        }
    }

    @Test
    fun `ERROR level only logs errors`() {
        val errorLogger = Logger.createLogger(level = ERROR, handle = mockLogHandler)

        errorLogger.debug("test", null)
        errorLogger.info("test", null)
        errorLogger.warn("test", null)
        errorLogger.error("test", null)

        // Only ERROR should be logged
        verify(exactly(1)) {
            mockLogHandler.invoke(ERROR, "test", null)
        }

        // Others should not be logged
        verify(exactly(0)) {
            mockLogHandler.invoke(DEBUG, any(), any())
            mockLogHandler.invoke(INFO, any(), any())
            mockLogHandler.invoke(WARN, any(), any())
        }
    }
}