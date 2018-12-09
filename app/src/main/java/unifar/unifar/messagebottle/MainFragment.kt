package unifar.unifar.messagebottle


import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import unifar.unifar.messagebottle.ui.main.SubmitFragment

class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val view = inflater.inflate(R.layout.fragment_main, container, false)
        val inkView = view.findViewById(R.id.inkBottleImageView) as ImageView
        val bottleView = view.findViewById(R.id.messageBottleImageView) as ImageView

        inkView.setOnClickListener {
            fragmentManager?.beginTransaction()?.replace(R.id.container, SubmitFragment.newInstance())?.commit()
        }

        val db = FirebaseFirestore.getInstance()

        bottleView.setOnClickListener {
            //まずカウンター取得
            db.collection("counters")
                .document("content")
                .get()
                //次に内容取得
                .addOnCompleteListener { task1 ->
                    val data1 = task1.result?.data
                    val dbSize = data1?.get("size")?.toString()?.toInt()
                    val cursor = data1?.get("cursor")?.toString()?.toInt()
                    Log.d("myapp", "cursor:$cursor")

                    cursor?.let {
                        val nextCursor = cursor + 1
                        db.collection("messages")
                            .document("$cursor")
                            .get()
                            .addOnCompleteListener { task2 ->
                                val data2 = task2.result?.data
                                val content = data2?.get("message")?.toString()
                                Toast.makeText(activity, "$content", Toast.LENGTH_LONG).apply {setGravity(Gravity.CENTER,0,0)}.show()
                            }
                            //最後にカウンター更新
                            .addOnCompleteListener {
                                dbSize?.let {
                                    if (nextCursor <= dbSize)
                                        db.collection("counters")
                                            .document("content")
                                            .update("cursor", nextCursor)
                                            .addOnSuccessListener {
                                                Toast.makeText(activity, "cursor was updated to $nextCursor ", Toast.LENGTH_LONG).show()
                                            }
                                    else
                                        Toast.makeText(activity, "cursor is still $cursor ", Toast.LENGTH_LONG).show()
                                }
                            }
                    }
                }
        }



        return view
    }


}
