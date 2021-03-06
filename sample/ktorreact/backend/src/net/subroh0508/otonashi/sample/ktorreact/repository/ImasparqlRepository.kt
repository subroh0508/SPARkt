package net.subroh0508.otonashi.sample.ktorreact.repository

import net.subroh0508.otonashi.core.Kotori
import net.subroh0508.otonashi.core.Otonashi
import net.subroh0508.otonashi.core.operators.functions.*
import net.subroh0508.otonashi.core.vocabulary.common.rdfP
import net.subroh0508.otonashi.sample.ktorreact.httpclient.KtorClient
import net.subroh0508.otonashi.sample.ktorreact.model.ImasResult
import net.subroh0508.otonashi.vocabularies.foaf.FoafPrefix
import net.subroh0508.otonashi.vocabularies.foaf.foafP
import net.subroh0508.otonashi.vocabularies.foaf.foafVocabularies
import net.subroh0508.otonashi.vocabularies.imasparql.ImasparqlPrefix
import net.subroh0508.otonashi.vocabularies.imasparql.imasC
import net.subroh0508.otonashi.vocabularies.imasparql.imasP
import net.subroh0508.otonashi.vocabularies.imasparql.imasparqlVocabularies
import net.subroh0508.otonashi.vocabularies.schema.SchemaPrefix
import net.subroh0508.otonashi.vocabularies.schema.schemaP
import net.subroh0508.otonashi.vocabularies.schema.schemaVocabularies

object ImasparqlRepository {
    suspend fun fetch(idolName: String, contents: List<String>, additional: String): List<ImasResult>
        = KtorClient.get(
            buildQuery(idolName, contents, additional).toString(),
            ImasResult::class
        ).results()

    private fun buildQuery(idolName: String, contents: List<String>, additional: String): Kotori = init().where {
        v("s") be {
            rdfP.type to imasC.idol and
                    schemaP.name to v("name") and
                    imasP.title to v("title") and
                    foafP.age to v("age") and
                    imasP.bust to v("bust") and
                    imasP.waist to v("waist") and
                    imasP.hip to v("hip") and
                    imasP.bloodType to v("blood_type") and
                    imasP.handedness to v("handedness") and
                    schemaP.birthDate to v("birth_date") and
                    schemaP.birthPlace to v("birth_place")
        }
        filter {
            regex(v("title"), "(${titles(contents).joinToString("|")})") and
                    contains(v("name"), idolName)
        }
        optional {
            v("s") be { imasP.color to v("color") }
        }
        if (additional == "units") {
            where {
                v("s") be {
                    schemaP.memberOf to v("unit_url")
                }
                v("unit_url") be {
                    rdfP.type to imasC.unit and
                            schemaP.name to v("unit_name")
                }
            }.select {
                + v("s") + (groupConcat(v("unit_name"), separator = ",") `as` v("unit_names"))
            }.groupBy(v("s"))
        }

        if (additional == "clothes") {
            where {
                v("s") be {
                    schemaP.owns to v("clothes_url")
                }
                v("clothes_url") be {
                    rdfP.type to imasC.clothes and
                            schemaP.name to v("clothes_name")
                }
            }.select {
                + v("s") + (groupConcat(v("clothes_name"), separator = ",") `as` v("clothes_names"))
            }.groupBy(v("s"))
        }
    }.select {
        replace(
            str(v("s")),
            """https://sparql.crssnky.xyz/imasrdf/RDFs/detail/""",
            ""
        ) `as` v("id")
        concat("B", str(v("bust")), " W", str(v("waist")), " H", str(v("hip"))) `as` v("three_size")
        concat("#", str(v("color"))) `as` v("color_hex")
        str(v("age")) `as` v("age_str")

        (+ v("id") + v("name") +
                v("age_str") + v("color_hex") + v("blood_type") + v("handedness") +
                v("birth_date") + v("birth_place") + v("three_size") +
                when (additional) {
                    "units" -> v("unit_names", true)
                    "clothes" -> v("clothes_names", true)
                    else -> null
                }).filterNotNull()
    }

    private fun init() = Otonashi.Study {
        destination("https://sparql.crssnky.xyz/spql/imas/query")
        reminds(SchemaPrefix.SCHEMA, FoafPrefix.FOAF, ImasparqlPrefix.IMAS)
        buildsUp(*schemaVocabularies, *foafVocabularies, *imasparqlVocabularies)
    }

    private fun titles(contents: List<String>): List<String> = contents.let {
        val etcetera = mutableListOf<String>()

        if (contents.contains("staff")) {
            etcetera.addAll(listOf("765Staff", "876Staff", "961Staff", "315Staff", "283Staff", "CinderellaGirlsStaff"))
        }

        if (contents.contains("others")) {
            etcetera.addAll(listOf("DearlyStars", "961ProIdols"))
        }

        it + etcetera
    }

}