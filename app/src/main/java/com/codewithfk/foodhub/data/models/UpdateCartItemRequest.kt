package com.codewithfk.foodhub.data.models

data class UpdateCartItemRequest(
    val cartItemId: String,
    val quantity: Int
)
