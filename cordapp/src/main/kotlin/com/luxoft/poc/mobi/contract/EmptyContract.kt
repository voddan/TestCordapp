package com.luxoft.poc.mobi.contract

import net.corda.core.contracts.Contract
import net.corda.core.transactions.LedgerTransaction

class EmptyContract : Contract {
    override fun verify(tx: LedgerTransaction) {}
}