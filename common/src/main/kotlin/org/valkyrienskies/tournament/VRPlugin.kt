package org.valkyrienskies.tournament

import net.blf02.forge.VRAPIPlugin
import net.blf02.forge.VRAPIPluginProvider
import net.blf02.vrapi.api.IVRAPI

@VRAPIPlugin
class VRPlugin : VRAPIPluginProvider {
    companion object {
        var vrAPI: IVRAPI? = null
    }

    override fun getVRAPI(ivrapi: IVRAPI) {
        vrAPI = ivrapi
        VRPluginStatus.hasPlugin = true
    }
}