package com.hazzabro124.marionetta.registry

interface RegistrySupplier<T> {
    val name: String
    fun get(): T
}