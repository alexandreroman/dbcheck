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

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.ModelAndView

@RestController
class JdbcConnectionController(
        @Autowired private val connChecker: JdbcConnectionChecker) {
    private val logger = LoggerFactory.getLogger(JdbcConnectionController::class.java)

    @GetMapping("/status")
    fun checkConnection(): ResponseEntity<JdbcConnectionStatus> {
        val status = doCheckConnection()
        return when (status.state) {
            JdbcConnectionStatus.State.SUCCESS -> ResponseEntity.ok(status)
            else -> ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(status)
        }
    }

    @GetMapping
    fun getStatusPage(): ModelAndView {
        val status = doCheckConnection()
        val model = mapOf("status" to status)
        return ModelAndView("status", model)
    }

    private fun doCheckConnection(): JdbcConnectionStatus {
        logger.info("Checking database connection")
        val status = connChecker.checkConnection()
        when (status.state) {
            JdbcConnectionStatus.State.SUCCESS -> logger.info("Successfully opened connection to database")
            else -> logger.warn("Failed to open connection to database")
        }
        return status
    }
}
