package unifar.unifar.messagebottle

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        if (getSharedPreferences("DataSave", Context.MODE_PRIVATE)?.getLong("installedAt", -1L) == -1L) {
            getSharedPreferences("DataSave", Context.MODE_PRIVATE)
                ?.edit()
                ?.putLong("installedAt", System.currentTimeMillis())
                ?.apply()
        }
            if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MainFragment.newInstance())
                .commitNow()
        }
    }

}
