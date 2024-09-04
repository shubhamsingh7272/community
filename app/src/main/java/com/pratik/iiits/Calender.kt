package com.pratik.iiits

import android.content.ContentValues.TAG
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.sundeepk.compactcalendarview.CompactCalendarView
import com.github.sundeepk.compactcalendarview.CompactCalendarView.CompactCalendarViewListener
import com.github.sundeepk.compactcalendarview.domain.Event
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.time.Month
import java.time.Year
import java.util.*
import kotlin.collections.ArrayList


class Calender : AppCompatActivity() ,AlertDialogform.EventFormDailogListner{

    lateinit var calender: CompactCalendarView
    lateinit var dateformatMonth: SimpleDateFormat
    lateinit var currentmonth:TextView
    lateinit var currentyear:TextView
    lateinit var mondaytimetable: ArrayList<DynamicRvModel>
    lateinit var tuesdaytimetable: ArrayList<DynamicRvModel>
    lateinit var Wedtimetable: ArrayList<DynamicRvModel>
    lateinit var thustimetable: ArrayList<DynamicRvModel>
    lateinit var fridaytimetable: ArrayList<DynamicRvModel>
    lateinit var sattimetable: ArrayList<DynamicRvModel>
    lateinit var customtable : ArrayList<DynamicRvModel>
    lateinit var dynamicRecycler: DynamicRecycler
    lateinit var templist : ArrayList<DynamicRvModel>
    lateinit var recyclerView: RecyclerView
    lateinit var currdate: Date
    lateinit var database : FirebaseDatabase
    lateinit var yearlayout :LinearLayout

