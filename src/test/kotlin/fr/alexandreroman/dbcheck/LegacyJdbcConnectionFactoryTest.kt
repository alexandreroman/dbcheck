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

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.sql.SQLTransientConnectionException
import java.util.concurrent.atomic.AtomicInteger

class LegacyJdbcConnectionFactoryTest {
    companion object {
        private val DB_COUNT = AtomicInteger(0)
    }

    private fun newDatabaseName(): String = "LegacyJdbcConnectionFactoryTest" + DB_COUNT.getAndIncrement()

    @Test
    fun testCheckConnection() {
        val conf = LegacyConfiguration(
                url = "jdbc:hsqldb:mem:${newDatabaseName()}",
                user = "SA",
                password = null,
                query = null
        )
        LegacyJdbcConnectionFactory(conf).createConnection().use {
            assertNotNull("Connection null", it)
        }
    }

    @Test
    fun testCheckConnectionMissingUrl() {
        val conf = LegacyConfiguration(
                url = null,
                user = "SA",
                password = null,
                query = null
        )
        assertNull(LegacyJdbcConnectionFactory(conf).createConnection())
    }

    @Test(expected = SQLTransientConnectionException::class, timeout = 5000)
    fun testConnectionTimeout() {
        val conf = LegacyConfiguration(
                url = "jdbc:hsqldb:hsql://somehost.com",
                user = "SA",
                password = null,
                query = null
        )
        LegacyJdbcConnectionFactory(conf).createConnection()
    }
}
