package com.r3.developers.csdetemplate.governance.workflows

import com.r3.developers.csdetemplate.utxoexample.contracts.ProposalContract
import com.r3.developers.csdetemplate.utxoexample.states.ProposalState
import com.r3.developers.csdetemplate.governance.workflows.FinalizeProposalSubFlow
import net.corda.v5.application.flows.*
import net.corda.v5.application.marshalling.JsonMarshallingService
import net.corda.v5.application.membership.MemberLookup


import net.corda.v5.base.annotations.Suspendable
import net.corda.v5.ledger.common.NotaryLookup
import net.corda.v5.ledger.utxo.UtxoLedgerService
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.Instant

data class CreateProposalArgs(val name: String)

class CreateProposalFlow: ClientStartableFlow {

    private companion object {
        val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }

    @CordaInject
    lateinit var jsonMarshallingService: JsonMarshallingService

    @CordaInject
    lateinit var memberLookup: MemberLookup

    // Injects the UtxoLedgerService to enable the flow to make use of the Ledger API.
    @CordaInject
    lateinit var ledgerService: UtxoLedgerService

    @CordaInject
    lateinit var notaryLookup: NotaryLookup

    // FlowEngine service is required to run SubFlows.
    @CordaInject
    lateinit var flowEngine: FlowEngine


    @Suspendable
    override fun call(requestBody: ClientRequestBody): String {

        log.info("CreateProposalFlow.call() called")

        try {
            // Obtain the deserialized input arguments to the flow from the requestBody.
            val flowArgs = requestBody.getRequestBodyAs(jsonMarshallingService, CreateProposalArgs::class.java)
            val myInfo = memberLookup.myInfo()
            // fetching all keys -> Need to look back at this because we dont need to share this with everyone
            val keys = memberLookup.lookup().map {
                it.ledgerKeys.first()
            }
            val proposalState = ProposalState(
                proposalName = flowArgs.name,
                proposer= myInfo.name,
                favour= 0,
                oppose= 0,
                participants = keys
            )
            // Obtain the notary.
            val notary = notaryLookup.notaryServices.single()
            // Use UTXOTransactionBuilder to build up the draft transaction.
            val txBuilder= ledgerService.createTransactionBuilder()
                .setNotary(notary.name)
                .setTimeWindowBetween(Instant.now(), Instant.now().plusMillis(Duration.ofDays(1).toMillis()))
                .addOutputState(proposalState)
                .addCommand(ProposalContract.Create())
            // Only the proposer has to sign off on this, as it is their proposal
                .addSignatories(myInfo.ledgerKeys.first())

            val signedTransaction = txBuilder.toSignedTransaction()

            var names =  memberLookup.lookup().map {
                it.name
            }
            names = names - notary.name
            // temporary
            return flowEngine.subFlow(FinalizeProposalSubFlow(signedTransaction,  listOf(names[0],names[1],names[2], names[3])))

        }
        // Catch any exceptions, log them and rethrow the exception.
        catch (e: Exception) {
            log.warn("Failed to process utxo flow for request body '$requestBody' because:'${e.message}'")
            throw e
        }
    }
}


/*
RequestBody for triggering the flow via REST:
{
    "clientRequestId": "create-1",
    "flowClassName": "com.r3.developers.csdetemplate.utxoexample.workflows.CreateNewChatFlow",
    "requestBody": {
        "chatName":"Chat with Bob",
        "otherMember":"CN=Bob, OU=Test Dept, O=R3, L=London, C=GB",
        "message": "Hello Bob"
        }
}
 */