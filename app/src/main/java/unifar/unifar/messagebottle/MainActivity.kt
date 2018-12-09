package unifar.unifar.messagebottle

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import unifar.unifar.messagebottle.ui.main.SubmitFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MainFragment.newInstance())
                .commitNow()
        }
    }

}
