package com.dcdeveloper.shootchat

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase

class ShootsActivity : AppCompatActivity() {

    val mAuth = FirebaseAuth.getInstance()
    var shootsListView: ListView? = null
    var emails: ArrayList<String> = ArrayList()
    var shots: ArrayList<DataSnapshot> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shoots)

        shootsListView = findViewById(R.id.shootsListView)
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, emails)
        shootsListView?.adapter = adapter

        FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.currentUser!!.uid).child("shots").addChildEventListener(object: ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                emails.add(p0.child("from").value as String)
                shots.add(p0!!)
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(p0: DatabaseError) {}
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {}
            override fun onChildRemoved(p0: DataSnapshot) {
                var index = 0
                for (shot: DataSnapshot in shots) {
                    if (shot.key == p0.key) {
                        shots.removeAt(index)
                        emails.removeAt(index)
                    }
                    index++
                }
                adapter.notifyDataSetChanged()
            }
        })
        
        shootsListView?.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val snapshot = shots.get(position)

            var intent = Intent(this, ViewShootActivity::class.java)

            intent.putExtra("imageName", snapshot.child("imageName").value as String )
            intent.putExtra("imageUrl", snapshot.child("imageUrl").value as String)
            intent.putExtra("message", snapshot.child("message").value as String)
            intent.putExtra("shotKey", snapshot.key)

            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.shoots, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.createShoot) {
            val intent = Intent(this, CreateShootActivity::class.java)
            startActivity(intent)
        } else if (item?.itemId == R.id.logout) {
            mAuth.signOut()
            finish()
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        mAuth.signOut()
    }
}

