# SimplePOS is a simple point of sale application
This project demonstrates functional reactive domain principles using cats IO

##General instructions to run
> sbt test

> sbt run


#### Minimum requirements
sbt.version=1.2.8

scalaVersion := "2.12.8"

jdk 1.8
##Features

- Scan sale items
- Calculate total price
- Define products 
- Define volume based discount
- Calculate total price and apply discount

#Problem statement

Consider a store where items have prices per unit but also volume prices. For example, apples may be $1.00 each or 4 for $3.00. Implement a point-of-sale scanning API that accepts an arbitrary ordering of products (similar to what would happen at a checkout line) and then returns the correct total price for an entire shopping cart based on the per unit prices or the volume prices as applicable.
Here are the products listed by code and the prices to use (there is no sales tax):

##Test Case

| Product Code   | Description |  Price |
|----------|:-------------|------:|
|A              | Apple     | $2 each or 4 for $7 |
| B             | Banana   |   $12 |
| C             | Carrot |    $1.25 each or $6 for a six-pack |
| D             | Dragon |    $.15 |

###Test scenarios
- Scan these items in this order: ABCDABAA; Verify the total price is $32.40.
- Scan these items in this order: CCCCCCC; Verify the total price is $7.25.
- Scan these items in this order: ABCD; Verify the total price is $15.40.

## The api
```scala
trait PointOfSale[Commodity, SaleItem, Discount, Amount] {
  def upsertProduct(productCode: String, description: Option[String], price: Amount, discount: Option[Discount]): POSOperation[Commodity]

  def createFreshOrder: POSOperation[String]

  def scanItem(itemCode: String, orderId: String): POSOperation[SaleItem]
 
  def subTotal(orderId: String): POSOperation[BigDecimal]

  def totalPrice(orderId: String): POSOperation[BigDecimal] = for {
    subTotal <- subTotal(orderId)
  } yield subTotal

}
```

## The usage

```scala

    val totalPriceWithDiscountPrep = for {
      _ <- upsertProduct("A", Some("Apples"), BigDecimal(2.0), Some(DiscountByVolume(4, BigDecimal(7))))
      orderId <- createFreshOrder
      _ <- scanItem("A", orderId)
      tPrice <- totalPrice(orderId)
    } yield tPrice
    
    val priceWithDiscount = totalPriceWithDiscountPrep(new PosRepositoryInMemory).value.unsafeRunSync
  }


```
