package com.cs152.safetycompanion.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cs152.safetycompanion.AppContainer
import com.cs152.safetycompanion.R
import com.cs152.safetycompanion.controller.AppController
import com.cs152.safetycompanion.controller.FamilyController
import com.cs152.safetycompanion.controller.TravelerController
import com.cs152.safetycompanion.model.local.AppMode
import com.cs152.safetycompanion.view.components.BigButton
import com.cs152.safetycompanion.view.components.ScreenColumn
import com.cs152.safetycompanion.view.components.ScreenTitle
import com.cs152.safetycompanion.view.family.FamilyRoot
import com.cs152.safetycompanion.view.traveler.TravelerRoot

/**
 * VIEW (shared): picks Family or Traveler side. Each side has its own navigation file
 * (FamilyRoot.kt / TravelerRoot.kt), so the two teammates rarely edit the same file.
 */
@Composable
fun AppRoot(container: AppContainer) {
    val appController: AppController = viewModel(
        factory = viewModelFactory { initializer { AppController(container.sessionStore) } }
    )
    val mode by appController.mode.collectAsStateWithLifecycle()

    when (mode) {
        null -> RolePickerScreen(onChoose = appController::chooseMode)

        AppMode.FAMILY -> {
            val familyController: FamilyController = viewModel(
                factory = viewModelFactory {
                    initializer { FamilyController(container.authRepository, container.familyTripRepository) }
                }
            )
            FamilyRoot(controller = familyController, onLeaveFamilyMode = appController::resetMode)
        }

        AppMode.TRAVELER -> {
            val travelerController: TravelerController = viewModel(
                factory = viewModelFactory {
                    initializer { TravelerController(container.pairingRepository, container.travelPackageRepository) }
                }
            )
            TravelerRoot(controller = travelerController)
        }
    }
}

/** First launch only: "Who is using this phone?" */
@Composable
fun RolePickerScreen(onChoose: (AppMode) -> Unit) {
    ScreenColumn {
        ScreenTitle(stringResource(R.string.role_title))
        BigButton(text = stringResource(R.string.role_traveler), onClick = { onChoose(AppMode.TRAVELER) })
        BigButton(text = stringResource(R.string.role_family), onClick = { onChoose(AppMode.FAMILY) })
    }
}
