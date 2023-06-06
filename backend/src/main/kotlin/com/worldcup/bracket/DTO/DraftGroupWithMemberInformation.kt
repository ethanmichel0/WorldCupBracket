package com.worldcup.bracket.DTO

import com.worldcup.bracket.Entity.DraftGroup
import com.worldcup.bracket.Entity.PlayerDraft
import com.worldcup.bracket.Entity.User

data class DraftGroupWithMemberInformation(
    val draftGroup: DraftGroup,
    val members: List<User>,
    val playerDrafts: List<PlayerDraft>? 
)

// playerDrafts will be null if draft has not yet been scheduled