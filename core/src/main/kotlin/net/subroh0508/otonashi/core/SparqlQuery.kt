package net.subroh0508.otonashi.core

import net.subroh0508.otonashi.core.aggregates.GroupBy
import net.subroh0508.otonashi.core.aggregates.Having
import net.subroh0508.otonashi.core.clauses.Clause
import net.subroh0508.otonashi.core.clauses.Select
import net.subroh0508.otonashi.core.operators.nodes.Node
import net.subroh0508.otonashi.core.patterns.Where
import net.subroh0508.otonashi.core.sequences.Limit
import net.subroh0508.otonashi.core.sequences.Offset
import net.subroh0508.otonashi.core.sequences.OrderBy
import net.subroh0508.otonashi.core.vocabulary.Vocabulary
import net.subroh0508.otonashi.triples.Prefix
import net.subroh0508.otonashi.triples.Var
import net.subroh0508.otonashi.triples.vocabulary.IriVocabulary
import java.net.URLDecoder
import java.net.URLEncoder

class SparqlQuery internal constructor(
    private val endpoint: String,
    private val prefixes: List<Prefix>,
    private val vocabulary: Vocabulary
) {
    companion object Builder {
        private lateinit var endpoint: String
        private var prefixes: List<Prefix> = emptyList()
        private var vocabulary: Vocabulary = Vocabulary()

        operator fun invoke(scope: SparqlQuery.Builder.() -> Unit): SparqlQuery {
            val builder = apply(scope)

            return SparqlQuery(builder.endpoint, builder.prefixes, builder.vocabulary)
        }

        fun endpoint(endpoint: String) {
            this.endpoint = endpoint
        }

        fun prefixes(vararg prefixes: Prefix) {
            this.prefixes = prefixes.toList()
        }

        fun install(vararg vocabularies: IriVocabulary) {
            this.vocabulary = Vocabulary(*vocabularies)
        }
    }

    private var select: Select = Select(vocabulary, Var("*"))
    private val where: Where by lazy { Where(vocabulary) }
    private var groupBy: GroupBy? = null
    private var having: Having? = null
    private var orderBy: OrderBy? = null
    private var limit: Limit? = null
    private var offset: Offset? = null

    fun where(scope: Where.() -> Unit): SparqlQuery {
        where.apply(scope)
        return this
    }

    fun select(vararg vars: Var): SparqlQuery {
        select = Select(vocabulary, *vars)
        return this
    }

    fun select(scope: Clause.Scope.() -> List<Any>): SparqlQuery {
        select = Select(vocabulary, scope(Clause.Scope(vocabulary)))
        return this
    }

    fun groupBy(vararg vars: Var): SparqlQuery {
        groupBy = GroupBy(vocabulary, *vars)
        return this
    }

    fun groupBy(scope: Clause.Scope.() -> List<Any>): SparqlQuery {
        groupBy = GroupBy(vocabulary, scope(Clause.Scope(vocabulary)))
        return this
    }

    fun having(scope: Clause.Scope.() -> Node): SparqlQuery {
        having = Having(vocabulary, scope(Clause.Scope(vocabulary)))
        return this
    }

    fun orderBy(vararg vars: Var): SparqlQuery {
        orderBy = OrderBy(*vars)
        return this
    }

    fun limit(n: Int): SparqlQuery {
        limit = Limit(n)
        return this
    }

    fun offset(n: Int): SparqlQuery {
        offset = Offset(n)
        return this
    }

    override fun toString() = "$endpoint?force-accept=text%2Fplain&query=${buildQuery()}"

    fun urlDecode(): String = URLDecoder.decode(toString(), "UTF-8")

    private fun buildQuery(): String {
        val query = buildString {
            append(prefixes.joinToString(" "))
            append(
                listOfNotNull(
                    select, where,
                    groupBy, having,
                    orderBy, limit, offset
                ).joinToString(" ")
            )
        }

        return URLEncoder.encode(query, "UTF-8")
    }
}