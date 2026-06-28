package com.nutrisafe.app.ui.screens

import com.nutrisafe.app.ui.components.TinyIcons

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nutrisafe.app.data.model.ChatMessageEntity
import com.nutrisafe.app.data.repository.ExpertChatRepository
import com.nutrisafe.app.ui.components.GradientButton
import com.nutrisafe.app.ui.components.brandGradient
import com.nutrisafe.app.ui.theme.BrandMagenta
import com.nutrisafe.app.ui.theme.Ink
import com.nutrisafe.app.ui.theme.Line
import com.nutrisafe.app.ui.theme.Muted
import com.nutrisafe.app.viewmodel.NutriSafeViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun ExpertChatScreen(viewModel: NutriSafeViewModel, padding: PaddingValues) {
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    val messages by viewModel.chatMessages.collectAsStateWithLifecycle()

    if (preferences.dietitianName.isBlank()) {
        ExpertSetup(viewModel, padding)
    } else {
        ExpertChatRoom(viewModel, padding, preferences.dietitianName, messages)
    }
}

@Composable
private fun ExpertSetup(viewModel: NutriSafeViewModel, padding: PaddingValues) {
    var name by rememberSaveable { mutableStateOf("") }
    var contact by rememberSaveable { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(horizontal = 20.dp, vertical = 56.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(28.dp))
        Icon(TinyIcons.PersonAdd, null, tint = BrandMagenta, modifier = Modifier.size(54.dp))
        Spacer(Modifier.height(40.dp))
        Text("Private Consultation", color = Ink, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
        Text(
            "Securely connect with your personal\nnutritionist or medical expert.",
            color = Color(0xFF667085),
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
        Spacer(Modifier.height(34.dp))
        ChatSetupField(name, "Dietitian Name") { name = it }
        Spacer(Modifier.height(14.dp))
        ChatSetupField(contact, "Practice ID / Contact") { contact = it }
        Spacer(Modifier.height(18.dp))
        GradientButton(
            text = "Start Private Chat",
            showArrow = false,
            onClick = { viewModel.startPrivateChat(name.ifBlank { "abc" }, contact.ifBlank { "local" }) }
        )
    }
}

@Composable
private fun ChatSetupField(value: String, placeholder: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth().height(58.dp),
        shape = RoundedCornerShape(16.dp),
        singleLine = true,
        placeholder = { Text(placeholder, color = Muted) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Line,
            unfocusedBorderColor = Line,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        )
    )
}

@Composable
private fun ExpertChatRoom(
    viewModel: NutriSafeViewModel,
    padding: PaddingValues,
    name: String,
    messages: List<ChatMessageEntity>
) {
    var draft by rememberSaveable { mutableStateOf("") }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        viewModel.attachChatImage(uri)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(76.dp)
                .background(Color.White)
                .padding(horizontal = 22.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(38.dp).clip(CircleShape).background(Color(0xFFFFE8CC)), contentAlignment = Alignment.Center) {
                Icon(TinyIcons.PersonAdd, null, tint = Color(0xFFFF6B1A))
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(name, color = Ink, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                Text("ACTIVE NOW", color = Color(0xFF00B050), fontWeight = FontWeight.ExtraBold, fontSize = 10.sp)
            }
            Icon(TinyIcons.Phone, null, tint = Muted)
        }
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth().background(Color.White),
            contentPadding = PaddingValues(horizontal = 22.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(messages, key = { it.id }) { message -> ChatBubble(message) }
            if (viewModel.chatTyping) {
                item { TypingBubble(name) }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(78.dp)
                .background(Color.White)
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) {
                Icon(TinyIcons.Image, null, tint = Muted)
            }
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                modifier = Modifier.weight(1f).height(54.dp),
                shape = RoundedCornerShape(18.dp),
                singleLine = true,
                placeholder = { Text("Type your message...", color = Muted) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color(0xFFF8F9FB),
                    unfocusedContainerColor = Color(0xFFF8F9FB)
                )
            )
            Spacer(Modifier.width(10.dp))
            IconButton(
                modifier = Modifier.size(52.dp).clip(RoundedCornerShape(16.dp)).background(brandGradient()),
                onClick = {
                    viewModel.sendChatMessage(draft)
                    draft = ""
                }
            ) {
                Icon(TinyIcons.Send, null, tint = Color.White)
            }
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessageEntity) {
    val isUser = message.senderType == ExpertChatRepository.SENDER_USER
    val time = DateTimeFormatter.ofPattern("hh:mm a").format(
        Instant.ofEpochMilli(message.createdAt).atZone(ZoneId.systemDefault()).toLocalTime()
    )
    Box(Modifier.fillMaxWidth(), contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.74f)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 2.dp,
                        bottomEnd = if (isUser) 2.dp else 16.dp
                    )
                )
                .background(if (isUser) BrandMagenta else Color(0xFFF0F1F5))
                .padding(16.dp)
        ) {
            Text(message.message, color = if (isUser) Color.White else Ink, fontSize = 15.sp, lineHeight = 22.sp)
            Spacer(Modifier.height(6.dp))
            Text(time, color = if (isUser) Color.White.copy(alpha = 0.75f) else Muted, fontSize = 10.sp)
        }
    }
}

@Composable
private fun TypingBubble(name: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFF0F1F5))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("$name is typing...", color = Muted, fontSize = 13.sp)
    }
}


