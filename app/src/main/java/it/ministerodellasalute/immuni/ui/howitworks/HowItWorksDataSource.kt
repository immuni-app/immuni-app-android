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

package it.ministerodellasalute.immuni.ui.howitworks

import android.content.Context
import it.ministerodellasalute.immuni.R

class HowItWorksDataSource(
    val context: Context,
    val showFaq: Boolean
) {
    val data: List<HowItWorksItem> = mutableListOf(
        HowItWorksItem.Title(context.getString(R.string.permission_tutorial_how_immuni_works_title)),
        HowItWorksItem.Image(R.raw.lottie_hiw_1, R.drawable.how_it_works_1, "2:1.2"),
        HowItWorksItem.ParagraphTitle(context.getString(R.string.how_it_works_tile_1)),
        HowItWorksItem.Paragraph(context.getString(R.string.permission_tutorial_how_immuni_works_first_message)),
        HowItWorksItem.Separator(),
        HowItWorksItem.Image(R.raw.lottie_hiw_2, R.drawable.how_it_works_2, "2:1.4"),
        HowItWorksItem.ParagraphTitle(context.getString(R.string.how_it_works_tile_2)),
        HowItWorksItem.Paragraph(context.getString(R.string.permission_tutorial_how_immuni_works_second_message)),
        HowItWorksItem.Separator(),
        HowItWorksItem.Image(R.raw.lottie_hiw_3, R.drawable.how_it_works_3, "2:1"),
        HowItWorksItem.ParagraphTitle(context.getString(R.string.how_it_works_tile_3)),
        HowItWorksItem.Paragraph(context.getString(R.string.permission_tutorial_how_immuni_works_third_message)),
        HowItWorksItem.Separator(),
        HowItWorksItem.Image(R.raw.lottie_hiw_4, R.drawable.how_it_works_4, "2:0.9"),
        HowItWorksItem.ParagraphTitle(context.getString(R.string.how_it_works_tile_4)),
        HowItWorksItem.Paragraph(context.getString(R.string.permission_tutorial_how_immuni_works_fourth_message)),
        HowItWorksItem.Separator(),
        HowItWorksItem.Image(R.raw.lottie_hiw_5, R.drawable.how_it_works_5, "2:1.2"),
        HowItWorksItem.ParagraphTitle(context.getString(R.string.how_it_works_tile_5)),
        HowItWorksItem.Paragraph(context.getString(R.string.permission_tutorial_how_immuni_works_fifth_message))
    ).apply {
        if (showFaq) add(HowItWorksItem.Footer())
    }
}
