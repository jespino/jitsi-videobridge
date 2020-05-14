/*
 * Copyright @ 2018 - present 8x8, Inc.
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

package org.jitsi.videobridge.rest.root.colibri.mucclient

import org.eclipse.jetty.http.*
import org.glassfish.jersey.server.*
import org.glassfish.jersey.test.*
import org.jitsi.videobridge.rest.*
import org.jitsi.videobridge.util.*
import org.jitsi.videobridge.xmpp.*
import org.json.simple.*
import org.junit.*
import org.mockito.*

import javax.ws.rs.client.*
import javax.ws.rs.core.*
import javax.ws.rs.core.Application

import org.junit.Assert.*
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.*

class MucClientTest : JerseyTest() {
    protected var clientConnectionProvider: ClientConnectionProvider? = null
    protected var clientConnection: ClientConnectionImpl? = null
    val BASE_URL = "/colibri/muc-client"

    protected override fun configure(): Application {
        clientConnectionProvider = mock(ClientConnectionProvider::class.java)
        clientConnection = mock(ClientConnectionImpl::class.java)
        `when`(clientConnectionProvider!!.get()).thenReturn(clientConnection)

        enable(TestProperties.LOG_TRAFFIC)
        enable(TestProperties.DUMP_ENTITY)
        return object : ResourceConfig() {
            init {
                register(MockBinder(clientConnectionProvider!!, ClientConnectionProvider::class.java))
                register(MucClient::class.java)
            }
        }
    }

    @Test
    fun testAddMuc() {
        val jsonConfigCaptor = ArgumentCaptor.forClass(JSONObject::class.java)

        `when`(clientConnection?.addMucClient(jsonConfigCaptor.capture())).thenReturn(true)
        val json = JSONObject()
        json.put("id", "id")
        json.put("hostname", "hostname")
        json.put("username", "username")
        json.put("password", "password")
        json.put("muc_jids", "jid1, jid2")
        json.put("muc_nickname", "muc_nickname")

        val resp = target(BASE_URL + "/add").request().post(Entity.json(json.toJSONString()))
        assertEquals(HttpStatus.OK_200, resp.status)
        assertEquals(json, jsonConfigCaptor.value)
    }

    @Test
    fun testAddMucFailure() {
        `when`(clientConnection?.addMucClient(any())).thenReturn(false)
        val json = JSONObject()
        json.put("id", "id")
        json.put("hostname", "hostname")
        json.put("username", "username")
        json.put("password", "password")
        json.put("muc_jids", "jid1, jid2")
        json.put("muc_nickname", "muc_nickname")

        val resp = target(BASE_URL + "/add").request().post(Entity.json(json.toJSONString()))
        assertEquals(HttpStatus.BAD_REQUEST_400, resp.status)
    }

    @Test
    fun testRemoveMuc() {
        val jsonConfigCaptor = ArgumentCaptor.forClass(JSONObject::class.java)

        `when`(clientConnection?.removeMucClient(jsonConfigCaptor.capture())).thenReturn(true)
        val json = JSONObject()
        json.put("id", "id")

        val resp = target(BASE_URL + "/remove").request().post(Entity.json(json.toJSONString()))
        assertEquals(HttpStatus.OK_200, resp.status)
        assertEquals(json, jsonConfigCaptor.value)
    }

    @Test
    fun testRemoveMucFailure() {
        `when`(clientConnection?.removeMucClient(any())).thenReturn(false)
        val json = JSONObject()
        json.put("id", "id")

        val resp = target(BASE_URL + "/remove").request().post(Entity.json(json.toJSONString()))
        assertEquals(HttpStatus.BAD_REQUEST_400, resp.status)
    }
}
