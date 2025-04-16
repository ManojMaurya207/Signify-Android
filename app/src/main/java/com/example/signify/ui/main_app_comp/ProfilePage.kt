import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.signify.viewmodel.AuthState
import com.example.signify.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@Composable
fun ProfilePage(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
    navController: NavController,
) {
    val auth: FirebaseAuth = Firebase.auth
    val user = auth.currentUser
    val context = LocalContext.current
    var profilePic by remember { mutableStateOf(user?.photoUrl?.toString() ?: "") }
    val authState by authViewModel.authState.observeAsState()

    // Navigate to the login screen when the user is unauthenticated
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Unauthenticated -> navController.navigate("login") {
                popUpTo("mainscreen") { inclusive = true }
            }
            else -> Unit
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Profile Picture
        val painter: Painter = rememberAsyncImagePainter(profilePic)
        Image(
            painter = painter,
            contentDescription = "Profile Picture",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .clickable {
                    // Implement image picker to update profile picture
                    Toast.makeText(context, "Change Profile Picture", Toast.LENGTH_SHORT).show()
                }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Display Name
        Text(text = user?.displayName ?: "No Name", style = MaterialTheme.typography.bodyLarge)

        Spacer(modifier = Modifier.height(8.dp))

        // Email
        Text(text = user?.email ?: "No Email", style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(16.dp))

//        // Sign Out Button
//        Button(onClick = {
//            authViewModel.signout()
//            Toast.makeText(context, "Signed Out", Toast.LENGTH_SHORT).show()
//        }) {
//            Text("Sign Out")
//        }
    }
}
