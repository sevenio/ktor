package example.com

import example.com.plugins.*
import io.ktor.server.application.*

fun main(args: Array<String>) {
    try {


        io.ktor.server.netty.EngineMain.main(args)
    }catch (e: Exception){
        e.printStackTrace()
    }
}

fun Application.module() {
    configureRouting()
}
