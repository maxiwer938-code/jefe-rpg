package com.example.jefe

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

data class PreguntaJefe(
    val pregunta: String,
    val opciones: List<String>,
    val correcta: Int
)

data class Jefe(
    val nombre: String,
    val vidaMaxima: Int,
    val preguntas: List<PreguntaJefe>
)

class MainActivity : AppCompatActivity() {

    private val jefes = listOf(
        Jefe(
            "El Orco de los Videojuegos",
            100,
            listOf(
                PreguntaJefe("¿Cómo se llama el plomero protagonista de Nintendo?", listOf("Sonic", "Mario", "Link", "Donkey Kong"), 1),
                PreguntaJefe("¿En qué juego picás bloques y construís mundos con cubos?", listOf("Minecraft", "Tetris", "Pac-Man", "God of War"), 0),
                PreguntaJefe("¿Cuál es el estudio creador de Fortnite?", listOf("Valve", "Ubisoft", "Epic Games", "EA Sports"), 2),
                PreguntaJefe("¿Cómo se llama el erizo azul de SEGA?", listOf("Knuckles", "Tails", "Shadow", "Sonic"), 3)
            )
        ),
        Jefe(
            "El Titán del Cine y la Ciencia",
            150,
            listOf(
                PreguntaJefe("¿Qué planeta es conocido como el planeta rojo?", listOf("Venus", "Júpiter", "Marte", "Saturno"), 2),
                PreguntaJefe("¿Quién dirige las películas de Matrix?", listOf("Las hermanas Wachowski", "Steven Spielberg", "Christopher Nolan", "Quentin Tarantino"), 0),
                PreguntaJefe("¿Cuál es el elemento químico representado por el símbolo 'Au'?", listOf("Plata", "Oro", "Cobre", "Hierro"), 1),
                PreguntaJefe("¿En qué año llegó el hombre a la Luna?", listOf("1959", "1969", "1979", "1985"), 1)
            )
        ),
        Jefe(
            "El Dragón Galáctico del Deporte",
            200,
            listOf(
                PreguntaJefe("¿Cuántos jugadores por equipo juegan un partido de fútbol oficial en cancha?", listOf("9", "10", "11", "12"), 2),
                PreguntaJefe("¿En qué país se inventó el básquetbol?", listOf("Estados Unidos", "Brasil", "Inglaterra", "España"), 0),
                PreguntaJefe("¿Qué selección ganó el Mundial de Fútbol de Qatar 2022?", listOf("Francia", "Brasil", "Argentina", "Alemania"), 2),
                PreguntaJefe("¿Cuántos anillos dorados colecciona Sonic en sus aventuras clásicas?", listOf("Ninguno en particular como límite fijo", "10", "50", "100"), 3)
            )
        )
    )

    private var jefeActualIndex = 0
    private var vidaJefe = 100
    private var vidaJugador = 3
    private var indicePregunta = 0
    private var respondida = false
    private val handler = Handler(Looper.getMainLooper())
    private var timer: CountDownTimer? = null

    private lateinit var contenedor: LinearLayout
    private lateinit var tiempoView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        contenedor = LinearLayout(this)
        contenedor.orientation = LinearLayout.VERTICAL
        contenedor.setPadding(48, 48, 48, 48)

        val scroll = ScrollView(this)
        scroll.addView(contenedor)
        setContentView(scroll)

