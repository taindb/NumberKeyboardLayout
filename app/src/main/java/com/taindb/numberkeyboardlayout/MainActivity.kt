package com.taindb.numberkeyboardlayout

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import com.taindb.library.NumberKeyboardLayout

class MainActivity : AppCompatActivity() {

    private lateinit var numberInput : EditText

    private lateinit var keyboardLayout: NumberKeyboardLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        numberInput = findViewById(R.id.edtNumber)
        keyboardLayout = findViewById(R.id.numberKeyBoard)

        keyboardLayout.registerEditText(numberInput)
        numberInput.requestFocus()
    }
}
