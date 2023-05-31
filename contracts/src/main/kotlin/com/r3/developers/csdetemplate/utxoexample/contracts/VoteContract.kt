package com.r3.developers.csdetemplate.utxoexample.contracts
import net.corda.v5.base.exceptions.CordaRuntimeException
import net.corda.v5.ledger.utxo.Command
import net.corda.v5.ledger.utxo.Contract
import net.corda.v5.ledger.utxo.transaction.UtxoLedgerTransaction


sealed class VoteCommand: Command {
    object Vote : VoteCommand()

    object Spend: VoteCommand()
}
class VoteContract: Contract {


    // verify() function is used to apply contract rules to the transaction.
    override fun verify(transaction: UtxoLedgerTransaction) {
        // Ensures that there is only one command in the transaction
        val command = transaction.commands.filterIsInstance<VoteCommand>().single()

        // Switches case based on the command
        when(command) {
            // Rules applied only to transactions with the Create Command.
            is VoteCommand.Vote -> {
                // TODO: ADD VOTE CONSTRAINTS
            }
            is VoteCommand.Spend -> {
                // TODO: ADD SPEND CONSTRAINTS
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