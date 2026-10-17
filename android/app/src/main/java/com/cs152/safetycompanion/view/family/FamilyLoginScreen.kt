package com.cs152.safetycompanion.view.family

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.cs152.safetycompanion.R
import com.cs152.safetycompanion.controller.FamilyUiState
import com.cs152.safetycompanion.view.components.ScreenColumn
import com.cs152.safetycompanion.view.components.ScreenTitle

/** VIEW (Family side): sign in or create an account. Demo login: demo@cs152.dev / demo1234 */
@Composable
fun FamilyLoginScreen(
    state: FamilyUiState,
    onLogin: (email: String, password: String) -> Unit,
    onRegister: (name: String, email: String, password: String) -> Unit,
    onBack: () -> Unit,
) {
    var isRegister by rememberSaveable { mutableStateOf(false) }
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    ScreenColumn {
        ScreenTitle(
            stringResource(if (isRegister) R.string.family_register_title else R.string.family_login_title)
        )

        if (isRegister) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.family_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(R.string.family_email)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(R.string.family_password)) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
        )

        state.message?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        Button(
            onClick = { if (isRegister) onRegister(name, email, password) else onLogin(email, password) },
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(if (isRegister) R.string.family_create_account else R.string.family_sign_in))
        }
        TextButton(onClick = { isRegister = !isRegister }) {
            Text(stringResource(if (isRegister) R.string.family_have_account else R.string.family_need_account))
        }
        TextButton(onClick = onBack) {
            Text(stringResource(R.string.family_switch_mode))
        }
    }
}
