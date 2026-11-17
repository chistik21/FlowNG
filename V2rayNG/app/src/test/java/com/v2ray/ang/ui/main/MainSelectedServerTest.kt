package com.v2ray.ang.ui.main

import com.v2ray.ang.dto.entities.ServersCache
import com.v2ray.ang.dto.entities.ProfileItem
import com.v2ray.ang.enums.EConfigType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class MainSelectedServerTest {
    @Test
    fun `initial and empty selections do not publish a server`() {
        val initial = MainUiState()
        assertNull(initial.selectedServer)
        assertEquals(initial, initial.withSelectedServer("", null))
    }

    @Test
    fun `latest matching GUID publishes a server or clears a missing profile`() {
        val state = MainUiState(selectedGuid = "current")
        val server = ServersCache(guid = "current", profile = ProfileItem(configType = EConfigType.VMESS))
        assertEquals(server, state.withSelectedServer("current", server).selectedServer)
        assertNull(state.withSelectedServer("current", null).selectedServer)
    }

    @Test
    fun `late result from an older selection cannot replace the current one`() {
        val state = MainUiState(selectedGuid = "current")
        assertEquals(state, state.withSelectedServer("previous", ServersCache(
            guid = "previous", profile = ProfileItem(configType = EConfigType.VMESS)
        )))
    }
}
