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


package store.repo

import java.util.Date

import cats.effect.IO
import java.util.Date

import cats._
import cats.data._
import cats.instances.all._
import store.Common.ErrorOr
import store.model.{PosProduct, SaleItem}

trait PosRepository {
  type RepoOperation[A] = IO[ErrorOr[A]]

  def queryProduct(no: String): RepoOperation[Option[PosProduct]]

  def upsertProduct(a: PosProduct): RepoOperation[PosProduct]


  def queryOrder(orderId: String): RepoOperation[Option[Seq[SaleItem]]]
 
  def itemCountByOrder(orderId: String, productCode: String): RepoOperation[Int]

  def upsertOrder(orderId: String, saleItems: SaleItem): RepoOperation[SaleItem]
}
