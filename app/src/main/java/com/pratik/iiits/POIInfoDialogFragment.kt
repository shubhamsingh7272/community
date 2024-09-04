package com.pratik.iiits
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment

class POIInfoDialogFragment : DialogFragment() {

    companion object {
        const val ARG_NAME = "name"
        const val ARG_INFO = "info"
        const val ARG_CONTACT = "contact"

        fun newInstance(name: String, info: String, contact: String): POIInfoDialogFragment {
            val fragment = POIInfoDialogFragment()
            val args = Bundle()
            args.putString(ARG_NAME, name)
            args.putString(ARG_INFO, info)
            args.putString(ARG_CONTACT, contact)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_poi_info, container, false)

        val poiNameTextView = view.findViewById<TextView>(R.id.poi_name)
        val poiInfoTextView = view.findViewById<TextView>(R.id.poi_info)
        val poiContactTextView = view.findViewById<TextView>(R.id.poi_contact)
        val closeButton = view.findViewById<Button>(R.id.close_button)

        val name = arguments?.getString(ARG_NAME)
        val info = arguments?.getString(ARG_INFO)
        val contact = arguments?.getString(ARG_CONTACT)

        poiNameTextView.text = name
        poiInfoTextView.text = info
        poiContactTextView.text = contact

        closeButton.setOnClickListener {
            dismiss()
        }

        return view
    }
}
