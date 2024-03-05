package com.xyron.permissionsinjetpackcompose

import android.content.Intent
import android.os.Bundle
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.xyron.permissionsinjetpackcompose.ui.theme.PermissionsInJetpackComposeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PermissionsInJetpackComposeTheme {
                val viewModel = viewModel<MainViewModel>()
                val dialogQueue = viewModel.visiblePermissionDialogQueue

                /* val cameraPermissionResultLauncher = rememberLauncherForActivityResult(
                     contract = ActivityResultContracts.RequestPermission,
                     onResult = { isGranted ->

                         viewModel.onPermissionResult(
                             permission = Manifest.permission.CAMERA,
                             isGranted = isGranted
                         )
                     }
                 )*/


                val resultCallback: (Boolean) -> Unit = { isGranted ->

                    viewModel.onPermissionResult(
                        permission = Manifest.permission.CAMERA,
                        isGranted = isGranted
                    )
                }

                // Use rememberLauncherForActivityResult with explicit type arguments
                val cameraPermissionResultLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = resultCallback
                )


                val multiplePermissionResultLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions(),
                    onResult = { perms ->

                        perms.keys.forEach { permission ->

                            viewModel.onPermissionResult(
                                permission = permission,
                                isGranted = perms[permission] == true
                            )
                        }

                    }
                )


                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Button(onClick = {
                        cameraPermissionResultLauncher.launch(
                            Manifest.permission.CAMERA
                        )
                    }) {
                        Text(text = "Request one permission")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(onClick = {
                        multiplePermissionResultLauncher.launch(
                            arrayOf(
                                Manifest.permission.RECORD_AUDIO,
                                Manifest.permission.CALL_PHONE
                            )
                        )
                    }) {
                        Text(text = "Request multiple permission")
                    }
                }

                dialogQueue
                    .forEach { permission ->
                        PermissionDialog(
                            permissionTextProvider = when (permission) {
                                Manifest.permission.CAMERA -> {
                                    CameraPermissionTextProvider()
                                }

                                Manifest.permission.RECORD_AUDIO -> {
                                    AudioPermissionTextProvider()
                                }

                                Manifest.permission.CALL_PHONE -> {
                                    PhoneCallPermissionTextProvider()
                                }

                                else -> return@forEach
                            },
                            isPermanentlyDeclined = !shouldShowRequestPermissionRationale(permission),
                            onDismiss = viewModel::dismissDialog,
                            onOkClick = {
                                viewModel.dismissDialog()
                                multiplePermissionResultLauncher.launch(
                                    arrayOf(permission)
                                )
                            },
                            onGoToAppSettingsClick = ::openAppSettings
                        )
                    }

            }
        }
    }
}


fun Activity.openAppSettings() {

    Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null)
    ).also(::startActivity)
}

@Composable
fun Greeting() {

    val context = LocalContext.current
    val cameraPermission = Manifest.permission.CAMERA
    var isCameraPermissionGranted by remember {
        mutableStateOf(checkPermissionFor(context = context, permission = cameraPermission))
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->

            if (isGranted) {
                Log.d("TAG", "Camera $isGranted")
                isCameraPermissionGranted = true
            }

        }
    )

    Column {
        Button(onClick = {
            if (!isCameraPermissionGranted) {
                launcher.launch(cameraPermission)
            }

        }) {
            Text(text = "Camera Permission $isCameraPermissionGranted")
        }
    }
}

private fun checkPermissionFor(context: Context, permission: String): Boolean {
    return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PermissionsInJetpackComposeTheme {
        Greeting()
    }
}