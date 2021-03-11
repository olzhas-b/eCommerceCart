package repository

import java.time.Instant
import akka.actor.typed.{ActorRef, Behavior, SupervisorStrategy}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior, RetentionCriteria}
import model.{Product, Category, User}
import node.Node
import node.Node.Command
import serialize.CborSerializable

import scala.concurrent.duration.DurationInt

object UserAccount {

  final case class State(users: Map[String, User], products: Map[String, Product], cats: Map[String, Category], checkoutDate: Option[Instant]) extends CborSerializable {
    def isCheckedOut: Boolean =
      checkoutDate.isDefined

    def hasUser(itemId: String): Boolean =
      users.contains(itemId)

    def isEmptyUser: Boolean =
      users.isEmpty
    def getUserById(id: String): User ={
      users(id)
    }
    def addToCart(productId:String, user: User): User ={
      var newProductList = user.products :+ getProductById(productId)
      var changedUser = user.copy(products=newProductList)
      changedUser
    }
    def deleteFromCart(productId:String, user: User): User ={
       var newProductList = user.products.filter(product => (product.id != productId))
       var changedUser = user.copy(products=newProductList)
       changedUser
    }
    def updateUser(itemId: String, user: User): State = {
      hasUser(itemId) match {
        case true =>
          copy(users = users - itemId)
          copy(users = users + (itemId -> user))
        case false =>
          copy(users = users + (user.id -> user))
      }
    }

    def removeUser(itemId: String): State =
      copy(users = users - itemId)
//--------------------------------------------------------CATEGORY------------------------------------------------------------
  def hasCategory(itemId: String): Boolean =
    cats.contains(itemId)
  def toSummaryCat(id: String): SummaryCategory =
    SummaryCategory(cats.get(id), isCheckedOut)
  def toSummaryCats: SummaryCategories =
    SummaryCategories(cats, isCheckedOut)

  def updateCategory(itemId: String, cat: Category): State = {
    hasCategory(itemId) match {
      case true =>
        copy(cats = cats - itemId)
        copy(cats = cats + (itemId -> cat))
      case false =>
        copy(cats = cats + (cat.id -> cat))

    }
  }
  def removeCategory(itemId: String): State =
    copy(cats = cats - itemId)

//
    def checkout(now: Instant): State =
      copy(checkoutDate = Some(now))

    def toSummaryUsers: SummaryUsers =
      SummaryUsers(users, isCheckedOut)

    def toSummaryUser(id: String): SummaryUser =
      SummaryUser(users.get(id), isCheckedOut)

//--------------------------------------------------------Product------------------------------------------------------------
    def hasProduct(itemId: String): Boolean =
      products.contains(itemId)

    def isEmptyProducts: Boolean =
      products.isEmpty
    def getProductById(productId: String): Product = {
      products(productId)
    }
    def updateProduct(itemId: String, product: Product): State = {
      hasProduct(itemId) match {
        case true =>
          copy(products = products - itemId)
          copy(products = products + (itemId -> product))
        case false =>
          copy(products = products + (product.id -> product))
      }
    }

    def removeProduct(itemId: String): State =
      copy(products = products - itemId)
    def searchProductByName(productName: String): SummaryProduct = {
      val default = (-1,"")
      val id = products.find(_._2.name==productName).getOrElse(default)._1
      println("ID: " + id.toString)
      toSummaryProduct(id.toString)
    }
    def toSummaryProducts: SummaryProducts =
      SummaryProducts(products, isCheckedOut)

    def toSummaryProduct(id: String): SummaryProduct =
      SummaryProduct(products.get(id), isCheckedOut)
  }

  object State {
    val empty: State = State(users = Map.empty, products = Map.empty, cats = Map.empty, checkoutDate = None)
  }
//USER ACC
  final case class AddUser(userAccount: User, replyTo: ActorRef[Node.Command]) extends Command

  final case class RemoveUser(token: String, itemId: String, replyTo: ActorRef[Node.Command]) extends Command

  final case class AdjustUser(itemId: String, userAccount: User, replyTo: ActorRef[Node.Command]) extends Command

  final case class AddToCart(productId: String, userId:String, userToken: String, replyTo: ActorRef[Node.Command]) extends Command

