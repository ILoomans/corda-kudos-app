package com.r3.developers.csdetemplate.governance.workflows

import com.r3.developers.csdetemplate.utxoexample.states.KudosState
import net.corda.v5.application.flows.ClientRequestBody
import net.corda.v5.application.flows.ClientStartableFlow
import net.corda.v5.application.flows.CordaInject
import net.corda.v5.application.marshalling.JsonMarshallingService
import net.corda.v5.base.annotations.Suspendable
import net.corda.v5.base.types.MemberX500Name
import net.corda.v5.ledger.utxo.UtxoLedgerService
import org.slf4j.LoggerFactory
import java.security.PublicKey
import java.util.*


// Data class to hold the Flow results.
// The ChatState(s) cannot be returned directly as the JsonMarshallingService can only serialize simple classes
// that the underlying Jackson serializer recognises, hence creating a DTO style object which consists only of Strings
// and a UUID. It is possible to create custom serializers for the JsonMarshallingService, but this beyond the scope
// of this simple example.
data class KudosStateResult(val id: UUID, val owner: String)

// See Chat CorDapp Design section of the getting started docs for a description of this flow.
class ListKudosFlow : ClientStartableFlow {

    private companion object {
        val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }

    @CordaInject
    lateinit var jsonMarshallingService: JsonMarshallingService

    // Injects the UtxoLedgerService to enable the flow to make use of the Ledger API.
    @CordaInject
    lateinit var ledgerService: UtxoLedgerService

    @Suspendable
    override fun call(requestBody: ClientRequestBody): String {

        log.info("ListKudosFlow.call() called")

        // Queries the VNode's vault for unconsumed states and converts the result to a serializable DTO.
        val states = ledgerService.findUnconsumedStatesByType(KudosState::class.java)
        val results = states.map {
            KudosStateResult(
                it.state.contractState.id,
                it.state.contractState.owner.toString())
        }

        // Uses the JsonMarshallingService's format() function to serialize the DTO to Json.
        return jsonMarshallingService.format(results)
    }
}

/*
RequestBody for triggering the flow via REST:
{
    "clientRequestId": "list-1",
    "flowClassName": "com.r3.developers.csdetemplate.utxoexample.workflows.ListChatsFlow",
    "requestBody": {}
}
*/
