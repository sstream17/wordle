package com.example.wordle

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.wordle.databinding.FragmentGameBoardBinding
import com.example.wordle.util.flipListOfTextViews
import com.example.wordle.util.shakeAnimation
import com.example.wordle.util.slightlyScaleUpAnimation
import com.example.wordle.util.winAnimator
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [GameBoardFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GameBoardFragment : Fragment() {

    private var _binding: FragmentGameBoardBinding? = null
    private val binding get() = _binding!!

    private val viewModel by activityViewModels<WordleViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGameBoardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val listOfTextViews = listOf(
            listOf(
                binding.firstRow1,
                binding.firstRow2,
                binding.firstRow3,
                binding.firstRow4,
                binding.firstRow5
            ),
            listOf(
                binding.secondRow1,
                binding.secondRow2,
                binding.secondRow3,
                binding.secondRow4,
                binding.secondRow5
            ),
            listOf(
                binding.thirdRow1,
                binding.thirdRow2,
                binding.thirdRow3,
                binding.thirdRow4,
                binding.thirdRow5
            ),
            listOf(
                binding.fourthRow1,
                binding.fourthRow2,
                binding.fourthRow3,
                binding.fourthRow4,
                binding.fourthRow5
            ),
            listOf(
                binding.fifthRow1,
                binding.fifthRow2,
                binding.fifthRow3,
                binding.fifthRow4,
                binding.fifthRow5
            ),
            listOf(
                binding.sixthRow1,
                binding.sixthRow2,
                binding.sixthRow3,
                binding.sixthRow4,
                binding.sixthRow5
            )
        )

        val lettersRow = listOf(
            binding.firstLettersRow,
            binding.secondLettersRow,
            binding.thirdLettersRow,
            binding.fourthLettersRow,
            binding.fifthLettersRow,
            binding.sixthLettersRow
        )

        listOfTextViews.forEachIndexed { rows, list ->
            list.forEachIndexed { cols, textView ->
                lifecycleScope.launch {
                    viewModel.listOfTextViews[rows][cols].collect { s ->
                        if (s.letter != " " && s.backgroundColor == R.color.white) {
                            slightlyScaleUpAnimation(textView)
                        }
                        textView.apply {
                            text = s.letter
                            background = resources.getDrawable(s.backgroundColor)
                            setTextColor(resources.getColor(s.textColor))
                        }
                    }
                }
            }
        }

        var shakeAnimation = shakeAnimation(lettersRow[viewModel.currentPosition.row])

        lifecycleScope.launch {
            viewModel.signal.collect {
                when (it) {
                    Signal.NOTAWORD -> {
                        shakeAnimation()
                    }
                    Signal.NEEDLETTER -> {
                        shakeAnimation()
                    }
                    Signal.NEXTTRY -> {
                        flip(
                            listOfTextViews[viewModel.currentPosition.row],
                            viewModel.checkColor(),
                        ) {
                            viewModel.emitColor()
                            viewModel.currentPosition.nextRow()
                            shakeAnimation =
                                shakeAnimation(lettersRow[viewModel.currentPosition.row])
                        }
                    }
                    Signal.GAMEOVER -> {
                        flip(
                            listOfTextViews[viewModel.currentPosition.row],
                            viewModel.checkColor(),
                        ) {
                            viewModel.emitColor()
                            viewModel.resetGame()
                            shakeAnimation =
                                shakeAnimation(lettersRow[viewModel.currentPosition.row])
                        }
                    }
                    Signal.WIN -> {
                        val pos = viewModel.currentPosition.row
                        val tws = listOfTextViews[pos]

                        flip(
                            tws,
                            viewModel.checkColor(),
                        ) {
                            winAnimator(tws) {
                                viewModel.emitColor()
                                viewModel.resetGame()
                                shakeAnimation =
                                    shakeAnimation(lettersRow[viewModel.currentPosition.row])
                            }.start()
                        }
                    }
                }
            }
        }
    }

    private fun flip(
        listOfTextViews: List<TextView>,
        letters: List<Letter>,
        reset: Boolean = false,
        doOnEnd: () -> Unit
    ) {
        flipListOfTextViews(
            listOfTextViews,
            letters,
            reset = reset
        ) {
            doOnEnd()
        }.start()
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}