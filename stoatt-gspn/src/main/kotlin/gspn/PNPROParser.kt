/*
 *
 *   Copyright 2021 Budapest University of Technology and Economics
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package gspn

import org.xml.sax.Attributes
import org.xml.sax.InputSource
import org.xml.sax.helpers.DefaultHandler
import java.io.InputStream
import javax.xml.parsers.SAXParserFactory

object PNPROParser {
    fun parse(stream: InputStream) = PetriNet {
        val saxParser = SAXParserFactory.newDefaultInstance().newSAXParser()
        val reader = saxParser.xmlReader

        data class IncompleteArc(val place: String, val transition: String, val weight: Int, val type: String)
        reader.contentHandler = object : DefaultHandler() {
            val transitions = HashMap<String, Transition>()
            val places = HashMap<String, Place>()
            val arcs = arrayListOf<IncompleteArc>()

            override fun startElement(uri: String?, localName: String?, qName: String?, attributes: Attributes?) {
                when (qName) {
                    "place" -> {
                        val name = attributes!!.getValue("name")
                        places.put(name, p(
                                name,
                                attributes.getValue("marking")?.toInt() ?: 0)
                        )
                    }
                    "transition" -> {
                        val name = attributes!!.getValue("name")
                        val type = attributes.getValue("type")
                        val transition =
                                if (type == "IMM") immediate(name, attributes.getValue("priority")?.toInt() ?: 1) {}
                                else timed(name, attributes.getValue("delay")?.toDouble() ?: 1.0) {}
                        transitions.put(name, transition)
                    }
                    "arc" -> {
                        val kind = attributes!!.getValue("kind")
                        val head = attributes.getValue("head")
                        val tail = attributes.getValue("tail")
                        arcs.add(IncompleteArc(
                                if (kind == "OUTPUT") head else tail,
                                if (kind == "OUTPUT") tail else head,
                                attributes.getValue("mult")?.toInt() ?: 1,
                                kind
                        ))
                    }
                }
            }

            override fun endDocument() {
                for (arc in arcs) {
                    val newArc = Arc.ConstantArc(
                            places[arc.place]
                            ?: throw RuntimeException("Error when parsing arc {p: ${arc.place}, t: ${arc.transition}, type: ${arc.type}}: place not found"),
                            arc.weight
                    )
                    val transitionNotFoundText = "Error when parsing arc {p: ${arc.place}, t: ${arc.transition}, type: ${arc.type}}: transition not found"
                    when (arc.type) {
                        "INPUT" -> transitions[arc.transition]?.inputs?.add(newArc) ?: throw RuntimeException(transitionNotFoundText)
                        "OUTPUT" -> transitions[arc.transition]?.outputs?.add(newArc) ?: throw RuntimeException(transitionNotFoundText)
                        "INHIBIT" -> transitions[arc.transition]?.inhibitors?.add(newArc) ?: throw RuntimeException(transitionNotFoundText)
                        else ->
                            throw RuntimeException("Error when parsing arc {p: ${arc.place}, t: ${arc.transition}, type: ${arc.type}}: unknown type")
                    }
                }
            }
        }

        reader.parse(InputSource(stream))
    }
}