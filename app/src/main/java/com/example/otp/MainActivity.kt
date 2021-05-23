package com.example.otp

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.otp.databinding.ActivityMainBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    //For resending OTP
    private var forceResendingToken : PhoneAuthProvider.ForceResendingToken?= null

    private var mCallbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks?=null
    private var mVerificationId:String?=null
    private lateinit var firebaseAuth: FirebaseAuth

    private val TAG = "MAIN_TAG"

    //Progress Dialog
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please Wait")
        progressDialog.setCanceledOnTouchOutside(false)

        mCallbacks = object: PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
            override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                progressDialog.dismiss()
                Toast.makeText(this@MainActivity,"${e.message}", Toast.LENGTH_SHORT).show()

            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                super.onCodeSent(verificationId, token)
                Log.d(TAG, "onCodeSent: $verificationId")
                mVerificationId= verificationId
                forceResendingToken = token
                progressDialog.dismiss()


//                binding.phoneLl.visibility= View.VISIBLE
//                binding.codeLl.visibility = View.GONE

                Toast.makeText(this@MainActivity,"Verification Code Sent", Toast.LENGTH_SHORT).show()

            }
        }

        binding.phoneContinueBtn.setOnClickListener {
            val phone = binding.phoneEt.text.toString().trim()

            if(TextUtils.isEmpty(phone)){
                Toast.makeText(this@MainActivity,"Please Enter Phone Number", Toast.LENGTH_SHORT).show()
            }
            else{
                startPhoneNumberVerification(phone)
            }
        }

        binding.resendcodeTv.setOnClickListener {

            val phone = binding.phoneEt.text.toString().trim()

            if(TextUtils.isEmpty(phone)){
                Toast.makeText(this@MainActivity,"Please Enter Phone Number", Toast.LENGTH_SHORT).show()
            }
            else{
                resendVerificationCode(phone, forceResendingToken)
            }

        }

        binding.codesubmitBtn.setOnClickListener {

            val code = binding.codeEt.text.toString().trim()

            if(TextUtils.isEmpty(code)){
                Toast.makeText(this@MainActivity,"Please Enter Verification Code", Toast.LENGTH_SHORT).show()
            }
            else{
                verifyPhoneNumberWithCode(mVerificationId.toString(),code)
            }

        }

    }
    private fun startPhoneNumberVerification(phone:String){
        progressDialog.setMessage("Verifying Phone Number . . .")
        progressDialog.show()

        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(mCallbacks!!)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun resendVerificationCode(phone:String, token: PhoneAuthProvider.ForceResendingToken?){
        progressDialog.setMessage("Resending Code . . .")
        progressDialog.show()

        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(mCallbacks!!)
            .setForceResendingToken(token!!)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun verifyPhoneNumberWithCode(verificationId:String,code:String){
        progressDialog.setMessage("Verifying Code . .")
        progressDialog.show()

        val credential = PhoneAuthProvider.getCredential(verificationId,code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        progressDialog.setMessage("Logging In . .")

        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener {
                //Login Success
                progressDialog.dismiss()
                val phone = firebaseAuth.currentUser!!.phoneNumber
                Toast.makeText(this, "Logged in as $phone", Toast.LENGTH_SHORT).show()

                startActivity(Intent(this , ProfileActivity::class.java))
                finish()
            }
            .addOnFailureListener { e->
                //Login Failed
                progressDialog.dismiss()
                Toast.makeText(this,"${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

}