package com.luxoft.poc.mobi.flow.test

import co.paralleluniverse.fibers.Suspendable
import com.luxoft.poc.mobi.X500Name
import com.luxoft.poc.mobi.checkMyPartyToBe
import com.luxoft.poc.mobi.contract.EmptyContract
import com.luxoft.poc.mobi.partyOf
import com.luxoft.poc.mobi.partyOfNotary
import net.corda.core.contracts.*
import net.corda.core.flows.*
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder


@StartableByRPC
class TestingFlow() : FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        checkMyPartyToBe(X500Name.MobilityService)

        val bank = partyOf(X500Name.Bank)
        val newBankState = TestBankState(bank, ourIdentity)

        val transaction = TransactionBuilder(partyOfNotary())
            .addOutputState(newBankState.asTransactionState())
            .addCommand(NewTestBankState(bank, ourIdentity).asCommand())

        val signedByUs = serviceHub.signInitialTransaction(transaction)

        val signed = subFlow(SignAll(signedByUs, bank))

        val sxt = subFlow(FinalityFlow(signed))
        waitForLedgerCommit(sxt.id)
    }

    fun TestBankState.asTransactionState()
        = TransactionState(this, EmptyContract::class.java.canonicalName!!, partyOfNotary())
}



data class TestBankState(
    val bank: Party,
    val us: Party,
    override val linearId: UniqueIdentifier = UniqueIdentifier()
): LinearState {
    override val participants: List<AbstractParty> = listOf(bank, us)
}

class NewTestBankState(val bank: Party, val us: Party): CommandData {
    fun asCommand(): Command<NewTestBankState> = Command(this, signers = listOf(bank.owningKey, us.owningKey))
}



@InitiatingFlow
class SignAll(
    val transaction: SignedTransaction,
    val bank: Party
) : FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {
        val session = initiateFlow(bank)

        val signed = subFlow(CollectSignaturesFlow(transaction, listOf(session), listOf(ourIdentity.owningKey)))

        return signed
    }
}

@InitiatedBy(SignAll::class)
class TestingFlowSignAllResponder(val flowSession: FlowSession) : FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        val signFlow = object : SignTransactionFlow(flowSession) {
            @Suspendable override fun checkTransaction(stx: SignedTransaction) {
                // nothing
            }
        }

        val stx = subFlow(signFlow)
        waitForLedgerCommit(stx.id)
    }
}