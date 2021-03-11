//package repository
//
//import java.util.UUID
//
//import UsersAccount._
//
//import scala.concurrent.{ExecutionContext, Future}
//import model.AddProductToBasket
//import model._
//trait UsersRepository {
////  def allUsers(): Future[Seq[User]]
////  def createUser(newUser: NewUser): Future[User]
////  def login(logIn: LogIn): Future[Token]
////  def updateUser(id: String, addProduct: AddProductToBasket): Future[User]
////  def deleteUser(id: String): Future[User]
////  def getUser(id: String): Future[User]
////
////  def allProducts(): Future[Seq[Product]]
////  def deleteProduct(id: String): Future[Product]
////  def getProduct(id: String): Future[Product]
////  def searchProduct(name: String): Future[Seq[Product]]
//}
//
//
//class InMemoryRepository(usersList:Seq[User] = Seq.empty, booksList: Seq[Product] = Seq.empty)
//                        (implicit  ex:ExecutionContext) extends UsersRepository {
//
//  private var users: Vector[User] = usersList.toVector
//  private var products: Vector[Product] = booksList.toVector
//  private var cnt: Int = 0
////  override def allUsers(): Future[Seq[User]] = Future.successful(users)
////
////  override def createUser(createUser: NewUser): Future[User] = Future.successful {
////    val user = User(
////      id = UUID.randomUUID().toString,
////      email = createUser.email,
////      password = createUser.password,
////      products = Vector.empty[Product]
////    )
////    users = users :+ user
////    user
////  }
////  override def updateUser(id: String, addProduct: AddProductToBasket): Future[User] = {
////    users.find(_.id == id) match {
////      case Some(user) =>
////        if (user.products.find(_.id == addProduct.productId ) != null){
////          val book_tmp = products.find(_.id==addProduct.productId)
////          book_tmp match {
////            case Some(b) =>
////              val products_tmp = user.products :+ b
////              val tmp = user.copy(products = products_tmp)
////              users = users.map(user => if (user.id == id) tmp else user)
////              Future.successful(tmp)
////          }
////        }else{
////          Future.failed(BookFound(addProduct.productId))
////        }
////      case None =>
////        Future.failed(UserNotFound(id))
////    }
////  }
////
////  override def login(logIn: LogIn): Future[Token] = {
////    users.foreach(us=>{
////      if (us.email == logIn.email && us.password == logIn.password)
////        Future.successful(Token("user" + us.id))
////      else
////        Future.failed(UserNotFound(logIn.email))
////    })
////    Future.successful(Token("authorization Failed: No user Exists"))
////  }
////
////  override def deleteUser(id: String): Future[User] = {
////    users.find(_.id == id) match {
////      case Some(todo) =>
////        users = users.filter(_.id != id)
////        Future.successful(todo)
////      case None =>
////        Future.failed(UserNotFound(id))
////    }
////  }
////  override def getUser(id: String): Future[User] = {
////    users.find(_.id == id) match {
////      case Some(todo) =>
////        Future.successful(todo)
////      case None =>
////        Future.failed(UserNotFound(id))
////    }
////  }
//////-----------------------------------------------------------
////  override def allProducts(): Future[Seq[Product]] = {
////    Future.successful(products)
////  }
////
////
////
////  override def deleteProduct(id: String): Future[Product] = {
////    products.find(_.id == id) match {
////      case Some(book) =>
////        products = products.filter(_.id != id)
////        Future.successful(book)
////      case None =>
////        Future.failed(BookNotFound(id))
////    }
////  }
////
////  override def getProduct(id: String): Future[Product] = {
////    products.find(_.id == id) match {
////      case Some(book) =>
////        Future.successful(book)
////      case None =>
////        Future.failed(BookNotFound(id))
////    }
////  }
////
////  override def searchProduct(name: String): Future[Seq[Product]] = {
////    var bs: Seq[Product] = Seq.empty
////    products.foreach(b => if (b.name.contains(name)) bs = bs :+ b)
////    Future.successful(bs)
////  }
//}
//import akka.http.scaladsl.server.{Directive1, Directives}
//
//import scala.concurrent.Future
//import scala.util.Success
//trait UsersDirectives extends Directives {
//  def handle[T](f: Future[T])(e: Throwable => ApiError): Directive1[T] = onComplete(f) flatMap {
//    case Success(t) =>
//      provide(t)
////    case Failure(error) =>
////      val apiError = e(error)
////      complete(apiError.statusCode, apiError.message)
//  }
//}
//
//import akka.http.scaladsl.model.{StatusCode, StatusCodes}
//
//final case class ApiError private(statusCode: StatusCode, message: String)
//object ApiError {
//  private def apply(statusCode: StatusCode, message: String): ApiError = new ApiError(statusCode, message)
//
//  val generic: ApiError = new ApiError(StatusCodes.InternalServerError, "Unknown error.")
//  val emptyTitleField: ApiError = new ApiError(StatusCodes.BadRequest, "The title field must not be empty.")
//  val emptyNameField: ApiError = new ApiError(StatusCodes.BadRequest, "The Name field must not be empty.")
//  val emptyEmailField: ApiError = new ApiError(StatusCodes.BadRequest, "The Email field must not be empty.")
//  val emptyPhoneField: ApiError = new ApiError(StatusCodes.BadRequest, "The Phone field must not be empty.")
//  val emptyDescriptionField: ApiError = new ApiError(StatusCodes.BadRequest, "The description field must not be empty.")
//  val emailWrongFormat: ApiError = new ApiError(StatusCodes.BadRequest, "The email field is wrong.")
//  val phoneWrongFormat: ApiError = new ApiError(StatusCodes.BadRequest, "The phone number field is wrong.")
//
//  def todoNotFound(id: String): ApiError =
//    new ApiError(StatusCodes.NotFound, s"The todo with id $id could not be found.")
//
//  def addressNotFound(id: String): ApiError =
//    new ApiError(StatusCodes.NotFound, s"The addressBook with id $id could not be found.")
//}
