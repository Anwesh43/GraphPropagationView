package com.anwesh.uiprojects.graphpropagationanimation

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.anwesh.uiprojects.graphpropagationview.GraphPropagationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GraphPropagationView.create(this)
    }
}
