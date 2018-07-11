package com.example.android.fitnessdemoone

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.activity_home_page.*
import kotlinx.android.synthetic.main.item_row.view.*
import me.digi.sdk.core.DigiMeClient
import me.digi.sdk.core.SDKCallback
import me.digi.sdk.core.SDKException
import me.digi.sdk.core.SDKResponse
import org.jetbrains.anko.doAsync


class HomePageActivity : AppCompatActivity() {

    private lateinit var context: Context
    private var fitnessList = ArrayList<String>()
    private val jsonBodies = ArrayList<JsonObject>()
    private var dummyFitnessData: HashMap<String, ArrayList<String>>? = hashMapOf()
    private var anotherFitnessData = ArrayList<JsonObject>()
    private var count: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)
        context = this

        fitnessList = intent.getStringArrayListExtra("serviceType")

        Log.d("Fitness Intent", fitnessList.toString())

        initSocialFiles()

        for (fileId in fitnessList) requestData().execute(fileId)
        Log.d("Fitness list: ", fitnessList.toString())

        Log.d("Dummy fitness data ", dummyFitnessData.toString())

        recyclerView_HomePage.apply {
            layoutManager = LinearLayoutManager(this@HomePageActivity)
            hasFixedSize()
        }
    }

    private fun initSocialFiles() {
        dummyFitnessData = hashMapOf(ACTIVITY to ArrayList())
    }

    internal inner class requestData : AsyncTask<String, Void, Void>() {
        override fun doInBackground(vararg fileId: String?): Void? {
            DigiMeClient.getInstance().getFileJSON(fileId[0], object : SDKCallback<JsonElement>() {
                override fun succeeded(response: SDKResponse<JsonElement>?) {
                    count += 1

                    val jsonObject = response?.body?.asJsonObject

                    val jsonContent = jsonObject?.get("fileContent")?.asJsonArray

                    // Gets only first object in the array
                    val jsonBody = jsonContent?.get(0)?.asJsonObject
////
//                    for(i in 0 until jsonContent?.size()!!) {
//                        val jsonBoody = jsonContent.get(i)?.asJsonObject
////                        Log.d("JsonBody $i", jsonBoody.toString())
//                        anotherFitnessData.add(jsonBoody!!)
//                    }

//                    Log.d("Size: ", jsonContent.size().toString())
//                    Log.d("Size json content : ", anotherFitnessData.size.toString())

                    jsonBodies.add(jsonBody!!)

                    Log.d("Json Bodies", "$jsonBodies")
//                    jsonBodiesTxt.text = "$jsonBodies"
//                    jsonBodiesTxt.movementMethod = ScrollingMovementMethod()

//
//                    Log.d("Json Body", jsonBody.toString())
//                    jsonBodyTxt.text = jsonBody.toString()
//                    jsonBodyTxt.movementMethod = ScrollingMovementMethod()

//                    Log.d("Json Content", "$jsonContent")
//                    jsonContentTxt.text = "$jsonContent\n"
//                    jsonContentTxt.movementMethod = ScrollingMovementMethod()

                    checkProgress()

                    Log.d("Success: ", count.toString())
                }

                override fun failed(reason: SDKException?) {
                    Log.d("Error: ", reason?.message)
                    count += 1
                    checkProgress()
                    Log.d("Failed: ", count.toString())
                }
            })
            return null
        }
    }

    private fun checkProgress() {
        if (count == fitnessList.size) {
            Log.d("AAAAA", count.toString())
//
            progressBar?.visibility = View.GONE
            loadingTxt?.visibility = View.GONE

            val fitnessAdapter = FitnessDataAdapter(jsonBodies)

            doAsync {
                runOnUiThread {
                    recyclerView_HomePage?.adapter = fitnessAdapter
                }
            }
        } else {
            loadingTxt.text = "Currently loading data: $count of ${fitnessList.size}"
//            toast("Currently loading data: $count of ${jsonBodies.size}")
        }
    }
}

class FitnessDataAdapter(private var fileId: ArrayList<JsonObject>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(viewGroup: ViewGroup, p1: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_row, viewGroup, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = fileId.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val fitnessItems = fileId[position]

        holder.itemView.count_number_txt.text = "Counting #${position + 1}"

        var heart_rate = fitnessItems.get(average_heartrate).toString()
        heart_rate = heart_rate.replace("\"", "")
        holder.itemView.average_heart_rate_txt.text = heart_rate

        var calories = fitnessItems.get(calories).toString()
        calories = calories.replace("\"", "")
        holder.itemView.calories_txt.text = calories

        var steps = fitnessItems.get(steps).toString()
        steps = steps.replace("\"", "")
        holder.itemView.steps_txt.text = steps
    }
}

class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
