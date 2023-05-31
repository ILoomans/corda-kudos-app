package com.r3.developers.csdetemplate.governance.workflows

import com.r3.developers.csdetemplate.utxoexample.states.ProposalState
import net.corda.v5.application.flows.*
import net.corda.v5.application.messaging.FlowMessaging
import net.corda.v5.application.messaging.FlowSession
import net.corda.v5.base.annotations.Suspendable
import net.corda.v5.base.exceptions.CordaRuntimeException
import net.corda.v5.base.types.MemberX500Name
import net.corda.v5.ledger.utxo.UtxoLedgerService
import net.corda.v5.ledger.utxo.transaction.UtxoSignedTransaction
import org.slf4j.LoggerFactory
import net.corda.v5.application.membership.MemberLookup


// See Chat CorDapp Design section of the getting started docs for a description of this flow.

// @InitiatingFlow declares the protocol which will be used to link the initiator to the responder.
@InitiatingFlow(protocol = "finalize-proposal-protocol")
class FinalizeProposalSubFlow(private val signedTransaction: UtxoSignedTransaction, private val otherMembers: List<MemberX500Name>): SubFlow<String> {

    private companion object {
        val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }

    // Injects the UtxoLedgerService to enable the flow to make use of the Ledger API.
    @CordaInject
    lateinit var ledgerService: UtxoLedgerService

    @CordaInject
    lateinit var flowMessaging: FlowMessaging

    @Suspendable
    override fun call(): String {

        log.info("FinalizeKudosFlow.call() called")
        val sessions = otherMembers.map {
            flowMessaging.initiateFlow(it)
        }

        return try {
            val finalizedSignedTransaction = ledgerService.finalize(
                signedTransaction,
                sessions
            )
            finalizedSignedTransaction.transaction.id.toString().also {
                log.info("Success! Response: $it")
            }
        }
        // Soft fails the flow and returns the error message without throwing a flow exception.
        catch (e: Exception) {
            log.warn("Finality failed", e)
            "Finality failed, ${e.message}"
        }
    }
}


// Error: Please check CorDapp protocol expectations match on both sides regarding 'send' and 'receive' statements and that sessions are not closed unexpectedly on either side.
// Stack traces around this error originating in CorDapp code may provide a hint."

//

// See Chat CorDapp Design section of the getting started docs for a description of this flow.

//@InitiatingBy declares the protocol which will be used to link the initiator to the responder.
@InitiatedBy(protocol = "finalize-proposal-protocol")
class FinalizeProposalResponderFlow: ResponderFlow {

    private companion object {
        val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }

    // Injects the UtxoLedgerService to enable the flow to make use of the Ledger API.
    @CordaInject
    lateinit var ledgerService: UtxoLedgerService

    @CordaInject
    lateinit var memberLookup: MemberLookup

    @Suspendable
    override fun call(session: FlowSession) {

        log.info("FinalizeChatResponderFlow.call() called")

        try {

//            log(ledgerService.)
            // Calls receiveFinality() function which provides the responder to the finalise() function
            // in the Initiating Flow. Accepts a lambda validator containing the business logic to decide whether
            // responder should sign the Transaction.
            val finalizedSignedTransaction = ledgerService.receiveFinality(session) { ledgerTransaction ->
                // Note, this exception will only be shown in the logs if Corda Logging is set to debug.
                ledgerTransaction.getOutputStates(ProposalState::class.java).singleOrNull() ?:
                throw CordaRuntimeException("Failed verification - transaction did not have exactly one output VoteState.")

                log.info("Verified the transaction- ${ledgerTransaction.id}")
            }
            log.info("Finished responder flow - ${finalizedSignedTransaction.transaction.id}")
        }
        // Soft fails the flow and log the exception.
        catch (e: Exception) {
            log.warn("Exceptionally finished responder flow", e)
        }
    }
}