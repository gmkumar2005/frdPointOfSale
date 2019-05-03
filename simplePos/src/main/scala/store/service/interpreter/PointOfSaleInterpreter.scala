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

package store.service.interpreter

import java.util.UUID

import cats.data.{EitherT, Kleisli, _}
import org.slf4j.{Logger, LoggerFactory}
import store.Common._
import store.model.{Discount, DiscountByVolume, PosProduct, SaleItem}
import store.repo.PosRepository
import store.service._
import scala.math.Integral.Implicits._

class PointOfSaleInterpreter extends PointOfSale[PosProduct, SaleItem, Discount, Amount] {
  val logger: Logger = LoggerFactory.getLogger(this.getClass)

  override def upsertProduct(productCode: String, description: Option[String], price: Amount,
                             discount: Option[Discount]): POSOperation[PosProduct] = Kleisli[Valid, PosRepository, PosProduct] {
    repo: PosRepository =>
      EitherT {
        repo.upsertProduct(PosProduct(productCode, description, price, discount)).map {
          case Left(errs) => Left(MiscellaneousDomainExceptions(errs))
          case Right(updatedProduct) => Right(updatedProduct)
        }
      }
  }

  override def createFreshOrder: POSOperation[String] = Kleisli[Valid, PosRepository, String] {
    _ =>
      val orderId: Either[ServiceException, String] = Right(UUID.randomUUID().toString)
      EitherT.fromEither(orderId)
  }


  override def scanItem(itemCode: String, orderId: String): POSOperation[SaleItem] = Kleisli[Valid, PosRepository, SaleItem] {
    repo: PosRepository =>
      logger.debug(s"Started Scanning Item: $itemCode for Order: $orderId")
      EitherT {
        val scannedItem = for {
          pProduct <- EitherT {
            repo.queryProduct(itemCode).map {
              case Right(oposProduct) => oposProduct match {
                case Some(posProduct) => Right(posProduct)
                case _ => Left(ProductNotFound(itemCode, "scanning"))
              }
              case Left(fetchingError) => Left(MiscellaneousDomainExceptions(
                NonEmptyList.one(s"Error while fetching product with  $itemCode with errors : $fetchingError")))
            }
          }

          currentItemCount <- EitherT {
            val itemCountByOrder = repo.itemCountByOrder(orderId, pProduct.productCode)
            val itemCountResult = itemCountByOrder.map {
              case Right(itemCount) =>
                val saleResult: Either[ServiceException, Int] = Right(itemCount)
                saleResult
              case Left(countingErr) => Left(MiscellaneousDomainExceptions(
                NonEmptyList.one(s"Error while counting order $orderId with item code: $itemCode with message: $countingErr")))
            }
            itemCountResult
          }

          freshSaleItem <- EitherT {
            val saleItem = SaleItem(pProduct.productCode, pProduct.description, pProduct.price, pProduct.discount, currentItemCount + 1)
            val updateResult = repo.upsertOrder(orderId, saleItem)
            updateResult.map {
              case Right(item) => val saleResult: Either[ServiceException, SaleItem] = Right(item)
                saleResult
              case Left(err) => val saleResult: Either[ServiceException, SaleItem] = Left(MiscellaneousDomainExceptions(err))
                saleResult
            }
          }

        } yield freshSaleItem

        scannedItem.value
      }

  }


  def priceAfterDiscount(item: SaleItem): BigDecimal = {
    val oDiscount = item.discount
    val totalPriceAfterDiscount = oDiscount match {
      case Some(d) => {
        d match {
          case dv: DiscountByVolume => {
            val (quotient, remainder) = item.quantity /% dv.unitCount
            val volumePrice = dv.amount * quotient
            val priceOverVolume = item.price * remainder
            volumePrice + priceOverVolume
          }
          case _ => item.price * item.quantity
        }
      }
      case _ => item.price * item.quantity
    }
    totalPriceAfterDiscount
  }

  override def subTotal(orderId: String): POSOperation[BigDecimal] = Kleisli[Valid, PosRepository, BigDecimal] {
    repo: PosRepository =>
      logger.debug(s"Start calculating subtotal")
      EitherT {
        val subTotalPrep = for {
          orderInfo <- EitherT {
            repo.queryOrder(orderId).map {
              case Right(oOrder) => {
                val foldResult = oOrder match {
                  case Some(items) =>
                    logger.debug(s"Calculating subtotal for $items")
                    Right(items.foldLeft(BigDecimal(0))((acc: BigDecimal, b: SaleItem) => acc + priceAfterDiscount(b)))
                  case _ => Left(OrderNotFound(orderId))
                }
                foldResult
              }
              case Left(err) => val orderResult: Either[ServiceException, BigDecimal] = Left(MiscellaneousDomainExceptions(err))
                orderResult
            }
          }
        } yield orderInfo
        subTotalPrep.value
      }
  }

}

object PointOfSale extends PointOfSaleInterpreter