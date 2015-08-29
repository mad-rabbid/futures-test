package application

import java.util.concurrent.atomic.AtomicInteger

import com.github.mauricio.async.db.Configuration
import com.github.mauricio.async.db.pool.{PoolConfiguration, ConnectionPool}
import com.github.mauricio.async.db.postgresql.PostgreSQLConnection
import com.github.mauricio.async.db.postgresql.pool.PostgreSQLConnectionFactory
import scala.concurrent.{ExecutionContext, Await, Promise, Future}
import scala.concurrent.duration._
import scala.language.implicitConversions
import scala.util.{Success, Failure}


object Application extends App with Logging {
  log.info("Starting the application...")

  implicit def toOption[T](source: T): Option[T] = Option(source)

  val configuration = Configuration("mpi_user", "localhost", 5432, "mpi_password", "mpi")
  val factory = new PostgreSQLConnectionFactory(configuration)
  implicit val ec = ExecutionContext.global

  val pool = new ConnectionPool(factory, new PoolConfiguration(maxObjects = 20, maxIdle = 5.minutes.toMillis, maxQueueSize = 10000), ec)

  def allSucceed[T](fs: Future[T] *): Future[T] = {
    val remaining = new AtomicInteger(fs.length)

    val p = Promise[T]()

    fs foreach {
      _ onComplete {
        case s @ Success(_) if remaining.decrementAndGet() == 0 => p tryComplete s
        case Success(_) => log.info(s"Remaining counter value is: ${remaining.get}")
        case f @ Failure(_) => p tryComplete f
      }
    }

    p.future
  }

  val futures = (1 to 10000).map { i =>
    pool.inTransaction[Any] { conn =>
      conn.sendQuery("SELECT * FROM mpi_properties")
    }
  }

  val completionFuture = allSucceed(futures: _*)
  Await.result(completionFuture, 120.seconds)



}
