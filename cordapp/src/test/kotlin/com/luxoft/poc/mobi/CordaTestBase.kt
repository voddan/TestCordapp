package com.luxoft.poc.mobi

import net.corda.core.concurrent.CordaFuture
import net.corda.core.contracts.ContractState
import net.corda.core.flows.FlowLogic
import net.corda.core.internal.FlowStateMachine
import net.corda.core.node.services.Vault.StateStatus
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.utilities.getOrThrow
import net.corda.node.internal.StartedNode
import net.corda.node.services.api.StartedNodeServices
import net.corda.testing.node.internal.InternalMockNetwork
import net.corda.testing.node.internal.InternalMockNetwork.MockNode
import net.corda.testing.node.internal.newContext
import kotlin.test.assertEquals
import kotlin.test.assertTrue


/**
 * [CordaTestBase] is the base class for any test that uses mocked Corda network.
 * */
abstract class CordaTestBase {
    /**
     * The mocked Corda network
     * */
    abstract val net: InternalMockNetwork


    /**
     * Substitutes [StartedNodeServices.startFlow] method to run mocked Corda flows.
     *
     * Usage:
     *
     *     val did = store.services.startFlow(GetDidFlow.Initiator(name)).resultFuture.get()
     */
    protected fun <T> StartedNodeServices.startFlow(logic: FlowLogic<T>): FlowStateMachine<T> {
        val machine = startFlow(logic, newContext()).getOrThrow()

        return object : FlowStateMachine<T> by machine {
            override val resultFuture: CordaFuture<T>
                get() {
                    net.runNetwork()
                    return machine.resultFuture
                }
        }
    }



    data class StatesOfType<T: ContractState>(val consumed: List<T>, val unconsumed: List<T>)

    inline fun <reified T : ContractState> StartedNode<MockNode>.statesOfType(): StatesOfType<T> {
        return database.transaction {
            val unconsumed = services.vaultService.queryBy<T>(
                QueryCriteria.VaultQueryCriteria(status = StateStatus.UNCONSUMED)
            )
            val consumed = services.vaultService.queryBy<T>(
                QueryCriteria.VaultQueryCriteria(status = StateStatus.CONSUMED)
            )

            StatesOfType(
                consumed = consumed.states.map { it.state.data },
                unconsumed = unconsumed.states.map { it.state.data }
            )
        }
    }

    fun <T: ContractState> StatesOfType<T>.singleUnconsumed(): T {
        assertTrue ( consumed.isEmpty() )
        assertEquals(1, unconsumed.size )
        return unconsumed.single()
    }

    fun <T: ContractState> StatesOfType<T>.onlyUnconsumed(): List<T> {
        assertTrue ( consumed.isEmpty() )
        return unconsumed
    }
}