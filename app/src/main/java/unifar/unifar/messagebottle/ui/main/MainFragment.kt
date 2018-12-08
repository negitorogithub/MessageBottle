package unifar.unifar.messagebottle.ui.main

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.firestore.FieldValue
import unifar.unifar.messagebottle.R
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*


class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.main_fragment, container, false)
        val submitButton = view.findViewById(R.id.submitButton) as Button
        val messageEditText = view.findViewById(R.id.messageEditText) as EditText
        val getButton = view.findViewById(R.id.getButton) as Button

        submitButton.setOnClickListener {  _ ->
            //まずカウンター取得
            db.collection("counters")
                .document("content")
                .get()
                //次に内容送信
                .addOnCompleteListener { task1 ->
                    val data = task1.result?.data
                    val dbSize = data?.get("size")?.toString()?.toInt()
                    val content = messageEditText.text.toString()
                    Log.d("myapp", dbSize.toString())

                    dbSize?.let {
                        val id = dbSize + 1
                        db.collection("messages")
                            .document("$id")
                            .set(
                                HashMap<String, Any>()
                                    .apply {
                                        put("id", id)
                                        put("message", content)
                                        put("time", FieldValue.serverTimestamp())
                                    }
                            )
                            //最後にカウンター更新
                            .addOnCompleteListener { _ ->
                                db.collection("counters")
                                    .document("content")
                                    .update("size", dbSize + 1)
                                    .addOnSuccessListener { _ ->
                                        Toast.makeText(activity, "success!:$content", Toast.LENGTH_LONG).show()
                                    }
                            }
                    }
                }
        }

        getButton.setOnClickListener { _ ->
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

                    cursor?.let { _ ->
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
                            .addOnCompleteListener { _ ->
                                dbSize?.let {
                                    if (nextCursor <= dbSize)
                                        db.collection("counters")
                                            .document("content")
                                            .update("cursor", nextCursor)
                                            .addOnSuccessListener { _ ->
                                                Toast.makeText(activity, "cursor was updated to!:$nextCursor ", Toast.LENGTH_LONG).show()
                                            }
                                    else
                                        Toast.makeText(activity, "cursor is still!:$cursor ", Toast.LENGTH_LONG).show()
                                }
                            }
                    }
                }
        }
        return view
    }

    private lateinit var db: FirebaseFirestore

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        db = FirebaseFirestore.getInstance()

    }

}
