import akka.actor.ActorSystem
import akka.actor.Actor
import akka.actor.Props
import akka.actor.ActorRef
import scala.collection.mutable.ArrayBuffer
import akka.routing.RoundRobinRouter
import java.security.MessageDigest
import java.io._
import scala.util.control._
import scala.concurrent.duration._
import java.net.InetAddress
import com.typesafe.config.ConfigFactory

object BCClient {
  def main(args: Array[String]) {
   val hostname = InetAddress.getLocalHost.getHostName
    val config = ConfigFactory.parseString(
      """akka{
		  		actor{
        
		  			provider = "akka.remote.RemoteActorRefProvider"
		  		}
		  		remote{
                   enabled-transports = ["akka.remote.netty.tcp"]
		  			netty.tcp{
						hostname = "127.0.0.1"
						port = 0
					}
				}     
    	}""")

   var nrOfWorkers : Int = 4;
   var nrOfMessages : Int = 1000;
   
   implicit val system = ActorSystem("ClientBitCoinSystem", ConfigFactory.load(config))
   val resultSender = system.actorOf(Props(new ResultSender(args(0))),name= "resultSender")
   val listener = system.actorOf(Props(new Listener(resultSender:ActorRef)), name = "listener")
   val master = system.actorOf(Props(new Master(nrOfWorkers, nrOfMessages,listener)), name = "master")
   val workRequestActor =    system.actorOf(Props(new WorkRequestActor( args(0),master)),name = "workRequestActor")
   workRequestActor ! RequestWork
  
  }
}

sealed trait BitCoinGen
case object RequestWork extends BitCoinGen
case class Calculate(arg : Int) extends BitCoinGen
case class Result(value: ArrayBuffer[String]) extends BitCoinGen
case class Work(nrOfMessages: Int,arg:Int) extends BitCoinGen
case class PrintCoins(output: ArrayBuffer[String], duration: Duration) extends BitCoinGen
case class LocalMessage(output:ArrayBuffer[String], duration : Duration) extends BitCoinGen
case class RemoteMessage(result:ArrayBuffer[String], duration : Duration) extends BitCoinGen
case class AllocateWork(arg: Int) extends BitCoinGen

class WorkRequestActor(serverAddress : String, master: ActorRef) extends Actor
{
     val remote = context.actorFor("akka.tcp://ServerBitCoinSystem@" + serverAddress + ":5150/user/RemoteActor")
    def receive = 
    {
    case RequestWork =>
    		remote ! "GETWORK"
    case AllocateWork(arg) =>
    	println("Work received from server :" )
    		master ! Calculate(arg)  
    }
}

class Master(nrOfWorkers: Int, nrOfMessages: Int, listener:ActorRef) extends Actor 
{
  var BITcoin: ArrayBuffer[String] = new ArrayBuffer[String]()
  var nrOfResults: Int = _
  val start: Long = System.currentTimeMillis
  val workerRouter = context.actorOf(Props[Worker].withRouter(RoundRobinRouter(nrOfWorkers)), name = "workerRouter")
  def receive = 
  {
  		case Calculate(arg)  =>
  		for (i <- 0 until nrOfMessages) workerRouter ! Work(nrOfMessages,arg)
        case Result(value) =>
        BITcoin ++= value
        nrOfResults += 1
        if (nrOfResults == nrOfMessages) {
        	listener ! PrintCoins(BITcoin, duration= (System.currentTimeMillis - start).millis)
        	context.stop(self)
        } 

  }
}

class Worker extends Actor {
  
def findBitCoins(nrOfMessages: Int,arg: Int): ArrayBuffer[String] = {
      val sha: MessageDigest = MessageDigest.getInstance("SHA-256");
      var Success = new ArrayBuffer[String]();
      for (i <- 0 until nrOfMessages) {
        val text: String = "kudai".concat(scala.util.Random.alphanumeric.take(10).mkString);
      val m = java.security.MessageDigest.getInstance("SHA-256").digest(text.getBytes("UTF-8"))
		var No_of_zeroes=0
		val result = m.map("%02x".format(_)).mkString
		var bytearr=result.getBytes()
        val loop = new Breaks;
		loop.breakable
		{
			for (x<- bytearr)
			{
				if(x=='0')
					No_of_zeroes+=1
				else
					loop.break
			}
		}
        if (No_of_zeroes==arg) {
          Success += result;
          Success += text;
        }
      }
      Success;
    }

    def receive = {
      case Work(nrOfMessages,arg) =>
        sender ! Result(findBitCoins(nrOfMessages,arg)) 
    }
  
}

class Listener(resultSender: ActorRef) extends Actor{
  def receive = {
    case PrintCoins(output, duration) =>
      {
        resultSender ! LocalMessage(output, duration)
      }
  }
}

class ResultSender(serverAddress : String) extends Actor{
 
  val remote = context.actorFor("akka.tcp://ServerBitCoinSystem@" + serverAddress + ":5150/user/RemoteActor")
  def receive = {
    case LocalMessage(output, duration) =>
       println("Sending output to Remote")
       remote ! RemoteMessage(output, duration)
       println("Output sent") 
   }
}