  final case class DeleteFromCart(productId: String, userId:String, userToken: String, replyTo: ActorRef[Node.Command]) extends Command

  final case class GetUser(token: String, id: String, replyTo: ActorRef[Node.Command]) extends Command

  final case class GetUsers(token: String, replyTo: ActorRef[Node.Command]) extends Command

  final case class SummaryUsers(users: Map[String, User], checkedOut: Boolean) extends Command

  final case class SummaryUser(user: Option[User], checkedOut: Boolean) extends Command

  final case class GetBasket(token: String, userId: String, replyTo: ActorRef[Node.Command]) extends Command

  //--------------------------------------------------------Product------------------------------------------------------------
  final case class AddProduct(token:String, product: Product, replyTo: ActorRef[Node.Command]) extends Command

  final case class RemoveProduct(itemId: String, replyTo: ActorRef[Node.Command]) extends Command

  final case class AdjustProduct(itemId: String, product: Product, replyTo: ActorRef[Node.Command]) extends Command

  final case class UpdateProduct(token: String, newProduct: Product, replyTo: ActorRef[Node.Command]) extends Command

  final case class GetProduct(id: String, replyTo: ActorRef[Node.Command]) extends Command

  final case class GetProducts(replyTo: ActorRef[Node.Command]) extends Command

  final case class SearchProductByName(productName:String, replyTo: ActorRef[Node.Command]) extends Command
  //--------------------------------------------------------CATEGORY------------------------------------------------------------
  final case class AddCategory(cat: Category, replyTo: ActorRef[Node.Command]) extends Command

  final case class RemoveCategory(itemId: String, replyTo: ActorRef[Node.Command]) extends Command

  final case class GetCategory(id: String, replyTo: ActorRef[Node.Command]) extends Command

  final case class GetCategories(replyTo: ActorRef[Node.Command]) extends Command

  final case class UpdateCategory(token: String, newCategory: Category, replyTo: ActorRef[Node.Command]) extends Command




  final case class SummaryProducts(products: Map[String, Product], checkedOut: Boolean) extends Command

  final case class SummaryProduct(product: Option[Product], checkedOut: Boolean) extends Command

  final case class SummaryCategory(category: Option[Category], checkedOut: Boolean) extends Command

  final case class SummaryCategories(cats: Map[String, Category], checkedOut: Boolean) extends Command

//
  final case class GetToken(email: String, password: String, replyTo: ActorRef[Node.Command]) extends Command

  final case class Checkout(replyTo: ActorRef[Node.Command]) extends Command

  final case class CheckoutProduct(replyTo: ActorRef[Node.Command]) extends Command

  sealed trait Event extends CborSerializable {
    def cartId: String
  }
  //--------------------------------------------------------Product------------------------------------------------------------
  final case class UserAdded(cartId: String, itemId: String, userAccount: User) extends Event

  final case class UserRemoved(cartId: String, itemId: String) extends Event

  final case class UserAdjusted(cartId: String, itemId: String, userAccount: User) extends Event

  final case class UserCartUpdated(cartId: String, productId: String, userId: String) extends Event

  final case class UserCartDeleteProduct(cartId: String, productId: String, userId: String) extends Event
//
  final case class ProductAdded(cartId: String, itemId: String, product: Product) extends Event

  final case class ProductRemoved(cartId: String, itemId: String) extends Event

  final case class ProductAdjusted(cartId: String, itemId: String, product: Product) extends Event
//
  final case class CategoryAdded(cartId: String, itemId: String, cat: Category) extends Event

  final case class CategoryRemoved(cartId: String, itemId: String) extends Event

  final case class CategoryAdjusted(cartId: String, itemId: String, cat: Category) extends Event
//
  final case class CheckedOut(cartId: String, eventTime: Instant) extends Event

  def apply(cartId: String): Behavior[Command] = {
    EventSourcedBehavior[Command, Event, State](
      PersistenceId("ShoppingCart", cartId),
      State.empty,
      (state, command) =>
        if (state.isCheckedOut) checkedOutShoppingCart(cartId, state, command)
        else openShoppingCart(cartId, state, command),
      (state, event) => handleEvent(state, event))
      .withRetention(RetentionCriteria.snapshotEvery(numberOfEvents = 100, keepNSnapshots = 3))
      .onPersistFailure(SupervisorStrategy.restartWithBackoff(200.millis, 5.seconds, 0.1))
  }

