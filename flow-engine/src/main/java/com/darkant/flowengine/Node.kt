package com.darkant.flowengine

abstract class Node<I, O>(
    private val engine: Engine
) {

    abstract suspend fun start(input: I): O

    abstract suspend fun stop()
}