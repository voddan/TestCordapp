package com.luxoft.poc.mobi

import com.luxoft.poc.mobi.data.Step
import net.corda.core.flows.FlowLogic
import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import java.lang.IllegalStateException

object X500Name {
    val MobilityService = CordaX500Name("Mobility Service", "London", "GB")
    val Bank = CordaX500Name("Bank", "London", "GB")
    val CarTP = CordaX500Name("CAR", "London", "GB")
    val TaxiTP = CordaX500Name("TAXI", "London", "GB")
    val PlaneTP = CordaX500Name("PLANE", "London", "GB")

    val transportProviders = listOf(CarTP, TaxiTP, PlaneTP)

    fun TransportProvider(type: Step.TransportType): CordaX500Name {
        return when(type) {
            Step.TransportType.CAR -> CarTP
            Step.TransportType.TAXI -> TaxiTP
            Step.TransportType.PLANE -> PlaneTP
            else -> throw IllegalStateException("Can't find a Transport Provider node for $type")
        }
    }
}

fun FlowLogic<*>.partyOfNotary(): Party {
    return serviceHub.networkMapCache.notaryIdentities.single()
}


fun FlowLogic<*>.partyOf(name: CordaX500Name): Party {
    return serviceHub.identityService.wellKnownPartyFromX500Name(name)
        ?: throw IllegalStateException("Can't find a node for $name")
}

fun FlowLogic<*>.checkMyPartyToBe(name: CordaX500Name): Party {
    check(ourIdentity.name == name) { "Flow ${this.javaClass.simpleName} may only be started by ${name.organisation}" }
    return ourIdentity
}

fun FlowLogic<*>.partyOfTP(step: Step): Party = partyOf(X500Name.TransportProvider(step.transportDetails.type))