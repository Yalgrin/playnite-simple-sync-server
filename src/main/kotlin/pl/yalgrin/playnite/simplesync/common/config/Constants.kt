package pl.yalgrin.playnite.simplesync.common.config

const val CURRENT_API_VERSION = 3

const val GAME = "game"
const val PLATFORM = "platform"
const val PLUGIN = "plugin"

const val ICON = "Icon"
const val COVER_IMAGE = "CoverImage"
const val BACKGROUND_IMAGE = "BackgroundImage"

val ALLOWED_FOLDERS = setOf(PLATFORM, GAME)
val ALLOWED_FILE_NAMES = setOf(ICON, COVER_IMAGE, BACKGROUND_IMAGE)
val ALLOWED_FILE_NAMES_PLUGIN = setOf(ICON, BACKGROUND_IMAGE)