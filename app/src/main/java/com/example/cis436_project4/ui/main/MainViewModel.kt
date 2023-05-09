package com.example.cis436_project4.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.json.JSONArray
import org.json.JSONObject

class MainViewModel : ViewModel() {
    // TODO: Implement the ViewModel

    private var PokemonSpecies : MutableLiveData<JSONObject> = MutableLiveData()
    private var PokemonForm : MutableLiveData<JSONObject> = MutableLiveData()
    private var PokemonDesc : MutableLiveData<String> = MutableLiveData()



    fun setPokemonSpecies(pSpecies : JSONObject) {
        PokemonSpecies.setValue(pSpecies)
    }
    fun getPokemonSpecies() : MutableLiveData<JSONObject> {
        return PokemonSpecies
    }


    fun setPokemonForm(pForm : JSONObject) {
        PokemonForm.setValue(pForm)
    }
    fun getPokemonForm() : MutableLiveData<JSONObject> {
        return PokemonForm
    }


    fun setPokemonDesc(pDesc : String) {
        PokemonDesc.setValue(pDesc)
    }
    fun getPokemonDesc() : MutableLiveData<String> {
        return PokemonDesc
    }






}