        mostrarPantallaInicio()
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
        handler.removeCallbacksAndMessages(null)
    }

    private fun texto(s: String, size: Float, margenArriba: Int = 0): TextView {
        val t = TextView(this)
        t.text = s
        t.textSize = size
        t.setPadding(0, margenArriba, 0, 0)
        contenedor.addView(t)
        return t
    }

    private fun boton(s: String, alClick: () -> Unit): Button {
        val b = Button(this)
        b.text = s
        b.setOnClickListener { alClick() }
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        lp.topMargin = 16
        b.layoutParams = lp
        contenedor.addView(b)
        return b
    }

    private fun mostrarPantallaInicio() {
        contenedor.removeAllViews()
        texto("⚔️ RPG Trivia: Batalla de Jefes", 26f)
        texto("Derrota a los jefes respondiendo preguntas de cultura general, videojuegos y deportes antes de quedarte sin vidas.", 16f, 16)
        boton("¡Comenzar Aventura!") {
            jefeActualIndex = 0
            empezarJefe()
        }
    }

    private fun empezarJefe() {
        if (jefeActualIndex >= jefes.size) {
            mostrarVictoriaTotal()
            return
        }
        val jefe = jefes[jefeActualIndex]
        vidaJefe = jefe.vidaMaxima
        vidaJugador = 3
        indicePregunta = 0
        mostrarCombate()
    }

    private fun mostrarCombate() {
        timer?.cancel()
        val jefe = jefes[jefeActualIndex]

        if (vidaJefe <= 0) {
            jefeActualIndex++
            mostrarVictoriaParcial()
            return
        }
        if (vidaJugador <= 0) {
            mostrarDerrota()
            return
        }

        if (indicePregunta >= jefe.preguntas.size) {
            indicePregunta = 0
        }

        respondida = false
        val p = jefe.preguntas[indicePregunta]
        contenedor.removeAllViews()

        // Interfaz de Batalla
        texto("🎯 JEFE: ${jefe.nombre}", 22f)
        texto("❤️ Vida del Jefe: $vidaJefe / ${jefe.vidaMaxima}", 18f, 8)
        texto("🛡️ Tus Vidas: " + "♥".repeat(vidaJugador), 18f, 8)
        tiempoView = texto("⏱️ Tiempo: 15", 16f, 8)
        
        texto("\n💬 ${p.pregunta}", 20f, 16)

        val correctaTexto = p.opciones[p.correcta]
        val botones = ArrayList<Button>()
        for (op in p.opciones.shuffled()) {
            val b = boton(op) { }
            b.setBackgroundColor(Color.LTGRAY)
            b.setTextColor(Color.BLACK)
            b.setOnClickListener { 
                atacarJefe(op == correctaTexto, botones, correctaTexto) 
            }
            botones.add(b)
        }

        iniciarTimer(botones, correctaTexto)
    }

    private fun iniciarTimer(botones: List<Button>, correctaTexto: String) {
        timer?.cancel()
        timer = object : CountDownTimer(15000L, 1000L) {
            override fun onTick(ms: Long) {
                tiempoView.text = "⏱️ Tiempo: ${(ms + 999) / 1000}"
            }
            override fun onFinish() {
                tiempoView.text = "⏱️ Tiempo: 0"
                atacarJefe(false, botones, correctaTexto)
            }
        }
        timer?.start()
    }

    private fun atacarJefe(acierto: Boolean, botones: List<Button>, correctaTexto: String) {
        if (respondida) return
        respondida = true
        timer?.cancel()

        if (acierto) {
            vidaJefe -= 40 // Daño contundente al jefe
            for (b in botones) {
                if (b.text.toString() == correctaTexto) b.setBackgroundColor(Color.GREEN)
            }
        } else {
            vidaJugador-- // El jefe te castiga
            for (b in botones) {
                if (b.text.toString() == correctaTexto) {
                    b.setBackgroundColor(Color.GREEN)
                }
            }
        }

        handler.postDelayed({
            indicePregunta++
            mostrarCombate()
        }, 1500)
    }

    private fun mostrarVictoriaParcial() {
        contenedor.removeAllViews()
        texto("🎉 ¡Derrotaste al jefe!", 26f)
        texto("¡Avanzás al siguiente nivel de la mazmorra!", 16f, 16)
        boton("Siguiente Batalla") {
            empezarJefe()
        }
    }

    private fun mostrarVictoriaTotal() {
        contenedor.removeAllViews()
        texto("🏆 ¡VICTORIA ÉPICA!", 28f)
        texto("¡Derrotaste a todos los jefes y completaste el juego!", 18f, 16)
        boton("Jugar de nuevo") {
            mostrarPantallaInicio()
        }
    }

    private fun mostrarDerrota() {
        contenedor.removeAllViews()
        texto("💀 ¡GAME OVER!", 28f)
        texto("El jefe te ha vencido en combate.", 18f, 16)
        boton("Reintentar batalla") {
            empezarJefe()
        }
        boton("Volver al inicio") {
            mostrarPantallaInicio()
        }
    }
}
