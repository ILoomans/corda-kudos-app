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

        // Ensures that there is only one command in the transaction
        val command = transaction.commands.filterIsInstance<KudosCommand>().single()

        // Applies a universal constraint (applies to all transactions irrespective of command)
//        "The output state should have two and only two participants." using {
//            val output = transaction.outputContractStates.first() as KudosState
//            output.participants.size== 2

//        if(command is)
//        }
        // Switches case based on the command
        when(command) {
            // Rules applied only to transactions with the Create Command.
            is KudosCommand.Issue -> {
//                "When command is Vote there should be one and only one output state." using (transaction.outputContractStates.size == 1)
                "Only two participants for an an issued Kudos" using (transaction.outputContractStates.first().participants.size==2)
//                "Owner should be one of the participants of the Kudos" using (transaction.outputContractStates.first().owner)
            }
            is KudosCommand.Spend -> {
//                "When command is Create there should be no input states." using (transaction.inputContractStates.isEmpty())
//                "When command is Create there should be one and only one output state." using (transaction.outputContractStates.size == 1)
                val input = transaction.inputContractStates.single() as KudosState
                // When changing to public key we need to check they have the right to spend
                // input.owner

                // Check the signatories transaction.signatories
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