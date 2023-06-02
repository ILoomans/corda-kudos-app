package com.r3.developers.csdetemplate.governance.workflows

import com.r3.developers.csdetemplate.utxoexample.contracts.KudosContract
import com.r3.developers.csdetemplate.utxoexample.contracts.ProposalCommand
import com.r3.developers.csdetemplate.utxoexample.contracts.VoteCommand

import com.r3.developers.csdetemplate.utxoexample.contracts.ProposalContract
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
data class ReconcileVotesArgs(val id: UUID)

// See Chat CorDapp Design section of the getting started docs for a description of this flow.
class ReconcileVotesFlow: ClientStartableFlow {

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

        log.info("ReconcileVotesFlow.call() called")

        try {
            // Obtain the deserialized input arguments to the flow from the requestBody.
            val flowArgs = requestBody.getRequestBodyAs(jsonMarshallingService, ReconcileVotesArgs::class.java)

            val myInfo = memberLookup.myInfo()

            val keys = memberLookup.lookup()

            log.info(keys.toString())

            val stateAndRef = ledgerService.findUnconsumedStatesByType(VoteState::class.java)
            // We will consume these states
            var favour = 0;
            var oppose = 0;
            val proposalStateAndRef = ledgerService.findUnconsumedStatesByType(ProposalState::class.java).singleOrNull(){
                it.state.contractState.id == flowArgs.id
            }?: throw CordaRuntimeException("Could not find the proposal state")
            val consumableStates = stateAndRef.filter{
                it.state.contractState.proposalId==flowArgs.id
            }.map{
                favour += it.state.contractState.favour
                oppose += it.state.contractState.oppose
                it.ref
            }.plus(proposalStateAndRef.ref)

            println("For ${favour}")
            println("Against: ${oppose}")


//            val newState = proposalStateAndRef.state.contractState.setVotes(favour,oppose)

            val newState = ProposalState(
                id=flowArgs.id,
                proposalName = proposalStateAndRef.state.contractState.proposalName,
                proposer = proposalStateAndRef.state.contractState.proposer,
                favour = favour,
                oppose = oppose,
                participants = proposalStateAndRef.state.contractState.participants
            )
            // Obtain the notary.
            val notary = notaryLookup.notaryServices.single()
            // Use UTXOTransactionBuilder to build up the draft transaction.
            val txBuilder= ledgerService.createTransactionBuilder()
                .setNotary(notary.name)
                .setTimeWindowBetween(Instant.now(), Instant.now().plusMillis(Duration.ofDays(1).toMillis()))
                .addInputStates(consumableStates)
                .addOutputState(newState)
                .addCommand(ProposalCommand.Reconcile)
                .addCommand(VoteCommand.Spend)
                .addSignatories(myInfo.ledgerKeys.first())

            val signedTransaction = txBuilder.toSignedTransaction()

            val names = memberLookup.lookup().filter {
                it.memberProvidedContext["corda.notary.service.name"] != notary.name.toString()
            }.map {
                it.name
            }
            // Only need to send this back to me
            return flowEngine.subFlow(FinalizeVoteReconciliationFlow(signedTransaction, listOf(myInfo.name)))
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