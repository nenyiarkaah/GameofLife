import scala.collection.mutable.ListBuffer
import scala.util.Random

/**
  * Created by Nenyi on 10/02/2017.
  */
object Game {
  type State = Boolean
  type Board = List[Coordinates]
  val alive = true
  val dead = false

  case class Coordinates(x: Int, y: Int, state: State)

  def max(a: Coordinates, b: Coordinates): Coordinates = if ((a.x > b.x) & (a.y > b.y)) a else b

  def prod[T](lst: List[T], n: Int) = List.fill(n)(lst)
    .flatten.combinations(n).flatMap(_.permutations)

  def createBoard(x: Int, y: Int): Board = {
    val xRange = List.range(0, x)
    val yRange = List.range(0, y)
    xRange.map(xPoint => yRange.map(yPoint => storeCoordinate(xPoint, yPoint, Random.nextBoolean))).flatten
  }

  def getNeighbours(board: Board, x: Int, y: Int) = {

    val radius = 1
    val maxCoordinates = board.reduceLeft(max)
    val xMin = clamp(x - radius, 0, maxCoordinates.x)
    val xMax = clamp(x + radius, 0, maxCoordinates.x) + 1
    val yMin = clamp(y - radius, 0, maxCoordinates.y)
    val yMax = clamp(y + radius, 0, maxCoordinates.y) + 1
    var neighbours = ListBuffer[Coordinates]()
    generateCoordinates(x, y, xMin, xMax, yMin, yMax).map(n =>
      board.filter(b => (b.x == n._1) & (b.y == n._2)).headOption.get)
  }

  def getLiveNeighbours(board: Board, x: Int, y: Int) = {
    getNeighbours(board, x, y).filter(n => n.state == alive).length
  }

  def storeCoordinate(x: Int, y: Int, state: State) = {
    new Coordinates(x, y, state)
  }

  def clamp(v: Int, min: Int, max: Int) = {
    if (v < min) min
    else if (v > max) max
    else v
  }

  def transformCoordinates(coordinates: Coordinates, liveNeighbours: Int) = {
    val state = coordinates.state match {
      case `alive` => liveNeighbours match {
        case s if s < 2 => dead
        case s if s >= 2 & s <= 3 => alive
        case s if s > 3 => dead
      }
      case `dead` => liveNeighbours match {
        case s if s == 3 => alive
        case _ => dead
      }
    }
    storeCoordinate(coordinates.x, coordinates.y, state)
  }

  def transformBoard(board: Board): Board = {
    board.map(b => transformCoordinates(b, getLiveNeighbours(board, b.x, b.y)))
  }

  def printBoard(board: Board) = {
    val maxCoordinates = board.reduceLeft(max)
    val xMin = 0
    val xMax = maxCoordinates.x + 1
    val yMin = 0
    val yMax = maxCoordinates.y + 1
    for {y <- yMin until yMax} {
      for {x <- xMin until xMax} {
        val state = board.filter(b => (b.x == x) & (b.y == y)).headOption.get.state
        print(if (state) "alive " else "dead ")
      }
      println("")
    }
  }

  def transformBoardOverGenerations(board: Board, epoch: Int) = {
    var epochBoard = board
    println("Starting Point")
    printBoard(epochBoard)
    for {i <- 0 until epoch} {
      epochBoard = transformBoard(epochBoard)
      println("epoch " + i)
      printBoard(epochBoard)
    }
  }

  def generateCoordinates(x: Int, y: Int, xMin: Int, xMax: Int, yMin: Int, yMax: Int): List[(Int, Int)] = {
    val xRange = List.range(xMin, xMax)
    val yRange = List.range(yMin, yMax)

    prod(yRange, xRange.size)
      .map(xRange.zip(_)).toList.flatten.distinct
      .filter(_ != (x, y))
  }


}
