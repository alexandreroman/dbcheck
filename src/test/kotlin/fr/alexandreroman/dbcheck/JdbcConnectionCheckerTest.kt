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

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito.anyString
import org.mockito.BDDMockito.given
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit4.SpringRunner
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

@RunWith(SpringRunner::class)
class JdbcConnectionCheckerTest {
    @MockBean
    lateinit var connFactory: JdbcConnectionFactory
    @MockBean
    lateinit var conn: Connection
    @MockBean
    lateinit var stmt: PreparedStatement
    @MockBean
    lateinit var rs: ResultSet

    @Test
    fun testCheckConnection() {
        given(connFactory.createConnection()).willReturn(conn)
        val connChecker = JdbcConnectionChecker(connFactory)
        val status = connChecker.checkConnection()
        assertEquals(JdbcConnectionStatus.State.SUCCESS, status.state)
    }

    @Test
    fun testCheckConnectionNoUrl() {
        given(connFactory.createConnection()).willReturn(null)
        val connChecker = JdbcConnectionChecker(connFactory)
        val status = connChecker.checkConnection()
        assertEquals(JdbcConnectionStatus.State.BAD_CONFIG, status.state)
    }

    @Test
    fun testCheckConnectionInvalidUrl() {
        given(connFactory.createConnection()).willThrow(RuntimeException::class.java)
        val connChecker = JdbcConnectionChecker(connFactory)
        val status = connChecker.checkConnection()
        assertEquals(JdbcConnectionStatus.State.ERROR, status.state)
        assertNotNull(status.cause)
    }

    @Test
    fun testCheckConnectionQuery() {
        given(stmt.execute()).willReturn(true)
        given(stmt.resultSet).willReturn(rs)
        given(conn.prepareStatement(anyString())).willReturn(stmt)
        given(rs.next()).willReturn(true)
        given(connFactory.createConnection()).willReturn(conn)
        val connChecker = JdbcConnectionChecker(connFactory)
        connChecker.jdbcQuery = "SELECT * FROM users"

        val status = connChecker.checkConnection()
        assertEquals(JdbcConnectionStatus.State.SUCCESS, status.state)
    }

    @Test
    fun testCheckConnectionQueryError() {
        given(stmt.execute()).willThrow(SQLException::class.java)
        given(conn.prepareStatement(anyString())).willReturn(stmt)
        given(connFactory.createConnection()).willReturn(conn)
        val connChecker = JdbcConnectionChecker(connFactory)
        connChecker.jdbcQuery = "SELECT * FROM foo"

        val status = connChecker.checkConnection()
        assertEquals(JdbcConnectionStatus.State.ERROR, status.state)
    }

    @Test
    fun testCheckConnectionQueryFailed() {
        given(stmt.execute()).willReturn(true)
        given(stmt.resultSet).willReturn(rs)
        given(rs.next()).willReturn(false)
        given(conn.prepareStatement(anyString())).willReturn(stmt)
        given(connFactory.createConnection()).willReturn(conn)
        val connChecker = JdbcConnectionChecker(connFactory)
        connChecker.jdbcQuery = "SELECT * FROM users"

        val status = connChecker.checkConnection()
        assertEquals(JdbcConnectionStatus.State.ERROR, status.state)
        assertNull(status.cause)
    }
}
