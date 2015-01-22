import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.Actor
import akka.actor.ActorRef
import akka.routing.RoundRobinRouter
import java.security.MessageDigest
import scala.util.Random
import scala.util.control._
import java.io._
import scala.collection.mutable.ArrayBuffer
import java.net.InetAddress
import com.typesafe.config.ConfigFactory
import scala.concurrent.duration.Duration
import scala.concurrent.duration._

object BCServer 
{
	System.setOut(new PrintStream(new FileOutputStream("bitcoins.txt")));
  def main(args: Array[String]) 
  {
	  val hostname = InetAddress.getLocalHost.getHostName
	  val config = ConfigFactory.parseString(
      """ 
	    akka{ 
    		actor{ 
    			provider = "akka.remote.RemoteActorRefProvider" 
    		} 
    		remote{ 
                enabled-transports = ["akka.remote.netty.tcp"] 
            netty.tcp{ 
    			hostname = "10.136.71.57" 
    			port = 5150 
    		} 
      }      
    }""")
    
     val arg = Integer.parseInt(args(0))
  calculate(nrOfWorkers = 4, nrOfMessages = 1000)
  def calculate(nrOfWorkers: Int, nrOfMessages: Int) {
    val system = ActorSystem("ServerBitCoinSystem", ConfigFactory.load(config))
    val listener = system.actorOf(Props[Listener], name = "listener")
    val remoteActor = system.actorOf(Props(new RemoteActor(arg)), name = "RemoteActor")
    val master = system.actorOf(Props(new Master(
      nrOfWorkers, nrOfMessages, listener,arg)),
      name = "master")
    master ! Calculate

  }
  }
}

  sealed trait BitCoinGen
  case object Calculate extends BitCoinGen
  case class Work(nrOfMessages: Int,arg: Int) extends BitCoinGen
  case class Result(value: ArrayBuffer[String]) extends BitCoinGen
  case class PrintCoins(output: ArrayBuffer[String], duration: Duration) extends BitCoinGen
  case class RemoteMessage(result:ArrayBuffer[String], duration: Duration) extends BitCoinGen
  case class AllocateWork(arg:Int) extends BitCoinGen
  
  class RemoteActor(arg : Int) extends Actor {
  def receive = {
    case "GETWORK" => {
      sender ! AllocateWork(arg)
    }
    case RemoteMessage(result, duration) => {
      println("Bitcoins Received from client")
      println("Number of bitcoins from client   :  " + (result.length/2))
      for (i <- 0 until (result.length)) 
      {
    	  if(i%2==0)
    	    print(result(i) + "   : " )
    	   else
                print(result(i)+"\n" )            
      }    
      println("Client duration: \t%s".format(duration))
    }
  }
} 
  class Master(nrOfWorkers: Int, nrOfMessages: Int, listener: ActorRef,arg : Int) extends Actor {

    var BITcoin: ArrayBuffer[String] = new ArrayBuffer[String]()
    var nrOfResults: Int = _
    val start: Long = System.currentTimeMillis
    val workerRouter = context.actorOf(
      Props[Worker].withRouter(RoundRobinRouter(nrOfWorkers)), name = "workerRouter")
    def receive = {
      case Calculate =>
        for (i <- 0 until nrOfMessages) workerRouter ! Work( nrOfMessages,arg)
      case Result(value) =>
        BITcoin ++= value
        nrOfResults += 1
        if (nrOfResults == nrOfMessages) {
          listener ! PrintCoins(BITcoin, duration = (System.currentTimeMillis - start).millis)
          context.stop(self)
        } 
        
    }

  }

  class Worker extends Actor {

    def findBitCoins(nrOfMessages: Int, arg: Int): ArrayBuffer[String] = {
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
          Success += text;
          Success += result;
        }
      }
      
      Success;
    }

    def receive = {
      case Work(nrOfMessages,arg) =>
        sender ! Result(findBitCoins(nrOfMessages, arg)) 
    }
  }

  class Listener extends Actor {
    def receive = {
      case PrintCoins(output, duration) =>
        {
        for (i <- 0 until (output.length)) 
      {
    	  if(i%2!=0)
    	    print(output(i) + "\n" )
    	   else
                print(output(i)+"   : " )
      }
      println("Server Duration: \t%s".format(duration))
      }
    }
  }

