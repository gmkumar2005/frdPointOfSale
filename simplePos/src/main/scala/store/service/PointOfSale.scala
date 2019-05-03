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

package store.service


trait PointOfSale[Commodity, SaleItem, Discount, Amount] {
  def upsertProduct(productCode: String, description: Option[String], price: Amount, discount: Option[Discount]): POSOperation[Commodity]

  def createFreshOrder: POSOperation[String]

  def scanItem(itemCode: String, orderId: String): POSOperation[SaleItem]


  def subTotal(orderId: String): POSOperation[BigDecimal]

  def totalPrice(orderId: String): POSOperation[BigDecimal] = for {
    subTotal <- subTotal(orderId)
  } yield subTotal

}