package com.worldcup.bracket.DTO

import java.beans.ConstructorProperties

data class OverrideGroupSettings @ConstructorProperties("first", "second", "third", "fourth", "groupLetter","overridePass") constructor(
    val first : String,
    val second : String,
    val third : String,
    val fourth : String,
    val groupLetter : String,
    val overridePass : String
)