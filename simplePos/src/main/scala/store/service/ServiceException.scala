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

import cats.data.NonEmptyList

trait ServiceException {
  def message: NonEmptyList[String]
}

case class SaleItemDoesNotExist(itemCode: String) extends ServiceException {
  val message: NonEmptyList[String] = NonEmptyList.of(s"Item with item code : $itemCode does not exist ")
}

case class ProductNotFound(productCode: String, caller: String) extends ServiceException {
  val message: NonEmptyList[String] = NonEmptyList.of(s"Product with product code  : $productCode does not exist while $caller")
}

case class OrderNotFound(productCode: String) extends ServiceException {
  val message: NonEmptyList[String] = NonEmptyList.of(s"Product with product code  : $productCode does not exist")
}

case class MiscellaneousDomainExceptions(msgs: NonEmptyList[String]) extends ServiceException {
  val message: NonEmptyList[String] = msgs
}

