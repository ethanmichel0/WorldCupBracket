package com.worldcup.bracket.DTO

import com.worldcup.bracket.Entity.DraftGroup
import com.worldcup.bracket.Entity.PlayerDraft
import com.worldcup.bracket.Entity.User

data class DraftGroupWithMemberInformation(
    val draftGroup: DraftGroup? = null,
    val members: List<User>? = null,
    val playerDrafts: List<PlayerDraft>? = null,
    val error: String? = null
)

// playerDrafts will be null if draft has not yet been scheduled