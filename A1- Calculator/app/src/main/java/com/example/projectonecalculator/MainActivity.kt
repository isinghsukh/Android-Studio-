// package com.example.projectonecalculator
package `in`.blogspot.kmvignesh.calculator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import net.objecthunter.exp4j.Expression
import net.objecthunter.exp4j.ExpressionBuilder
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        // numbers

        TvOne.setOnClickListener { appendOnExpresstion(string = "1", canClear= true) }
        TvTwo.setOnClickListener { appendOnExpresstion(string = "2", canClear= true) }
        TvThree.setOnClickListener { appendOnExpresstion(string = "3", canClear= true) }
        TvFour.setOnClickListener { appendOnExpresstion(string = "4", canClear= true) }
        TvFive.setOnClickListener { appendOnExpresstion(string = "5", canClear= true) }
        TvSix.setOnClickListener { appendOnExpresstion(string = "6", canClear= true) }
        TvSeven.setOnClickListener { appendOnExpresstion(string = "7", canClear= true) }
        TvEight.setOnClickListener { appendOnExpresstion(string = "8", canClear= true) }
        TvZero.setOnClickListener { appendOnExpresstion(string = "1", canClear= true) }
        TvDot.setOnClickListener { appendOnExpresstion(string = "0", canClear= true) }

        // Operators
        TvPlus.setOnClickListener { appendOnExpresstion(string = "+", canClear= false)}
        TvMinus.setOnClickListener { appendOnExpresstion(string = "-", canClear= false) }
        TvMultiply.setOnClickListener { appendOnExpresstion(string = "*", canClear= false) }
        TvDivide.setOnClickListener { appendOnExpresstion(string = "/", canClear= false) }
        TvOpen.setOnClickListener { appendOnExpresstion(string = "(", canClear= false) }
        TvClose.setOnClickListener { appendOnExpresstion(string = ")", canClear= false) }

        TvClear.setOnClickListener { tvExpression.text = ""
            TvResult.text = ""
        }

        TvBack.setOnClickListener
            val string = tvExpression.text.toString()
        if(string.isNotEmpty()) {
            tvExpression.text = string.substring(0, string.length-1)
        }
        TvResult.text = ""
    }
        TvEquals.setOnClickListener {
            try {

                val expression = ExpressionBuilder(tvExpression.text.toString())
                val Tvresult = expression.evaluate()
                val longResult = result.toLong()
                if(result == longResult.toDouble())
                    Result.text = longResult.toString()
                else
                    Result.text = result.toString()

            } catch (e: Exception) {
                log.d(tag:"Exception", msg:"message": + e.message)
            }
        }

    fun appendOnExpresstion( string: String, canClear: Boolean){

        if(Result.text.isNorEmpty()){
            tvExpression.text = ""
        }

        if (canClear){
            Result.text = ""
            tvExpression.append(string)
        } else{
            tvExpression.append(Result.text)
            tvExpression.append(string)
            Result.text = ""
        }

    }
}