package cl.inacap.dispensador

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.inacap.dispensador.ui.theme.DispensadorTheme
import kotlinx.coroutines.delay

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            DispensadorTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    HomeDispensadorScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun HomeDispensadorScreen(modifier: Modifier = Modifier) {
    var tiempoRestanteSegundos by remember { mutableStateOf(4 * 3600 + 3 * 60 + 53) }

    // Guardamos el último tiempo configurado para que al llegar a 0 se reinicie solo
    var tiempoObjetivoSegundos by remember { mutableStateOf(4 * 3600 + 3 * 60 + 53) }

    val context = LocalContext.current
    val db = remember { DBHelper(context) }

    var historialLecturas by remember { mutableStateOf(db.obtenerLecturas()) }

    // Cuenta atrás y bucle automático al llegar a 0
    LaunchedEffect(tiempoRestanteSegundos) {
        if (tiempoRestanteSegundos > 0) {
            delay(1000L)
            tiempoRestanteSegundos--
        } else {
            // Cuando llega a 0, se reinicia automáticamente con la última cantidad configurada
            delay(2000L) // Da un pequeño respiro de 2 segundos mostrando el aviso de "¡PUM!" antes de reiniciar
            tiempoRestanteSegundos = tiempoObjetivoSegundos
        }
    }

    val horasActuales = tiempoRestanteSegundos / 3600
    val minutosActuales = (tiempoRestanteSegundos % 3600) / 60
    val segundosActuales = tiempoRestanteSegundos % 60
    val timerActualTexto = String.format("%02d:%02d:%02d", horasActuales, minutosActuales, segundosActuales)

    var horasConfig by remember { mutableStateOf(0) }
    var minutosConfig by remember { mutableStateOf(4) }
    var segundosConfig by remember { mutableStateOf(53) }

    var brillo by remember { mutableStateOf(1) }
    val brilloMaximo = 5

    var tiempoApertura by remember { mutableStateOf(3) }
    val tiempoAperturaMaximo = 15

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        Text(text = "app :D - holaaa", fontSize = 16.sp, modifier = Modifier.align(androidx.compose.ui.Alignment.Start))

        Image(
            painter = painterResource(id = R.drawable.gato),
            contentDescription = "Gato dispensador",
            modifier = Modifier
                .height(100.dp)
                .fillMaxWidth()
        )

        // Tarjeta superior: Timer actual
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                Text(text = "timer actual:", fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))

                if (tiempoRestanteSegundos > 0) {
                    Text(
                        text = timerActualTexto,
                        fontSize = 28.sp,
                        style = MaterialTheme.typography.headlineMedium
                    )
                } else {
                    Text(
                        text = "¡PUM! Entregando comida por ${tiempoApertura}s 🐾",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }
        }

        // Tarjeta: Configurar tiempo
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "configurar tiempo", fontSize = 16.sp)

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Botón Guardar (💾) -> Actualiza el objetivo, el contador y guarda en BBDD
                    Button(onClick = {
                        val totalSegundos = (horasConfig * 3600) + (minutosConfig * 60) + segundosConfig
                        tiempoObjetivoSegundos = totalSegundos
                        tiempoRestanteSegundos = totalSegundos
                        db.insertarLectura(totalSegundos, "Temporizador guardado")
                        historialLecturas = db.obtenerLecturas()
                    }) {
                        Text("💾")
                    }

                    // Botón Reiniciar (🔄)
                    Button(onClick = {
                        horasConfig = 0
                        minutosConfig = 0
                        segundosConfig = 0
                        tiempoRestanteSegundos = 0
                        tiempoObjetivoSegundos = 0
                    }) {
                        Text("🔄")
                    }

                    // Botón Ejecutar (▶) -> Actualiza el objetivo, el contador y guarda en BBDD
                    Button(onClick = {
                        val totalSegundos = (horasConfig * 3600) + (minutosConfig * 60) + segundosConfig
                        tiempoObjetivoSegundos = totalSegundos
                        tiempoRestanteSegundos = totalSegundos
                        db.insertarLectura(totalSegundos, "Ejecución manual")
                        historialLecturas = db.obtenerLecturas()
                    }) {
                        Text("▶")
                    }
                }

                Row(
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                        Button(onClick = { if (horasConfig < 23) horasConfig++ }) { Text("+") }
                        Text(text = String.format("%02d", horasConfig), fontSize = 22.sp)
                        Button(onClick = { if (horasConfig > 0) horasConfig-- }) { Text("-") }
                    }

                    Text(text = ":", fontSize = 22.sp)

                    Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                        Button(onClick = { if (minutosConfig < 59) minutosConfig++ }) { Text("+") }
                        Text(text = String.format("%02d", minutosConfig), fontSize = 22.sp)
                        Button(onClick = { if (minutosConfig > 0) minutosConfig-- }) { Text("-") }
                    }

                    Text(text = ":", fontSize = 22.sp)

                    Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                        Button(onClick = { if (segundosConfig < 59) segundosConfig++ }) { Text("+") }
                        Text(text = String.format("%02d", segundosConfig), fontSize = 22.sp)
                        Button(onClick = { if (segundosConfig > 0) segundosConfig-- }) { Text("-") }
                    }
                }
            }
        }

        // Tarjeta: Ajustar brillo
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text(text = "ajustar brillo", fontSize = 16.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { if (brillo > 1) brillo-- }) { Text("-") }
                        Button(onClick = { if (brillo < brilloMaximo) brillo++ }) { Text("+") }
                    }
                }
                Text(text = "valor actual = $brillo", fontSize = 14.sp)
                Text(text = "valor maximo = $brilloMaximo", fontSize = 14.sp)
            }
        }

        // Tarjeta: Ajustar tiempo apertura
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text(text = "ajustar tiempo apertura", fontSize = 16.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { if (tiempoApertura > 1) tiempoApertura-- }) { Text("-") }
                        Button(onClick = { if (tiempoApertura < tiempoAperturaMaximo) tiempoApertura++ }) { Text("+") }
                    }
                }
                Text(text = "valor actual = ${tiempoApertura}s", fontSize = 14.sp)
                Text(text = "valor maximo = ${tiempoAperturaMaximo}s", fontSize = 14.sp)
            }
        }

        // Tarjeta: Historial de Registros BBDD
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "historial de registros (BBDD)", fontSize = 16.sp)

                if (historialLecturas.isEmpty()) {
                    Text(text = "No hay registros aún", fontSize = 14.sp, color = MaterialTheme.colorScheme.outline)
                } else {
                    historialLecturas.forEach { item ->
                        Text(text = "• $item", fontSize = 14.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}