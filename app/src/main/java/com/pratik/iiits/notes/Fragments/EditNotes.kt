package com.pratik.iiits.notes.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.pratik.iiits.Models.Notes
import com.pratik.iiits.R
import com.pratik.iiits.ViewModel.NotesViewModel
import com.pratik.iiits.databinding.FragmentEditNotesBinding
import java.text.SimpleDateFormat
import java.util.*


class EditNotes : Fragment() {

    val OldNotes by navArgs<EditNotesArgs>()
    lateinit var binding: FragmentEditNotesBinding
    val viewModel: NotesViewModel by viewModels()
    var priority:String = "1"


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        binding = FragmentEditNotesBinding.inflate(layoutInflater,container,false)

        binding.edittitle.setText(OldNotes.data.title)
        binding.editsubtitle.setText(OldNotes.data.subtitle)
        binding.edtNotes.setText(OldNotes.data.note)

        when(OldNotes.data.priority){
            "1"->{
                priority="1"
                binding.pgreen.setImageResource(R.drawable.ic_baseline_done_24)
                binding.pred.setImageResource(0)
                binding.pyellow.setImageResource(0)

            }
            "2"->{
                priority="2"
                binding.pyellow.setImageResource(R.drawable.ic_baseline_done_24)
                binding.pred.setImageResource(0)
                binding.pgreen.setImageResource(0)

            }
            "3"->{
                priority="3"
                binding.pred.setImageResource(R.drawable.ic_baseline_done_24)
                binding.pgreen.setImageResource(0)
                binding.pyellow.setImageResource(0)
            }
        }

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

        binding.btneditnotes.setOnClickListener{
            updateNotes(it)
        }
        binding.btndeletenotes.setOnClickListener {
            val temp = it
            val bottomSheetDialog : BottomSheetDialog = BottomSheetDialog(requireContext(),R.style.BottomSheetStyle)
            bottomSheetDialog.setContentView(R.layout.dialog_delete)
            bottomSheetDialog.show()

            val delete_yes= bottomSheetDialog.findViewById<TextView>(R.id.deletenote_yes)
            val delete_no= bottomSheetDialog.findViewById<TextView>(R.id.deletenote_no)

            delete_yes?.setOnClickListener {
                viewModel.deleteNotes(OldNotes.data.id!!)
                Navigation.findNavController(temp!!).navigate(R.id.action_editNotes_to_notes_Home)
                bottomSheetDialog.dismiss()

            }
            delete_no?.setOnClickListener {
                bottomSheetDialog.dismiss()
            }



        }








        return binding.root
    }

    private fun updateNotes(it: View?) {
        val title = binding.edittitle.text.toString()
        val subtitle = binding.editsubtitle.text.toString()
        val notes = binding.edtNotes.text.toString()
        val todaydate: String = SimpleDateFormat("MMMM d, YYYY").format(Date()).toString()

        val data= Notes(OldNotes.data.id,
            title = title,
            subtitle = subtitle,
            note = notes,
            date = todaydate,
            priority
        )
        viewModel.updateNotes(data)
        Toast.makeText(requireContext(),"Notes Updated Successfully", Toast.LENGTH_SHORT).show()

        Navigation.findNavController(it!!).navigate(R.id.action_editNotes_to_notes_Home)
        Navigation.findNavController(it).popBackStack()

    }

}
