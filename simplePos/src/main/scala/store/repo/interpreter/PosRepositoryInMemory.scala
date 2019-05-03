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


package store.repo.interpreter

import cats.data.NonEmptyList
import cats.effect.IO
import store.Common.ErrorOr
import store.model.{PosProduct, SaleItem}
import store.repo.PosRepository

import scala.collection.mutable.{Map => MMap}
import org.slf4j.{Logger, LoggerFactory}

class PosRepositoryInMemory extends PosRepository {
  val logger: Logger = LoggerFactory.getLogger(this.getClass)
  lazy val productRepo: MMap[String, PosProduct] = MMap.empty[String, PosProduct]
  lazy val orderRepo: MMap[String, Seq[SaleItem]] = MMap.empty[String, Seq[SaleItem]]

  override def queryProduct(productCode: String): IO[ErrorOr[Option[PosProduct]]] = IO(Right(productRepo.get(productCode)))

  override def upsertProduct(posProduct: PosProduct): IO[ErrorOr[PosProduct]] = IO {
    productRepo.remove(posProduct.productCode)
    val _ = productRepo += ((posProduct.productCode, posProduct))
    Right(posProduct)
  }


  override def queryOrder(orderId: String): IO[ErrorOr[Option[Seq[SaleItem]]]] = IO(Right(orderRepo.get(orderId)))

  override def upsertOrder(orderId: String, saleItem: SaleItem): IO[ErrorOr[SaleItem]] = IO {
    val validOrder = orderRepo.get(orderId)
    val items = validOrder.getOrElse(Seq[SaleItem]())
    val originalRemoved = items.filterNot(elm => elm.productCode == saleItem.productCode)
    val updatedItems = originalRemoved :+ saleItem
    val _ = orderRepo += ((orderId, updatedItems))
    Right(saleItem)
  }


  override def itemCountByOrder(orderId: String, productCode: String): IO[ErrorOr[Int]] = IO {
    val iCount = for {
      availableItems <- orderRepo.get(orderId)
      validItem <- availableItems.find(i => i.productCode == productCode)
    } yield validItem.quantity
    Right(iCount.getOrElse(0))
  }
}
