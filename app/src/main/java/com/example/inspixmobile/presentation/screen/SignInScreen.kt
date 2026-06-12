package com.example.inspixmobile.presentation.screen

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.inspixmobile.R
import com.example.inspixmobile.core.util.ImageHelper
import com.example.inspixmobile.presentation.component.CollectionCardComponent
import com.example.inspixmobile.presentation.viewmodel.AuthViewModel
import org.koin.compose.viewmodel.koinViewModel

val AccentPurple = Color(0xFF534AB7)

@Composable
fun SignInScreen(
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    authViewModel: AuthViewModel = koinViewModel()
) {
    val context = LocalContext.current

    var email by rememberSaveable { mutableStateOf("tomnguyenhieu2004@gmail.com") }
    var password by rememberSaveable { mutableStateOf("12345678") }

    val gridState = rememberLazyStaggeredGridState()

    val rawCollections by authViewModel.collections.collectAsStateWithLifecycle()
    val collections = remember(rawCollections) {
        if (rawCollections.isEmpty()) rawCollections
        else List(200) { rawCollections[it % rawCollections.size] }
    }

    LaunchedEffect(Unit) {
        while (true) {
            gridState.animateScrollBy(
                value = 300f,
                animationSpec = tween(
                    durationMillis = 10000,
                    easing = LinearEasing
                )
            )
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyVerticalStaggeredGrid(
            state = gridState,
            columns = StaggeredGridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalItemSpacing = 4.dp,
            userScrollEnabled = false
        ) {
            items(collections.size) { index ->
                val collection = collections[index]

                val coverImage = collection.images?.firstOrNull()
                val resolvedRatio = ImageHelper.aspectRatio(
                    coverImage?.width,
                    coverImage?.height
                )

                CollectionCardComponent(
                    context = context,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope,
                    collection = collection,
                    aspectRatio = resolvedRatio,
                    likeButtonVisible = false,
                    onClick = { }
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Inspix",
                fontSize = 48.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Đăng nhập để lưu những gì bạn thích \nvà khám phá nhiều hơn nha",
                fontSize = 13.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            EmailField(
                email = email,
                onEmailChange = { email = it })

            Spacer(modifier = Modifier.height(12.dp))

            PasswordField(
                password = password,
                onPasswordChange = { password = it })

            Spacer(modifier = Modifier.height(36.dp))

            Button(
                onClick = {
                    authViewModel.signIn(
                        email = email,
                        password = password
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(80.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = "Đăng nhập",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OrDivider()

            Spacer(modifier = Modifier.height(16.dp))

            GoogleButton()

            Spacer(modifier = Modifier.height(64.dp))
        }
    }
}

@Composable
private fun EmailField(
    email: String,
    onEmailChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            modifier = Modifier.padding(start = 20.dp),
            text = "Email",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(5.dp))

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = "example@email.com",
                    fontSize = 12.sp,
                    lineHeight = 12.sp,
                    color = Color(0xFFB4B2A9)
                )
            },
            textStyle = LocalTextStyle.current.copy(
                fontSize = 13.sp,
                lineHeight = 13.sp
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            ),
            shape = RoundedCornerShape(36.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = AccentPurple,
                unfocusedBorderColor = Color(0xFFD3D1C7),
                focusedTextColor = Color(0xFF1A1A1A),
                unfocusedTextColor = Color(0xFF1A1A1A)
            )
        )
    }
}

@Composable
private fun PasswordField(
    password: String,
    onPasswordChange: (String) -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            modifier = Modifier.padding(start = 20.dp),
            text = "Mật khẩu",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(5.dp))

        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = "••••••••••••••••",
                    fontSize = 12.sp,
                    lineHeight = 12.sp,
                    color = Color(0xFFB4B2A9)
                )
            },
            textStyle = LocalTextStyle.current.copy(
                fontSize = 13.sp,
                lineHeight = 13.sp
            ),
            singleLine = true,
            visualTransformation = if (passwordVisible)
                VisualTransformation.None
            else
                PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            trailingIcon = {
                IconButton(
                    onClick = {
                        passwordVisible = !passwordVisible
                    }
                ) {
                    Icon(
                        imageVector =
                            if (passwordVisible)
                                Icons.Outlined.VisibilityOff
                            else
                                Icons.Outlined.Visibility,
                        contentDescription = null,
                        tint = Color(0xFFB4B2A9)
                    )
                }
            },
            shape = RoundedCornerShape(36.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = AccentPurple,
                unfocusedBorderColor = Color(0xFFD3D1C7),
                focusedTextColor = Color(0xFF1A1A1A),
                unfocusedTextColor = Color(0xFF1A1A1A)
            )
        )
    }
}

@Composable
private fun OrDivider() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = Color.White,
            thickness = 0.8.dp
        )
        Text(
            text = "hoặc",
            modifier = Modifier.padding(horizontal = 12.dp),
            fontSize = 12.sp,
            color = Color.White
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = Color.White,
            thickness = 0.8.dp
        )
    }
}

@Composable
private fun GoogleButton() {
    OutlinedButton(
        onClick = {},
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        shape = RoundedCornerShape(80.dp),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
        border = BorderStroke(0.5.dp, Color(0xFFD3D1C7))
    ) {
        Icon(
            painter = painterResource(id = R.drawable.google_logo),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = Color.Unspecified
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Tiếp tục với Google",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1A1A1A)
        )
    }
}