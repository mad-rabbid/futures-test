package application

import org.slf4j.{LoggerFactory, Logger}

trait Logging {
  protected def log: Logger = LoggerFactory.getLogger(this.getClass)

}
