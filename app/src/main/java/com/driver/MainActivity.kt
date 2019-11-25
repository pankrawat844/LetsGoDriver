package com.driver

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    lateinit var regularRoboto: Typeface
    lateinit var boldRoboto: Typeface

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        regularRoboto = Typeface.createFromAsset(assets, getString(R.string.font_regular_roboto))
        boldRoboto = Typeface.createFromAsset(assets, getString(R.string.font_bold_roboto))

        //UI
        tv_newuser.typeface = boldRoboto
        tv_signin.typeface = boldRoboto

        layout_sign_up.setOnClickListener {
            val li = Intent(this@MainActivity, RegisterActivity::class.java)
            startActivity(li)
        }

        layout_sign_in.setOnClickListener {
            val li = Intent(this@MainActivity, LoginActivity::class.java)
            startActivity(li)
        }
    }
}
