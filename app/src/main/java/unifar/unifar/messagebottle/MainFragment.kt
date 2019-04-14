package unifar.unifar.messagebottle


import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Space
import com.google.firebase.firestore.FirebaseFirestore
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig
import unifar.unifar.messagebottle.ui.main.SubmitFragment
import java.util.*
import kotlin.random.Random


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
        val backView = view.findViewById(R.id.BackImageView) as ImageView
        val dummyView = view.findViewById(R.id.dummy) as Space


        // sequence example
        val config = ShowcaseConfig()
        config.delay = 500 // half second between each showcase view


        val tutorialID = "tutorial1"
        val sequence = MaterialShowcaseSequence(activity, tutorialID)

        sequence.setConfig(config)
        sequence.addSequenceItem(
            dummyView,
            resources.getString(R.string.thank), resources.getString(R.string.gotit)
        ).addSequenceItem(
            bottleView,
            resources.getString(R.string.bottle1), resources.getString(R.string.gotit)
        ).addSequenceItem(
            bottleView,
            resources.getString(R.string.bottle2), resources.getString(R.string.gotit)
        ).addSequenceItem(
            inkView,
            resources.getString(R.string.ink), resources.getString(R.string.gotit)
        )
        sequence.start()

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


        val refreshBackTimeIntervalSecond = 1L
        object :  Runnable{
            override fun run() {
                val currentHour = Calendar.getInstance().get(Calendar.HOUR)
                if ((currentHour <= 6)||(currentHour >= 20)){
                    backView.setImageResource(R.drawable.back)
                }else{
                    backView.setImageResource(R.drawable.back_night_2)
                }
                Handler().postDelayed(this, refreshBackTimeIntervalSecond * second2milli)
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
                    val installedAt = activity?.getSharedPreferences("DataSave", Context.MODE_PRIVATE)?.getLong("installedAt", -1L)
                    Log.d("myapp", "cursor:$cursor")

                    if (cursor == dbSize){
                        cursor?.let {
                            db.collection("messages")
                                .document("${Random.nextInt(1, cursor - 1)}")
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
                        }
                    }else {
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
                                    val androidId = data2?.get("androidId")?.toString()?.toLong()
                                    if (androidId == installedAt) {
                                        cursor.let {
                                            db.collection("messages")
                                                .document("${Random.nextInt(1, cursor - 1)}")
                                                .get()
                                                .addOnCompleteListener { task2 ->
                                                    activity?.getSharedPreferences("DataSave", Context.MODE_PRIVATE)
                                                        ?.edit()
                                                        ?.putLong(lastGetTimeKey, System.currentTimeMillis())
                                                        ?.apply()
                                                    val data3 = task2.result?.data
                                                    val content2 = data3?.get("message")?.toString()
                                                    content2?.let {
                                                        fragmentManager?.beginTransaction()
                                                            ?.replace(
                                                                R.id.container,
                                                                ShowMessageFragment.newInstance(content2)
                                                            )?.commit()
                                                    }
                                                }
                                        }
                                    }else {
                                        content?.let {
                                            fragmentManager?.beginTransaction()
                                                ?.replace(R.id.container, ShowMessageFragment.newInstance(content))
                                                ?.commit()
                                        }
                                        //最後にカウンター更新とローカルカウンタ更新
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
                }
        }
        return view
    }
}
