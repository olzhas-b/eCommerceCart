package Validator

import akka.http.scaladsl.model.{StatusCode, StatusCodes}

final case class UserNotFound(id: String) extends Exception("User " + id + " not found")
final case class ProductNotFound(id: String) extends Exception("Book " + id + " not found")
final case class ProductFound(id: String) extends Exception("Book " + id + "  is already in list")

final case class ApiError private(statusCode: StatusCode, message: String)
object ApiError {
  private def apply(statusCode: StatusCode, message: String): ApiError = new ApiError(statusCode, message)

  val generic: ApiError = new ApiError(StatusCodes.InternalServerError, "Unknown error.")
  val emptyTitleField: ApiError = new ApiError(StatusCodes.BadRequest, "The title field must not be empty.")
  val emptyNameField: ApiError = new ApiError(StatusCodes.BadRequest, "The Name field must not be empty.")
  val emptyEmailField: ApiError = new ApiError(StatusCodes.BadRequest, "The Email field must not be empty.")
  val emptyDescriptionField: ApiError = new ApiError(StatusCodes.BadRequest, "The description field must not be empty.")
  val emailWrongFormat: ApiError = new ApiError(StatusCodes.BadRequest, "The email field is wrong.")

  def productNotFounded(id: String): ApiError =
    new ApiError(StatusCodes.NotFound, s"The addressBook with id $id could not be found.")
}
