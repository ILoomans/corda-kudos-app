package com.r3.developers.csdetemplate.utxoexample.contracts

import com.r3.developers.csdetemplate.utxoexample.states.KudosState
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
                val output = transaction.outputContractStates.single() as ProposalState
                "Favour should be set to 0" using (output.favour==0)
                "Oppose should be set to 0" using (output.oppose==0)
                "Proposer should be the signer of the contract" using (transaction.signatories.contains(output.proposer))
            }
            // Rules applied only to transactions with the Update Command.
            is ProposalCommand.Reconcile -> {
                // TODO: Add appropriate commands here
                // in this case it is being used as the input
                val input = transaction.inputContractStates.single() as ProposalState
                "Reconciler must be owner of the contract" using transaction.signatories.contains(input.proposer)

                // TODO: Add a correct votes tally

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