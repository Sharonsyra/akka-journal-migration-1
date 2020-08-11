package com.namely.notable

import akka.actor.ActorSystem
import akka.http.scaladsl.{Http, HttpConnectionContext}
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import com.namely.protobuf.chief_of_state.ReadSideHandlerServiceHandler

import scala.concurrent.{ExecutionContext, Future}

class ReadSideHandlerServiceServer(system: ActorSystem) {
  def run(): Future[Http.ServerBinding] = {
    // Akka boot up code
    implicit val sys: ActorSystem = system
    implicit val ec: ExecutionContext = sys.dispatcher

    // Create service handlers
    val service: HttpRequest => Future[HttpResponse] =
      ReadSideHandlerServiceHandler(new ReadSideHandlerServiceImpl())

    // Bind service handler servers to localhost:50053
    val binding = Http()
      .bindAndHandleAsync(
        handler = service,
        interface = "0.0.0.0",
        port = 50053,
        connectionContext = HttpConnectionContext()
      )

    // report successful binding
    binding.foreach { binding => println(s"Read Side Handler gRPC server bound to: ${binding.localAddress}") }

    binding
  }
}
