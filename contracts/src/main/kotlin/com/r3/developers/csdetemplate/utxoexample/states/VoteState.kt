package com.r3.developers.csdetemplate.utxoexample.states
import com.r3.developers.csdetemplate.utxoexample.contracts.ProposalContract
import net.corda.v5.base.types.MemberX500Name
import net.corda.v5.ledger.utxo.BelongsToContract
import net.corda.v5.ledger.utxo.ContractState
import java.security.PublicKey
import java.util.*
import kotlin.collections.LinkedHashMap


@BelongsToContract(ProposalContract::class)
// need to reference the proposal
data class VoteState(
    // the id of the proposal
    val id : UUID = UUID.randomUUID(),
    // Non-unique name of the proposal
    val proposalId: UUID,
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

    fun voteInFavour(weight: Int): VoteState {
        return copy(favour = favour + weight)
    }

    fun voteInOpposition(weight: Int): VoteState {
        return copy(oppose = oppose + weight)
    }

}