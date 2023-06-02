package com.r3.developers.csdetemplate.governance.workflows

import com.r3.developers.csdetemplate.governance.workflows.FinalizeKudosSubFlow
import com.r3.developers.csdetemplate.utxoexample.contracts.KudosCommand
import com.r3.developers.csdetemplate.utxoexample.contracts.KudosContract
import com.r3.developers.csdetemplate.utxoexample.states.KudosState
import net.corda.v5.application.flows.*
import net.corda.v5.application.marshalling.JsonMarshallingService
import net.corda.v5.application.membership.MemberLookup
import net.corda.v5.base.annotations.Suspendable
import net.corda.v5.base.exceptions.CordaRuntimeException
import net.corda.v5.base.types.MemberX500Name
import net.corda.v5.ledger.common.NotaryLookup
import net.corda.v5.ledger.utxo.UtxoLedgerService

import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.Instant

// A class to hold the deserialized arguments required to start the flow.
data class GiveKudosArgs(val reason: String, val recipient: String)

// See Chat CorDapp Design section of the getting started docs for a description of this flow.
class GiveKudosFlow: ClientStartableFlow {

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

        log.info("GiveKudosFLow.call() called")

        try {
            // Obtain the deserialized input arguments to the flow from the requestBody.
            val flowArgs = requestBody.getRequestBodyAs(jsonMarshallingService, GiveKudosArgs::class.java)

            // Get MemberInfos for the Vnode running the flow and the otherMember.
            // Good practice in Kotlin CorDapps is to only throw RuntimeException.
            // Note, in Java CorDapps only unchecked RuntimeExceptions can be thrown not
            // declared checked exceptions as this changes the method signature and breaks override.
            val myInfo = memberLookup.myInfo()
            val otherMember = memberLookup.lookup(MemberX500Name.parse(flowArgs.recipient)) ?:
            throw CordaRuntimeException("MemberLookup can't find otherMember specified in flow arguments.")
            // This is creating the kudos state
            val kudosState = KudosState(
                owner = otherMember.ledgerKeys.first(),
                reason = flowArgs.reason,
                participants = listOf(myInfo.ledgerKeys.first(), otherMember.ledgerKeys.first())
            )
            // Obtain the notary.
            val notary = notaryLookup.notaryServices.single()

            // Use UTXOTransactionBuilder to build up the draft transaction.
            val txBuilder= ledgerService.createTransactionBuilder()
                .setNotary(notary.name)
                .setTimeWindowBetween(Instant.now(), Instant.now().plusMillis(Duration.ofDays(1).toMillis()))
                .addOutputState(kudosState)
                .addCommand(KudosCommand.Issue)
                .addSignatories(listOf(myInfo.ledgerKeys.first()))
            // Convert the transaction builder to a UTXOSignedTransaction. Verifies the content of the
            // UtxoTransactionBuilder and signs the transaction with any required signatories that belong to
            // the current node.
            val signedTransaction = txBuilder.toSignedTransaction()
            // Call FinalizeChatSubFlow which will finalise the transaction.
            // If successful the flow will return a String of the created transaction id,
            // If successful the flow will return a String of the created transaction id,
            // if not successful it will return an error message.
            return flowEngine.subFlow(FinalizeKudosSubFlow(signedTransaction, listOf(otherMember.name)))

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