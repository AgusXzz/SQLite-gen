package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ---------------------------------------------------------------------------
// Light scheme — indigo primary with a teal accent. Fresh and professional.
// ---------------------------------------------------------------------------
val md_theme_light_primary = Color(0xFF4655CA)
val md_theme_light_onPrimary = Color(0xFFFFFFFF)
val md_theme_light_primaryContainer = Color(0xFFDFE0FF)
val md_theme_light_onPrimaryContainer = Color(0xFF00105C)
val md_theme_light_secondary = Color(0xFF006A6A)
val md_theme_light_onSecondary = Color(0xFFFFFFFF)
val md_theme_light_secondaryContainer = Color(0xFF6FF7F6)
val md_theme_light_onSecondaryContainer = Color(0xFF002020)
val md_theme_light_tertiary = Color(0xFF6B538C)
val md_theme_light_onTertiary = Color(0xFFFFFFFF)
val md_theme_light_tertiaryContainer = Color(0xFFEDDCFF)
val md_theme_light_onTertiaryContainer = Color(0xFF260D43)
val md_theme_light_error = Color(0xFFBA1A1A)
val md_theme_light_errorContainer = Color(0xFFFFDAD6)
val md_theme_light_onError = Color(0xFFFFFFFF)
val md_theme_light_onErrorContainer = Color(0xFF410002)
val md_theme_light_background = Color(0xFFFBF8FF)
val md_theme_light_onBackground = Color(0xFF1B1B21)
val md_theme_light_surface = Color(0xFFFBF8FF)
val md_theme_light_onSurface = Color(0xFF1B1B21)
val md_theme_light_surfaceVariant = Color(0xFFE3E1EC)
val md_theme_light_onSurfaceVariant = Color(0xFF46464F)
val md_theme_light_outline = Color(0xFF777680)
val md_theme_light_inverseOnSurface = Color(0xFFF2EFF7)
val md_theme_light_inverseSurface = Color(0xFF303036)
val md_theme_light_inversePrimary = Color(0xFFBEC2FF)
val md_theme_light_surfaceTint = Color(0xFF4655CA)
val md_theme_light_outlineVariant = Color(0xFFC7C5D0)
val md_theme_light_scrim = Color(0xFF000000)
val md_theme_light_surfaceContainerLowest = Color(0xFFFFFFFF)
val md_theme_light_surfaceContainerLow = Color(0xFFF5F2FB)
val md_theme_light_surfaceContainer = Color(0xFFEFEDF5)
val md_theme_light_surfaceContainerHigh = Color(0xFFE9E7EF)
val md_theme_light_surfaceContainerHighest = Color(0xFFE4E1E9)

// ---------------------------------------------------------------------------
// Dark scheme
// ---------------------------------------------------------------------------
val md_theme_dark_primary = Color(0xFFBEC2FF)
val md_theme_dark_onPrimary = Color(0xFF11227B)
val md_theme_dark_primaryContainer = Color(0xFF2D3DB1)
val md_theme_dark_onPrimaryContainer = Color(0xFFDFE0FF)
val md_theme_dark_secondary = Color(0xFF4CDADA)
val md_theme_dark_onSecondary = Color(0xFF003737)
val md_theme_dark_secondaryContainer = Color(0xFF004F4F)
val md_theme_dark_onSecondaryContainer = Color(0xFF6FF7F6)
val md_theme_dark_tertiary = Color(0xFFD7BAFF)
val md_theme_dark_onTertiary = Color(0xFF3B2459)
val md_theme_dark_tertiaryContainer = Color(0xFF533B71)
val md_theme_dark_onTertiaryContainer = Color(0xFFEDDCFF)
val md_theme_dark_error = Color(0xFFFFB4AB)
val md_theme_dark_errorContainer = Color(0xFF93000A)
val md_theme_dark_onError = Color(0xFF690005)
val md_theme_dark_onErrorContainer = Color(0xFFFFDAD6)
val md_theme_dark_background = Color(0xFF131318)
val md_theme_dark_onBackground = Color(0xFFE4E1E9)
val md_theme_dark_surface = Color(0xFF131318)
val md_theme_dark_onSurface = Color(0xFFE4E1E9)
val md_theme_dark_surfaceVariant = Color(0xFF46464F)
val md_theme_dark_onSurfaceVariant = Color(0xFFC7C5D0)
val md_theme_dark_outline = Color(0xFF918F9A)
val md_theme_dark_inverseOnSurface = Color(0xFF131318)
val md_theme_dark_inverseSurface = Color(0xFFE4E1E9)
val md_theme_dark_inversePrimary = Color(0xFF4655CA)
val md_theme_dark_surfaceTint = Color(0xFFBEC2FF)
val md_theme_dark_outlineVariant = Color(0xFF46464F)
val md_theme_dark_scrim = Color(0xFF000000)
val md_theme_dark_surfaceContainerLowest = Color(0xFF0E0E13)
val md_theme_dark_surfaceContainerLow = Color(0xFF1B1B21)
val md_theme_dark_surfaceContainer = Color(0xFF1F1F25)
val md_theme_dark_surfaceContainerHigh = Color(0xFF2A2930)
val md_theme_dark_surfaceContainerHighest = Color(0xFF35343B)

