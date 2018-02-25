/*
 * Copyright 2018 stingzhan. https://github.com/stingzhan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package io.zhanjiashu.library.internal

import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler

internal class RegionsXMLParserHandler : DefaultHandler() {

    private val provinces: ArrayList<Province> = arrayListOf()
    val data: MutableMap<String, MutableMap<String, List<String>>> = mutableMapOf()

    private lateinit var tempProvince: Province
    private lateinit var tempCity: City

    private lateinit var tempDistrict: District

    override fun startElement(uri: String, localName: String, qName: String, attributes: Attributes) {
        when (qName) {
            "province" -> {
                tempProvince = Province(
                        attributes.getValue(0),
                        arrayListOf()
                )
            }

            "city" -> {
                tempCity = City(
                        attributes.getValue(0),
                        arrayListOf()
                )
            }

            "district" -> {
                tempDistrict = District(
                        attributes.getValue(0),
                        attributes.getValue(1)
                )
            }
        }
    }

    override fun endElement(uri: String, localName: String, qName: String) {
        when (qName) {
            "district" -> {
                tempCity.districts!!.add(tempDistrict)
            }

            "city" -> {
                tempProvince.cities!!.add(tempCity)
            }

            "province" -> {
                provinces.add(tempProvince)
            }
        }
    }

    override fun endDocument() {
        super.endDocument()

        for (p in provinces) {
            val cities = mutableMapOf<String, List<String>>()
            p.cities?.map { city ->
                cities[city.name] = city.districts?.map { it.name } ?: emptyList()
            }
            data[p.name] = cities
        }
    }
}