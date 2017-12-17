package com.coffeeanddistractions.advancecoroutines

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.UI
import org.jetbrains.anko.*
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.experimental.CoroutineContext

class MainActivity : AppCompatActivity() {
    private val parentJob = Job()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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
            println("number of posts ${result.body()?.size}")

        }.invokeOnCompletion { it: Throwable? ->
            println("caught an exception inside invoke on completion callback")
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

                println("Created user id is ${listOfUsers.body()?.id}")
            } catch (ex: IOException) {
                println("some exception $ex")
            } catch (ex: RuntimeException) {
                println("error decoding response $ex")
            }
        }
    }

    private val exceptionHandler: CoroutineContext = CoroutineExceptionHandler { _, throwable ->
        println("exception caught inside the exception handler")
        throwable.printStackTrace()
    }

    // exception handling done with exception handler
    private fun getAllUsers() {
        launch(parentJob + UI + exceptionHandler) {
            delay(5, TimeUnit.SECONDS)
            val listOfUsers = run(CommonPool) {
                RestClient.apiDefinition.listUsers().execute()
            }

            val numberOfUsers = "number of users ${listOfUsers.body()?.size}"
            numberOfUsersTextView.text = numberOfUsers
        }
    }

    fun createUser(view: View) {
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
}
