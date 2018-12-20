package com.luxoft.poc.mobi.flow

import net.corda.core.serialization.CordaSerializable
import net.corda.core.transactions.TransactionBuilder

@CordaSerializable
open class FlowResponse(val items: Collection<Any>) {
    constructor(vararg items: Any) : this(items.asList())
}

fun TransactionBuilder.withResponse(response: FlowResponse): TransactionBuilder
        = withItems(*response.items.toTypedArray())