package model

case class User(id: String, email: String, password:String, products: Seq[Product])
case class NewUser(email: String, password:String)
case class AddProductToBasket(productId: String, userId:String, userToken:String)

case class LogIn(email: String, password: String)

case class Token(token: String)
