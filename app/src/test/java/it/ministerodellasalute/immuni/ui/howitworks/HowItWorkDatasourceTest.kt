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
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.Before
import org.junit.Test

class HowItWorkDatasourceTest {

    @MockK(relaxed = true)
    lateinit var context: Context

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
    }

    @Test
    fun `includes the footer FAQ item`() {
        val dataSource = HowItWorksDataSource(context, true)
        assertTrue { dataSource.data.any { it is HowItWorksItem.Footer } }
    }

    @Test
    fun `does not includes the footer FAQ item`() {
        val dataSource = HowItWorksDataSource(context, false)
        assertFalse { dataSource.data.any { it is HowItWorksItem.Footer } }
    }
}
