package com.r3.developers.csdetemplate.utxoexample.contracts
import com.r3.developers.csdetemplate.utxoexample.states.KudosState
import net.corda.v5.base.exceptions.CordaRuntimeException
import net.corda.v5.ledger.utxo.Command
import net.corda.v5.ledger.utxo.Contract
import net.corda.v5.ledger.utxo.transaction.UtxoLedgerTransaction



sealed class KudosCommand : Command {
    object Issue : KudosCommand()
    object Spend : KudosCommand()
}

class KudosContract: Contract {

    // verify() function is used to apply contract rules to the transaction.
    override fun verify(transaction: UtxoLedgerTransaction) {

        val command = transaction.commands.filterIsInstance<KudosCommand>().single()
        when(command) {
            // Rules applied only to transactions with the Create Command.
            is KudosCommand.Issue -> {
                // Only two people should receive the kudos
                "Only two participants for an an issued Kudos" using (transaction.outputContractStates.first().participants.size==2)
                val output = transaction.outputContractStates.single() as KudosState
                // Cannot give award kudos to yourself
                "Cannot give the kudos to yourself" using(!transaction.signatories.contains(output.owner))

                "Owner should be one of the participants of the kudos" using(output.participants.contains(output.owner))
            }
            is KudosCommand.Spend -> {
                val input = transaction.inputContractStates.single() as KudosState
                // Check the signatories transaction.signatories, make sure that the owner of the kudos is a signer
                "Owner of the kudos must be a signatory" using transaction.signatories.contains(input.owner)


            }
            else -> {
                throw CordaRuntimeException("Command not allowed.")
            }
        }
    }

    // Helper function to allow writing constraints in the Corda 4 '"text" using (boolean)' style
    private infix fun String.using(expr: Boolean) {
        if (!expr) throw CordaRuntimeException("Failed requirement: $this")
    }

    // Helper function to allow writing constraints in '"text" using {lambda}' style where the last expression
    // in the lambda is a boolean.
    private infix fun String.using(expr: () -> Boolean) {
        if (!expr.invoke()) throw CordaRuntimeException("Failed requirement: $this")
    }
}