  private def openShoppingCart(cartId: String, state: State, command: Command): Effect[Event, State] =
    command match {
      case GetUsers(token, replyTo) =>
        if (token == "admin")
          replyTo ! Node.Success(state.toSummaryUsers)
        Effect.none


      case AddUser(userAccount, replyTo) =>
        if (state.hasUser(userAccount.id)) {
          replyTo ! Node.Error(s"Item '${userAccount.id}' was already added")
          Effect.none
        } else if (userAccount.email == "" || userAccount.password == "") {
          replyTo ! Node.Error("Name or Pass mustn't be empty")
          Effect.none
        } else {
          Effect
            .persist(UserAdded(cartId, userAccount.id, userAccount))
            .thenRun(updatedCart => replyTo ! Node.SuccessUser(updatedCart.toSummaryUser(userAccount.id)))
        }
      case AddToCart(productId, userId, userToken, replyTo) =>

        if (state.hasUser(userId)) {
          if("TOKEN" + userId == userToken || state.hasProduct(productId)){
            Effect
              .persist(UserCartUpdated(cartId, productId, userId))
              .thenRun(updatedCart => replyTo ! Node.SuccessUser(updatedCart.toSummaryUser(userId)))
          }else{
            replyTo ! Node.Error(s"Cant add product to cart, token expired or product not found")
            Effect.none
          }
        } else {
          replyTo ! Node.Error(s"Cant find user with id: '${userId}'")
          Effect.none
        }
      case DeleteFromCart(productId, userId, userToken, replyTo) =>
         if (state.hasUser(userId)) {
            if("TOKEN" + userId == userToken || state.hasProduct(productId)){
               Effect
                  .persist(UserCartDeleteProduct(cartId, productId, userId))
                  .thenRun(updatedCart => replyTo ! Node.SuccessUser(updatedCart.toSummaryUser(userId)))
            }else{
               replyTo ! Node.Error(s"Cant add product to cart, token expired or product not found")
               Effect.none
            }
         } else {
            replyTo ! Node.Error(s"Cant find user with id: '${userId}'")
            Effect.none
         }


      case RemoveUser(token, itemId, replyTo) =>
        if (state.hasUser(itemId) && token == "TOKEN" + itemId) {
          Effect.persist(UserRemoved(cartId, itemId)).thenRun(updatedCart => replyTo ! Node.SuccessUser(updatedCart.toSummaryUser(itemId)))
        } else {
          replyTo ! Node.Success(state.toSummaryUsers)
          Effect.none
        }
      case AdjustUser(itemId, userAccount, replyTo) =>
        if (userAccount.email == "" || userAccount.password == "") {
          replyTo ! Node.Error("Name or Pass mustn't be empty")
          Effect.none
        } else if (state.hasUser(itemId)) {
          Effect
            .persist(UserAdjusted(cartId, itemId, userAccount))
            .thenRun(updatedCart => replyTo ! Node.Success(updatedCart.toSummaryUsers))
        } else {
          replyTo ! Node.Error(s"Cannot adjust quantity for item '$itemId'. Item not present on cart")
          Effect.none
        }
//        --------------------------------------------------------------------------------------------------------------
      case Checkout(replyTo) =>
        if (state.isEmptyUser) {
          replyTo ! Node.Error("Cannot checkout an empty shopping cart")
          Effect.none
        } else {
          Effect
            .persist(CheckedOut(cartId, Instant.now()))
            .thenRun(updatedCart => replyTo ! Node.Success(updatedCart.toSummaryUsers))
        }
      case GetUser(token, id, replyTo) =>
        if (token == "TOKEN" + id && state.hasUser(id))
          replyTo ! Node.SuccessUser(state.toSummaryUser(id))
        Effect.none
//        --------------------------------------------Product--------------------------------------------------------------

      case GetProduct(id, replyTo) =>
        if (state.hasProduct(id))
          replyTo ! Node.SuccessProduct(state.toSummaryProduct(id))
        Effect.none
      case GetProducts(replyTo) =>
        replyTo ! Node.SuccessProducts(state.toSummaryProducts)
        Effect.none
      case SearchProductByName(productName, replyTo) =>
        replyTo ! Node.SuccessProduct(state.searchProductByName(productName))
        Effect.none
      case AddProduct(token, product, replyTo) =>
        if(token =="admin"){
          if (state.hasProduct(product.id)) {
            replyTo ! Node.Error(s"Item '${product.id}' was already added")
            Effect.none
          } else if (product.name == "" || product.description == "" || product.categoryId == "") {
            replyTo ! Node.Error("Name or description or category mustn't be empty")
            Effect.none
          } else if(state.hasCategory(product.categoryId)) {
            Effect
              .persist(ProductAdded(cartId, product.id, product))
              .thenRun(updatedCart => replyTo ! Node.SuccessProduct(updatedCart.toSummaryProduct(product.id)))
          }else
          {
            replyTo ! Node.Error("Category for product not found!")
            Effect.none
          }
        }else{
          replyTo ! Node.Error("Access denied!")
          Effect.none
        }

      case RemoveProduct(itemId, replyTo) =>
        if (state.hasProduct(itemId)) {
          Effect.persist(
            ProductRemoved(cartId, itemId)
          ).thenRun(updatedCart => replyTo ! Node.SuccessProduct(updatedCart.toSummaryProduct(itemId)))
        } else {
          replyTo ! Node.SuccessProducts(state.toSummaryProducts) // removing an item is idempotent
          Effect.none
        }
      case AdjustProduct(itemId, product, replyTo) =>
        if (product.name == "" && product.description == "") {
          replyTo ! Node.Error("Name or description mustn't be empty")
          Effect.none
        } else if (state.hasProduct(itemId)) {
          Effect
            .persist(ProductAdjusted(cartId, itemId, product))
            .thenRun(updatedCart => replyTo ! Node.SuccessProducts(updatedCart.toSummaryProducts))
        } else {
          replyTo ! Node.Error(s"Cannot adjust quantity for item '$itemId'. Item not present on cart")
          Effect.none
        }
      case UpdateProduct(token, product, replyTo) =>
        if (state.hasProduct(product.id) && token == "admin") {
          if (product.name == "" || product.description == "" || product.categoryId == "") {
            replyTo ! Node.Error("Name or description mustn't be empty")
            Effect.none
          }else if(state.hasCategory(product.categoryId)) {
            Effect
              .persist(ProductAdjusted(cartId, product.id, product))
              .thenRun(updatedCart => replyTo ! Node.SuccessProduct(updatedCart.toSummaryProduct(product.id)))
        } else{
          replyTo ! Node.Error("There's no such category or admin token expired")
          Effect.none
        }
        }else{
          replyTo ! Node.Error("Access denied!")
          Effect.none
        }
//        ------------------------------------------------------CATEGORY------------------------------------------------

      case GetCategory(id, replyTo) =>
        if (state.hasCategory(id))
          replyTo ! Node.SuccessCategory(state.toSummaryCat(id))
        Effect.none
      case GetCategories(replyTo) =>
        replyTo ! Node.SuccessCategories(state.toSummaryCats)
        Effect.none
      case UpdateCategory(token, cat, replyTo) =>
        if (state.hasCategory(cat.id) && token == "admin") {
          if (cat.name == "" || cat.description == "") {
            replyTo ! Node.Error("Name or description mustn't be empty")
            Effect.none
          }else{
            Effect
              .persist(CategoryAdjusted(cartId, cat.id, cat))
              .thenRun(updatedCart => replyTo ! Node.SuccessCategory(updatedCart.toSummaryCat(cat.id)))
          }
        } else{
          replyTo ! Node.Error("There's no such category or admin token expired")
          Effect.none
        }

      case AddCategory(cat, replyTo) =>
        if (state.hasCategory(cat.id)) {
          replyTo ! Node.Error(s"Item '${cat.id}' was already added")
          Effect.none
        } else if (cat.name == "" && cat.description == "") {
          replyTo ! Node.Error("Name or description mustn't be empty")
          Effect.none
        } else {
          Effect
            .persist(CategoryAdded(cartId, cat.id, cat))
            .thenRun(updatedCart => replyTo ! Node.SuccessCategory(updatedCart.toSummaryCat(cat.id)))
        }
      case RemoveCategory(itemId, replyTo) =>
        if (state.hasCategory(itemId)) {
          Effect.persist(
            CategoryRemoved(cartId, itemId)
          ).thenRun(updatedCart => replyTo ! Node.SuccessCategory(updatedCart.toSummaryCat(itemId)))
        } else {
          replyTo ! Node.SuccessCategories(state.toSummaryCats)
          Effect.none
        }






      case CheckoutProduct(replyTo) =>
        if (state.isEmptyProducts) {
          replyTo ! Node.Error("Cannot checkout an empty product cart")
          Effect.none
        } else {
          Effect
            .persist(CheckedOut(cartId, Instant.now()))
            .thenRun(updatedCart => replyTo ! Node.SuccessProducts(updatedCart.toSummaryProducts))
        }
      case GetToken(email, password, replyTo) =>
        state.users.foreach(u => {
          if (u._2.email == email && u._2.password == password)
            replyTo ! Node.Token("TOKEN" + u._1)
        })
        Effect.none
    }

