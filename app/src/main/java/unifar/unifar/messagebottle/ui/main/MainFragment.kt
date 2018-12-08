package unifar.unifar.messagebottle.ui.main

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.os.Debug
import android.support.v4.app.Fragment
import android.util.Log
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
        return view
    }

    private lateinit var db: FirebaseFirestore

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        db = FirebaseFirestore.getInstance()
    }

}
