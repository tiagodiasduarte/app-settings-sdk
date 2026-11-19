package pt.tiagodiasduarte.appsettings.sdk.presentation.model

import androidx.annotation.StringRes

internal data class SettingsUiState(
    val sections: List<SectionUi> = emptyList(),
)

/** A titled block on the settings screen. Future tools add sections here. */
internal data class SectionUi(
    @StringRes val titleRes: Int,
    val rows: List<InfoRowUi>,
)

internal data class InfoRowUi(
    @StringRes val labelRes: Int,
    val value: UiText,
)

/** Text that is either a literal value or a string resource resolved at render time. */
internal sealed interface UiText {
    data class Plain(val value: String) : UiText
    data class Resource(@StringRes val resId: Int) : UiText
}
