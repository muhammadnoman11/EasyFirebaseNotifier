package com.example.notifiersample

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.example.notifiersample.comp.ReceivedMessageDialog
import com.example.notifiersample.comp.SendMessageDialog
import com.example.notifiersample.ui.theme.NotifierSampleTheme
import com.example.notifiersample.viewmodel.NotificationViewModel
import com.github.muhammadnoman11.easyfirebasenotifier.FCMTopics
import com.github.muhammadnoman11.easyfirebasenotifier.pref.DialogPreferenceManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {


    private lateinit var dialogPrefsManager: DialogPreferenceManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        dialogPrefsManager = DialogPreferenceManager.getInstance(this)


        setContent {
            val viewModel = ViewModelProvider(this)[NotificationViewModel::class.java]

            NotifierSampleTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        viewModel = viewModel,
                        dialogPrefsManager = dialogPrefsManager,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clear dialog data when the activity is destroyed
        dialogPrefsManager.clearDialogData()
    }
}


@Composable
fun MainScreen(
    viewModel: NotificationViewModel,
    modifier: Modifier,
    dialogPrefsManager: DialogPreferenceManager
) {
    var showSendDialog by remember { mutableStateOf(false) }
    var showReceivedDialog by remember { mutableStateOf(false) }
    var selectedTopic by remember { mutableStateOf(FCMTopics.ALL_USERS) }
    var showTokenDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    var token by rememberSaveable { mutableStateOf("") }


    // Load dialog data from preferences
    val dialogData by dialogPrefsManager.dialogDataFlow.collectAsState()

    LaunchedEffect(dialogData) {
        if (dialogData != null) {
            showReceivedDialog = true
        }
    }

    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = { showSendDialog = true },
            modifier = Modifier.fillMaxWidth(0.8f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Send Dialog Message")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {

                viewModel.sendNotificationToAllUsers(
                    "Hello",
                    "Android Engineers!",
                    "https://www.fita.in/wp-content/uploads/2019/10/android.jpg"
                )
            },
            modifier = Modifier.fillMaxWidth(0.8f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Notify All Users")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                showTokenDialog = true
            },
            modifier = Modifier.fillMaxWidth(0.8f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Notify Specific User")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                getFcmToken { _token ->
                    if (_token != null) {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("FCM Token", _token)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Your FCM token copied", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Failed to fetch token", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(0.8f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Copy FCM Token")
        }

    }

    if (showSendDialog) {
        SendMessageDialog(
            onClose = {
                showSendDialog = false
            },
            onSend = { t, m, i ->
                title = t
                message = m
                imageUrl = i
                showSendDialog = false
                viewModel.sendDialogMessage(topic = selectedTopic, title, message, imageUrl)
            }
        )
    }

    if (showReceivedDialog) {
        ReceivedMessageDialog(
            title = dialogData?.title,
            message = dialogData?.body,
            imageUrl = dialogData?.imageUrl,
            onClose = {
                showReceivedDialog = false
                // Clear dialog data when the dialog is closed
                dialogPrefsManager.clearDialogData()
            }

        )
    }

    if (showTokenDialog) {
        AlertDialog(
            onDismissRequest = { showTokenDialog = false },
            title = { Text("Notify Specific User") },
            text = {
                Column {
                    TextField(
                        value = token,
                        onValueChange = { token = it },
                        label = { Text("Enter or Paste Token") },
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Copy the FCM token from the target device or retrieve it from your database.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (token.isNotEmpty()) {
                        viewModel.sendToSpecificUser(
                            token,
                            "Hello",
                            "Android Engineers!",
                            "https://www.fita.in/wp-content/uploads/2019/10/android.jpg"
                        )
                        showTokenDialog = false
                    } else {
                        Toast.makeText(context, "Please enter a valid token", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text("Send")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTokenDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

private fun getFcmToken(onTokenReceived: (String?) -> Unit) {
    FirebaseMessaging.getInstance().token
        .addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.e("FCMService", "Failed to fetch FCM token", task.exception)
                onTokenReceived(null)
                return@addOnCompleteListener
            }

            val token = task.result
            Log.d("FCMService", "FCM token retrieved: $token")
            onTokenReceived(token)
        }
}



@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    NotifierSampleTheme {
        MainScreen(
            viewModel = NotificationViewModel(),
            dialogPrefsManager = DialogPreferenceManager.getInstance(LocalContext.current),
            modifier = Modifier.fillMaxSize(),
        )
    }
}