  private def checkedOutShoppingCart(cartId: String, state: State, command: Command): Effect[Event, State] =
    command match {
      case GetUser(id, token, replyTo) =>
        if (token == "TOKEN" + id && state.hasUser(id))
          replyTo ! Node.SuccessUser(state.toSummaryUser(id))
        Effect.none
      case cmd: AddUser =>
        cmd.replyTo ! Node.Error("Can't add an item to an already checked out account")
        Effect.none
      case cmd: AddToCart =>
        cmd.replyTo ! Node.Error("add to cart error IDK")
        Effect.none
      case cmd: RemoveUser =>
        cmd.replyTo ! Node.Error("Can't remove an item from an already checked out account")
        Effect.none
      case cmd: AdjustUser =>
        cmd.replyTo ! Node.Error("Can't adjust item on an already checked out account")
        Effect.none
      case cmd: Checkout =>
        cmd.replyTo ! Node.Error("Can't checkout already checked out account")
        Effect.none
      case GetProduct(id, replyTo) =>
        if (state.hasProduct(id))
          replyTo ! Node.SuccessProduct(state.toSummaryProduct(id))
        Effect.none
      case GetProducts(replyTo) =>
        replyTo ! Node.SuccessProducts(state.toSummaryProducts)
        Effect.none
      case cmd: AddProduct =>
        cmd.replyTo ! Node.Error("Can't add an item to an already checked out account")
        Effect.none

      case cmd: RemoveProduct =>
        cmd.replyTo ! Node.Error("Can't remove an item from an already checked out account")
        Effect.none
      case cmd: AdjustProduct =>
        cmd.replyTo ! Node.Error("Can't adjust item on an already checked out account")
        Effect.none
      case cmd: CheckoutProduct =>
        cmd.replyTo ! Node.Error("Can't checkout already checked out account")
        Effect.none
    }

