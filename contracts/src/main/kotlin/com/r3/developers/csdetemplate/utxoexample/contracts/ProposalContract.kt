package com.r3.developers.csdetemplate.utxoexample.contracts

import com.r3.developers.csdetemplate.utxoexample.states.ProposalState
import net.corda.v5.base.exceptions.CordaRuntimeException
import net.corda.v5.ledger.utxo.Command
import net.corda.v5.ledger.utxo.Contract
import net.corda.v5.ledger.utxo.transaction.UtxoLedgerTransaction
sealed class ProposalCommand : Command {
    object Propose: ProposalCommand()
    object Reconcile: ProposalCommand()

    object Vote: ProposalCommand()
}


class ProposalContract: Contract {




    override fun verify(transaction: UtxoLedgerTransaction) {

        // Ensures that there is only one command in the transaction
        val command = transaction.commands.filterIsInstance<ProposalCommand>().single()

        // Applies a universal constraint (applies to all transactions irrespective of command)
//        "The output state should have two and only two participants." using {
//            val output = transaction.outputContractStates.first() as ProposalState
//            output.participants.size== 2
//        }
        // Switches case based on the command
        when(command) {
            // Rules applied only to transactions with the Create Command.
            is ProposalCommand.Propose -> {
                // TODO: Add appropriate commands here
//                "When command is Create there should be no input states." using (transaction.inputContractStates.isEmpty())
//                "When command is Create there should be one and only one output state." using (transaction.outputContractStates.size == 1)
            }
            // Rules applied only to transactions with the Update Command.
            is ProposalCommand.Reconcile -> {
                // TODO: Add appropriate commands here

            }

            is ProposalCommand.Vote -> {
                //TODO: Add something here

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