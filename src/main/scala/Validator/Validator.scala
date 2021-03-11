package Validator
import model.NewUser
trait Validator[T] {
   def validate(t: T): Option[ApiError]
}

object LoginValidator extends Validator[NewUser] {
   def validate(newUser: NewUser): Option[ApiError] =
      if (newUser.password.isEmpty || newUser.password.isEmpty)
         Some(ApiError.emptyTitleField)
      else
         None
}