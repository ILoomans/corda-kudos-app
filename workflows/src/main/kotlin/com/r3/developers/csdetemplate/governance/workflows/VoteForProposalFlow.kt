package com.r3.developers.csdetemplate.governance.workflows

import com.r3.developers.csdetemplate.utxoexample.contracts.*

import com.r3.developers.csdetemplate.utxoexample.states.KudosState
import com.r3.developers.csdetemplate.utxoexample.states.ProposalState
import com.r3.developers.csdetemplate.utxoexample.states.VoteState
import net.corda.v5.application.flows.*
import net.corda.v5.application.marshalling.JsonMarshallingService
import net.corda.v5.application.membership.MemberLookup
import net.corda.v5.base.annotations.Suspendable
import net.corda.v5.base.exceptions.CordaRuntimeException

import net.corda.v5.ledger.common.NotaryLookup
import net.corda.v5.ledger.utxo.UtxoLedgerService
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.Instant
import java.util.*


// A class to hold the deserialized arguments required to start the flow.
data class VoteForProposalArgs(val kudos: List<UUID>, val proposalId: UUID, val favour: Boolean)

// See Chat CorDapp Design section of the getting started docs for a description of this flow.
class VoteForProposalFlow: ClientStartableFlow {

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

        log.info("VoteForProposalFLow.call() called")

        try {
            // Obtain the deserialized input arguments to the flow from the requestBody.
            val flowArgs = requestBody.getRequestBodyAs(jsonMarshallingService, VoteForProposalArgs::class.java)
            val myInfo = memberLookup.myInfo()
            val keys = memberLookup.lookup()
            log.info(keys.toString())
            val states = ledgerService.findUnconsumedStatesByType(KudosState::class.java)
            val inputStates = states.filter {
                val id: UUID = it.state.contractState.id
                flowArgs.kudos.contains(id)
            }.map {
                it.ref
            }
            // need to get the latest unconsumed state of the proposal
            val stateAndRef = ledgerService.findUnconsumedStatesByType(ProposalState::class.java).singleOrNull {
                it.state.contractState.id == flowArgs.proposalId
            } ?: throw CordaRuntimeException("Multiple or zero Proposal states with id ${flowArgs.proposalId} found.")

            val proposer = memberLookup.lookup(stateAndRef.state.contractState.proposer)
                ?: throw CordaRuntimeException("Proposer does not exit")

            val f = if(flowArgs.favour) flowArgs.kudos.size else 0
            val o = if(flowArgs.favour) 0 else flowArgs.kudos.size
            val voteState = VoteState(
                proposalId=flowArgs.proposalId,
                favour= f,
                oppose= o,
                participants = listOf(myInfo.ledgerKeys.first(), proposer.ledgerKeys.first())
            )
            // Obtain the notary.
            val notary = notaryLookup.notaryServices.single()

            // Use UTXOTransactionBuilder to build up the draft transaction.
            val txBuilder= ledgerService.createTransactionBuilder()
                .setNotary(notary.name)
                .setTimeWindowBetween(Instant.now(), Instant.now().plusMillis(Duration.ofDays(1).toMillis()))
                .addOutputState(voteState)
                .addInputStates(inputStates)
                .addCommand(VoteCommand.Vote)
                .addCommand(KudosCommand.Spend)
                .addCommand(ProposalCommand.Vote)
                // only the owner needs to sign
                .addSignatories(voteState.participants)
            val signedTransaction = txBuilder.toSignedTransaction()
            return flowEngine.subFlow(FinalizeVoteSubFlow(signedTransaction,proposer.name))
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