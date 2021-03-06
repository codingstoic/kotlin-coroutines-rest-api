package com.coffeeanddistractions.advancecoroutines

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.widget.TextView
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.UI
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import java.io.IOException
import kotlin.coroutines.experimental.CoroutineContext

class MainActivity : AppCompatActivity() {
    private val parentJob = Job()
    lateinit var numberOfUsersTextView : TextView
    lateinit var numberOfPostsTextView : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        verticalLayout {
            padding = 100
            gravity = Gravity.CENTER
            numberOfUsersTextView = textView("Loading Number of users ...")
            numberOfPostsTextView = textView("Loading Number of posts ...")
            button("Create User") {
                onClick {
                    createUser()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        getAllUsers()
        getAllPosts()
    }

    override fun onPause() {
        super.onPause()
        parentJob.cancel()
    }

    // exception handling done with job.invokeOnCompletion
    fun getAllPosts(){
        async(parentJob + UI) {
            val task = async(parentJob + CommonPool) {
                RestClient.apiDefinition.listPosts().execute()
            }

            val result = task.await()
            val numberOfPosts = "number of posts ${result.body()?.size}"
            numberOfPostsTextView.text = numberOfPosts

        }.invokeOnCompletion { it: Throwable? ->
            alertWithOkButton("Caught an exception inside invoke on completion callback")
            it?.printStackTrace()
        }
    }

    // exception handling done with try catch block
    fun sendCreateUserRequest(firstName: String, lastName: String) {
        launch(parentJob + UI) {
            try {
                val listOfUsers = run(CommonPool) {
                    val user = User(firstName, lastName)
                    RestClient.apiDefinition.createUser(user).execute()
                }

                toast("Created user id is ${listOfUsers.body()?.id}")
            } catch (ex: IOException) {
                alertWithOkButton("Error with internet connection")
            } catch (ex: RuntimeException) {
                alertWithOkButton("Error with parsing server response")
            }
        }
    }

    private val exceptionHandler: CoroutineContext = CoroutineExceptionHandler { _, throwable ->
        alertWithOkButton("Exception caught inside the exception handler")
        throwable.printStackTrace()
    }

    // exception handling done with exception handler
    private fun getAllUsers() {
        launch(parentJob + UI + exceptionHandler) {
            val listOfUsers = run(CommonPool) {
                RestClient.apiDefinition.listUsers().execute()
            }

            val numberOfUsers = "number of users ${listOfUsers.body()?.size}"
            numberOfUsersTextView.text = numberOfUsers
        }
    }

    fun createUser() {
        alert("Enter user first name and last name") {
            customView {
                verticalLayout {
                    padding = 50
                    gravity = Gravity.CENTER
                    val firstName = editText { hint = "first name" }.lparams(width = 500, height = wrapContent)
                    val lastName = editText { hint = "last name" }.lparams(width = 500, height = wrapContent)
                    yesButton {
                        sendCreateUserRequest(firstName = firstName.text.toString(), lastName = lastName.text.toString())
                    }
                    noButton { }
                }
            }
        }.show()
    }

    private fun alertWithOkButton(message: String){
        alert(message) {
            okButton {  }
        }.show()
    }

}
