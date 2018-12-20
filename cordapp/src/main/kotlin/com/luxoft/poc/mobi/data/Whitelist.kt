package com.luxoft.poc.mobi.data

import net.corda.core.contracts.ContractState
import net.corda.core.serialization.SerializationWhitelist
import net.corda.core.transactions.TransactionBuilder

class Whitelist : SerializationWhitelist {
    override val whitelist: List<Class<*>> = listOf(
//        Corda classes that need serialization
    )
}