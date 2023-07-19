package com.worldcup.bracket.DTO

import com.worldcup.bracket.Entity.PlayerSeason
import com.worldcup.bracket.Entity.PlayerDraft
import com.worldcup.bracket.Entity.DraftGroup

data class DraftGroupInfoDuringDraft(
    val playerSelected: PlayerSeason? = null,
    val draftGroup: DraftGroup? = null,
    val playerDraft: PlayerDraft? = null,
    val error: String? = null
)