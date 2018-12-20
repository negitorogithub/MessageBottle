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
import unifar.unifar.messagebottle.MainFragment
import java.util.*



class SubmitFragment : Fragment() {

    companion object {
        fun newInstance() = SubmitFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.submit_fragment, container, false)

        val submitButton = view.findViewById(R.id.submitButton) as Button
        val messageEditText = view.findViewById(R.id.messageEditText) as EditText

        val db = FirebaseFirestore.getInstance()

        submitButton.setOnClickListener {button ->
            button.isEnabled = false
            submitButton.text = resources.getString(R.string.sendingMessage)
            val content = messageEditText.text.toString()
            if (content.isEmpty()){
                Toast.makeText(activity, resources.getText(R.string.fillblank), Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            //まずカウンター取得
            db.collection("counters")
                .document("content")
                .get()
                //次に内容送信
                .addOnCompleteListener { task1 ->
                    val data = task1.result?.data
                    val dbSize = data?.get("size")?.toString()?.toInt()
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
                            .addOnCompleteListener {
                                db.collection("counters")
                                    .document("content")
                                    .update("size", dbSize + 1)
                                    .addOnSuccessListener {
                                        Toast.makeText(activity, resources.getText(R.string.success), Toast.LENGTH_LONG).show()
                                        fragmentManager?.beginTransaction()?.replace(R.id.container, MainFragment.newInstance())?.commitAllowingStateLoss()
                                    }
                            }
                            .addOnFailureListener {
                                button.isEnabled = true
                            }
                    }
                }
        }

        return view
    }

}
