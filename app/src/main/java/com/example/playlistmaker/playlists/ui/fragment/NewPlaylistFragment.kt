package com.example.playlistmaker.playlists.ui.fragment

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentNewPlaylistBinding
import com.example.playlistmaker.playlists.ui.viewmodel.NewPlaylistViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel

class NewPlaylistFragment : Fragment() {

    private var _binding: FragmentNewPlaylistBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NewPlaylistViewModel by viewModel()

    private var savedImagePath: String? = null
    private var hasImage = false

    private val pickMedia = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            binding.ivPlaylistCover.setImageURI(uri)
            binding.ivPlaylistCover.scaleType = ImageView.ScaleType.CENTER_CROP
            binding.ivPlaylistCover.background = null
            viewLifecycleOwner.lifecycleScope.launch {
                val path = saveImageToPrivateStorage(uri)
                if (path != null) {
                    savedImagePath = path
                    hasImage = true
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.image_load_error),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBackButton()
        setupCreateButton()
        setupImagePicker()
        setupTextWatcher()
        setupBackPressedCallback()
    }

    private fun setupBackButton() {
        binding.icVectorBuck.setOnClickListener { handleBack() }
    }

    private fun setupCreateButton() {
        binding.btnCreate.setOnClickListener {
            binding.btnCreate.isEnabled = false

            val name = binding.etPlaylistName.text?.toString().orEmpty()
            val description = binding.etPlaylistDescription.text?.toString().orEmpty()

            viewModel.createPlaylist(name, description, savedImagePath)

            binding.etPlaylistName.clearFocus()
            binding.etPlaylistDescription.clearFocus()
            hideKeyboard()

            findNavController().popBackStack()

            Toast.makeText(
                requireContext(),
                getString(R.string.playlist_created, name),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setupImagePicker() {
        binding.ivPlaylistCover.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    private fun setupTextWatcher() {
        binding.etPlaylistName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                binding.btnCreate.isEnabled = !s.isNullOrBlank()
            }
        })
    }

    private fun setupBackPressedCallback() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() { handleBack() }
            }
        )
    }

    private fun handleBack() {
        binding.etPlaylistName.clearFocus()
        binding.etPlaylistDescription.clearFocus()
        hideKeyboard()

        val name = binding.etPlaylistName.text?.toString().orEmpty()
        val description = binding.etPlaylistDescription.text?.toString().orEmpty()

        if (name.isNotBlank() || description.isNotBlank() || hasImage) {
            showExitDialog()
        } else {
            findNavController().popBackStack()
        }
    }

    private fun showExitDialog() {
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.exit_dialog_title)
            .setMessage(R.string.exit_dialog_message)
            .setNegativeButton(R.string.exit_dialog_cancel) { d, _ -> d.dismiss() }
            .setPositiveButton(R.string.exit_dialog_confirm) { d, _ ->
                d.dismiss()
                binding.etPlaylistName.clearFocus()
                binding.etPlaylistDescription.clearFocus()
                hideKeyboard()
                findNavController().popBackStack()
            }
            .show()

        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)

        dialog.findViewById<TextView>(android.R.id.title)?.setTextColor(
            ContextCompat.getColor(requireContext(), R.color.black)
        )
        dialog.findViewById<TextView>(android.R.id.message)?.setTextColor(
            ContextCompat.getColor(requireContext(), R.color.black)
        )
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(
            ContextCompat.getColor(requireContext(), R.color.black)
        )
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(
            ContextCompat.getColor(requireContext(), R.color.black)
        )
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(InputMethodManager::class.java)
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

    private suspend fun saveImageToPrivateStorage(uri: Uri): String? {
        return withContext(Dispatchers.IO) {
            val filePath = File(
                requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "myalbum"
            )
            if (!filePath.exists()) { filePath.mkdirs() }
            val file = File(filePath, "cover_${System.currentTimeMillis()}.jpg")

            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (bitmap == null) {
                return@withContext null
            }

            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
            }

            file.absolutePath
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