  private def handleEvent(state: State, event: Event) = {
    event match {
      case CheckedOut(_, eventTime) => state.checkout(eventTime)
      //--------------------------------------------------------USERACCOUNT------------------------------------------------------------
      case UserAdded(_, itemId, quantity) => state.updateUser(itemId, quantity)

      case UserRemoved(_, itemId) => state.removeUser(itemId)
      case UserAdjusted(_, itemId, quantity) => state.updateUser(itemId, quantity)
      case UserCartUpdated(_, productId, userId) => state.updateUser(userId, state.addToCart(productId, state.getUserById(userId)))
      case UserCartDeleteProduct(_, productId, userId) => state.updateUser(userId, state.deleteFromCart(productId, state.getUserById(userId)))

      //--------------------------------------------------------PRODUCT------------------------------------------------------------
      case ProductAdded(_, itemId, quantity) => state.updateProduct(itemId, quantity)
      case ProductRemoved(_, itemId) => state.removeProduct(itemId)
      case ProductAdjusted(_, itemId, quantity) =>state.updateProduct(itemId, quantity)
      //--------------------------------------------------------CATEGORY------------------------------------------------------------
      case CategoryAdded(_, itemId, quantity) => state.updateCategory(itemId, quantity)
      case CategoryRemoved(_, itemId) => state.removeCategory(itemId)
      case CategoryAdjusted(_, itemId, quantity) =>state.updateCategory(itemId, quantity)
    }
  }
}
