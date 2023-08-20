package com.example.authenticationsample.firestore

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.authenticationsample.databinding.FragmentUploadPhotoBinding
import com.example.authenticationsample.firestore.model.PhotoItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class UploadPhotoFragment : Fragment() {

    private var _binding: FragmentUploadPhotoBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference
    private val photoListAdapter = PhotoListAdapter(::updateCommentInFirestore)
    private val pickMedia =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                if (data != null) {
                    val selectedImageUri: Uri? = data.data
                    selectedImageUri?.let { uri ->
                        // Upload photo to Firebase Storage
                        val photoRef =
                            storageReference.child("photos/${auth.currentUser?.uid}/${System.currentTimeMillis()}.jpg")
                        photoRef.putFile(uri)
                            .addOnSuccessListener { taskSnapshot ->
                                // Get download URL
                                taskSnapshot.storage.downloadUrl.addOnSuccessListener { downloadUri ->
                                    // Save photo info to Firestore
                                    val userPhoto = hashMapOf(
                                        "userId" to auth.currentUser?.uid,
                                        "photoUrl" to downloadUri.toString(),
                                        "username" to auth.currentUser?.email,
                                        "comment" to "Add Comment"
                                    )
                                    db.collection("photos")
                                        .add(userPhoto)
                                        .addOnSuccessListener { documentReference ->
                                            Toast.makeText(
                                                requireContext(),
                                                "Photo uploaded successfully!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            fetchPhotos()
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(
                                                requireContext(),
                                                "Error uploading photo",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                }
                            }
                            .addOnFailureListener {
                                Toast.makeText(
                                    requireContext(),
                                    "Error uploading photo",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentUploadPhotoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference

        binding.btnUploadPhoto.setOnClickListener {
            openGalleryForImage()
        }

        // Set up RecyclerView

        binding.recyclerview.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = photoListAdapter
        }

        fetchPhotos()

    }

    private fun openGalleryForImage() {
        val intent = Intent(
            Intent.ACTION_PICK,
            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        pickMedia.launch(intent)
    }

    private fun fetchPhotos() {

        // Fetch photos from Firestore
        db.collection("photos")
            .get()
            .addOnSuccessListener { result ->
                val photoList = mutableListOf<PhotoItem>()
                for (document in result) {
                    val photo = document.toObject(PhotoItem::class.java)
                    photoList.add(photo)
                }
                photoListAdapter.submitList(photoList)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Error getting photos", Toast.LENGTH_SHORT).show()
            }
    }


    private fun updateCommentInFirestore(position: Int, newComment: String) {
        val db = Firebase.firestore

        db.collection("photos")
            .get()
            .addOnSuccessListener { result ->
                val documentIdByPosition = result.documents[position].id

                val photoDocumentRef = db.collection("photos").document(documentIdByPosition)

                val dataToUpdate = mapOf<String,String>(
                    "comment" to newComment
                )

                photoDocumentRef.update(dataToUpdate)
                    .addOnSuccessListener {
                        fetchPhotos()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            requireContext(),
                            "Error updating comment in Firestore",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Error getting photos", Toast.LENGTH_SHORT).show()
            }

//        val photoDocumentRef1 = db.collection("photos").whereEqualTo("userId", photoItemId).get()
//            .addOnSuccessListener {
//                val dfbhfd = photoItemId
//                val aaa = it
//                val bbb= aaa
//
//                val sdgdfhg = it.documents[position].id
//
//                val photoDocumentRef = db.collection("photos").where(sdgdfhg)
//
//
//            }.addOnFailureListener {
//                val aaa = it
//                val bbb= aaa
//            }



    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
