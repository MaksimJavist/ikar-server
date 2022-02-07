<template>
    <b-card class="w-25 border border-primary position-fixed rounded text-center"
            style="height: 500px; border-width: medium !important; bottom: 3%; right: 3%; background-color: #e1e2e3;">
        <b-button
            class="position-absolute"
            style="right: 5px; top: 5px"
            variant="outline-danger"
            title="Закрыть"
            @click="hideChatEmit">
            <b-icon-x/>
        </b-button>
        <h3>Чат</h3>
        <div ref="chatDiv" class="mb-4 bg-white text-white rounded overflow-auto" style="height: 65%">
            <span v-for="(message, index) in chatMessages" :key="index">
                <div v-if="message.senderUuid === senderUuid" class="text-right">
                    <div class="m-2 p-2 w-auto d-inline-block rounded text-left bg-success"
                         style="max-width: 75%">
                        <small>{{ dateTimeFormat(message.time) }}</small>
                        <br/>
                        <strong>{{ message.senderName}}:</strong>
                        <div>{{ message.text }}</div>
                    </div>
                </div>
                <div v-else class="text-left" @mouseenter="checkMessage(index)">
                    <div class="m-2 p-2 w-auto d-inline-block rounded text-left bg-dark"
                         style="max-width: 75%">
                        <strong>{{ message.senderName}}:</strong>
                        <div>{{ message.text }}</div>
                    </div>
                    <b-icon-check-all v-if="message.checked" variant="primary" style="height: 30px; width: 30px"/>
                </div>
            </span>
        </div>
        <b-form-input class="mb-2" v-model="chatInputText" placeholder="Введите сообщение:"></b-form-input>
        <b-button
            class="w-100"
            variant="outline-info"
            v-b-tooltip.hover
            title="Отправить сообщение"
            @click="sendChatMessage">
            Отправить
        </b-button>
    </b-card>
</template>

<script>
import { DateTime } from 'luxon'

export default {
    name: "chat",
    props: {
        senderUuid: String,
        chatMessages: Array
    },
    data () {
        return {
            chatInputText: null
        }
    },
    updated: function () {
        const chatElement = this.$refs.chatDiv
        chatElement.scrollTop = chatElement.scrollHeight
    },
    methods: {
        sendChatMessage: function () {
            if (this.chatInputText) {
                this.$emit('send-chat', this.chatInputText)
                this.chatInputText = null
            }
        },
        checkMessage: function (index) {
            if (!this.chatMessages[index].checked) {
                this.$emit('check-message', index)
            }
        },
        hideChatEmit: function () {
            this.$emit('hide-chat')
        },
        dateTimeFormat: function (value) {
            return DateTime.fromISO(value).toFormat('dd.MM.yyyy HH:mm:ss')
        }
    }
}
</script>