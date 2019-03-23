package net.subroh0508.sparkt.core.patterns

import net.subroh0508.sparkt.core.QueryItem
import net.subroh0508.sparkt.core.operators.nodes.Node
import net.subroh0508.sparkt.core.triples.TripleItem

abstract class GraphPattern internal constructor(protected val prefix: String) : Pattern, QueryItem {
    protected val patterns: MutableList<Pattern> = mutableListOf()

    infix fun TripleItem.be(pattern: TriplePattern.() -> Unit): GraphPattern {
        patterns.add(TriplePattern(this).apply(pattern))
        return this@GraphPattern
    }

    fun optional(optional: Optional.() -> Unit): GraphPattern {
        patterns.add(Optional().apply(optional))
        return this
    }

    fun filter(filter: Filter.Scope.() -> Node): GraphPattern {
        patterns.add(Filter(filter(Filter.Scope)))
        return this
    }

    override fun toString() = buildString {
        append("$prefix { ")
        append(patterns.joinToString(" "))
        append(" }")
    }
}