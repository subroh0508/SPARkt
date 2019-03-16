package net.subroh0508.core

data class Prefix(
    private val name: String,
    private val iri: String
) {
    override fun toString() = "PREFIX $name: $iri"
}