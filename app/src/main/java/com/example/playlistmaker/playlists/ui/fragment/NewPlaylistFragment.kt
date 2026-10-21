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
import androidx.navigation.fragment.navArgs
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentNewPlaylistBinding
import com.example.playlistmaker.playlists.ui.viewmodel.NewPlaylistViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel

class NewPlaylistFragment : Fragment() {

    private var _binding: FragmentNewPlaylistBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NewPlaylistViewModel by viewModel()
    private val args: NewPlaylistFragmentArgs by navArgs()

    private val isEditMode: Boolean
        get() = args.playlistId > 0L

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
        observePlaylistCreation()
        observePlaylistUpdate()

        if (isEditMode) {
            setupEditMode()
            observePlaylistLoading()
            viewModel.loadPlaylist(args.playlistId)
        }
    }

    private fun setupBackButton() {
        binding.icVectorBuck.setOnClickListener { handleBack() }
    }

    private fun setupCreateButton() {
        binding.btnCreate.setOnClickListener {
            val name = binding.etPlaylistName.text.toString().trim()
            val description = binding.etPlaylistDescription.text.toString().trim()

            if (name.isBlank()) return@setOnClickListener

            binding.btnCreate.isEnabled = false

            binding.etPlaylistName.clearFocus()
            binding.etPlaylistDescription.clearFocus()
            hideKeyboard()

            if (isEditMode) {
                viewModel.updatePlaylist(
                    playlistId = args.playlistId,
                    name = name,
                    description = description,
                    coverPath = savedImagePath.orEmpty()
                )
            } else {
                viewModel.createPlaylist(
                    name = name,
                    description = description,
                    coverPath = savedImagePath
                )
            }
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

        if (isEditMode) {
            findNavController().popBackStack()
        } else if (name.isNotBlank() || description.isNotBlank() || hasImage) {
            showExitDialog()
        } else {
            findNavController().popBackStack()
        }
    }

    private fun showExitDialog() {
        val dialog = MaterialAlertDialogBuilder(
            requireContext(),
            R.style.Theme_PlaylistMaker_AlertDialog
        )
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
            try {
                val context = requireContext().applicationContext
                val resolver = context.contentResolver
                val rotation = resolver.openInputStream(uri)?.use { stream ->
                    ExifInterface(stream).rotationDegrees
                } ?: return@withContext null
                val bitmap = resolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                } ?: return@withContext null
                val correctedBitmap = if (rotation != 0) {
                    val matrix = Matrix().apply {
                        postRotate(rotation.toFloat())
                    }

                    Bitmap.createBitmap(
                        bitmap,
                        0,
                        0,
                        bitmap.width,
                        bitmap.height,
                        matrix,
                        true
                    ).also {
                        if (it !== bitmap) bitmap.recycle()
                    }
                } else {
                    bitmap
                }

                val directory = File(
                    context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                    "myalbum"
                )

                if (!directory.exists() && !directory.mkdirs()) {
                    correctedBitmap.recycle()
                    return@withContext null
                }

                val file = File(
                    directory,
                    "cover_${System.currentTimeMillis()}.jpg"
                )

                val saved = FileOutputStream(file).use { output ->
                    correctedBitmap.compress(
                        Bitmap.CompressFormat.JPEG,
                        80,
                        output
                    )
                }

                correctedBitmap.recycle()

                if (saved) file.absolutePath else null
            } catch (_: Exception) {
                null
            }
        }
    }
    private fun observePlaylistCreation() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.playlistCreated.collect { name ->
                binding.btnCreate.isEnabled = true

                if (name.isNotEmpty()) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.playlist_created, name),
                        Toast.LENGTH_SHORT
                    ).show()

                    findNavController().popBackStack()
                } else {
                    Toast.makeText(
                        requireContext(),
                        (R.string.playlist_create_error),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
    private fun setupEditMode() {
        binding.tvNewPlaylist.setText(R.string.edit_playlist)
        binding.btnCreate.setText(R.string.save_playlist)
    }

    private fun observePlaylistLoading() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.playlistLoaded.collect { playlist ->
                if (playlist.id != args.playlistId) return@collect

                binding.etPlaylistName.setText(playlist.name)
                binding.etPlaylistDescription.setText(playlist.description)

                savedImagePath = playlist.coverPath
                hasImage = playlist.coverPath.isNotBlank()

                if (playlist.coverPath.isNotBlank()) {
                    val file = File(playlist.coverPath)
                    if (file.exists()) {
                        binding.ivPlaylistCover.setImageURI(Uri.fromFile(file))
                        binding.ivPlaylistCover.scaleType = ImageView.ScaleType.CENTER_CROP
                        binding.ivPlaylistCover.background = null
                    }
                }

                binding.btnCreate.isEnabled = playlist.name.isNotBlank()
            }
        }
    }
    private fun observePlaylistUpdate() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.playlistUpdated.collect { success ->
                binding.btnCreate.isEnabled =
                    binding.etPlaylistName.text.toString().isNotBlank()

                if (success) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.playlist_update_success),
                        Toast.LENGTH_SHORT
                    ).show()

                    findNavController().popBackStack()
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.playlist_update_error),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
