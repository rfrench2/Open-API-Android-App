package com.codingwithmitch.openapi.session

import com.codingwithmitch.openapi.models.AuthToken

data class SessionState(
    val authToken: AuthToken? = null,
    val didCheckForPreviousAuthUser: Boolean = false,
)
