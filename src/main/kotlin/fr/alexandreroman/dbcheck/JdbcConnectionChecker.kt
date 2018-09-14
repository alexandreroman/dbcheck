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
import org.springframework.stereotype.Component
import java.sql.Connection
import java.sql.Statement

@Component
class JdbcConnectionChecker(@Autowired val conf: JdbcConfiguration) {
    private val logger = LoggerFactory.getLogger(JdbcConnectionChecker::class.java)

    fun checkConnection(): JdbcConnectionStatus {
        val conn: Connection
        try {
            val nullableConn = conf.openConnection()
            if (nullableConn == null) {
                return JdbcConnectionStatus(
                        state = JdbcConnectionStatus.State.BAD_CONFIG,
                        message = "Missing configuration: please recompile this project with required properties",
                        cause = null
                )
            } else {
                conn = nullableConn
            }
        } catch (e: Throwable) {
            return JdbcConnectionStatus(
                    state = JdbcConnectionStatus.State.ERROR,
                    message = "Cannot open connection to database",
                    cause = e)
        }

        conn.use {
            if (conf.jdbcQuery.isNullOrEmpty()) {
                logger.debug("No JDBC query provided: cannot fully check database access")
            } else {
                logger.debug("Checking database access using query: {}", conf.jdbcQuery)
                val stmt: Statement
                try {
                    stmt = conn.prepareStatement(conf.jdbcQuery)
                } catch (e: Throwable) {
                    return JdbcConnectionStatus(
                            state = JdbcConnectionStatus.State.ERROR,
                            message = "Cannot validate access to database",
                            cause = e)
                }
                stmt.use {
                    if (stmt.execute()) {
                        stmt.resultSet.use {
                            if (!it.next()) {
                                return JdbcConnectionStatus(
                                        state = JdbcConnectionStatus.State.ERROR,
                                        message = "Database access failed: no result was returned",
                                        cause = null
                                )
                            }
                        }
                    }
                }
            }

            return JdbcConnectionStatus(
                    state = JdbcConnectionStatus.State.SUCCESS,
                    message = "Successfully opened connection to database",
                    cause = null
            )
        }
    }
}
