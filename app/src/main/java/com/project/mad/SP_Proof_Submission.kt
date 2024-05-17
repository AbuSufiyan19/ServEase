package com.project.mad

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import org.w3c.dom.Text

class SP_Proof_Submission : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var uploadImageButton: Button
    private lateinit var login: TextView
    private lateinit var approvalstatus: TextView
    private var progressDialog: ProgressDialog? = null



    private val PICK_IMAGE_REQUEST = 1
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sp_proof_submission)

        imageView = findViewById(R.id.imageView)
        uploadImageButton = findViewById(R.id.uploadImageButton)
        approvalstatus = findViewById(R.id.approvalstatus)
        login = findViewById(R.id.login)

        login.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // Finish the current activity, as we don't want the user to go back to the login page
        }
        progressDialog = ProgressDialog(this, R.style.CustomProgressDialog)
        progressDialog?.setMessage("Uploading...")
        progressDialog?.setCancelable(false)

        uploadImageButton.setOnClickListener {
            val userId = getUserIdFromSharedPreferences()
            if (userId.isNotEmpty()) {
                val imageUri = imageView.tag as? Uri
                if (imageUri != null) {
                    progressDialog?.show()
                    upload(userId, imageUri)
                } else {
                    // Show a toast message to prompt the user to choose the proof
                    Toast.makeText(this@SP_Proof_Submission, "Please choose the document to be uploaded", Toast.LENGTH_SHORT).show()
                }
            }
        }

        checkproofexists()

    }

    private fun checkproofexists() {
        // Check and display approval status and image if available
        val userId = getUserIdFromSharedPreferences()
        if (userId.isNotEmpty()) {
            val databaseReference = FirebaseDatabase.getInstance().reference
            val serviceManRef = databaseReference.child("serviceMan").child(userId)
            serviceManRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val newApprovalStatus = dataSnapshot.child("approvalstatus").getValue(String::class.java)
                        val statusText = if (newApprovalStatus.isNullOrEmpty()) {
                            "No Document Uploaded"
                        } else if(newApprovalStatus=="ReUpload") {
                            "Upload a Valid Document"
                        } else {
                            "$newApprovalStatus"
                        }
                        approvalstatus.text = statusText

                        if (newApprovalStatus == "Proof Uploaded") {
                            uploadImageButton.visibility = View.GONE // Hide the upload button
                        }

                        if (newApprovalStatus != "ReUpload") {

                            val imageURL = dataSnapshot.child("imageURL").getValue(String::class.java)
                        imageURL?.let {
                            // Load the image into ImageView using a library like Glide or Picasso
                            Glide.with(this@SP_Proof_Submission)
                                .load(it)
                                .into(imageView)
                            imageView.tag = Uri.parse(it) // Store the image URI in tag for later use
                        }

                        }
                        // Set OnClickListener on ImageView
                        imageView.setOnClickListener {
                            if (newApprovalStatus != "Proof Uploaded") {
                                // Open gallery to select image
                                val intent = Intent()
                                intent.type = "image/*"
                                intent.action = Intent.ACTION_GET_CONTENT
                                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
                            }
                        }
                    }

                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle errors
                }
            })
        }
    }


    private fun upload(userId: String, imageUri: Uri) {
        // Reference to Firebase Storage
        uploadImageButton.visibility = View.INVISIBLE
        Toast.makeText(this@SP_Proof_Submission, "Document is Uploading", Toast.LENGTH_SHORT).show()
        val storageReference = FirebaseStorage.getInstance().reference.child("proofs").child("$userId.jpg")

        // Upload the image to Firebase Storage
        storageReference.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                // Get the download URL of the uploaded image
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                    // Update imageURL attribute in the serviceMan collection
                    val databaseReference = FirebaseDatabase.getInstance().reference
                    val serviceManRef = databaseReference.child("serviceMan").child(userId)
                    serviceManRef.child("imageURL").setValue(uri.toString())
                        .addOnSuccessListener {
                            // Image URL successfully stored in the database
                            // Now update the approvalstatus attribute to indicate proofuploaded
                            serviceManRef.child("approvalstatus").setValue("Proof Uploaded")
                                .addOnSuccessListener {
                                    progressDialog?.dismiss()
                                    // Approval status updated successfully
                                    Toast.makeText(this@SP_Proof_Submission, "Document Submitted Successfully", Toast.LENGTH_SHORT).show()
                                    uploadImageButton.visibility = View.VISIBLE

                                    checkproofexists()
                                }
                                .addOnFailureListener { e ->
                                    // Handle any errors
                                    progressDialog?.dismiss()
                                    Toast.makeText(this@SP_Proof_Submission, "Submission Error", Toast.LENGTH_SHORT).show()

                                }
                        }
                        .addOnFailureListener { e ->
                            // Handle any errors
                        }
                }
            }
            .addOnFailureListener { e ->
                // Handle any errors
            }
    }


    private fun getUserIdFromSharedPreferences(): String {
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("userToken", "") ?: ""
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            // Set the selected image to ImageView
            val imageUri = data.data
            imageView.setImageURI(imageUri)
            imageView.tag = imageUri
        }
    }
}
