package com.example.authenticationsample

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.authenticationsample.databinding.FragmentRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        auth = Firebase.auth


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnRegister.setOnClickListener {
            val email = binding.tfRegisterEmail.text.toString()
            val password = binding.tfRegisterPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                registerUserWithEmailAndPassword(email, password)
            }
        }

        binding.tvSignIn.setOnClickListener {
            val action = RegisterFragmentDirections.actionRegisterFragmentToSignInFragment()
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun registerUserWithEmailAndPassword(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                val user = auth.currentUser
                if (task.isSuccessful) {
                    val action =
                        RegisterFragmentDirections.actionRegisterFragmentToVerificationFragment(
                            email
                        )
                    findNavController().navigate(action)

                    Toast.makeText(context, "successful", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "error", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
