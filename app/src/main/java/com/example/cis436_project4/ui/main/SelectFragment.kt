package com.example.cis436_project4.ui.main

import android.R
import android.graphics.Color
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.activityViewModels
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.cis436_project4.databinding.FragmentSelectBinding
import org.json.JSONArray
import org.json.JSONObject

class SelectFragment : Fragment() {

    companion object {
        fun newInstance() = SelectFragment()
    }

    private val viewModel : MainViewModel by activityViewModels()
    private lateinit var binding : FragmentSelectBinding
    lateinit var PokeSpecies: JSONObject
    lateinit var DescArray: JSONArray
    lateinit var FormArray: JSONArray

    var LastPokeName = ""
    var LastPokeNum = ""
    var DescIndex = 0
    var FormIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //viewModel = ViewModelProvider(this).get(ViewModel::class.java)
        // TODO: Use the ViewModel


        fun formatWords(input: String) : String {         //replace dashes with spaces, and capitalize all words.
            var output = input
                .split('-')
                .joinToString(" ") { it.replaceFirstChar(Char::uppercaseChar) }
            return output
        }


        fun getPokeForm(specifyAltURL: String) {
            var FormUrl = specifyAltURL

            val queue = Volley.newRequestQueue(requireContext())
            // Request a string response from the provided URL.
            val stringRequest = StringRequest(
                Request.Method.GET, FormUrl,
                Response.Listener<String> { response ->
                    var PokeForm = JSONObject(response)
                    var abilities = PokeForm.getJSONArray("abilities")

                    Log.i(
                        "SelectFragment",
                        "Pokemon form's name: ${PokeForm.getString("name").capitalize()}"
                    )
                    for (i in 0 until abilities.length()) {
                        var ability: JSONObject = abilities.getJSONObject(i)

                        Log.i("SelectFragment",
                            "Pokemon has the ${ability.getJSONObject("ability").getString("name")}")
                    }

                    viewModel.setPokemonForm(PokeForm)

                },
                Response.ErrorListener {
                    Log.i("SelectFragment",
                        "Pokemon form not found! If you see this, contact the creator, as this is likely a bug.")
                })
            // Add the request to the RequestQueue.
            queue.add(stringRequest)
        }


        fun changePokeForm(newPokemon : Boolean = false) {
            if (FormArray.length() == 1 && newPokemon == false) {
                Log.i("SelectFragment", "No Alternate Forms!")
                return
            }

            FormIndex++
            if (newPokemon || FormIndex >= FormArray.length())
                FormIndex = 0

            var form: JSONObject = FormArray.getJSONObject(FormIndex)
            Log.i(
                "SelectFragment",
                "Pokemon Form: ${form.getJSONObject("pokemon").getString("name")}"
            )

            getPokeForm(form.getJSONObject("pokemon").getString("url"))
        }


        fun changePokeDesc(newPokemon : Boolean = false) {
            if (DescArray.length() == 0) {
                Log.i("SelectFragment", "No Pokedex Entry Available.")
                viewModel.setPokemonDesc("\nNo Pokedex Entries Available.")
                return
            }

            DescIndex++
            if (newPokemon || DescIndex >= DescArray.length())
                DescIndex = 0

            var desc: JSONObject = DescArray.getJSONObject(DescIndex)
            while (desc.getJSONObject("language").getString("name") != "en") {
                DescIndex++
                if (DescIndex >= DescArray.length())
                    DescIndex = 0
                desc = DescArray.getJSONObject(DescIndex)
            }

            Log.i(
                "SelectFragment",
                "Pokedesc: ${desc.getString("flavor_text")}"
            )
            Log.i(
                "SelectFragment",
                "Version: ${desc.getJSONObject("version").getString("name")}"
            )

            var gameVersionText = formatWords(desc.getJSONObject("version").getString("name"))

            var flavorText = desc.getString("flavor_text")
                .replace("\n", " ")
                .replace("\u000c", " ")

            var descString = "\n[Pokemon $gameVersionText]" + "\n$flavorText\n"
            viewModel.setPokemonDesc(descString)
        }


        // method to interact with API
        fun getPokeData(specifyURL : String) {

            var PokeUrl = "https://pokeapi.co/api/v2/pokemon-species/" + specifyURL

            val queue = Volley.newRequestQueue(requireContext())
            // Request a string response from the provided URL.
            val stringRequest = StringRequest(
                Request.Method.GET, PokeUrl,
                Response.Listener<String> { response ->
                    PokeSpecies = JSONObject(response)
                    DescArray = PokeSpecies.getJSONArray("flavor_text_entries")
                    FormArray = PokeSpecies.getJSONArray("varieties")

                    Log.i(
                        "SelectFragment",
                        "Pokemon name: ${PokeSpecies.getString("name").capitalize()}"
                    )
                    Log.i(
                        "SelectFragment",
                        "Pokemon array of descriptions: ${DescArray}"
                    )

                    LastPokeName = PokeSpecies.getString("name")
                    LastPokeNum = PokeSpecies.getString("id")
                    changePokeForm(true)
                    changePokeDesc(true)


                    viewModel.setPokemonSpecies(PokeSpecies)
                    binding.resultText.text = "The Pokemon was successfully found and is displayed below."
                    binding.resultText.setTextColor(Color.rgb(0, 0, 0))

                },
                Response.ErrorListener {
                    Log.i("SelectFragment", "No Pokemon found. Did you mistype its name?")
                    binding.resultText.text = ("No Pokemon found. Did you mistype its name?")
                    binding.resultText.setTextColor(Color.rgb(255, 0, 0))
                })
            // Add the request to the RequestQueue.
            queue.add(stringRequest)

        }//end getPokeData



        fun isDouble(input : String) : Boolean {
            try {
                var str = input.toDouble()
                return true
            }
            catch (e: Exception) {
                return false
            }
        }


        fun redundantDouble(input : String) : String {   //check if string is a double that can be converted to int
            var str = input
            if (str.contains(".") == false) {
                return str
            }

            var periodReached = false
            for (i in str) {
                if (periodReached == false && i.toString() == ".") {
                    periodReached = true
                    continue
                }
                if (periodReached == true && !(i.toString() == "0")) {
                    return str
                }
            }
            str = str.toDouble().toInt().toString()
            return str
        }



        binding.selectBtn.setOnClickListener {
            var specifyURL = binding.inputText.text.toString().lowercase()
                .split(' ')
                .joinToString("-")
            if (specifyURL == LastPokeName || specifyURL == LastPokeNum) {
                Log.i("SelectFragment", "Already did that Pokemon! Select another one.")

            }
            else if (isDouble(specifyURL)) {

                specifyURL = redundantDouble(specifyURL)
                Log.i("SelectFragment", "$specifyURL")
                if (specifyURL.toDouble() > 0 && specifyURL.toDouble() <= 1010 && specifyURL.contains(".") == false)
                    getPokeData(specifyURL)
                else {
                    Log.i("SelectFragment", "Invalid Pokedex Number! Please insert an integer from 1-1010.")
                    binding.resultText.text = "Invalid Pokedex Number! Please insert an integer from 1-1010."
                    binding.resultText.setTextColor(Color.rgb(255, 0, 0))
                }
            }
            else {
                getPokeData(specifyURL)
            }
        }


        binding.nextDescBtn.setOnClickListener {
            if (LastPokeName != "" && LastPokeNum != "") {
                changePokeDesc()
            }

        }

        binding.nextFormBtn.setOnClickListener {
            if (LastPokeName != "" && LastPokeNum != "") {
                changePokeForm()
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentSelectBinding.inflate(inflater, container, false)
        return binding.root

    }

}