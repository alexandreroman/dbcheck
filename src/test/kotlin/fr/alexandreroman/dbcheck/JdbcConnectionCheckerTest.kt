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

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class JdbcConnectionCheckerTest {
    companion object {
        private val DB_COUNT = AtomicInteger(0)
    }

    private fun newDatabaseName(): String = "JdbcConnectionCheckerTest" + DB_COUNT.getAndIncrement()

    @Test
    fun testCheckConnection() {
        val connChecker = JdbcConnectionChecker(JdbcConfiguration(
                jdbcUrl = "jdbc:hsqldb:mem:${newDatabaseName()}",
                jdbcUser = "SA",
                jdbcPassword = null,
                jdbcQuery = null
        ))
        val status = connChecker.checkConnection()
        assertEquals(JdbcConnectionStatus.State.SUCCESS, status.state)
    }

    @Test
    fun testCheckConnectionNoUrl() {
        val connChecker = JdbcConnectionChecker(JdbcConfiguration(
                jdbcUrl = null,
                jdbcUser = "SA",
                jdbcPassword = null,
                jdbcQuery = null
        ))
        val status = connChecker.checkConnection()
        assertEquals(JdbcConnectionStatus.State.BAD_CONFIG, status.state)
    }

    @Test
    fun testCheckConnectionInvalidUrl() {
        val connChecker = JdbcConnectionChecker(JdbcConfiguration(
                jdbcUrl = "dummy",
                jdbcUser = "SA",
                jdbcPassword = null,
                jdbcQuery = null
        ))
        val status = connChecker.checkConnection()
        assertEquals(JdbcConnectionStatus.State.ERROR, status.state)
        assertNotNull(status.cause)
    }

    @Test
    fun testCheckConnectionQuery() {
        val connChecker = JdbcConnectionChecker(JdbcConfiguration(
                jdbcUrl = "jdbc:hsqldb:mem:${newDatabaseName()}",
                jdbcUser = "SA",
                jdbcPassword = null,
                jdbcQuery = "SELECT COUNT(*) FROM person"
        ))

        connChecker.conf.openConnection()?.use {
            it.prepareStatement("CREATE TABLE person (id IDENTITY PRIMARY KEY)").use {
                it.execute()
            }
        }

        val status = connChecker.checkConnection()
        assertEquals(JdbcConnectionStatus.State.SUCCESS, status.state)
    }

    @Test
    fun testCheckConnectionQueryError() {
        val connChecker = JdbcConnectionChecker(JdbcConfiguration(
                jdbcUrl = "jdbc:hsqldb:mem:${newDatabaseName()}",
                jdbcUser = "SA",
                jdbcPassword = null,
                jdbcQuery = "SELECT foo FROM person"
        ))

        connChecker.conf.openConnection()?.use {
            it.prepareStatement("CREATE TABLE person (id IDENTITY PRIMARY KEY)").use {
                it.execute()
            }
        }

        val status = connChecker.checkConnection()
        assertEquals(JdbcConnectionStatus.State.ERROR, status.state)
    }

    @Test
    fun testCheckConnectionQueryFailed() {
        val connChecker = JdbcConnectionChecker(JdbcConfiguration(
                jdbcUrl = "jdbc:hsqldb:mem:${newDatabaseName()}",
                jdbcUser = "SA",
                jdbcPassword = null,
                jdbcQuery = "SELECT COUNT(*) FROM person"
        ))

        val status = connChecker.checkConnection()
        assertEquals(JdbcConnectionStatus.State.ERROR, status.state)
        assertNotNull(status.cause)
    }

    @Test
    fun testCheckConnectionQueryUpdate() {
        val connChecker = JdbcConnectionChecker(JdbcConfiguration(
                jdbcUrl = "jdbc:hsqldb:mem:${newDatabaseName()}",
                jdbcUser = "SA",
                jdbcPassword = null,
                jdbcQuery = "INSERT INTO check VALUES(NOW())"
        ))

        connChecker.conf.openConnection()?.use {
            it.prepareStatement("CREATE TABLE check (checkdate DATETIME NOT NULL)").use {
                it.execute()
            }
        }

        val status = connChecker.checkConnection()
        assertEquals(JdbcConnectionStatus.State.SUCCESS, status.state)
    }
}
