package com.dabi.partypoker.featureClient.model.data

import com.dabi.partypoker.managers.ConnectionStatusEnum
import com.dabi.partypoker.managers.ServerType


data class ClientState(
    var connectionStatus: ConnectionStatusEnum = ConnectionStatusEnum.NONE,

    var serverID: String = "",
    var serverType: ServerType = ServerType.IS_TABLE
)