    var seclist = arrayOf("UG2/Sec1", "UG2/Sec2", "UG2/Sec3", "UG2/Sec4")
    var sec = seclist[3];
    var calenderopen = true
    val customeventimage = "https://bit.ly/3SvqVCV"
    lateinit var timetableug2 : DatabaseReference
    val formDialog : AlertDialogform = AlertDialogform()
    lateinit var fab:FloatingActionButton
    lateinit var  pref:SharedPreferences
    lateinit var editor : SharedPreferences.Editor
    lateinit var gson: Gson
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calender)

        hook();
        getdata();
        recyclerView.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
        currdate = Date()
        val actionBar: ActionBar? = supportActionBar
        if (actionBar != null) {
            actionBar.title= null
        }
        calender.setUseThreeLetterAbbreviation(true)

        currentmonth.text = firstlettercapital(Month.of(SimpleDateFormat("MM", Locale.getDefault()).format(Date()).toInt()).name)

        calender.setListener(object : CompactCalendarViewListener {
            override fun onDayClick(dateClicked: Date) {
                currdate = dateClicked
                val temp = SimpleDateFormat("MMM dd yyyy")
                val json: String? = pref.getString(temp.format(dateClicked).toString() ,null)
                val type = object : TypeToken<java.util.ArrayList<DynamicRvModel?>?>() {}.type
                customtable.clear()
                if (json != null) {
                    customtable = gson.fromJson(json,type)
                }

                showevents(dateClicked.day-1)

            }

            override fun onMonthScroll(date: Date) {
                Log.d(TAG, "Month was scrolled to: $date")
                currentmonth.text= firstlettercapital(Month.of(date.month+1).name)
            }
        })



    }
    var itemTouchHelperCallback: ItemTouchHelper.SimpleCallback =
        object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                if(templist[viewHolder.adapterPosition].image == customeventimage){

                    customtable.remove(customtable.get(viewHolder.adapterPosition))

                    saveevent(SimpleDateFormat("MMM dd yyyy").format(currdate))
                }
                templist.removeAt(viewHolder.adapterPosition)
                val toast = Toast.makeText(this@Calender,"Event deleted successfully!",Toast.LENGTH_SHORT)
                toast.view?.setBackgroundColor(Color.parseColor("#FF6200"))
                toast.show()
                dynamicRecycler.notifyDataSetChanged()
            }
        }

    private fun showtodayevents(date: Date) {
        val temp:String = SimpleDateFormat("MMM dd yyyy",Locale.getDefault()).format(date)
        val json: String? = pref.getString(temp ,null)
        val type = object : TypeToken<java.util.ArrayList<DynamicRvModel?>?>() {}.type
        customtable.clear()
        if (json != null) {
            customtable = gson.fromJson(json,type)
        }

        showevents(date.day-1)
    }

    private fun showevents(day: Int) {
        val list = listOf<ArrayList<DynamicRvModel>>(mondaytimetable,tuesdaytimetable,Wedtimetable,thustimetable,fridaytimetable,sattimetable)
        templist = ArrayList()
        for(event in customtable)templist.add(event)
        if(day>=0)templist.addAll(list[day])
        dynamicRecycler = DynamicRecycler(templist)
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView)

        recyclerView.adapter = dynamicRecycler
        recyclerView.scheduleLayoutAnimation()

    }
    private fun getdata() {
        timetableug2.addValueEventListener(object: ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                for (dataSnapshot in snapshot.children) {
                    if (dataSnapshot.key == "Monday") {
                        for (datasnapshot2 in dataSnapshot.children){
                            for(datasnapshot3 in datasnapshot2.children)
                            mondaytimetable.add(DynamicRvModel(datasnapshot2.key,datasnapshot3.key,datasnapshot3.getValue(String::class.java)))
                        }
                    }
                    if (dataSnapshot.key == "Tuesday") {
                        for (datasnapshot2 in dataSnapshot.children){
                            for(datasnapshot3 in datasnapshot2.children)
                                tuesdaytimetable.add(DynamicRvModel(datasnapshot2.key,datasnapshot3.key,datasnapshot3.getValue(String::class.java)))
                        }
                    }
                    if (dataSnapshot.key == "Wednesday") {
                        for (datasnapshot2 in dataSnapshot.children){
                            for(datasnapshot3 in datasnapshot2.children)
                                Wedtimetable.add(DynamicRvModel(datasnapshot2.key,datasnapshot3.key,datasnapshot3.getValue(String::class.java)))
                        }
                    }
                    if (dataSnapshot.key == "Thursday") {
                        for (datasnapshot2 in dataSnapshot.children){
                            for(datasnapshot3 in datasnapshot2.children)
                                thustimetable.add(DynamicRvModel(datasnapshot2.key,datasnapshot3.key,datasnapshot3.getValue(String::class.java)))
                        }
                    }
                    if (dataSnapshot.key == "Friday") {
                        for (datasnapshot2 in dataSnapshot.children){
                            for(datasnapshot3 in datasnapshot2.children)
                                fridaytimetable.add(DynamicRvModel(datasnapshot2.key,datasnapshot3.key,datasnapshot3.getValue(String::class.java)))
                        }
                    }
                    if (dataSnapshot.key == "Saturday") {
                        for (datasnapshot2 in dataSnapshot.children){
                            for(datasnapshot3 in datasnapshot2.children)
                                sattimetable.add(DynamicRvModel(datasnapshot2.key,datasnapshot3.key,datasnapshot3.getValue(String::class.java)))
                        }
                    }
                }
                showtodayevents(Date())
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read value.", error.toException())
            }

        })
    }
    var rotatecheck = true
    fun addevent(view: View) {
        fab = view as FloatingActionButton
        if(rotatecheck)rotateFabForward(fab)
        else rotateFabBackward(fab)
        rotatecheck = !rotatecheck
//        Toast.makeText(this,"clicked",Toast.LENGTH_SHORT).show()
        showEventForm();
    }
    private fun showEventForm(){
        formDialog.show(supportFragmentManager,"Event Form Dialog");
        formDialog.isCancelable=false
    }
    override fun appplyTexts(label: String?, Date: String?, Time: String?) {
        formDialog.dismiss()
        rotateFabBackward(fab)
        if (label != null && Date != null && Time !=null) {
            //Toast.makeText(this, " $label $Date $Time",Toast.LENGTH_SHORT).show()
            val df = SimpleDateFormat("MMM dd yyyy HH:mm:ss.SSS zzz")
            val date = df.parse("$Date $Time")
            val epoch: Long = date.time

            calender.addEvent(Event(Color.WHITE,epoch,label))
            val temp = SimpleDateFormat("MMM dd yyyy")
            val json: String? = pref.getString(Date,null)
            val type = object : TypeToken<java.util.ArrayList<DynamicRvModel?>?>() {}.type
            customtable.clear()
            if (json != null) {
                customtable = gson.fromJson(json,type)
            }
            val timeformat = SimpleDateFormat("HH:mm aa")
            val name:String = timeformat.format(epoch).toString()
            customtable.add(0,DynamicRvModel(name,label,customeventimage))
            saveevent(Date)
            if (date != null) {
                showtodayevents(date)
            }
        }
    }
    private fun saveevent(date: String){
        val json:String = gson.toJson(customtable)
        editor.putString(date,json).apply()
    }

    fun open_calender(view: View) {
        calender.clearAnimation()

        if(!calenderopen)
        calender.showCalendar()
        else calender.hideCalendar()
        calenderopen=!calenderopen
    }

    private fun hook() {
        gson = Gson()
        pref = getSharedPreferences("Events", MODE_PRIVATE)
        editor = pref.edit()
        calender = findViewById<CompactCalendarView>(R.id.calendarview);
        dateformatMonth = SimpleDateFormat("MMMM-yyyy", Locale.getDefault())
        currentmonth= findViewById(R.id.currmonth)
       // currentyear = findViewById<TextView>(R.id.curryear)
        mondaytimetable = ArrayList<DynamicRvModel>()
        tuesdaytimetable= ArrayList<DynamicRvModel>()
        Wedtimetable = ArrayList<DynamicRvModel>()
        thustimetable= ArrayList<DynamicRvModel>()
        fridaytimetable = ArrayList<DynamicRvModel>()
        sattimetable= ArrayList<DynamicRvModel>()
        recyclerView = findViewById(R.id.rv)
        database = Firebase.database
        yearlayout = findViewById<LinearLayout>(R.id.yearlayout);
        timetableug2 = database.getReference("TimeTable/$sec")
        customtable= ArrayList<DynamicRvModel>()
    }
    fun rotateFabForward(fab: FloatingActionButton) {
        ViewCompat.animate(fab)
            .rotation(135.0f)
            .withLayer()
            .setDuration(300L)
            .setInterpolator(OvershootInterpolator(10.0f))
            .start()
    }

    fun rotateFabBackward(fab: FloatingActionButton) {
        ViewCompat.animate(fab)
            .rotation(0.0f)
            .withLayer()
            .setDuration(300L)
            .setInterpolator(OvershootInterpolator(10.0f))
            .start()
    }
    fun firstlettercapital(myString: String): String {
        return myString.substring(0, 1).toUpperCase() + myString.substring(1).toLowerCase()
    }

}