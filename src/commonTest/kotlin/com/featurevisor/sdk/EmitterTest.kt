package com.featurevisor.sdk

import com.featurevisor.types.EventName
import com.featurevisor.types.EventName.ACTIVATION
import com.featurevisor.types.EventName.READY
import com.featurevisor.types.EventName.REFRESH
import com.featurevisor.types.EventName.UPDATE
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode.Companion.atMost
import dev.mokkery.verify.VerifyMode.Companion.exactly
import kotlin.test.Test

class EmitterTest {

    private val readyCallback: Listener = mock<Listener> {
        every { invoke(any()) } returns Unit
    }
    private val refreshCallback: Listener = mock<Listener> {
        every { invoke(any()) } returns Unit
    }
    private val updateCallback: Listener = mock<Listener> {
        every { invoke(any()) } returns Unit
    }
    private val activationCallback: Listener = mock<Listener> {
        every { invoke(any()) } returns Unit
    }

    private val systemUnderTest = Emitter()

    @Test
    fun `add listeners and confirm they are invoked`() {
        systemUnderTest.addListener(READY, readyCallback)
        systemUnderTest.addListener(REFRESH, refreshCallback)
        systemUnderTest.addListener(UPDATE, updateCallback)
        systemUnderTest.addListener(ACTIVATION, activationCallback)

        EventName.entries.forEach {
            systemUnderTest.emit(it)
        }

        verify(exactly(1)) {
            readyCallback.invoke(any())
            refreshCallback.invoke(any())
            updateCallback.invoke(any())
            activationCallback.invoke(any())
        }
    }

    @Test
    fun `removed listener is no longer invoked`() {
        systemUnderTest.addListener(READY, readyCallback)
        systemUnderTest.addListener(REFRESH, refreshCallback)
        systemUnderTest.addListener(UPDATE, updateCallback)

        systemUnderTest.removeListener(REFRESH)
        EventName.entries.forEach {
            systemUnderTest.emit(it)
        }

        verify(atMost(1)) {
            readyCallback.invoke(any())
            updateCallback.invoke(any())
        }
        verify(exactly(0)) {
            refreshCallback.invoke(any())
        }
    }

    @Test
    fun `removeAllListeners works correctly`() {
        systemUnderTest.addListener(READY, readyCallback)
        systemUnderTest.addListener(REFRESH, refreshCallback)
        systemUnderTest.addListener(UPDATE, updateCallback)
        systemUnderTest.addListener(ACTIVATION, activationCallback)

        systemUnderTest.removeAllListeners()
        EventName.entries.forEach {
            systemUnderTest.emit(it)
        }

        verify(exactly(0)) {
            readyCallback.invoke(any())
            refreshCallback.invoke(any())
            updateCallback.invoke(any())
            activationCallback.invoke(any())
        }
    }
}