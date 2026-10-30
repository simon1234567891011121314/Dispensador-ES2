package cl.inacap.dispensador

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cl.inacap.dispensador.ui.theme.DispensadorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = DBHelper(this)

        setContent {
            DispensadorTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LoginScreen(
                        db = db,
                        modifier = Modifier.padding(innerPadding),
                        onLoginOk = {
                            startActivity(Intent(this@MainActivity, HomeActivity::class.java))
                            finish()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun LoginScreen(db: DBHelper, modifier: Modifier = Modifier, onLoginOk: () -> Unit) {
    val context = LocalContext.current
    var usuario by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }


    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        OutlinedTextField(
            value = usuario,
            label = { Text("Ingresa tu Usuario") },
            onValueChange = { usuario = it },
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            label = { Text("Ingresa tu Contraseña") },
            onValueChange = { password = it },
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                if (db.validarLogin(usuario.trim(), password)) {
                    onLoginOk()
                } else {
                    Toast.makeText(context, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show()
                }
            }
        ) {
            Text("Iniciar Sesión")
        }

        Spacer(Modifier.height(8.dp))

        OutlinedButton(
            onClick = {
                when {
                    usuario.isBlank() || password.isEmpty() ->
                        Toast.makeText(context, "Completa ambos campos", Toast.LENGTH_SHORT).show()
                    db.registrarUsuarios(usuario.trim(), password) ->
                        Toast.makeText(context, "Usuario registrado correctamente", Toast.LENGTH_SHORT).show()
                    else ->
                        Toast.makeText(context, "El usuario ya existe", Toast.LENGTH_SHORT).show()
                }
            }
        ) {
            Text("Registrarse")
        }
    }
}