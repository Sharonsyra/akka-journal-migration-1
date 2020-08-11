package com.namely.notable

import akka.actor.ActorSystem
import akka.http.scaladsl.{Http, HttpConnectionContext}
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import com.namely.protobuf.chief_of_state.WriteSideHandlerServiceHandler

import scala.concurrent.{ExecutionContext, Future}

class WriteHandlerServer(system: ActorSystem) {

  def run(): Future[Http.ServerBinding] = {
    // Akka boot up code
    implicit val sys: ActorSystem = system
    implicit val ec: ExecutionContext = sys.dispatcher

    // Create service handlers
    val service: HttpRequest => Future[HttpResponse] =
      WriteSideHandlerServiceHandler(new WriteSideHandlerServiceImpl())

    // Bind service handler servers to localhost:8080/8081
    val binding = Http()
      .bindAndHandleAsync(
      handler = service,
      interface = "0.0.0.0",
      port = 50052,
      connectionContext = HttpConnectionContext()
    )

    // report successful binding
    binding.foreach { binding => println(s"Write Handler gRPC server bound to: ${binding.localAddress}") }

    binding
  }
}
