package com.example.wordle

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.wordle.databinding.GuessBoardItemBinding
import com.example.wordle.util.flipListOfTextViews
import com.example.wordle.util.slightlyScaleUpAnimation
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class GuessBoardAdapter(private val dataSet: List<List<List<MutableStateFlow<Letter>>>>) :
    RecyclerView.Adapter<GuessBoardAdapter.ViewHolder>() {

    private var _binding: GuessBoardItemBinding? = null
    private val binding get() = _binding!!

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    class ViewHolder(viewHolderBinding: GuessBoardItemBinding) : RecyclerView.ViewHolder(viewHolderBinding.root)

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        _binding = GuessBoardItemBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)

        return ViewHolder(binding)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
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
                GlobalScope.launch {
                    dataSet[position][rows][cols].collect { s ->
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

        /*var shakeAnimation = shakeAnimation(lettersRow[viewModel.currentPosition.row])

        GlobalScope.launch {
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
        }*/
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

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

}