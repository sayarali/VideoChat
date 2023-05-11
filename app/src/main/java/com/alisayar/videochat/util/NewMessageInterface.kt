package com.alisayar.videochat.util

import com.alisayar.videochat.models.MessageModel

interface NewMessageInterface {
    fun onNewMessage(message: MessageModel)
}