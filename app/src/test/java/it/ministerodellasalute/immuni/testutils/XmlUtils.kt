/*
 * Copyright (C) 2020 Presidenza del Consiglio dei Ministri.
 * Please refer to the AUTHORS file for more information.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package it.ministerodellasalute.immuni.testutils

import java.io.File
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory
import org.w3c.dom.Document
import org.w3c.dom.NodeList
import org.xml.sax.InputSource

fun readXml(filename: String): Document {

    val xmlFile = File(filename)

    val dbFactory = DocumentBuilderFactory.newInstance()
    val dBuilder = dbFactory.newDocumentBuilder()
    val xmlInput = InputSource(StringReader(xmlFile.readText()))

    return dBuilder.parse(xmlInput)
}

fun getStringValueByName(doc: Document, name: String): List<String> {

    val xPath = XPathFactory.newInstance().newXPath()
    val path = "/resources/string[contains(@name, '$name')]"
    val itemsTypeT1 = xPath.evaluate(path, doc, XPathConstants.NODESET) as NodeList

    val itemList: MutableList<String> = ArrayList()
    for (i in 0 until itemsTypeT1.length) {
        itemList.add(itemsTypeT1.item(i).textContent)
    }

    return ArrayList(itemList)
}

fun getStringPluralValueByName(doc: Document, name: String, quantity: String): List<String> {

    val xPath = XPathFactory.newInstance().newXPath()
    val path = "/resources/plurals[contains(@name, '$name')]/item[contains(@quantity, '$quantity')]"
    val itemsTypeT1 = xPath.evaluate(path, doc, XPathConstants.NODESET) as NodeList

    val itemList: MutableList<String> = ArrayList()
    for (i in 0 until itemsTypeT1.length) {
        itemList.add(itemsTypeT1.item(i).textContent)
    }

    return ArrayList(itemList)
}
