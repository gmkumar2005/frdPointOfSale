/*
 * Copyright 2019 gmkumar2005
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package app

import cats.effect.IO
import org.slf4j.{Logger, LoggerFactory}
import pureconfig.module.catseffect.loadConfigF
import store.model.Configuration.Precision
import store.service.interpreter.PointOfSaleInterpreter
import store.service.interpreter.PointOfSale._
import pureconfig.generic.auto._
import java.math.BigDecimal._

import store.model.DiscountByVolume
import store.repo.PosRepository
import store.repo.interpreter.PosRepositoryInMemory

object App {
  val logger: Logger = LoggerFactory.getLogger(this.getClass)
  val AMOUNTSCALE: Int = loadConfigF[IO, Int]("amount-precision").unsafeRunSync()

  def main(args: Array[String]): Unit = {
    usecase1()
  }

  def usecase1(): Unit = {
    val commodities = for {
      _ <- upsertProduct("A", Some("Apples"), BigDecimal(2.0), Some(DiscountByVolume(4, BigDecimal(7))))
      _ <- upsertProduct("B", Some("Banana"), BigDecimal(12.0), None)
      _ <- upsertProduct("C", Some("Carrot"), BigDecimal(1.25), Some(DiscountByVolume(6, BigDecimal(6))))
      _ <- upsertProduct("D", Some("Dragon"), BigDecimal(0.15), None)
    } yield ()


    //ABCDABAA
    val purchaseOrder = for {
      orderId <- createFreshOrder
      _ <- scanItem("A", orderId)
      _ <- scanItem("B", orderId)
      _ <- scanItem("C", orderId)
      _ <- scanItem("D", orderId)
      _ <- scanItem("A", orderId)
      _ <- scanItem("B", orderId)
      _ <- scanItem("A", orderId)
      _ <- scanItem("A", orderId)
    } yield orderId

    val price = for {
      _ <- commodities
      orderId <- purchaseOrder
      tPrice <- totalPrice(orderId)
    } yield tPrice

    val terminal100 = price(new PosRepositoryInMemory)

    val result: Unit = terminal100.value.unsafeRunAsync {
      case Left(th) => logger.error(th.getMessage)
      case Right(vs) => vs match {
        case Left(asex) => logger.error(asex.message.toString())
        case Right(vals) =>
          val printablePrice = vals.bigDecimal.setScale(AMOUNTSCALE, ROUND_HALF_UP).toString
          logger.info(s" Total price of the items scanned $printablePrice")
      }
    }
  }
}
