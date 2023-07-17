package com.example.wordle

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.wordle.database.StatisticDatabase
import com.example.wordle.database.StatisticEntity
import com.example.wordle.databinding.FragmentGameScreenBinding
import com.example.wordle.databinding.StatisticDialogBinding
import com.example.wordle.util.showInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


class GameScreenFragment : Fragment() {

    private var _binding: FragmentGameScreenBinding? = null
    private val binding get() = _binding!!

    private val viewModel by activityViewModels<WordleViewModel>()

    private lateinit var database: StatisticDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentGameScreenBinding.inflate(inflater, container, false)

        database = (activity!!.application as WordleApplication).database

        CoroutineScope(Dispatchers.Default).launch {
            val currentStat = database.statisticDao().getStat()

            if (currentStat == null) {
                database.statisticDao().insert(StatisticEntity())
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val keyboardMap = mapOf<String, Button>(
            "A" to binding.A,
            "B" to binding.B,
            "C" to binding.C,
            "D" to binding.D,
            "E" to binding.E,
            "F" to binding.F,
            "G" to binding.G,
            "H" to binding.H,
            "I" to binding.I,
            "J" to binding.J,
            "K" to binding.K,
            "L" to binding.L,
            "M" to binding.M,
            "N" to binding.N,
            "O" to binding.O,
            "P" to binding.P,
            "Q" to binding.Q,
            "R" to binding.RR,
            "S" to binding.S,
            "T" to binding.T,
            "U" to binding.U,
            "V" to binding.V,
            "W" to binding.W,
            "X" to binding.X,
            "Y" to binding.Y,
            "Z" to binding.Z,
        )

        keyboardMap.forEach { (letter, button) ->
            lifecycleScope.launch {
                viewModel.listOfKeys[letter]!!.collect { key ->
                    button.apply {
                        setBackgroundColor(resources.getColor(key.backgroundColor))
                        setTextColor(resources.getColor(key.textColor))
                    }
                }
            }
            button.setOnClickListener {
                viewModel.setLetter(letter)
            }
        }

        binding.deleteButton.setOnClickListener {
            viewModel.deleteLetter()
        }

        binding.enterButton.setOnClickListener {
            lifecycleScope.launch {
                viewModel.checkRow()
            }
        }


        val inflater = requireActivity().layoutInflater

        val binderDialog = StatisticDialogBinding.inflate(inflater)
        val builder =
            AlertDialog.Builder(requireContext()).apply {
                setView(binderDialog.root)
            }.create()
        binderDialog.next.setOnClickListener {
            builder.cancel()
        }


        lifecycleScope.launch {
            viewModel.signal.collect {
                when (it) {
                    Signal.NOTAWORD -> {
                        showInfo(binding.info, "Not in word list")
                    }
                    Signal.NEEDLETTER -> {
                        showInfo(binding.info, "Not enough letters")
                    }
                    Signal.NEXTTRY -> Unit
                    Signal.GAMEOVER -> {
                        CoroutineScope(Dispatchers.Default).launch {
                            val currentStat = database.statisticDao().getStat()
                            currentStat!!.lost()
                            bindDialog(binderDialog, currentStat)
                            database.statisticDao().update(currentStat)
                        }
                        showInfo(binding.info, viewModel.wordle)

                    }
                    Signal.WIN -> {
                        val pos = viewModel.currentPosition.row

                        when (pos) {
                            0 -> showInfo(binding.info, "Genius")
                            1 -> showInfo(binding.info, "Magnificent")
                            2 -> showInfo(binding.info, "Impressive")
                            3 -> showInfo(binding.info, "Splendid")
                            4 -> showInfo(binding.info, "Great")
                            5 -> showInfo(binding.info, "Phew")
                        }

                        CoroutineScope(Dispatchers.Default).launch {
                            val currentStat = database.statisticDao().getStat()
                            currentStat!!.won(pos)
                            bindDialog(binderDialog, currentStat)
                            database.statisticDao().update(currentStat)
                        }
                        builder.show()
                    }
                }
            }
        }
    }

    private fun bindDialog(binding: StatisticDialogBinding, stats: StatisticEntity) {
        binding.apply {
            var totalWin =
                (stats.first + stats.second + stats.third + stats.fourth + stats.fifth + stats.sixth)
            totalWin = if (totalWin == 0) 1 else totalWin

            played.text = stats.played.toString()
            winPercentage.text = winRatio(stats).toString()
            currentStreak.text = stats.streak.toString()
            maxStreak.text = stats.maxStreak.toString()

            one.title = "1"
            one.percentage = stats.first / totalWin.toFloat()
            one.countText = stats.first.toString()

            two.title = "2"
            two.percentage = stats.second / totalWin.toFloat()
            two.countText = stats.second.toString()

            three.title = "3"
            three.percentage = stats.third / totalWin.toFloat()
            three.countText = stats.third.toString()

            four.title = "4"
            four.percentage = stats.fourth / totalWin.toFloat()
            four.countText = stats.fourth.toString()

            five.title = "5"
            five.percentage = stats.fifth / totalWin.toFloat()
            five.countText = stats.fifth.toString()

            six.title = "6"
            six.percentage = stats.sixth / totalWin.toFloat()
            six.countText = stats.sixth.toString()
        }
    }

    fun winRatio(stats: StatisticEntity): Int {
        val totalWin =
            (stats.first + stats.second + stats.third + stats.fourth + stats.fifth + stats.sixth)
        val ratio = totalWin.toFloat() / stats.played
        return (ratio * 100).toInt()
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}
