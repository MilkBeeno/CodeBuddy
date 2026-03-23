package com.milk.codebuddy.main.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.milk.codebuddy.base.ui.theme.LocalAppColors
import com.milk.codebuddy.base.ui.theme.LocalTypography
import com.milk.codebuddy.resource.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var stockName by remember { mutableStateOf(TextFieldValue("")) }
    var industryTag by remember { mutableStateOf(TextFieldValue("")) }
    var hotTopic by remember { mutableStateOf(TextFieldValue("")) }
    var buyPrice by remember { mutableStateOf(TextFieldValue("")) }
    var stopLoss by remember { mutableStateOf(TextFieldValue("")) }
    var takeProfit by remember { mutableStateOf(TextFieldValue("")) }
    var addCondition by remember { mutableStateOf(TextFieldValue("")) }
    var reduceCondition by remember { mutableStateOf(TextFieldValue("")) }
    var buyReason by remember { mutableStateOf(TextFieldValue("")) }

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
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回",
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
            // 股票名称
            OutlinedTextField(
                value = stockName,
                onValueChange = { stockName = it },
                label = {
                    Text(
                        text = stringResource(R.string.main_stock_name),
                        style = LocalTypography.current.bodyMedium
                    )
                },
                placeholder = {
                    Text(
                        text = stringResource(R.string.main_stock_name_hint),
                        style = LocalTypography.current.bodySmall
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
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

            // 行业标签
            OutlinedTextField(
                value = industryTag,
                onValueChange = { industryTag = it },
                label = {
                    Text(
                        text = stringResource(R.string.main_industry_tag),
                        style = LocalTypography.current.bodyMedium
                    )
                },
                placeholder = {
                    Text(
                        text = stringResource(R.string.main_industry_tag_hint),
                        style = LocalTypography.current.bodySmall
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
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

            // 热点题材
            OutlinedTextField(
                value = hotTopic,
                onValueChange = { hotTopic = it },
                label = {
                    Text(
                        text = stringResource(R.string.main_hot_topic),
                        style = LocalTypography.current.bodyMedium
                    )
                },
                placeholder = {
                    Text(
                        text = stringResource(R.string.main_hot_topic_hint),
                        style = LocalTypography.current.bodySmall
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
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

            // 买入价
            OutlinedTextField(
                value = buyPrice,
                onValueChange = { 
                    if (it.text.isEmpty() || it.text.matches(Regex("^\\d*\\.?\\d*$"))) {
                        buyPrice = it
                    }
                },
                label = {
                    Text(
                        text = stringResource(R.string.main_buy_price),
                        style = LocalTypography.current.bodyMedium
                    )
                },
                placeholder = {
                    Text(
                        text = stringResource(R.string.main_buy_price_hint),
                        style = LocalTypography.current.bodySmall
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
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

            // 止损价格
            OutlinedTextField(
                value = stopLoss,
                onValueChange = { 
                    if (it.text.isEmpty() || it.text.matches(Regex("^\\d*\\.?\\d*$"))) {
                        stopLoss = it
                    }
                },
                label = {
                    Text(
                        text = stringResource(R.string.main_stop_loss),
                        style = LocalTypography.current.bodyMedium
                    )
                },
                placeholder = {
                    Text(
                        text = stringResource(R.string.main_stop_loss_hint),
                        style = LocalTypography.current.bodySmall
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
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

            // 止盈价格
            OutlinedTextField(
                value = takeProfit,
                onValueChange = { 
                    if (it.text.isEmpty() || it.text.matches(Regex("^\\d*\\.?\\d*$"))) {
                        takeProfit = it
                    }
                },
                label = {
                    Text(
                        text = stringResource(R.string.main_take_profit),
                        style = LocalTypography.current.bodyMedium
                    )
                },
                placeholder = {
                    Text(
                        text = stringResource(R.string.main_take_profit_hint),
                        style = LocalTypography.current.bodySmall
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
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

            // 加仓条件
            OutlinedTextField(
                value = addCondition,
                onValueChange = { addCondition = it },
                label = {
                    Text(
                        text = stringResource(R.string.main_add_condition),
                        style = LocalTypography.current.bodyMedium
                    )
                },
                placeholder = {
                    Text(
                        text = stringResource(R.string.main_add_condition_hint),
                        style = LocalTypography.current.bodySmall
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
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

            // 减仓条件
            OutlinedTextField(
                value = reduceCondition,
                onValueChange = { reduceCondition = it },
                label = {
                    Text(
                        text = stringResource(R.string.main_reduce_condition),
                        style = LocalTypography.current.bodyMedium
                    )
                },
                placeholder = {
                    Text(
                        text = stringResource(R.string.main_reduce_condition_hint),
                        style = LocalTypography.current.bodySmall
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
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

            // 买入理由
            OutlinedTextField(
                value = buyReason,
                onValueChange = { buyReason = it },
                label = {
                    Text(
                        text = stringResource(R.string.main_buy_reason),
                        style = LocalTypography.current.bodyMedium
                    )
                },
                placeholder = {
                    Text(
                        text = stringResource(R.string.main_buy_reason_hint),
                        style = LocalTypography.current.bodySmall
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                minLines = 4,
                maxLines = 6,
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

            Spacer(modifier = Modifier.height(16.dp))

            // 提交按钮
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
