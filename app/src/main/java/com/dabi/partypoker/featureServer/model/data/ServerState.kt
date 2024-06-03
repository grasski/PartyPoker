package com.dabi.partypoker.featureServer.model.data

import com.dabi.partypoker.managers.ServerStatusEnum
import com.dabi.partypoker.managers.ServerType

data class ServerState(
    var serverStatus: ServerStatusEnum = ServerStatusEnum.OFF,
    var serverType: ServerType = ServerType.IS_TABLE,

    var connectedClients: List<String> = emptyList()    // endpointID = string
)
