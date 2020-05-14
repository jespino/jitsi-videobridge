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

import org.jitsi.videobridge.rest.root.colibri.ColibriResource
import org.jitsi.videobridge.util.ClientConnectionProvider
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser

import javax.inject.Inject
import javax.servlet.http.HttpServletResponse
import javax.ws.rs.Path
import javax.ws.rs.POST
import javax.ws.rs.Consumes
import javax.ws.rs.core.Response
import javax.ws.rs.core.MediaType

/**
 * Add or remove XMPP environments to which the bridge will connect
 */
@Path("/colibri/muc-client")
class MucClient : ColibriResource() {
    @Inject protected var clientConnectionProvider: ClientConnectionProvider? = null

    @Path("/add")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    fun addMucClient(requestBody: String): Response {
        // NOTE: unfortunately MucClientConfiguration is not a compliant bean (it doesn't have
        // a no-arg ctor) so we can't parse the json directly into a MucClientConfiguration
        // instance and just take that as an argument here, we have to read the json
        // ourselves.
        val o = JSONParser().parse(requestBody)
        if ((o !is JSONObject)) {
            return Response.status(HttpServletResponse.SC_BAD_REQUEST).build()
        }
        val clientConnection = clientConnectionProvider!!.get()
        if (clientConnection.addMucClient(o)) {
            return Response.ok().build()
        }
        return Response.status(HttpServletResponse.SC_BAD_REQUEST).build()
    }

    @Path("/remove")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    fun removeMucClient(requestBody: String): Response {
        val o = JSONParser().parse(requestBody)
        if ((o !is JSONObject)) {
            return Response.status(HttpServletResponse.SC_BAD_REQUEST).build()
        }
        val clientConnection = clientConnectionProvider!!.get()
        if (clientConnection.removeMucClient(o)) {
            return Response.ok().build()
        }
        return Response.status(HttpServletResponse.SC_BAD_REQUEST).build()
    }
}
