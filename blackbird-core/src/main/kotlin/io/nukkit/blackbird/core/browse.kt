package io.nukkit.blackbird.core

class Browser {

    private val adapters = mutableSetOf<Adapter>()

    fun adapt(a: Adapter): Browser {
        adapters.add(a)
        return this
    }

}