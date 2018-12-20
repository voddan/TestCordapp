package com.luxoft.poc.mobi.flow

import co.paralleluniverse.fibers.Suspendable
import com.luxoft.poc.mobi.CordaTestBase
import com.luxoft.poc.mobi.X500Name
import com.luxoft.poc.mobi.flow.test.TestBankState
import com.luxoft.poc.mobi.flow.test.TestingFlow
import com.luxoft.poc.mobi.flow.test.TestingFlowSignAllResponder
import net.corda.core.utilities.getOrThrow
import net.corda.node.internal.StartedNode
import net.corda.testing.node.internal.InternalMockNetwork
import net.corda.testing.node.internal.InternalMockNetwork.MockNode
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestingFlowTest : CordaTestBase() {
    override lateinit var net: InternalMockNetwork
    private lateinit var notary: StartedNode<MockNode>

    private lateinit var mobilityService: StartedNode<MockNode>
    private lateinit var bank: StartedNode<MockNode>

    @org.junit.Before
    fun setUp() {
        net = InternalMockNetwork(
            cordappPackages = listOf(
                "com.luxoft.poc.mobi.contract",  // for Contracts
                "com.luxoft.poc.mobi.data.state",  // for Schemas' persistent states
                "com.luxoft.poc.mobi.flow"  // for Contracts
            )
        )

        notary = net.defaultNotaryNode
        mobilityService = net.createPartyNode(X500Name.MobilityService)
        bank = net.createPartyNode(X500Name.Bank)

        bank.registerInitiatedFlow(TestingFlowSignAllResponder::class.java)

    }

    @org.junit.After
    fun tearDown() {
        net.stopNodes()
    }

    @Suspendable
    @Test fun `testing the testing flow 0_0`() {
        mobilityService.services.startFlow(TestingFlow())

        val (consumedMS, unconsumedMS) = mobilityService.statesOfType<TestBankState>()

        assertEquals(0, consumedMS.size)
        assertEquals(1, unconsumedMS.size)

        val (consumedBank, unconsumedBank) = mobilityService.statesOfType<TestBankState>()

        assertEquals(0, consumedBank.size)
        assertEquals(1, unconsumedBank.size)
    }
}