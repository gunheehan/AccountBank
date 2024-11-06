package com.redhorse.accountbank

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mainboardFragment = MainboardFragment()
        val earningFragment = EarningFragment()
        val expensesFragment = ExpensesFragment()
        val fixedInformationFragment = FixedInformationFragment()
        val yearFragment = YearFragment()

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavi)
        replaceFragment(mainboardFragment)

        bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.mainItem -> replaceFragment(mainboardFragment)
                R.id.earningItem -> replaceFragment(earningFragment)
                R.id.expensesItem -> replaceFragment(expensesFragment)
                R.id.fixedsettingItem -> replaceFragment(fixedInformationFragment)
                R.id.yearItem -> replaceFragment(yearFragment)
            }
            true
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        if(fragment != null){
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frame_layout, fragment)
            transaction.commit()
        }
    }
}