/**
 * Palette used to syntax-highlight generated code. `plain` is supplied by the
 * caller so it matches the surrounding theme text color.
 */
data class CodeSyntaxColors(
    val keyword: Color,
    val type: Color,
    val string: Color,
    val number: Color,
    val comment: Color,
    val function: Color,
    val plain: Color,
)

/** Editor-style syntax colors that read well on the code surface in both schemes. */
fun codeSyntaxColors(dark: Boolean, plain: Color): CodeSyntaxColors =
    if (dark) CodeSyntaxColors(
        keyword = Color(0xFF569CD6),
        type = Color(0xFF4EC9B0),
        string = Color(0xFFCE9178),
        number = Color(0xFFB5CEA8),
        comment = Color(0xFF7A9F60),
        function = Color(0xFFDCDCAA),
        plain = plain,
    ) else CodeSyntaxColors(
        keyword = Color(0xFF0033B3),
        type = Color(0xFF1A7F8E),
        string = Color(0xFF067D17),
        number = Color(0xFF098658),
        comment = Color(0xFF6A737D),
        function = Color(0xFF795E26),
        plain = plain,
    )

/**
 * Container/content color pair used by chips that label a SQLite data type.
 */
data class TypeColors(val container: Color, val content: Color)

/**
 * Distinct, accessible chip colors per SQLite storage class. Picked to read well
 * in both light and dark schemes (the same hues are used; Material surfaces sit
 * behind them with enough contrast).
 */
fun sqliteTypeColors(type: String, dark: Boolean): TypeColors {
    return when (type.uppercase()) {
        "INTEGER" -> if (dark) TypeColors(Color(0xFF2D3DB1), Color(0xFFDFE0FF))
        else TypeColors(Color(0xFFDFE0FF), Color(0xFF00105C))
        "TEXT" -> if (dark) TypeColors(Color(0xFF004F4F), Color(0xFF6FF7F6))
        else TypeColors(Color(0xFF6FF7F6), Color(0xFF002020))
        "REAL" -> if (dark) TypeColors(Color(0xFF533B71), Color(0xFFEDDCFF))
        else TypeColors(Color(0xFFEDDCFF), Color(0xFF260D43))
        "BLOB" -> if (dark) TypeColors(Color(0xFF4E4458), Color(0xFFF1DCFF))
        else TypeColors(Color(0xFFE8DEF8), Color(0xFF332742))
        "NUMERIC" -> if (dark) TypeColors(Color(0xFF7A5900), Color(0xFFFFDF9E))
        else TypeColors(Color(0xFFFFDF9E), Color(0xFF261A00))
        else -> if (dark) TypeColors(Color(0xFF46464F), Color(0xFFC7C5D0))
        else TypeColors(Color(0xFFE3E1EC), Color(0xFF46464F))
    }
}
