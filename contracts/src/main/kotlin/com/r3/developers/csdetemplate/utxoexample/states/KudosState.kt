package com.r3.developers.csdetemplate.utxoexample.states

import com.r3.developers.csdetemplate.utxoexample.contracts.KudosContract
import net.corda.v5.base.types.MemberX500Name
import net.corda.v5.ledger.utxo.BelongsToContract
import net.corda.v5.ledger.utxo.ContractState
import java.security.PublicKey
import java.util.*
import kotlin.collections.LinkedHashMap


//The Chat State represents data stored on ledger.A chat consists of a linear series of messages between two
// participants and is represented by a UUID.A nygivenpairofparticipantscanhavemultiplechats

// We need to verify how many kudos a person has
// Centralize the information
// Global state isnt great
// Thats why this bit should represet 1 kudos
// On the creation a kudos
// Can be spent on the proposal 
// Could update a ledger that they are a token
// Need to consume the previous state
// Have to handle notary failure 
// Send the two kudos at the same time, handle error gracefully
// Business network operator, listed members of particpants
// Add a member that tracks this state




@BelongsToContract(KudosContract::class)
data class KudosState(
    // Unique identifier for the chat.
    // Remove uid
    val id: UUID = UUID.randomUUID(),
    //A map from the user to the amount of kudos that they have
    // Change to public key
    val owner: PublicKey,
    //The participants to the chat,represented by their publickey.
    private val participants: List<PublicKey>): ContractState {

    override fun getParticipants():List<PublicKey>{
        return participants
    }

    // fun isParticipant(who: PublicKey): Boolean {
    //     return participants.contains(who)
    // }
}