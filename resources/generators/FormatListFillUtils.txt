package com.intellij;

import org.jetbrains.likePrinter.templateBase.template.*

import org.jetbrains.likePrinter.util.base.*
import java.util.ArrayList
import org.jetbrains.format.Format
import org.jetbrains.format.FormatSet
import org.jetbrains.likePrinter.util.box.toBox

/**
 * User: anlun
 */
public trait FormatListFillUtils {
    final public fun fillVariantsToInsertPlaceList<IPT: SmartInsertPlace>(
              insertPlaceMap: Map<String, IPT>
            , variants      : Map<String, FormatSet>
    ): List<Pair<InsertPlace, FormatSet>>? {
        if (insertPlaceMap.size != variants.size) { return null }

        val list = ArrayList<Pair<InsertPlace, FormatSet>>()
        for (place in insertPlaceMap) {
            val placeTag  = place.getKey()
            val placeInfo = place.getValue()

            val placeVariants = variants.get(placeTag)
            if (placeVariants == null) { return null }

            val placeBox = placeInfo.boxToSuit
            val suitableVariants = placeVariants filter { v -> placeBox isSuitable v.toBox() }
            if (suitableVariants.isEmpty()) { return null }

            list.add(Pair(placeInfo.toInsertPlace(), suitableVariants))
        }

        return list
    }

    public fun fillVariantsToInsertPlaceList_SingleFormat<IPT: SmartInsertPlace>(
              insertPlaceMap: Map<String, IPT>
            , variants      : Map<String, Format>
    ): List<Pair<InsertPlace, Format>>? {
        if (insertPlaceMap.size != variants.size) { return null }

        val list = ArrayList<Pair<InsertPlace, Format>>()
        for (place in insertPlaceMap) {
            val placeTag  = place.getKey  ()
            val placeInfo = place.getValue()

            val placeVariant = variants.get(placeTag)
            if (placeVariant == null) { return null }
            list.add(Pair<InsertPlace, Format>(placeInfo.toInsertPlace(), placeVariant))
        }

        return list
    }
}