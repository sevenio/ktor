package example.com.plugins

import example.com.model.Priority
import example.com.model.Task
import example.com.model.TaskRepository
import example.com.model.tasksAsTable
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    install(StatusPages) {
        exception<IllegalStateException> { call, cause ->
            call.respondText("App in illegal state as ${cause.message}")
        }
    }


    routing {
//        http://0.0.0.0:9292/content/sample.html
        staticResources(remotePath = "/content", basePackage = "mycontent")
//        http://0.0.0.0:9292/task-ui/task-form.html
        staticResources("/task-ui", "task_ui")

        get("/") {
            call.respondText("Hello World!")
        }
        get("/test1") {
            val text = "<h1>Hello From Ktor</h1>"
            val type = ContentType.parse("text/html")
            call.respondText(text, type)
        }
//         http://0.0.0.0:9292/error-test
        get("/error-test") {
            throw IllegalStateException("Too Busy")
        }

        route("/tasks"){
//         http://0.0.0.0:9292/tasks
            get {
                val tasks = TaskRepository.allTasks()
                call.respondText(
                    contentType = ContentType.parse("text/html"),
                    text = tasks.tasksAsTable()
                )


            }
            route("/byPriority") {
                //          http://0.0.0.0:9292/tasks/byPriority/Medium
                get("/{priority}") {
                    val priorityAsText = call.parameters["priority"]
                    if (priorityAsText == null) {
                        call.respond(HttpStatusCode.BadRequest)
                        return@get
                    }

                    try {
                        val priority = Priority.valueOf(priorityAsText)
                        val tasks = TaskRepository.tasksByPriority(priority)

                        if (tasks.isEmpty()) {
                            call.respond(HttpStatusCode.NotFound)
                            return@get
                        }

                        call.respondText(
                            contentType = ContentType.parse("text/html"),
                            text = tasks.tasksAsTable()
                        )
                    } catch (ex: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest)
                    }
                }
            }

            post {
                val formContent = call.receiveParameters()

                val params = Triple(
                    formContent["name"] ?: "",
                    formContent["description"] ?: "",
                    formContent["priority"] ?: ""
                )

                if (params.toList().any { it.isEmpty() }) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@post
                }

                try {
                    val priority = Priority.valueOf(params.third)
                    TaskRepository.addTask(
                        Task(
                            params.first,
                            params.second,
                            priority
                        )
                    )

                    call.respond(HttpStatusCode.NoContent)
                } catch (ex: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest)
                } catch (ex: IllegalStateException) {
                    call.respond(HttpStatusCode.BadRequest)
                }
            }

        }



    }
}
