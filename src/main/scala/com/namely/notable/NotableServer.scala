package com.namely.notable

import akka.actor.ActorSystem
import akka.http.scaladsl.{Http, Http2, HttpConnectionContext}
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import com.namely.protobuf.notable.grpc.NotableServiceHandler
import com.typesafe.config.ConfigFactory

import scala.concurrent.{ExecutionContext, Future}

object NotableServer {

  def main(args: Array[String]): Unit = {

    // Important: enable HTTP/2 in ActorSystem's config
    val conf = ConfigFactory.load()

    val system = ActorSystem("NotableClient", conf)
    new NotableServer(system).run()

    new WriteHandlerServer(system).run()

    // ActorSystem threads will keep the app alive until `system.terminate()` is called
  }

}

class NotableServer(system: ActorSystem) {

  def run(): Future[Http.ServerBinding] = {
    // Akka boot up code
    implicit val sys: ActorSystem = system
    implicit val ec: ExecutionContext = sys.dispatcher

    // Create service handlers
    val service: HttpRequest => Future[HttpResponse] =
      NotableServiceHandler(new NotableServiceImpl())

    // Bind service handler servers to localhost:8080/8081
    val binding =
      Http().bindAndHandleAsync(
      handler = service,
      interface = "0.0.0.0",
      port = 50051,
      connectionContext = HttpConnectionContext()
    )

    // report successful binding
    binding.foreach { binding => println(s"Notable gRPC server bound to: ${binding.localAddress}") }

    binding
  }
}