package com.innowise

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.Materializer
import com.innowise.controller.ApiController
import com.innowise.service.ApiService
import com.innowise.validator.ApiValidator
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.{ExecutionContextExecutor, Future}

object MainApp extends App {
  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: Materializer = Materializer(system)
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  private val apiService = new ApiService
  private val apiController: ApiController = new ApiController(apiService, new ApiValidator(apiService))
  private val logger = LoggerFactory.getLogger(MainApp.getClass)

  private val futureBinding: Future[Http.ServerBinding] = {
    Http().newServerAt("localhost", 8080).bind(apiController.getRoute)
  }

  futureBinding.onComplete { server =>
    if (server.isSuccess) {
      logger.info("server started")
    } else logger.info("failed to start server")
  }
}
