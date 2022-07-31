package com.r3.examples
/*

import net.corda.v5.application.flows.CordaInject
import net.corda.v5.application.flows.InitiatingFlow
import net.corda.v5.application.flows.RPCRequestData
import net.corda.v5.application.flows.RPCStartableFlow
import net.corda.v5.application.flows.ResponderFlow
import net.corda.v5.application.flows.getRequestBodyAs
import net.corda.v5.application.marshalling.JsonMarshallingService
import net.corda.v5.application.messaging.FlowSession
import net.corda.v5.base.annotations.Suspendable

typealias SomeType = Int
typealias AnotherType = Int

data class InputMessage(
    val thing1: SomeType?,
    val thing2: AnotherType?
    // etc ...
)

typealias Type1 = Int
typealias Type2 = Int
data class OutputMessage(
    val outThing1: Type1,
    val outThing2: Type2
    // etc ...
)

@InitiatingFlow
class DoStuffWithAnotherVNode : RPCStartableFlow {

    @CordaInject
    lateinit var jsonMarshallingService: JsonMarshallingService

    @Suspendable
    override fun call(requestBody: RPCRequestData): String {
        val args = requestBody.getRequestBodyAs<InputMessage>(jsonMarshallingService)
        // n
        val (outThing1, outThing2) = doStuffFn(args.thing1, args.thing2)
        return jsonMarshallingService.format(OutputMessage(outThing1, outThing2))
    }

    fun doStuffFn(v1: SomeType, v2: AnotherType) = (Type1(1), Type2(2))
}

class DoStuffAsTheOtherVNode : ResponderFlow {
    override fun call(session: FlowSession) {
        // TODO
    }
}
 */