package com.worldcup.bracket.DTO

import java.beans.ConstructorProperties

data class OverrideGroupSettings @ConstructorProperties("first", "second", "groupLetter","overridePass") constructor(
    val first : String,
    val second : String,
    val groupLetter : String,
    val overridePass : String
)