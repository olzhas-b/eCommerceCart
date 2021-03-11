package node

import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, Scheduler}
import akka.util.Timeout
import model.{Product, Category, User}
import repository.UserAccount
import serialize.CborSerializable

import scala.concurrent.duration.DurationInt

object Node {
  val NodeServiceKey: ServiceKey[Command] = ServiceKey[Command]("node-service-key")

  trait Command extends CborSerializable

  case class AddItem(userAccount: User, replyTo: ActorRef[Command]) extends Command

  case class Token(token: String) extends Command

  case class GetToken(email: String, password: String, replyTo: ActorRef[Command]) extends Command

  case class Check(userToken: String, replyTo: ActorRef[Command]) extends Command

  case class Checked(status: String, userToken: String) extends Command

  case class Error(status: String) extends Command
  //--------------------------------------------------------PRODUCT------------------------------------------------------------
  case class Success(result: UserAccount.SummaryUsers) extends Command

  case class SuccessUser(result: UserAccount.SummaryUser) extends Command

  case class SuccessProducts(result: UserAccount.SummaryProducts) extends Command

  case class SuccessProduct(result: UserAccount.SummaryProduct) extends Command

  //--------------------------------------------------------CATEGORY------------------------------------------------------------
  case class SuccessCategory(result: UserAccount.SummaryCategory) extends Command

  case class SuccessCategories(result: UserAccount.SummaryCategories) extends Command

  //--------------------------------------------------------PRODUCT------------------------------------------------------------
  case class DeleteProduct(id: String, replyTo: ActorRef[Command]) extends Command

  case class GetProduct(id: String, replyTo: ActorRef[Command]) extends Command

  case class FindProduct(productName: String, replyTo: ActorRef[Command]) extends Command

  case class GetProducts(replyTo: ActorRef[Command]) extends Command

  case class NewProduct(token: String, newProduct: Product, replyTo: ActorRef[Command]) extends Command

  case class UpdateProduct(token: String, newProduct: Product, replyTo: ActorRef[Command]) extends Command

  //--------------------------------------------------------CATEGORY------------------------------------------------------------
  case class DeleteCategory(id: String, replyTo: ActorRef[Command]) extends Command

  case class GetCategory(id: String, replyTo: ActorRef[Command]) extends Command

  case class GetCategories(replyTo: ActorRef[Command]) extends Command

  case class NewCategory(token: String, newCategory: Category, replyTo: ActorRef[Command]) extends Command

  case class UpdateCategory(token: String, newCat: Category, replyTo: ActorRef[Command]) extends Command


  //--------------------------------------------------------USERACCOUNT------------------------------------------------------------
  case class GetAccount(token: String, id: String, replyTo: ActorRef[Command]) extends Command

  case class GetAccounts(token: String, replyTo: ActorRef[Command]) extends Command

  case class DeleteAccount(token: String, id: String, replyTo: ActorRef[Command]) extends Command

  case class UpdateAccount(token: String, id: String, replyTo: ActorRef[Command])

  case class AddProductToBasket(productId: String, userId:String, token: String, replyTo: ActorRef[Command]) extends Command

  case class DeleteProductFromAccount(productId: String, userId:String, token: String, replyTo: ActorRef[Command]) extends Command
//
  case class Create(userAccount: User, replyTo: ActorRef[Command]) extends Command

  case class Created(userAccount: User) extends Command

  def apply(): Behavior[Command] = {
    Behaviors.setup[Command] { context =>
      implicit def system: ActorSystem[Nothing] = context.system

      implicit def scheduler: Scheduler = context.system.scheduler

      implicit lazy val timeout: Timeout = Timeout(5.seconds)
      context.system.receptionist ! Receptionist.Register(NodeServiceKey, context.self)
      val account = context.spawnAnonymous(UserAccount("users_product"))

      Behaviors.receiveMessage { message => {
        message match {
          case Check(userToken, replyTo) =>
            replyTo ! Checked("Working", userToken)

 //--------------------------------------------------------CATEGORY------------------------------------------------------------
          case Create(userAccount, replyTo) =>
            account ! UserAccount.AddUser(userAccount, replyTo)
          case GetAccount(token, id, replyTo) =>
            account ! UserAccount.GetUser(token, id, replyTo)
          case DeleteAccount(token, id, replyTo) =>
            account ! UserAccount.RemoveUser(token, id, replyTo)
          case GetAccounts(token, replyTo) =>
            account ! UserAccount.GetUsers(token, replyTo)
          case AddProductToBasket(productId, userId, token, replyTo) =>
            account ! UserAccount.AddToCart(productId, userId, token, replyTo)
          case DeleteProductFromAccount(productId, userId, token, replyTo) =>
            account ! UserAccount.DeleteFromCart(productId, userId, token, replyTo)

   //--------------------------------------------------------PRODUCT------------------------------------------------------------
          case GetProduct(id, replyTo) =>
            account ! UserAccount.GetProduct(id, replyTo)
          case GetProducts(replyTo) =>
            account ! UserAccount.GetProducts(replyTo)
          case FindProduct(productName, replyTo) =>
            account ! UserAccount.SearchProductByName(productName, replyTo)
          case DeleteProduct(id, replyTo) =>
            account ! UserAccount.RemoveProduct(id, replyTo)
          case NewProduct(token, newProduct, replyTo) =>
            account ! UserAccount.AddProduct(token, newProduct, replyTo)
          case UpdateProduct(token, newProduct, replyTo) =>
            account ! UserAccount.UpdateProduct(token, newProduct, replyTo)
    //--------------------------------------------------------CATEGORY------------------------------------------------------------
          case GetCategory(id, replyTo) =>
            account ! UserAccount.GetCategory(id, replyTo)
          case GetCategories(replyTo) =>
            account ! UserAccount.GetCategories(replyTo)
          case DeleteCategory(id, replyTo) =>
            account ! UserAccount.RemoveCategory(id, replyTo)
          case NewCategory(id, createCat, replyTo) =>
            account ! UserAccount.AddCategory(createCat, replyTo)
          case UpdateCategory(token, category, replyTo) =>
            account ! UserAccount.UpdateCategory(token, category,  replyTo)

//            TOKEN
          case GetToken(email, password, replyTo) =>
            account ! UserAccount.GetToken(email, password, replyTo)
        }
        Behaviors.same
      }
      }
    }
  }
}
