package com.r3.developers.csdetemplate.utxoexample.states
import com.r3.developers.csdetemplate.utxoexample.contracts.ProposalContract
import net.corda.v5.base.types.MemberX500Name
import net.corda.v5.ledger.utxo.BelongsToContract
import net.corda.v5.ledger.utxo.ContractState
import java.security.PublicKey
import java.util.*
import kotlin.collections.LinkedHashMap


@BelongsToContract(ProposalContract::class)
data class ProposalState(
    // the id of the proposal
    val id : UUID = UUID.randomUUID(),
    // Non-unique name of the proposal
    val proposalName: String,
    // Proposal Maker
    val proposer: MemberX500Name,
    // For
    val favour: Int,
    // Opposed
    val oppose: Int,
    // participants
    private val participants: List<PublicKey>

): ContractState {
    override fun getParticipants(): List<PublicKey> {
        return participants
    }
    fun setVotes(favour: Int, oppose: Int): ProposalState {
        return copy(favour = favour, oppose=oppose)
    }


}