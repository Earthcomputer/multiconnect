package net.earthcomputer.multiconnect.compiler

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

sealed class Either<L, R> {
    data class Left<L, R>(val left: L) : Either<L, R>() {
        override fun toString() = "Either.Left($left)"
    }
    data class Right<L, R>(val right: R) : Either<L, R>() {
        override fun toString() = "Either.Right($right)"
    }

    @OptIn(ExperimentalContracts::class)
    fun leftOrNull(): L? {
        contract {
            returnsNotNull() implies (this@Either is Left<L, R>)
        }
        return (this as? Left<L, R>)?.left
    }

    @OptIn(ExperimentalContracts::class)
    fun left(): L {
        contract {
            returns() implies (this@Either is Left<L, R>)
        }
        return (this as Left<L, R>).left
    }

    @OptIn(ExperimentalContracts::class)
    fun rightOrNull(): R? {
        contract {
            returnsNotNull() implies (this@Either is Right<L, R>)
        }
        return (this as? Right<L, R>)?.right
    }

    @OptIn(ExperimentalContracts::class)
    fun right(): R {
        contract {
            returns() implies (this@Either is Right<L, R>)
        }
        return (this as Right<L, R>).right
    }
}
