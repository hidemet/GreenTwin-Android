import com.ndumas.appdt.core.ui.UiText
import com.ndumas.appdt.presentation.home.model.DashboardItem

data class HomeUiState(
    val isLoading: Boolean = false,
    val isEditMode: Boolean = false,
    val dashboardItems: List<DashboardItem> = emptyList(),
    val error: UiText? = null,
)
