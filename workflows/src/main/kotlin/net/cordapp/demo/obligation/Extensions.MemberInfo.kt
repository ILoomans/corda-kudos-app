package net.cordapp.demo.obligation

import net.corda.v5.application.membership.MemberLookup
import net.corda.v5.base.types.MemberX500Name
import net.corda.v5.ledger.common.Party

fun getNotaryParty(memberLookup: MemberLookup, notaryService: MemberX500Name, index: Int = 0): Party {
    val notaryKey = memberLookup.lookup().first {
        it.memberProvidedContext["corda.notary.service.name"] == notaryService.toString()
    }.ledgerKeys[index]
    return Party(notaryService, notaryKey)
}