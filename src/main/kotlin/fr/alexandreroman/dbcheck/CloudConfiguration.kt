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

import org.springframework.cloud.CloudException
import org.springframework.cloud.config.java.AbstractCloudConfig
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.io.PrintWriter
import java.sql.Connection
import java.sql.SQLException
import java.util.logging.Logger
import javax.sql.DataSource

@Configuration
@Profile("cloud")
class CloudConfiguration : AbstractCloudConfig() {
    @Bean
    fun dataSource(): DataSource {
        try {
            return cloud().getSingletonServiceConnector(DataSource::class.java, null)
        } catch (e: CloudException) {
            return NoOpDataSource
        }
    }

    private object NoOpDataSource : DataSource {
        override fun setLogWriter(out: PrintWriter?) {
        }

        override fun setLoginTimeout(seconds: Int) {
        }

        override fun isWrapperFor(iface: Class<*>?): Boolean {
            return false
        }

        override fun <T : Any?> unwrap(iface: Class<T>?): T {
            throw UnsupportedOperationException()
        }

        override fun getConnection(): Connection {
            throw SQLException("No connection available: no service bound")
        }

        override fun getConnection(username: String?, password: String?): Connection {
            return connection
        }

        override fun getParentLogger(): Logger {
            throw UnsupportedOperationException()
        }

        override fun getLogWriter(): PrintWriter {
            throw UnsupportedOperationException()
        }

        override fun getLoginTimeout(): Int {
            throw UnsupportedOperationException()
        }
    }
}
