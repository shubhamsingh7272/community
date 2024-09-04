package com.pratik.iiits.notes.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import com.pratik.iiits.Models.Notes
import com.pratik.iiits.R
import com.pratik.iiits.ViewModel.NotesViewModel
import com.pratik.iiits.databinding.FragmentCreateNotesBinding
import java.text.SimpleDateFormat
import java.util.*


class CreateNotes : Fragment() {


    lateinit var binding: FragmentCreateNotesBinding
    var priority:String = "1"
    val viewModel: NotesViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentCreateNotesBinding.inflate(layoutInflater,container,false)

        binding.btnsavenotes.setOnClickListener {
            createNotes(it)

        }

        binding.pgreen.setImageResource(R.drawable.ic_baseline_done_24)
        binding.pgreen.setOnClickListener {
            priority="1"
            binding.pgreen.setImageResource(R.drawable.ic_baseline_done_24)
            binding.pred.setImageResource(0)
            binding.pyellow.setImageResource(0)

        }
        binding.pred.setOnClickListener {
            priority="3"
            binding.pred.setImageResource(R.drawable.ic_baseline_done_24)
            binding.pgreen.setImageResource(0)
            binding.pyellow.setImageResource(0)
        }
        binding.pyellow.setOnClickListener {
            priority="2"
            binding.pyellow.setImageResource(R.drawable.ic_baseline_done_24)
            binding.pred.setImageResource(0)
            binding.pgreen.setImageResource(0)
        }

        return binding.root
    }

    private fun createNotes(it: View?) {

        val title = binding.edittitle.text.toString()
        val subtitle = binding.editsubtitle.text.toString()
        val notes = binding.edittextNotes.text.toString()
        val todaydate: String = SimpleDateFormat("MMMM d, YYYY").format(Date()).toString()

        val data= Notes(null,
            title = title,
            subtitle = subtitle,
            note = notes,
            date = todaydate,
            priority
        )
        viewModel.addNotes(data)
        Toast.makeText(requireContext(),"Notes Created Successfully",Toast.LENGTH_SHORT).show()

        Navigation.findNavController(it!!).navigate(R.id.action_createNotes_to_notes_Home)
        Navigation.findNavController(it!!).popBackStack()



    }

}