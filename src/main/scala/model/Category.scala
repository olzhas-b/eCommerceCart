package model

case class Category(id: String, name: String, description:String)
case class CreateCategory(token: String, name: String, description:String)

case class UpdateCategory(token:String, name: String, description: String)
case class DeleteCategory(id: String)

