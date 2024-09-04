package com.pratik.iiits.notes.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.pratik.iiits.R
import com.pratik.iiits.ViewModel.NotesViewModel
import com.pratik.iiits.databinding.FragmentNotesHomeBinding
import com.pratik.iiits.notes.Adapters.NotesAdapter


class Notes_Home : Fragment() {
    lateinit var binding:FragmentNotesHomeBinding
    val viewModel: NotesViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding= FragmentNotesHomeBinding.inflate(layoutInflater,container,false)
//        //val staggeredGridLayoutManager =
//        staggeredGridLayoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS

        viewModel.getallnotes().observe(viewLifecycleOwner) {notesList ->
            binding.allnotes.layoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
            binding.allnotes.adapter = NotesAdapter(requireContext(),notesList)
        }

        binding.btAddnotes.setOnClickListener{
            Navigation.findNavController(it).navigate(R.id.action_notes_Home_to_createNotes)
            
        }

        binding.filterhigh.setOnClickListener {
            viewModel.getHighNotes().observe(viewLifecycleOwner) { notesList ->
                binding.allnotes.layoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
                binding.allnotes.adapter = NotesAdapter(requireContext(),notesList)
            }

        }
        binding.showallnotesBtn.setOnClickListener {
            viewModel.getallnotes().observe(viewLifecycleOwner) { notesList ->
                binding.allnotes.layoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
                binding.allnotes.adapter = NotesAdapter(requireContext(),notesList)
            }

        }
        binding.filtermid.setOnClickListener {

            viewModel.getMediumNotes().observe(viewLifecycleOwner) { notesList ->
                binding.allnotes.layoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
                binding.allnotes.adapter = NotesAdapter(requireContext(),notesList)
            }
        }
        binding.filterlow.setOnClickListener {
            viewModel.getLowNotes().observe(viewLifecycleOwner) { notesList ->
                binding.allnotes.layoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
                binding.allnotes.adapter = NotesAdapter(requireContext(),notesList)
            }

        }


        return binding.root
    }


}