package model

case class Product(id: String, categoryId:String, name: String, description:String)
case class NewProduct(token: String, categoryId:String, name: String, description:String)

//TODO categoryId:String,
case class UpdateProduct(token:String, categoryId:String, name: String, description: String)
case class Delete(id: String)


