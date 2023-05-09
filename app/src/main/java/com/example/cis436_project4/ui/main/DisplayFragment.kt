package com.example.cis436_project4.ui.main

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import coil.load
import com.example.cis436_project4.R
import com.example.cis436_project4.databinding.FragmentDisplayBinding
import org.json.JSONObject

class DisplayFragment : Fragment() {

    companion object {
        fun newInstance() = DisplayFragment()
    }

    private val viewModel : MainViewModel by activityViewModels()
    private lateinit var binding : FragmentDisplayBinding

    private lateinit var PokeSpecies: JSONObject
    private lateinit var PokeForm: JSONObject

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //viewModel = ViewModelProvider(this).get(ViewModel::class.java)
        // TODO: Use the ViewModel

        fun distinctWords(input: String) : String {
            var output = input
                .trim()
                .split(".")
                .distinct()
                .joinToString(", ")
                .dropLast(2)
            if ("(Hidden Ability)" in output) {
                if (output.split(", ").size == 2)
                    output = output.replace(", (Hidden Ability)", "")
                else {
                    output = output.replace(", (Hidden Ability)", " (Hidden Ability)")
                }
            }

            return output
        }

        fun formatWords(input: String) : String {         //replace dashes with spaces, and capitalize all words.
            var output = input
                .split('-')
                .joinToString(" ") { it.replaceFirstChar(Char::uppercaseChar) }
            return output
        }

        fun addPokeClassify(input: String) : String {
            var output = input
            if (output.contains("Pokémon") == false)
                output += "Pokémon"
            return output
        }

        fun classifyFind() : String {
            var classifyList = PokeSpecies.getJSONArray("genera")
            for (i in 0 until classifyList.length()) {
                var cIndex = classifyList.getJSONObject(i)
                if (cIndex.getJSONObject("language").getString("name") == "en") {
                    return addPokeClassify(cIndex.getString("genus"))
                }
            }
            if (classifyList.length() > 0)
                return addPokeClassify(classifyList.getJSONObject(0).getString("genus"))
            return "(No Classification) Pokemon"
        }




        var SpeciesObserver = Observer<JSONObject> {
            result -> PokeSpecies = result
            var id = PokeSpecies.getString("id")
            var name = formatWords(PokeSpecies.getString("name"))
            var classify = classifyFind()

            binding.nameText.text = "#$id: $name"
            binding.classifyText.text = "{The $classify}\n"
        }
        viewModel.getPokemonSpecies().observe(viewLifecycleOwner, SpeciesObserver)




        var FormObserver = Observer<JSONObject> {
            result -> PokeForm = result

            //Picture
            try {
                val imgJSON: JSONObject? = PokeForm.getJSONObject("sprites")
                var imgURL = PokeForm.getJSONObject("sprites")
                    .getJSONObject("other")
                    .getJSONObject("official-artwork")
                    .getString("front_default")

                val img = view?.findViewById<ImageView>(R.id.pokeImage)

                // below line is for loading
                // image url inside imageview.
                img?.load(imgURL) {
                    // placeholder image is the image used
                    // when our image url fails to load.
                    placeholder(R.drawable.questionmark)
                    Log.i("DisplayFragment", "Default values used.")
                }
            }
            catch(e: Exception) {
                var imageID = resources.getIdentifier("@drawable/questionmark", "drawable", getActivity()?.getPackageName())
                Log.i("DisplayFragment", "This should never happen.")
                binding.pokeImage.setImageResource(imageID)
            }


            //Form
            binding.formText.text = "Current Form: " +
                    formatWords(PokeForm.getString("name"))


            //Abilities
            var abilities = PokeForm.getJSONArray("abilities")
            var abilitiesText = ""
            for (i in 0 until abilities.length()) {
                var ability: JSONObject = abilities.getJSONObject(i)
                abilitiesText += "${formatWords(ability.getJSONObject("ability").getString("name"))}"
                if (ability.getBoolean("is_hidden") == true) {
                    abilitiesText += ".(Hidden Ability)"
                }
                abilitiesText += "."
            }

            abilitiesText = "Abilities: " + distinctWords(abilitiesText)
            binding.abilitiesText.text = abilitiesText


            //Typing
            var typings = PokeForm.getJSONArray("types")
            var typingsText = "Typing: "
            for (i in 0 until typings.length()) {
                var type: JSONObject = typings.getJSONObject(i)
                typingsText += "${formatWords(type.getJSONObject("type").getString("name"))}."
            }
            binding.typeText.text = distinctWords(typingsText)


            //Height and Weight
            var h = (PokeForm.getString("height").toDouble()) / 10     //convert to meters
            var w = (PokeForm.getString("weight").toDouble()) / 10     //convert to kilograms
            binding.heightAndWeightText.text = "Height -- $h m, Weight -- $w kg"


        }
        viewModel.getPokemonForm().observe(viewLifecycleOwner, FormObserver)





        var DescObserver = Observer<String> {
            result -> var PokeDesc = result
            binding.descText.text = PokeDesc
        }
        viewModel.getPokemonDesc().observe(viewLifecycleOwner, DescObserver)


    }//end OnActivityCreated



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentDisplayBinding.inflate(inflater, container, false)
        return binding.root

    }

}