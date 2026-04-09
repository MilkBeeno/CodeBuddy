package com.milk.codebuddy.main.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.milk.codebuddy.base.ui.theme.LocalAppColors
import com.milk.codebuddy.base.ui.theme.LocalTypography
import com.milk.codebuddy.resource.R

private val PRICE_REGEX = Regex("^\\d*\\.?\\d*$")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var stockName by rememberSaveable { mutableStateOf("") }
    var industryTag by rememberSaveable { mutableStateOf("") }
    var hotTopic by rememberSaveable { mutableStateOf("") }
    var buyPrice by rememberSaveable { mutableStateOf("") }
    var stopLoss by rememberSaveable { mutableStateOf("") }
    var takeProfit by rememberSaveable { mutableStateOf("") }
    var addCondition by rememberSaveable { mutableStateOf("") }
    var reduceCondition by rememberSaveable { mutableStateOf("") }
    var buyReason by rememberSaveable { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.main_add_transaction_title),
                        style = LocalTypography.current.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.main_back_desc),
                            tint = LocalAppColors.current.primaryTextColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LocalAppColors.current.primaryBackgroundColor
                )
            )
        },
        containerColor = LocalAppColors.current.primaryBackgroundColor,
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TransactionTextField(
                value = stockName,
                onValueChange = { stockName = it },
                labelRes = R.string.main_stock_name,
                placeholderRes = R.string.main_stock_name_hint
            )

            TransactionTextField(
                value = industryTag,
                onValueChange = { industryTag = it },
                labelRes = R.string.main_industry_tag,
                placeholderRes = R.string.main_industry_tag_hint
            )

            TransactionTextField(
                value = hotTopic,
                onValueChange = { hotTopic = it },
                labelRes = R.string.main_hot_topic,
                placeholderRes = R.string.main_hot_topic_hint
            )

            TransactionTextField(
                value = buyPrice,
                onValueChange = { if (it.isEmpty() || it.matches(PRICE_REGEX)) buyPrice = it },
                labelRes = R.string.main_buy_price,
                placeholderRes = R.string.main_buy_price_hint,
                keyboardType = KeyboardType.Decimal
            )

            TransactionTextField(
                value = stopLoss,
                onValueChange = { if (it.isEmpty() || it.matches(PRICE_REGEX)) stopLoss = it },
                labelRes = R.string.main_stop_loss,
                placeholderRes = R.string.main_stop_loss_hint,
                keyboardType = KeyboardType.Decimal
            )

            TransactionTextField(
                value = takeProfit,
                onValueChange = { if (it.isEmpty() || it.matches(PRICE_REGEX)) takeProfit = it },
                labelRes = R.string.main_take_profit,
                placeholderRes = R.string.main_take_profit_hint,
                keyboardType = KeyboardType.Decimal
            )

            TransactionTextField(
                value = addCondition,
                onValueChange = { addCondition = it },
                labelRes = R.string.main_add_condition,
                placeholderRes = R.string.main_add_condition_hint
            )

            TransactionTextField(
                value = reduceCondition,
                onValueChange = { reduceCondition = it },
                labelRes = R.string.main_reduce_condition,
                placeholderRes = R.string.main_reduce_condition_hint
            )

            TransactionTextField(
                value = buyReason,
                onValueChange = { buyReason = it },
                labelRes = R.string.main_buy_reason,
                placeholderRes = R.string.main_buy_reason_hint,
                singleLine = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                minLines = 4,
                maxLines = 6
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    // TODO: 提交交易单逻辑
                    onBack()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LocalAppColors.current.primaryTextColor,
                    contentColor = LocalAppColors.current.primaryBackgroundColor
                )
            ) {
                Text(
                    text = stringResource(R.string.main_submit),
                    style = LocalTypography.current.titleMedium
                )
            }
        }
    }
}

@Composable
private fun TransactionTextField(
    value: String,
    onValueChange: (String) -> Unit,
    labelRes: Int,
    placeholderRes: Int,
    modifier: Modifier = Modifier.fillMaxWidth(),
    singleLine: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    minLines: Int = 1,
    maxLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = stringResource(labelRes),
                style = LocalTypography.current.bodyMedium
            )
        },
        placeholder = {
            Text(
                text = stringResource(placeholderRes),
                style = LocalTypography.current.bodySmall
            )
        },
        modifier = modifier,
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = LocalAppColors.current.primaryTextColor,
            unfocusedTextColor = LocalAppColors.current.primaryTextColor,
            focusedBorderColor = LocalAppColors.current.primaryTextColor,
            unfocusedBorderColor = LocalAppColors.current.secondaryTextColor,
            focusedPlaceholderColor = LocalAppColors.current.secondaryTextColor,
            unfocusedPlaceholderColor = LocalAppColors.current.secondaryTextColor,
            cursorColor = LocalAppColors.current.primaryTextColor,
            focusedLabelColor = LocalAppColors.current.primaryTextColor,
            unfocusedLabelColor = LocalAppColors.current.secondaryTextColor
        )
    )
}
