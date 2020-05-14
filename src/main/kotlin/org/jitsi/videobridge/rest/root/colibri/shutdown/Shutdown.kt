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

package org.jitsi.videobridge.rest.root.colibri.shutdown

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonCreator
import org.eclipse.jetty.http.HttpStatus
import org.jitsi.videobridge.rest.annotations.EnabledByConfig
import org.jitsi.videobridge.rest.root.colibri.ColibriResource
import org.jitsi.videobridge.util.VideobridgeProvider
import org.jitsi.xmpp.extensions.colibri.ShutdownIQ
import org.jivesoftware.smack.packet.IQ
import org.jivesoftware.smack.packet.XMPPError
import org.jxmpp.jid.impl.JidCreate

import javax.inject.Inject
import javax.servlet.http.HttpServletRequest
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.Consumes
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Context
import javax.ws.rs.core.Response

/**
 * A resource for shutting down the videobridge via REST.  This
 * must be enabled explicitly via the {@link Constants#ENABLE_REST_SHUTDOWN_PNAME}
 * config value.
 */
@Path("/colibri/shutdown")
@EnabledByConfig(Constants.ENABLE_REST_SHUTDOWN_PNAME)
class Shutdown : ColibriResource() {
    @Inject
    protected var videobridgeProvider: VideobridgeProvider? = null

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    fun shutdown(shutdown: ShutdownJson, @Context request: HttpServletRequest): Response {
        val shutdownIq = shutdown.toIq()

        val ipAddress = request.getHeader("X-FORWARDED-FOR") ?: request.remoteAddr
        shutdownIq.setFrom(JidCreate.from(ipAddress))
        val responseIq = videobridgeProvider!!.get().handleShutdownIQ(shutdownIq)
        if (IQ.Type.result.equals(responseIq.type)) {
            return Response.ok().build()
        }
        val condition = responseIq.error.condition
        if (XMPPError.Condition.not_authorized.equals(condition)) {
            return Response.status(HttpStatus.UNAUTHORIZED_401).build()
        } else if (XMPPError.Condition.service_unavailable.equals(condition)) {
            return Response.status(HttpStatus.SERVICE_UNAVAILABLE_503).build()
        }
        return Response.status(HttpStatus.INTERNAL_SERVER_ERROR_500).build()
    }

    /**
     * A class binding for the shutdown JSON passed to the shutdown request
     * Currently, it only has a single field:
     * {
     *     graceful-shutdown: Boolean [required]
     * }
     */
    companion object {
        class ShutdownJson {
            // Unfortunately, using @JsonProperty here alone is not enough to throw an error
            // if the property is missing from the JSON, but we can't leave it out entirely
            // as it's needed for serialization (since the constructor param JsonProperty
            // is only for the parameter, not the member, so it isn't used when
            // serializing).
            @JsonProperty(value = "graceful-shutdown", required = true)
            private var isGraceful: Boolean = true

            @JsonCreator
            constructor(@JsonProperty(value = "graceful-shutdown", required = true) isGraceful: Boolean) {
                this.isGraceful = isGraceful
            }

            fun toIq(): ShutdownIQ {
                if (isGraceful) {
                    return ShutdownIQ.createGracefulShutdownIQ()
                }
                return ShutdownIQ.createForceShutdownIQ()
            }
        }
    }
}

