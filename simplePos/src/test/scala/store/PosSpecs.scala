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

package store

import cats.effect.IO
import org.scalatest.{FeatureSpec, GivenWhenThen, Matchers}
import pureconfig.module.catseffect.loadConfigF
import store.model.{DiscountByVolume, PosProduct, SaleItem}
import store.repo.interpreter.PosRepositoryInMemory
import store.service.interpreter.PointOfSale.{createFreshOrder, scanItem, subTotal, totalPrice, upsertProduct}

class PosSpecs extends FeatureSpec with GivenWhenThen with Matchers {
  info("As a user I should be able to add products to the point of sale terminal")
  info("then calculate the total price and apply volume based discount on  items scanned by the terminal")
  feature("Point of sale") {
    scenario("Step 1 : Add products to the pos") {
      Given("An empty pos")
      import store.service.interpreter.PointOfSale._
      When("the user adds a product the pos")
      val commodities = for {
        product <- upsertProduct("A", Some("Apples"), BigDecimal(2.0),
          Some(DiscountByVolume(4, BigDecimal(7))))
      } yield product
      val terminal1 = commodities(new PosRepositoryInMemory)

      Then("the pos should contain one product")
      val expectedProduct = PosProduct("A", Some("Apples"), BigDecimal(2.0), Some(DiscountByVolume(4, BigDecimal(7))))

      terminal1.value.unsafeRunAsync {
        case Left(th) => fail(th.getMessage)
        case Right(vs) => vs match {
          case Left(ex) => ex.message
          case Right(actualResult) => expectedProduct shouldEqual actualResult
        }
      }
    }
    scenario("Step 2 : Create a fresh order") {
      Given("An empty pos")
      import store.service.interpreter.PointOfSale._
      When("the user creates a fresh order")
      val orderIdPrep = for {
        freshOrder <- createFreshOrder
      } yield freshOrder
      val terminal2 = orderIdPrep(new PosRepositoryInMemory)
      Then("new order with unique orderid should be created")
      terminal2.value.unsafeRunAsync {
        case Left(th) => fail(th.getMessage)
        case Right(vs) => vs match {
          case Left(ex) => ex.message
          case Right(orderId) => {
            orderId should not be empty
            orderId.toString.length shouldEqual 36
          }
        }
      }
    }

    scenario("Step 3 : Scan an item ") {
      Given("A pos with one product \"A\" $2 each or 4 for $7 ")
      import store.service.interpreter.PointOfSale._
      val commodities = for {
        product <- upsertProduct("A", Some("Apples"), BigDecimal(2.0), Some(DiscountByVolume(4, BigDecimal(7))))
      } yield product
      val orderIdPrep = for {
        freshOrder <- createFreshOrder
      } yield freshOrder

      When("the user scans first item")
      val purchaseOrderPrep = for {
        _ <- commodities
        orderId <- orderIdPrep
        saleItem <- scanItem("A", orderId)
      } yield saleItem
      val terminal3 = purchaseOrderPrep.run(new PosRepositoryInMemory)
      Then("Fresh order should contain one sale item")
      terminal3.value.unsafeRunAsync {
        case Left(th) => fail(th.getMessage)
        case Right(vs) => vs match {
          case Left(ex) => fail(ex.message.toString())
          case Right(saleItem) =>
            saleItem shouldEqual SaleItem("A", Some("Apples"), BigDecimal(2.0), Some(DiscountByVolume(4, BigDecimal(7))), 1)
        }
      }

      When("second sale item is added to an order")
      val orderRepo = new PosRepositoryInMemory
      val scanPrep = for {
        _ <- orderRepo.upsertOrder("1", SaleItem("A", Some("Apples"), BigDecimal(2.0), Some(DiscountByVolume(4, BigDecimal(7.0))), 1))
        _ <- orderRepo.upsertOrder("1", SaleItem("A", Some("Apples"), BigDecimal(2.0), Some(DiscountByVolume(4, BigDecimal(7.0))), 2))
        items <- orderRepo.queryOrder("1")
      } yield items
      Then("the quantity of item in the order should be two")
      val itemCountPrep = scanPrep.unsafeRunSync()
      itemCountPrep match {
        case Left(err) => fail("Error in fetching items root cause :" + err.toString())
        case Right(oSaleItem) => {
          oSaleItem match {
            case Some(saleItem1) => saleItem1.size shouldEqual 1
            case _ => fail("Error in fetching zero items found:")
          }
        }
      }

      When("the user scans a item twice")
      val purchaseOrderPrep2 = for {
        _ <- commodities
        orderId <- orderIdPrep
        _ <- scanItem("A", orderId)
        saleItem <- scanItem("A", orderId)
      } yield saleItem
      val terminal4 = purchaseOrderPrep2(new PosRepositoryInMemory)
      Then("Fresh order should contain two sale items")
      terminal4.value.unsafeRunAsync {
        case Left(th) => fail("Unkown Error " + th.getMessage)
        case Right(vs) => vs match {
          case Left(ex) => fail(ex.message.toString())
          case Right(saleItem2) => {
            saleItem2 shouldEqual SaleItem("A", Some("Apples"), BigDecimal(2.0), Some(DiscountByVolume(4, BigDecimal(7.0))), 2)
          }
        }
      }
    }

    scenario("Step 4 : Scan two items ") {
      Given("A pos with two  products  \"A\" $2 each or 4 for $7  AND \"B\" $12 no discount")
      val commodities = for {
        product <- upsertProduct("A", Some("Apples"), BigDecimal(2.0), Some(DiscountByVolume(4, BigDecimal(7.0))))
        product <- upsertProduct("B", Some("Banana"), BigDecimal(12.0), None)
      } yield product
      val orderIdPrep = for {
        freshOrder <- createFreshOrder
      } yield freshOrder
      When("the user scans second item")
      val purchaseOrderPrep = for {
        _ <- commodities
        orderId <- orderIdPrep
        saleItem <- scanItem("B", orderId)
      } yield saleItem
      val purchaseOrderIO = purchaseOrderPrep.run(new PosRepositoryInMemory)
      Then("Fresh order should contain one sale item")
      purchaseOrderIO.value.unsafeRunAsync {
        case Left(th) => fail(th.getMessage)
        case Right(vs) => vs match {
          case Left(ex) => fail("Error " + ex.message)
          case Right(saleItem4) => saleItem4 shouldEqual SaleItem("B", Some("Banana"), BigDecimal(12.0), None, 1)

        }
      }

      When("the user scans a item twice")
      val purchaseOrderPrep2 = for {
        _ <- commodities
        orderId <- orderIdPrep
        _ <- scanItem("A", orderId)
        _ <- scanItem("A", orderId)
        _ <- scanItem("B", orderId)
        saleItem <- scanItem("B", orderId)
      } yield saleItem
      val purchaseOrderIO2 = purchaseOrderPrep2(new PosRepositoryInMemory)
      Then("Fresh order should contain two sale items")
      purchaseOrderIO2.value.unsafeRunAsync {
        case Left(th) => fail("Unkown Error " + th.getMessage)
        case Right(vs) => vs match {
          case Left(ex) => fail("Error " + ex.message)
          case Right(saleItem5) => saleItem5 shouldEqual SaleItem("B", Some("Banana"), BigDecimal(12.0), None, 2)
        }
      }

      When("the user scans a wrong item")
      val purchaseOrderPrep3 = for {
        _ <- commodities
        orderId <- orderIdPrep
        saleItem <- scanItem("X", orderId)
      } yield saleItem
      val purchaseOrderIO3 = purchaseOrderPrep3(new PosRepositoryInMemory)
      Then("Scan should fail with an error:  ProductNotFound(X) ")
      purchaseOrderIO3.value.unsafeRunAsync {
        case Left(th) => fail("Unkown Error " + th.getMessage)
        case Right(vs) => vs match {
          case Left(ex) => ex.toString shouldEqual "ProductNotFound(X,scanning)"
          case Right(_) => fail("should return error")
        }
      }

    }

    scenario("Step 5 : Total price for single item ") {
      Given("A pos with one product \"A\" $2 each or 4 for $7 ")
      import store.service.interpreter.PointOfSale._
      val commodities = for {
        product <- upsertProduct("A", Some("Apples"), BigDecimal(2.0), Some(DiscountByVolume(4, BigDecimal(7))))
      } yield product
      val orderIdPrep = for {
        freshOrder <- createFreshOrder
      } yield freshOrder

      When("Item scanned once there is no discount total price is 2")
      val pricePrep = for {
        _ <- commodities
        freshOrder <- orderIdPrep
        _ <- scanItem("A", freshOrder)
        subtotal <- subTotal(freshOrder)
      } yield subtotal
      val terminal5 = pricePrep(new PosRepositoryInMemory)
      Then("Total price of one apple is 2")
      terminal5.value.unsafeRunAsync {
        case Left(th) => fail("Unkown Error " + th.getMessage)
        case Right(vs) => vs match {
          case Left(ex) => ex.message.toString shouldBe empty
          case Right(subTotal) => subTotal shouldEqual BigDecimal(2.0)
        }
      }
      When("Item A scanned for 5 times total price should be 11")
      val pricePrep2 = for {
        _ <- commodities
        freshOrder <- orderIdPrep
        _ <- scanItem("A", freshOrder)
        _ <- scanItem("A", freshOrder)
        _ <- scanItem("A", freshOrder)
        _ <- scanItem("A", freshOrder)
        _ <- scanItem("A", freshOrder)
        _ <- scanItem("A", freshOrder)
        subtotal <- subTotal(freshOrder)
      } yield subtotal
      val terminal6 = pricePrep2(new PosRepositoryInMemory)
      Then("Total price of one apple is 2")
      terminal6.value.unsafeRunAsync {
        case Left(th) => fail("Unkown Error " + th.getMessage)
        case Right(vs) => vs match {
          case Left(ex) => fail(ex.message.toString)
          case Right(subTotal) => subTotal shouldEqual BigDecimal(11.0)
        }
      }
    }

    scenario("Step 6 : Verify total price with business data") {
      Given("Products A B C D defined")
      val commoditiesPrep = for {
        _ <- upsertProduct("A", Some("Apples"), BigDecimal(2.0), Some(DiscountByVolume(4, BigDecimal(7))))
        _ <- upsertProduct("B", Some("Banana"), BigDecimal(12.0), None)
        _ <- upsertProduct("C", Some("Carrot"), BigDecimal(1.25), Some(DiscountByVolume(6, BigDecimal(6))))
        _ <- upsertProduct("D", Some("Dragon"), BigDecimal(0.15), None)
      } yield ()

      When("Scan these items in this order: ABCDABAA")
      val scanPrepA = for {
        uniqueOrder <- createFreshOrder
        _ <- scanItem("A", uniqueOrder)
        _ <- scanItem("B", uniqueOrder)
        _ <- scanItem("C", uniqueOrder)
        _ <- scanItem("D", uniqueOrder)
        _ <- scanItem("A", uniqueOrder)
        _ <- scanItem("B", uniqueOrder)
        _ <- scanItem("A", uniqueOrder)
        _ <- scanItem("A", uniqueOrder)
      } yield uniqueOrder

      val totalPricePrepA = for {
        _ <- commoditiesPrep
        orderId <- scanPrepA
        totalPrice <- totalPrice(orderId)
      } yield totalPrice

      Then("Verify the total price is $32.40.")
      val terminalS1 = totalPricePrepA(new PosRepositoryInMemory)
      terminalS1.value.unsafeRunAsync {
        case Left(th) => fail("Unkown Error " + th.getMessage)
        case Right(vs) => vs match {
          case Left(ex) => fail(ex.message.toString)
          case Right(totalPrice) => totalPrice shouldEqual BigDecimal(32.40)
        }
      }

      When("Scan these items in this order: CCCCCCC; ")
      val scanPrepB = for {
        uniqueOrder <- createFreshOrder
        _ <- scanItem("C", uniqueOrder)
        _ <- scanItem("C", uniqueOrder)
        _ <- scanItem("C", uniqueOrder)
        _ <- scanItem("C", uniqueOrder)
        _ <- scanItem("C", uniqueOrder)
        _ <- scanItem("C", uniqueOrder)
        _ <- scanItem("C", uniqueOrder)

      } yield uniqueOrder
      val totalPricePrepB = for {
        _ <- commoditiesPrep
        orderId <- scanPrepB
        totalPrice <- totalPrice(orderId)
      } yield totalPrice

      Then("Verify the total price is $7.25")
      val terminalS2 = totalPricePrepB(new PosRepositoryInMemory)
      terminalS2.value.unsafeRunAsync {
        case Left(th) => fail("Unkown Error " + th.getMessage)
        case Right(vs) => vs match {
          case Left(ex) => fail(ex.message.toString)
          case Right(totalPrice) => totalPrice shouldEqual BigDecimal(7.25)
        }
      }

      When("Scan these items in this order: ABCD;")
      val scanPrepC = for {
        uniqueOrder <- createFreshOrder
        _ <- scanItem("A", uniqueOrder)
        _ <- scanItem("B", uniqueOrder)
        _ <- scanItem("C", uniqueOrder)
        _ <- scanItem("D", uniqueOrder)
      } yield uniqueOrder
      val totalPricePrepC = for {
        _ <- commoditiesPrep
        orderId <- scanPrepC
        totalPrice <- totalPrice(orderId)
      } yield totalPrice

      Then("Verify the total price is $15.40.")
      val terminalS3 = totalPricePrepC(new PosRepositoryInMemory)
      terminalS3.value.unsafeRunAsync {
        case Left(th) => fail("Unkown Error " + th.getMessage)
        case Right(vs) => vs match {
          case Left(ex) => fail(ex.message.toString)
          case Right(totalPrice) => totalPrice shouldEqual BigDecimal(15.40)
        }
      }
    }
  }
}
