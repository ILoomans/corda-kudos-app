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
    // owner of the vote
    val owner: PublicKey,
    private val participants: List<PublicKey>

): ContractState {
    override fun getParticipants(): List<PublicKey> {
        return participants
    }

}