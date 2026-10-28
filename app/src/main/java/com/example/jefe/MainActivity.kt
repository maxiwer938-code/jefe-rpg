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
import org.json.JSONArray

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
            "El Goblin de la Cueva",
            100,
            listOf(
                PreguntaJefe("¿Cuánto es 2 + 2?", listOf("3", "4", "5", "6"), 1),
                PreguntaJefe("¿Cuál es la capital de Argentina?", listOf("Córdoba", "Buenos Aires", "Rosario", "Mendoza"), 1),
                PreguntaJefe("¿Qué color se forma mezclando azul y amarillo?", listOf("Verde", "Rojo", "Naranja", "Violeta"), 0)
            )
        ),
        Jefe(
            "El Gran Monstruo de Electricidad",
            200,
            listOf(
                PreguntaJefe("¿Qué fenómeno genera una f.e.m. al cortar líneas magnéticas?", listOf("Inducción electromagnética", "Resistencia", "Capacitancia", "Cortocircuito"), 0),
                PreguntaJefe("¿Qué dispositivo endereza la corriente alternada?", listOf("Transformador", "Colector o conmutador", "Reóstato", "Batería"), 1),
                PreguntaJefe("¿Qué ley define el sentido de la f.c.e.m. en un motor?", listOf("Ley de Ohm", "Ley de Lenz", "Ley de Watt", "Ley de Coulomb"), 1)
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
    private lateinit var vidaJefeView: TextView
    private lateinit var vidaJugadorView: TextView

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
        texto("Batalla RPG: Derrota al Jefe", 28f)
        texto("Respondé bien las preguntas para atacarlo antes de que se acaben tus vidas.", 16f, 16)
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
            indicePregunta = 0 // Reinicia preguntas si el jefe resiste mucho
        }

        respondida = false
        val p = jefe.preguntas[indicePregunta]
        contenedor.removeAllViews()

        // Interfaz de Batalla
        texto("⚔️ JEFE: ${jefe.nombre}", 22f)
        vidaJefeView = texto("❤️ Vida del Jefe: $vidaJefe / ${jefe.vidaMaxima}", 18f, 8)
        vidaJugadorView = texto("🛡️ Tus Vidas: " + "♥".repeat(vidaJugador), 18f, 8)
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
            vidaJefe -= 35 // Daño al jefe
            for (b in botones) {
                if (b.text.toString() == correctaTexto) b.setBackgroundColor(Color.GREEN)
            }
        } else {
            vidaJugador-- // Daño al jugador
            for (b in botones) {
                if (b.text.toString() == correctaTexto) {
                    b.setBackgroundColor(Color.GREEN)
                } else if (b.isPressed) {
                    b.setBackgroundColor(Color.RED)
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
        texto("Pasaste al siguiente nivel de la mazmorra.", 16f, 16)
        boton("Siguiente Batalla") {
            empezarJefe()
        }
    }

    private fun mostrarVictoriaTotal() {
        contenedor.removeAllViews()
        texto("🏆 ¡VICTORIA ÉPICA!", 28f)
        texto("¡Derrotaste a todos los jefes y salvaste el mundo!", 18f, 16)
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
