package com.soen490chrysalis.papilio.repository.users

import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseUser
import com.soen490chrysalis.papilio.services.network.responses.GetUserByFirebaseIdResponse
import retrofit2.Response

/*
    DESCRIPTION:
    Interface that declares all the functionalities that the user repositories (mock or not) will
    have to implement. Having an interface of this kind allows for dependency injection when
    performing unit tests.

    Author: Anastassy Cap
    Date: October 5, 2022
*/
interface IUserRepository
{
    fun initialize(googleSignInClient : GoogleSignInClient)

    fun getUser() : FirebaseUser?

    suspend fun getUserByFirebaseId() : GetUserByFirebaseIdResponse?

    suspend fun createUser(
        user : FirebaseUser?,
        updateDisplayName : Boolean,
        firstName : String?,
        lastName : String?
    ) : Response<Void>

    suspend fun updateUser(
        variableMap : Map<String, kotlin.Any>
    ) : Response<Void>

    suspend fun firebaseAuthWithGoogle(idToken : String) : Pair<Boolean, String>

    suspend fun firebaseCreateAccountWithEmailAndPassword(
        firstName : String,
        lastName : String,
        emailAddress : String,
        password : String
    ) : Pair<Boolean, String>

    suspend fun firebaseLoginWithEmailAndPassword(
        emailAddress : String,
        password : String
    ) : Pair<Boolean, String>
}