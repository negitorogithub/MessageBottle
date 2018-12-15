package unifar.unifar.messagebottle


import android.content.Context
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
import android.content.Context.MODE_PRIVATE
import android.content.Context.RECEIVER_VISIBLE_TO_INSTANT_APPS
import android.content.SharedPreferences
import android.opengl.Visibility
import android.os.Handler
import com.google.android.gms.common.util.concurrent.HandlerExecutor


class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
        const val lastGetTimeKey = "lastGetTimeKey"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val view = inflater.inflate(R.layout.fragment_main, container, false)
        val inkView = view.findViewById(R.id.inkBottleImageView) as ImageView
        val bottleView = view.findViewById(R.id.messageBottleImageView) as ImageView


        val second2milli = 1000L
        val getIntervalSecond = 40L
        val reshowTimeIntervalSecond = 1L


        object :  Runnable{
            override fun run() {
                val lastGetTime = activity?.getSharedPreferences("DataSave", Context.MODE_PRIVATE)?.getLong(lastGetTimeKey, 0L)
                lastGetTime?.let {
                    Log.d("myapp", "delta:${System.currentTimeMillis() - lastGetTime}")
                    Log.d("myapp", "now:${System.currentTimeMillis()}")
                    Log.d("myapp", "last:$lastGetTime")

                    if (System.currentTimeMillis() - lastGetTime > getIntervalSecond * second2milli) {
                        bottleView.visibility = View.VISIBLE
                    }else{
                        bottleView.visibility = View.INVISIBLE
                    }
                }
                Handler().postDelayed(this, reshowTimeIntervalSecond * second2milli)
            }
        }.run()

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
                                activity?.getSharedPreferences("DataSave", Context.MODE_PRIVATE)?.edit()
                                    ?.putLong(lastGetTimeKey, System.currentTimeMillis())
                                    ?.apply()

                                val data2 = task2.result?.data
                                val content = data2?.get("message")?.toString()
                                content?.let {
                                    fragmentManager?.beginTransaction()
                                        ?.replace(R.id.container, ShowMessageFragment.newInstance(content))?.commit()
                                }
                            }
                            //最後にカウンター更新とローカルカウンタ更新
                            .addOnCompleteListener {
                                dbSize?.let {
                                    if (nextCursor <= dbSize)
                                        db.collection("counters")
                                            .document("content")
                                            .update("cursor", nextCursor)
                                }
                            }
                    }
                }
        }



        return view
    }


}
