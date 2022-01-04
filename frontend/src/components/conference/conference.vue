<template>
    <div>
        <b-container fluid v-if="joinFrameVisible">
            <b-row class="vh-100 justify-content-center" align-v="center">
                <b-col cols="4">
                    <b-card title="Присоедениться к конференции" align="center">
                        <b-form-input v-if="!authenticatedUser"
                                      class="mt-3 mb-3 text-center"
                                      v-model="userName"
                                      placeholder="Введите имя"/>
                        <b-button class="w-50" @click="connectConference" variant="outline-success" pill>
                            Присоедениться
                        </b-button>
                    </b-card>
                </b-col>
            </b-row>
        </b-container>
    </div>
</template>

<script>
import api from "@/api"

export default {
    name: "conference",
    data () {
        return {
            socket: null,
            joinFrameVisible: true,
            authenticatedUser: false,
            userName: null
        }
    },
    beforeCreate() {
        api.getAuthInfo()
            .then(resp => {
                const { data } = resp
                if (data.authenticated) {
                    const { firstName, secondName } = data
                    this.authenticatedUser = true
                    this.userName = `${firstName} ${secondName}`
                }
            })
    },
    methods: {
        connectConference: function () {
            this.socket = new WebSocket('ws://localhost:8080/conference')
            this.joinFrameVisible = false
            this.socket.onopen = this.joinConference
        },
        joinConference: function () {
            this.socket.onmessage = this.socketHandleMessage
            const conferenceIdentifier = this.$route.params.conferenceId
            const message = {
                id: 'registerViewer',
                name: this.userName,
                conference: conferenceIdentifier,
            }
            this.sendMessage(message)
        },
        socketHandleMessage: function (message) {
            const parsedMessage = JSON.parse(message.data)
            const messageId = parsedMessage.id
            switch (messageId) {
            case 'viewerRegistered':
                this.viewerRegistered()
                break
            }
        },
        viewerRegistered: function () {

        },
        sendMessage: function (message) {
            const jsonMessage = JSON.stringify(message)
            this.socket.send(jsonMessage)
        }
    }
}
</script>