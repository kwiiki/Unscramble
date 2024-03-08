package com.example.unscramble.ui

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unscramble.R
import com.example.unscramble.ui.theme.UnscrambleTheme
import kotlinx.coroutines.delay


//@Composable
//fun GameScreen(
//    gameViewModel: GameViewModel = viewModel()
//) {
//    val gameUiState by gameViewModel.uiState.collectAsState()
//}
@Composable
fun GameStatus(score: Int, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
    ) {
        Text(
            text = stringResource(R.string.score, score),
            style = typography.headlineMedium,
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Composable
fun GameScreen(gameViewModel: GameViewModel = viewModel()) {
    val mediumPadding = dimensionResource(R.dimen.padding_medium)

    val gameUiState by gameViewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(mediumPadding),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = stringResource(R.string.app_name),
            style = typography.titleLarge,
        )

        GameLayout(
            onUserGuessChanged = { gameViewModel.updateUserGuess(it) },
            onKeyboardDone = { gameViewModel.checkUserGuess() },
            userGuess = gameViewModel.userGuess,
            isGuessWrong = gameUiState.isGuessedWordWrong,
            wordCount = gameUiState.currentWordCount,
            currentScrambledWord = gameUiState.currentScrambledWord,
            time = gameViewModel.getTime(),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(mediumPadding)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(mediumPadding),
            verticalArrangement = Arrangement.spacedBy(mediumPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { gameViewModel.checkUserGuess() },
            ) {
                Text(
                    text = stringResource(R.string.submit),
                    fontSize = 16.sp
                )
            }

            Row(
                modifier = Modifier.fillMaxSize(1f),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { gameViewModel.previous() },
                    enabled = gameViewModel.checkPreviousCurrentWordCount(),
                    modifier = Modifier
                        .weight(0.9f)
                        .fillMaxHeight()
                        .padding(end = 8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.previous),
                        fontSize = 16.sp
                    )
                }

                Button(
                    onClick = { gameViewModel.next() },
                    enabled = gameViewModel.checkNextCurrentWordCount(),
                    modifier = Modifier
                        .weight(0.9f)
                        .fillMaxHeight()
                ) {
                    Text(
                        text = stringResource(R.string.next),
                        fontSize = 16.sp
                    )
                }
            }



            OutlinedButton(
                onClick = { gameViewModel.doneGame() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.done),
                    fontSize = 16.sp
                )
            }
        }

        GameStatus(score = gameUiState.score, modifier = Modifier.padding(20.dp))
    }
    if (gameUiState.isGameOver) {
        FinalScoreDialog(
            score = gameUiState.score,
            onPlayAgain = {
                gameViewModel.resetGame()
            }
        )
    }
}

@Composable
fun GameLayout(
    onUserGuessChanged: (String) -> Unit,
    userGuess: String,

    onKeyboardDone: () -> Unit,
    isGuessWrong: Boolean,
    currentScrambledWord: String,
    wordCount: Int,
    time: String,
    modifier: Modifier = Modifier
) {
    val mediumPadding = dimensionResource(R.dimen.padding_medium)
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(mediumPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(mediumPadding)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    modifier = Modifier
                        .clip(shapes.medium)
                        .background(colorScheme.surfaceTint)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    text = time,
                    style = typography.titleMedium,
                    color = colorScheme.onPrimary
                )

                Text(
                    modifier = Modifier
                        .clip(shapes.medium)
                        .background(colorScheme.surfaceTint)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    text = stringResource(R.string.word_count, wordCount),
                    style = typography.titleMedium,
                    color = colorScheme.onPrimary
                )
            }

            Text(
                text = currentScrambledWord,
                style = typography.displayMedium
            )
            Text(
                text = stringResource(R.string.instructions),
                textAlign = TextAlign.Center,
                style = typography.titleMedium
            )
            OutlinedTextField(
                value = userGuess,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                onValueChange = onUserGuessChanged,
                label = {
                    if (isGuessWrong) {
                        Text(stringResource(R.string.wrong_guess))
                    } else {
                        Text(stringResource(R.string.enter_your_word))
                    }
                },
                isError = isGuessWrong,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { onKeyboardDone() }
                ),
            )
        }
    }

}

@Composable
private fun FinalScoreDialog(
    score: Int,
    onPlayAgain: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activity = (LocalContext.current as Activity)

    AlertDialog(

        onDismissRequest = {
        },
        title = { Text(text = stringResource(R.string.congratulations)) },
        text = { Text(text = stringResource(R.string.you_scored, score)) },
        modifier = modifier.fillMaxWidth(0.87f),
        dismissButton = {
            TextButton(
                onClick = {
                    activity.finish()
                }
            ) {
                Text(text = stringResource(R.string.exit))
            }
        },
        confirmButton = {
            TextButton(onClick = onPlayAgain) {
                Text(text = stringResource(R.string.play_again))
            }
        }
    )
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    UnscrambleTheme {
        GameScreen()
    }
}