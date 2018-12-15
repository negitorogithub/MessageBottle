package unifar.unifar.messagebottle

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
import android.widget.Button
import android.widget.TextView
import android.view.KeyEvent.KEYCODE_BACK




// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [ShowMessageFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [ShowMessageFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class ShowMessageFragment : Fragment() {
    private var message: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            message = it.getString(ARG_PARAM1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val view = inflater.inflate(R.layout.fragment_show_message, container, false)
        view.findViewById<Button>(R.id.showMessageEndButton).setOnClickListener {
            fragmentManager?.beginTransaction()?.replace(R.id.container, MainFragment.newInstance())?.commit()
        }
        view.findViewById<TextView>(R.id.messageTextView).text = message
        return view
    }




    companion object {
        @JvmStatic
        fun newInstance(message2show: String) =
            ShowMessageFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, message2show)
                }
            }
    }
}
