package net.subroh0508.sparkt.vocabraries.schema

import net.subroh0508.sparkt.triples.vocabulary.IriVocabulary

object Schema : IriVocabulary(
    "schema", "name", "memberOf"
) {
    object Prefix : net.subroh0508.sparkt.triples.Prefix {
        override val prefix = "schema"
        override val iri = "<http://schema.org/>"

        override fun toString() = "PREFIX $prefix: $iri"
    }

    val name by store
    val memberOf by store
}
