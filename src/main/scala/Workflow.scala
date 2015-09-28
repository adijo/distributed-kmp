import akka.actor._  
import scala.collection.mutable.ArrayBuffer
import scala.annotation.tailrec
import scala.collection.mutable.HashMap;

case class SearchMain(string: String, pattern: String, offset: Int, batchNumber: Int)
case class Search(string : String, pattern : String)
case class Result(matches : ArrayBuffer[Int], batchNumber: Int)

class Manager(workers: Array[ActorRef]) extends Actor {
    import Manager._
    val numWorkers = workers.length
    var batchNumber = 1
    var batchToReqs = HashMap[Int, Int]()
    var batchResult = HashMap[Int, List[Int]]()
    var doneResult = HashMap[Int, Int]()
    def receive = {
      case Search(string : String, pattern : String) => {
          val piecesList = split(string, List(), Math.max(numWorkers, pattern.length)) filter (_.length > 0)
          val pieces = piecesList.toArray
          var i = 0 
          var j = 0
          val offsetMultiplier = pieces(0).length
          batchToReqs.+=((batchNumber, pieces.length))
          batchResult.+=((batchNumber, List()))
          doneResult.+=((batchNumber, 0))
          while(i < pieces.length) {
              workers(j) ! SearchMain(pieces(i), pattern, i * offsetMultiplier, batchNumber)
              i = i + 1
              j = (j + 1) % numWorkers
          }
              batchNumber += 1
      }
      
      case Result(matches: ArrayBuffer[Int], batchNumber: Int) => {

        doneResult.update(batchNumber, doneResult(batchNumber) + 1)
        if(doneResult(batchNumber) == batchToReqs(batchNumber)) {
           // we are done for this batch.
          println((matches.toList ++ batchResult(batchNumber)).sorted)
           
        }  
        else batchResult.update(batchNumber, batchResult(batchNumber) ++ matches.toList) 
        
      }
    }
  
}

class Worker extends Actor {
  import Worker._
  
  def receive = {
    case SearchMain(string : String, pattern : String, offset: Int, batchNumber: Int) => {
        var matches = kmp(string, pattern, offset)
        sender ! Result(matches, batchNumber)
    }
  }
}

case object Manager {
  
  @tailrec
  def split(string: String, res: List[String], takeChars: Int) : List[String] = {
      if(string.length < takeChars) (string :: res).reverse
      else {
        val currentString = string.substring(0, takeChars)
        val remainingString = string.substring(takeChars, string.length)
        split(remainingString, currentString :: res, takeChars)
      }
  }
  
}

case object Worker {
  
  def calculatePrefixTable(string : String) : Array[Int] = {
    var prefixTable = Array.fill[Int](string.length)(0)
    var j = 0
    var i = 1
    while(i < string.length) {
        if(string.charAt(i) == string.charAt(j)) {
            prefixTable(i) = j + 1
            j += 1
        }
        else {
          while(j > 0 && string.charAt(i) != string.charAt(j)) {
              j = prefixTable(j - 1)
          }
           if(string.charAt(j) == string.charAt(i)) {
                  prefixTable(i) = j + 1
                  j += 1
              }
        }
        i += 1
    }
    prefixTable
  }
  
  def kmp(string : String, pattern : String, offset: Int) : ArrayBuffer[Int] = {
      var prefixTable = calculatePrefixTable(pattern)
      var i = 0
      var j = 0
      
      var matches = ArrayBuffer[Int]()
      
      if(string.length >= pattern.length) {
        while(i < string.length) {
          
            if(j == pattern.length) {
                matches.+=(i - j)
                j = prefixTable(j - 1)
            }
            else {
              
                if(string.charAt(i) == pattern.charAt(j)) {
                    i += 1
                    j += 1
                }
                else {
                    if(j != 0) j = prefixTable(j - 1)
                    else i += 1
                }
            }
        }
        if(j == pattern.length) matches.+=(i - j)
      }
      
     matches map (_ + offset)
  }
}

object Workflow extends App {
    val workerLimit = 3
    val system = ActorSystem("DistributedKMP")
    var workersList = for(i <- range(0, workerLimit, List())) yield system.actorOf(Props[Worker], name = "worker" + i)
    val workers = workersList.toArray
    val manager = system.actorOf(Props(new Manager(workers)), name = "waiter")    
    manager ! Search("adityaadsasdaditya", "aditya")
    
   @tailrec
   def range(a: Int, b: Int, res: List[Int]) : List[Int] = {
       if(a >= b) res
       else range(a + 1, b, a :: res)
    }
      
}