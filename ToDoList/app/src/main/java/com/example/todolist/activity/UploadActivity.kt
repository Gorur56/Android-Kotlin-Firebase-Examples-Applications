package com.example.todolist.activity

import android.content.Intent
import android.icu.text.DateFormat
import android.icu.util.Calendar
import android.net.Uri
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.todolist.R
import com.example.todolist.databinding.ActivityUploadBinding
import com.example.todolist.entity.DataClass
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class UploadActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUploadBinding
    var imageURL: String? = null
    var uri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val activityResultLauncher = registerForActivityResult<Intent, ActivityResult>(
            ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                uri = data!!.data
                binding.ImageviewUploadImage.setImageURI(uri)
            } else {
                Snackbar.make(binding.root, "No Image Selected", Snackbar.LENGTH_SHORT).show()
            }
        }

        //Photo yükleme işlemi
        binding.ImageviewUploadImage.setOnClickListener {
            val photoPicker = Intent(Intent.ACTION_PICK)
            photoPicker.type = "image/*"
            activityResultLauncher.launch(photoPicker)
        }
        binding.buttonSave.setOnClickListener {
            saveData()
        }
    }

    private fun saveData() {
        val storeReference = FirebaseStorage.getInstance().reference.child("Task Images")
            .child(uri!!.lastPathSegment!!)

        val builder = AlertDialog.Builder(this@UploadActivity)
        builder.setCancelable(false)
        builder.setView(R.layout.progress_layout)

        val dialog = builder.create()
        dialog.show()

        storeReference.putFile(uri!!).addOnSuccessListener { taskSnapshot ->
            val uriTask = taskSnapshot.storage.downloadUrl
            while(!uriTask.isComplete);
            val urlImage = uriTask.result
            imageURL = urlImage.toString()
            uploadData()
        }.addOnFailureListener {
            dialog.dismiss()
        }
    }

    private fun uploadData() {
        val title = binding.edittextUploadTitle.text.toString()
        val desc = binding.edittextUploadDesc.text.toString()
        val priority = binding.edittextUploadPriority.text.toString()

        val dataClass = DataClass( title, desc, priority, imageURL)
        val currentData = DateFormat.getDateTimeInstance().format(Calendar.getInstance().time)

        FirebaseDatabase.getInstance().getReference("ToDo List").child(currentData)
            .setValue(dataClass).addOnCompleteListener { task ->
                if( task.isSuccessful){
                    Snackbar.make(binding.root, "Saved",Snackbar.LENGTH_SHORT).show()
                    finish()
                }
            }.addOnFailureListener{ e ->
                Snackbar.make(binding.root, e.message.toString(), Snackbar.LENGTH_SHORT).show()
            }
    }
}