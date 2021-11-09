package com.example.clone_insta

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.clone_insta.databinding.ActivityLoginBinding
import com.example.clone_insta.databinding.ActivityMainBinding
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import java.util.*


class LoginActivity : AppCompatActivity() {
    var auth : FirebaseAuth? = null
    var googleSigninClient : GoogleSignInClient? = null
    var GOOGLE_LOGIN_CODE = 1
    var callbackManager : CallbackManager? = null
    val binding by lazy { ActivityLoginBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()


        //이메일, 구글, 페이스북 로그인 버튼 이벤트
        binding.emailLoginButton.setOnClickListener {
            signinAndSignup()
        }
        binding.googleSigninButton.setOnClickListener {
            googleLogin()
        }
        binding.facebookSigninButton.setOnClickListener {
            facebookLogin()
        }
        // 구글 로그인
        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        // 구글 로그인 클라이언트 객체
        googleSigninClient = GoogleSignIn.getClient(this, gso)

        // 콜백 매니저 초기화
        callbackManager = CallbackManager.Factory.create()
    }

    fun googleLogin(){
        //signInIntent는 구글 로그인시 출력되는 SubActivity를 말함
        var signInIntent = googleSigninClient?.signInIntent
        startActivityForResult(signInIntent, GOOGLE_LOGIN_CODE)
    }
    fun facebookLogin(){
        var loginManager = LoginManager.getInstance() //페이스북 로그인 매니저 객체 획득
        loginManager
            .logInWithReadPermissions(this, Arrays.asList("public_profile", "email")) //로그인 매니저를 통해 읽어오기 권한 획득

        // 로그인 매니저에 콜백을 관리하는 callbackManager와 응답 콜백인 FacebookCallback<LoginResult>를 등록
        loginManager
            .registerCallback(callbackManager, object : FacebookCallback<LoginResult>{
                override fun onSuccess(result: LoginResult?) {
                    handleFacebookAccessToken(result?.accessToken)
                }

                override fun onCancel() {

                }

                override fun onError(error: FacebookException?) {

                }
            })
    }
    fun handleFacebookAccessToken(token : AccessToken?){
        var credential = FacebookAuthProvider.getCredential(token?.token!!)
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener {
                    task ->
                if(task.isSuccessful){
                    //Creating a user account
                    moveMainPage(task.result?.user)
                }else{
                    //Show the error message
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager?.onActivityResult(requestCode, resultCode, data)
        if(requestCode == GOOGLE_LOGIN_CODE){
            var result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if(result.isSuccess){
                var account = result.signInAccount
                firebaseAuthWithGoogle(account)
            }
        }
    }

    fun firebaseAuthWithGoogle(account : GoogleSignInAccount?){
        var credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener {
                    task ->
                if(task.isSuccessful){
                    //Creating a user account
                    moveMainPage(task.result?.user)
                }else{
                    //Show the error message
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                }
            }
    }

    fun signinAndSignup(){
        auth?.createUserWithEmailAndPassword(binding.emailEdittext.text.toString(), binding.passwordEdittext.text.toString())
            ?.addOnCompleteListener {
            task ->
                if(task.isSuccessful){
                    //Creating a user account
                    moveMainPage(task.result?.user)
                }else if(task.exception?.message.isNullOrEmpty()){
                    //Show the error message
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                }else{
                    signinEmail()
                }
        }
    }
    fun signinEmail(){
        auth?.createUserWithEmailAndPassword(binding.emailEdittext.text.toString(), binding.passwordEdittext.text.toString())?.addOnCompleteListener {
                task ->
            if(task.isSuccessful){
                moveMainPage(task.result?.user)
            }else{
                Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
            }
        }
    }
    fun moveMainPage(user:FirebaseUser?){
        if(user != null){
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}

// keytool -exportcert -alias AndroidDebugKey -keystore "C:\Users\morta\.android\debug.keystore" | "C:\OpenSSL\bin\openssl" sha1 -binary | "C:\OpenSSL\bin\openssl" base64

