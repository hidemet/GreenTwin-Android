import com.ndumas.appdt.core.ui.UiText
import com.ndumas.appdt.presentation.home.model.DashboardItem
import com.ndumas.appdt.presentation.home.model.DashboardSectionType

data class HomeUiState(
    val isLoading: Boolean = false,
    val isEditMode: Boolean = false,
    val dashboardItems: List<DashboardItem> = emptyList(),
    val hiddenSections: Set<DashboardSectionType> = emptySet(),
    val error: UiText? = null,
)
