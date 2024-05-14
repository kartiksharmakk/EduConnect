package com.kartik.tutordashboard.Adapter

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kartik.tutordashboard.R

class OtpAdapter(context: Context, val recyclerView: RecyclerView): RecyclerView.Adapter<OtpAdapter.OtpViewHolder>(){

    inner class OtpViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val editText: EditText = itemView.findViewById(R.id.edtOtp)
        fun bind(){
            editText.addTextChangedListener(object: TextWatcher{
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    if(p0?.length == 1){
                        if (adapterPosition < itemCount - 1) {
                            val nextEditText = (itemView.parent as RecyclerView)
                                .getChildAt(adapterPosition + 1).findViewById<EditText>(R.id.edtOtp)
                            nextEditText.requestFocus()
                        }
                    }
                }
                override fun afterTextChanged(p0: Editable?) {}
            })
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OtpViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.custom_oval_edittext, parent, false)
        return  OtpViewHolder(view)
    }

    override fun getItemCount(): Int {
        return 6
    }

    override fun onBindViewHolder(holder: OtpViewHolder, position: Int) {
        holder.bind()
    }

    fun getOtp(): String {
        val otpStringBuilder = StringBuilder()
        for (i in 0 until itemCount) {
            val editText = (recyclerView.layoutManager as LinearLayoutManager).findViewByPosition(i)?.findViewById<EditText>(R.id.edtOtp)
            editText?.let {
                otpStringBuilder.append(it.text.toString())
            }
        }
        return otpStringBuilder.toString()
    }
}