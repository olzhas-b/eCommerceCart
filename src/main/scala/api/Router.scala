package api
import Directive.ValidatorDirectives
import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ActorRef, ActorSystem, Scheduler}
import akka.http.scaladsl.server.{Directives, Route}
import akka.util.Timeout
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import Validator._
import node.Node
import node.Node.Checked
import model._

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
case class PostText(email: String, password: String)
trait Router {
  def route: Route
}
class NodeRouter( node:ActorRef[Node.Command])(implicit system: ActorSystem[_], ex:ExecutionContext)
  extends  Router
    with  Directives with ValidatorDirectives{
  private var idUser: Int = 0
  private var idProduct: Int = 0
  private var idCategory: Int = 0
  implicit val timeout: Timeout = 3.seconds
  implicit val scheduler: Scheduler = system.scheduler
  override def route: Route =
    concat(
      pathPrefix("login"){
        post{
          entity(as[PostText]) { token =>
            val futureResult: Future[Node.Token] = node
               .ask(ref => Node.GetToken(token.email, token.password,ref))(timeout, scheduler)
               .mapTo[Node.Token]
            onSuccess(futureResult) { token =>
              complete(token)
            }
          }
        }
      },
      pathPrefix("register"){
        post{
          entity(as[NewUser]) { newUser =>
            val futureResult: Future[Node.SuccessUser] = node
               .ask(ref => Node.Create(User(idUser.toString, newUser.email, newUser.password, Seq.empty),ref))(timeout, scheduler)
               .mapTo[Node.SuccessUser]
            onSuccess(futureResult) { response =>
              idUser += 1
              complete(response)
            }
          }
        }
      },
      pathPrefix("basket") {
        concat(
          delete {
            entity(as[AddProductToBasket]) { updateUser =>
              val futureResult: Future[Node.SuccessUser] = node
                 .ask(ref => Node.DeleteProductFromAccount(updateUser.productId, updateUser.userId, updateUser.userToken, ref))(timeout, scheduler)
                 .mapTo[Node.SuccessUser]
              onSuccess(futureResult) { response =>
                complete(response)
              }
            }
          },
          post {
            entity(as[AddProductToBasket]) { updateUser =>
              val futureResult: Future[Node.SuccessUser] = node
                 .ask(ref => Node.AddProductToBasket(updateUser.productId, updateUser.userId, updateUser.userToken, ref))(timeout, scheduler)
                 .mapTo[Node.SuccessUser]
              onSuccess(futureResult) { response =>
                complete(response)
              }
            }
          })
      },
      pathPrefix("users") {
        concat(
          path(Segment){ id: String =>
            concat(
              get{
                val futureResult: Future[Node.SuccessUser] = node
                   .ask(ref => Node.GetAccount("TOKEN"+id, id, ref))(timeout, scheduler)
                   .mapTo[Node.SuccessUser]
                onSuccess(futureResult) { response =>
                  complete(response)
                }
              },
              delete{
                val futureResult: Future[Node.SuccessUser] = node
                   .ask(ref => Node.DeleteAccount("TOKEN" + id, id, ref))(timeout, scheduler)
                   .mapTo[Node.SuccessUser]
                onSuccess(futureResult) { response =>
                  complete(response)
                }
              },
              put{
                entity(as[AddProductToBasket]) { updateUser =>
                  val futureResult: Future[Node.SuccessUser] = node
                     .ask(ref => Node.AddProductToBasket(updateUser.productId, updateUser.userId, updateUser.userToken, ref))(timeout, scheduler)
                     .mapTo[Node.SuccessUser]
                  onSuccess(futureResult) { response =>
                    complete(response)
                  }
                }
              }
            )
          },
          get{
            val futureResult: Future[Node.Success] = node
               .ask(ref => Node.GetAccounts("admin", ref))(timeout, scheduler)
               .mapTo[Node.Success]
            onSuccess(futureResult) { response =>
              complete(response)
            }
          },
          post{
            entity(as[NewUser]) { createUser =>
              val futureResult: Future[Node.SuccessUser] = node
                 .ask(ref => Node.Create(User(idUser.toString, createUser.email, createUser.password, Seq.empty) ,ref))(timeout, scheduler)
                 .mapTo[Node.SuccessUser]
              onSuccess(futureResult) { response =>
                idUser += 1
                complete(response)
              }
            }
          },
        )
      },

      pathPrefix("categories"){
        concat(
          path(Segment){ id: String =>
            concat(
              get{
                val futureResult: Future[Node.SuccessCategory] = node
                   .ask(ref => Node.GetCategory(id, ref))(timeout, scheduler)
                   .mapTo[Node.SuccessCategory]
                onSuccess(futureResult) { response =>
                  complete(response)
                }
              },
              delete{
                val futureResult: Future[Node.SuccessCategory] = node
                   .ask(ref => Node.DeleteCategory(id, ref))(timeout, scheduler)
                   .mapTo[Node.SuccessCategory]
                onSuccess(futureResult) { response =>
                  complete(response)
                }
              },
              put{
                entity(as[UpdateCategory]) { updateCat =>
                  val futureResult: Future[Node.SuccessCategory] = node
                     .ask(ref => Node.UpdateCategory(updateCat.token, new Category(id,updateCat.name, updateCat.description), ref))(timeout, scheduler)
                     .mapTo[Node.SuccessCategory]
                  onSuccess(futureResult) { response =>
                    complete(response)
                  }
                }
              }
            )
          },
          concat(
            get{
              val futureResult: Future[Node.SuccessCategories] = node
                 .ask(ref => Node.GetCategories( ref))(timeout, scheduler)
                 .mapTo[Node.SuccessCategories]
              onSuccess(futureResult) { response =>
                complete(response)
              }
            },
            post{
              entity(as[CreateCategory]) { createCat =>
                val futureResult: Future[Node.SuccessCategory] = node
                   .ask(ref => Node.NewCategory(createCat.token, Category(idCategory.toString, createCat.name, createCat.description),ref))(timeout, scheduler)
                   .mapTo[Node.SuccessCategory]
                onSuccess(futureResult) { response =>
                  idCategory += 1
                  complete(response)
                }
              }
            }
          ),
        )
      },

      pathPrefix("products"){
        concat(
          pathPrefix("search"){
            path(Segment){ bookName: String =>
              get{
                val futureResult: Future[Node.SuccessProduct] = node.ask(
                  ref => Node
                   .FindProduct(bookName, ref))(timeout, scheduler)
                   .mapTo[Node.SuccessProduct]
                onSuccess(futureResult) { response =>
                  complete(response)
                }
              }
            }
          },
          path(Segment){ id: String =>
            concat(
              get{
                val futureResult: Future[Node.SuccessProduct] = node.ask(
                  ref => Node
                     .GetProduct(id, ref))(timeout, scheduler)
                   .mapTo[Node.SuccessProduct]
                onSuccess(futureResult) { response =>
                  complete(response)
                }
              },
              delete{
                val futureResult: Future[Node.SuccessProduct] = node.ask(
                  ref => Node
                     .DeleteProduct(id, ref))(timeout, scheduler)
                   .mapTo[Node.SuccessProduct]
                onSuccess(futureResult) { response =>
                  complete(response)
                }
              },
              put{
                entity(as[UpdateProduct]) { updateProduct=>
                  val futureResult: Future[Node.SuccessProduct] = node.ask(
                    ref => Node
                       .UpdateProduct(updateProduct.token, new Product(id,updateProduct.categoryId, updateProduct.name, updateProduct.description), ref))(timeout, scheduler)
                     .mapTo[Node.SuccessProduct]
                  onSuccess(futureResult) { response =>
                    complete(response)
                  }
                }
              }
            )
          },
          concat(
            get{
              val futureResult: Future[Node.SuccessProducts] =
                node
                   .ask(ref => Node.GetProducts( ref))(timeout, scheduler)
                   .mapTo[Node.SuccessProducts]
              onSuccess(futureResult) { response =>
                complete(response)
              }
            },
            post{
              entity(as[NewProduct]) { newProduct =>
                val futureResult: Future[Node.SuccessProduct] = node
                   .ask(ref => Node.NewProduct(newProduct.token, Product(idProduct.toString,newProduct.categoryId, newProduct.name, newProduct.description),ref)
                )(timeout, scheduler)
                   .mapTo[Node.SuccessProduct]
                onSuccess(futureResult) { response =>
                  idProduct += 1
                  complete(response)
                }
              }
            }
          ),
        )
      },

    )
}
