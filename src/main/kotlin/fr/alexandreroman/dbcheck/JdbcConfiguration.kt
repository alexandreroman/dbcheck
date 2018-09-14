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

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import java.sql.Connection
import java.sql.DriverManager

@Configuration
class JdbcConfiguration(
        @Value("\${dbcheck.url}") private val jdbcUrl: String?,
        @Value("\${dbcheck.user}") private val jdbcUser: String?,
        @Value("\${dbcheck.password}") private val jdbcPassword: String?,
        @Value("\${dbcheck.query}") val jdbcQuery: String?) {

    private val logger: Logger = LoggerFactory.getLogger(JdbcConfiguration::class.java)

    fun openConnection(): Connection? {
        if (jdbcUrl.isNullOrEmpty()) {
            logger.error("Missing configuration property: JDBC URL")
            return null
        }

        logger.debug("Opening JDBC connection: {}", jdbcUrl)
        DriverManager.setLoginTimeout(4)
        return DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPassword)
    }
}
