package cn.ac.nci.ztb.hs.resource.web

import java.util.Date

import akka.actor.Actor
import spray.routing._
import spray.http._
import MediaTypes._
import cn.ac.nci.ztb.hs.resource.master.WorkersManager

/**
  * @author Young
  * @version 1.0
  * CreateTime: 16-12-5 下午4:28
  */
// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class MyServiceActor extends Actor with MyService {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(myRoute)
}


// this trait defines our service behavior independently from the service actor
trait MyService extends HttpService {

  val myRoute =
    path("hello") {
      get {
        respondWithMediaType(`text/html`) { // XML is marshalled to `text/xml` by default, so we simply override here
          complete {
            <html>
              <body>
                <title>heterogeneous streaming</title>
                <center>
                <table border="1">
                  <tr>
                    <th>ID</th>
                    <th>Node</th>
                    <th>Last contact</th>
                    <th>Launcher Port</th>
                    <th>Remaing Resource</th>
                  </tr>
                  {
                    WorkersManager.getRegisterWorker map {
                      worker =>
                        <tr>
                          <td>{worker._1}</td>
                          <td>{worker._2.getAddress.getHostName}</td>
                          <td>{(System.currentTimeMillis - worker._2.getLastUpdateTimestamp)/1000}</td>
                          <td>{worker._2.getAddress.getPort}</td>
                          <td>{worker._2.getRemainingResource}</td>
                        </tr>
                    }
                  }
                  {

                    /*0 until 10 map {
                      row =>
                        <tr>
                          <td>row {row}, cell 1</td>
                          <td>row {row}, cell 2</td>
                        </tr>
                    }*/
                  }
                </table>
                </center>
              </body>
            </html>
          }
        }
      }
    }
}
