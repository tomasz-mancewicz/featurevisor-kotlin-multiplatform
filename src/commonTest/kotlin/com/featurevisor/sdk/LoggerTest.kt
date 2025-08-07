package com.featurevisor.sdk

import com.featurevisor.sdk.Logger.LogLevel.DEBUG
import com.featurevisor.sdk.Logger.LogLevel.ERROR
import com.featurevisor.sdk.Logger.LogLevel.INFO
import com.featurevisor.sdk.Logger.LogLevel.WARN
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

    private val systemUnderTest: Logger = Logger.createLogger(
        handle = mockLogHandler,
    )

    @Test
    fun `log DEBUG message when level DEBUG is set`() {
        systemUnderTest.setLevels(listOf(DEBUG))

        systemUnderTest.debug(
            message = mockLogMessage,
            details = mockLogDetails,
        )

        verify(exactly(1)) {
            mockLogHandler.invoke(DEBUG, mockLogMessage, mockLogDetails)
        }
    }

    @Test
    fun `log INFO message when level INFO is set`() {
        systemUnderTest.setLevels(listOf(INFO))

        systemUnderTest.info(
            message = mockLogMessage,
            details = mockLogDetails,
        )

        verify(exactly(1)) {
            mockLogHandler.invoke(INFO, mockLogMessage, mockLogDetails)
        }
    }

    @Test
    fun `log WARN message when level WARN is set`() {
        systemUnderTest.setLevels(listOf(WARN))

        systemUnderTest.warn(
            message = mockLogMessage,
            details = mockLogDetails,
        )

        verify(exactly(1)) {
            mockLogHandler.invoke(WARN, mockLogMessage, mockLogDetails)
        }
    }

    @Test
    fun `log ERROR message when level ERROR is set`() {
        systemUnderTest.setLevels(listOf(ERROR))

        systemUnderTest.error(
            message = mockLogMessage,
            details = mockLogDetails,
        )

        verify(exactly(1)) {
            mockLogHandler.invoke(ERROR, mockLogMessage, mockLogDetails)
        }
    }

    @Test
    fun `do not log any message when not set in log levels`() {
        systemUnderTest.setLevels(listOf())

        systemUnderTest.info(
            message = mockLogMessage,
            details = mockLogDetails,
        )
        systemUnderTest.warn(
            message = mockLogMessage,
            details = mockLogDetails,
        )
        systemUnderTest.debug(
            message = mockLogMessage,
            details = mockLogDetails,
        )
        systemUnderTest.error(
            message = mockLogMessage,
            details = mockLogDetails,
        )

        verify(exactly(0)) {
            mockLogHandler.invoke(INFO, any(), any())
            mockLogHandler.invoke(WARN, any(), any())
            mockLogHandler.invoke(DEBUG, any(), any())
            mockLogHandler.invoke(ERROR, any(), any())
        }
    }
}