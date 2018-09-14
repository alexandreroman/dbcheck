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
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.sql.Connection
import javax.sql.DataSource

@Component
@Profile("cloud")
class CloudJdbcConnectionFactory(private val ds: DataSource) : JdbcConnectionFactory {
    private val logger = LoggerFactory.getLogger(CloudJdbcConnectionFactory::class.java)

    override fun createConnection(): Connection? {
        logger.debug("Opening JDBC connection through Cloud-provided service")
        return ds.connection
    }
}
