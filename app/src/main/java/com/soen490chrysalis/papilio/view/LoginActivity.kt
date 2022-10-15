package com.soen490chrysalis.papilio.view

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseUser
import com.soen490chrysalis.papilio.viewModel.LoginViewModel
import com.soen490chrysalis.papilio.R
import com.soen490chrysalis.papilio.databinding.ActivityLoginBinding
import com.soen490chrysalis.papilio.viewModel.LoginViewModelFactory


class LoginActivity : AppCompatActivity()
{
    private lateinit var binding : ActivityLoginBinding

    private val RC_SIGN_IN = 9001
    private lateinit var googleSignInClient: GoogleSignInClient

    private lateinit var loginViewModel : LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.tvSignUpNoAccount.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        // We cannot place this code in the LoginViewModel because it requires the activity object
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val loginFactory = LoginViewModelFactory()
        loginViewModel = ViewModelProvider(this, loginFactory)[LoginViewModel::class.java]
        loginViewModel.initialize(googleSignInClient)

        val hasUserAuthenticatedObserver = Observer<Boolean> { isUserLoggedIn ->
            // The user has successfully logged in. We can now move to the next page/activity
            Log.d(Log.DEBUG.toString(), "Auth observer has detected changes $isUserLoggedIn")
            if ( isUserLoggedIn )
            {
                // Go to main page
                val homePage = Intent(this, MainActivity::class.java)
                startActivity(homePage)
                finish()
            }
        }

        // Register the observer we create above
        loginViewModel.signUpSuccessful.observe(this, hasUserAuthenticatedObserver)

        binding.signInWithGoogleButton.setOnClickListener {
            // Initiate the whole Google Sign In procedure
            val intent = googleSignInClient.signInIntent
            startActivityForResult(intent, RC_SIGN_IN)
        }

        binding.loginButton.setOnClickListener {
            // Extract the email and password from the input fields
            val email : String = binding.userEmailAddress.text.toString()
            val password : String = binding.userPassword.text.toString()

            val emailValidation = loginViewModel.validateEmailAddress(email)
            if ( emailValidation != null )
            {
                binding.userEmailAddress.error = emailValidation
            }

            val passwordValidation = loginViewModel.validatePassword(password)
            if ( passwordValidation != null )
            {
                binding.userPassword.error = passwordValidation
            }

            if ( emailValidation == null && passwordValidation == null )
            {
                // The email and password
                loginViewModel.firebaseLoginWithEmailAndPassword(email, password)
            }
        }

    }

    override fun onStart()
    {
        super.onStart()

        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser : FirebaseUser? = loginViewModel.getUser()

        if ( currentUser != null )
        {
            Log.d(Log.DEBUG.toString(), "Current user: ${currentUser.displayName}")
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN)
        {
            val task : Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            try
            {
                // Google Sign In was successful, authenticate with Firebase
                val account : GoogleSignInAccount = task.result
                Log.d(Log.DEBUG.toString(), "firebaseAuthWithGoogle: " + account.id)
                loginViewModel.firebaseAuthWithGoogle(account.idToken!!)
            }
            catch (e: ApiException)
            {
                // Google Sign In failed, update UI appropriately
                Log.w(Log.DEBUG.toString(), "Google sign in failed", e)
            }
        }
    }

}