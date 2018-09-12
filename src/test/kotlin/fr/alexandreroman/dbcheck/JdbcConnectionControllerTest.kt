/*
 * Copyright 2018 Alexandre Roman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.alexandreroman.dbcheck

import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@RunWith(SpringRunner::class)
@WebMvcTest
class JdbcConnectionControllerTest() {
    @Autowired
    lateinit var mvc: MockMvc
    @MockBean
    lateinit var checker: JdbcConnectionChecker

    @Test
    fun testStatus() {
        given(checker.checkConnection()).willReturn(JdbcConnectionStatus(
                state = JdbcConnectionStatus.State.SUCCESS,
                message = null,
                cause = null
        ))

        val content = """
            { "state": "SUCCESS" }
        """.trimIndent()

        mvc.perform(get("/status"))
                .andExpect(status().isOk)
                .andExpect(content().json(content))
    }

    @Test
    fun testStatusError() {
        given(checker.checkConnection()).willReturn(JdbcConnectionStatus(
                state = JdbcConnectionStatus.State.ERROR,
                message = "Oops",
                cause = null
        ))

        val content = """
            { "state": "ERROR", 'message': 'Oops' }
        """.trimIndent()

        mvc.perform(get("/status"))
                .andExpect(status().isServiceUnavailable)
                .andExpect(content().json(content))
    }
}
