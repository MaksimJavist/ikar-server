<template>
    <div>
        <b-container fluid v-if="joinFrameVisible">
            <b-row class="vh-100 justify-content-center" align-v="center">
                <b-col cols="4">
                    <b-card title="Присоедениться к комнате" align="center">
                        <b-form-input v-if="!authenticatedUser"
                                      class="mt-3 mb-3 text-center"
                                      v-model="userName"
                                      placeholder="Введите имя"/>
                        <b-button class="w-50" @click="connectRoom" variant="outline-success" pill>
                            Присоедениться
                        </b-button>
                    </b-card>
                </b-col>
            </b-row>
        </b-container>
        <b-container class="vh-100" fluid v-else>
            <b-row class="pt-4 justify-content-center" style="height: 85%">
                <b-col cols="10 text-center" style="max-height: 100%">
                    <video v-show="isActivePresentation" id="video" ref="presentationVideo" style="max-height: 100%; object-fit: contain; border-width: medium !important;" autoplay class="rounded border border-info"></video>
                    <b-card v-show="!isActivePresentation" no-body class="h-100 border border-info justify-content-center" align="center" style="background-color: #e1e2e3; border-width: medium !important;">
                        <h5>Пока никто не начал трансляцию</h5>
                    </b-card>
                </b-col>
                <b-col cols="2">
                    <b-card class="h-100 pl-1 pr-1 overflow-auto border border-info" style="size: auto; background-color: #e1e2e3; border-width: medium !important;" title="Участники:" align="center">
                        <b-row>
                            <b-col cols="12" class="h-50 p-1" v-if="getLocalParticipant">
                                <ParticipantLocal :participant="getLocalParticipant.getObject()"/>
                            </b-col>
                            <b-col cols="12" class="h-50 p-1" v-for="(participant, index) in getRemoteParticipants" :key="index">
                                <ParticipantRemote :participant="participant.getObject()"/>
                            </b-col>
                        </b-row>
                    </b-card>
                </b-col>
            </b-row>
            <b-row v-if="getLocalParticipant" class="justify-content-center" align-v="center" style="height: 15%">
                <b-col cols="7" class="h-75">
                    <b-card no-body class="h-100 border border-info justify-content-center" align="center" style="background-color: #e1e2e3; border-width: medium !important;">
                        <div class="d-inline-block" style="margin: 0 10px">
                            <span class="buttonGroup">
                                <b-button
                                    v-if="isPresenter"
                                    pill
                                    v-b-tooltip.hover
                                    @click="stopCommunication"
                                    title="Прекратить показ"
                                    variant="outline-danger">
                                    Прекратить показ
                                </b-button>
                                <b-button
                                    v-else
                                    pill
                                    v-b-tooltip.hover
                                    @click="presenterConnectPermission"
                                    title="Начать показ"
                                    variant="outline-success">
                                    Начать показ
                                </b-button>
                            </span>
                            <span class="buttonGroup">
                                <b-button v-if="microEnable"
                                          v-b-tooltip.hover
                                          title="Выключить микрофон"
                                          @click="changeMicroDisabled(false)"
                                          variant="outline-success">
                                    <b-icon-mic/>
                                </b-button>
                                <b-button
                                    v-else
                                    v-b-tooltip.hover
                                    title="Включить микрофон"
                                    @click="changeMicroDisabled(true)"
                                    variant="outline-danger">
                                    <b-icon-mic-mute/>
                                </b-button>
                            </span>
                            <span class="buttonGroup">
                                <b-button
                                    v-if="videoEnable"
                                    v-b-tooltip.hover
                                    title="Выключить камеру"
                                    @click="changeVideoEnabled(false)"
                                    variant="outline-success">
                                    <b-icon-camera-video/>
                                </b-button>
                                <b-button
                                    v-else
                                    v-b-tooltip.hover
                                    title="Включить камеру"
                                    @click="changeVideoEnabled(true)"
                                    variant="outline-danger">
                                    <b-icon-camera-video-off/>
                                </b-button>
                            </span>
                            <span class="buttonGroup">
                                <b-button
                                    variant="outline-primary"
                                    v-b-tooltip.hover
                                    title="Скопировать ссылку на конференцию"
                                    v-clipboard:copy="getRoomFullReference()"
                                    @click="copyLinkToast">
                                <b-icon-share/>
                            </b-button>
                            </span>
                            <span class="buttonGroup">
                                <b-button
                                    variant="outline-info"
                                    v-b-tooltip.hover
                                    title="Открыть чат"
                                    @click="switchChatVisible">
                                    <b-icon-chat-dots/>
                                </b-button>
                            </span>
                            <span class="buttonGroup">
                                <b-button
                                    pill
                                    variant="outline-danger"
                                    v-b-tooltip.hover
                                    title="Покинуть комнату"
                                    @click="exitFromRoom">
                                    Покинуть комнату
                                </b-button>
                            </span>
                        </div>
                    </b-card>
                </b-col>
            </b-row>
            <Chat v-show="chatVisible" :sender-uuid="localParticipantUuid" :chat-messages="chatMessages" @send-chat="sendChatMessage"/>
        </b-container>
    </div>
</template>

<script>
import Chat from '@/components/chat'
import ParticipantMixin from '@/mixin/ParticipantMixin'
import RoomParticipantsMixin from '@/mixin/room-participants-mixin'
import RoomConferenceMixin from '@/mixin/room-conference-mixin'
import ParticipantLocal from '@/components/room/participant/participant-local'
import ParticipantRemote from '@/components/room/participant/participant-remote'
import api from '@/api'

export default {
    name: "room",
    components: {
        ParticipantLocal,
        ParticipantRemote,
        Chat
    },
    mixins: [
        ParticipantMixin,
        RoomParticipantsMixin,
        RoomConferenceMixin
    ],
    data() {
        return {
            socket: null,
            joinFrameVisible: true,
            userName: null,
            roomName: null,
            localParticipantUuid: null,
            participants: [],
            chatMessages: [],
            authenticatedUser: false,
            microEnable: true,
            videoEnable: true,
            chatVisible: false,
            chatInputText: null
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
    beforeDestroy() {
        if (this.socket) {
            this.closePeerForAllParticipants()
            this.socket.close()
        }
    },
    watch: {
        microEnable: function (newVal) {
            this.getLocalParticipant.rtcPeer.audioEnabled = newVal
        },
        videoEnable: function (newVal) {
            this.getLocalParticipant.rtcPeer.videoEnabled = newVal
        }
    },
    computed: {
        getLocalParticipant: function () {
            return this.getFilledParticipants.find(el => el.uuid === this.localParticipantUuid)
        },
        getRemoteParticipants: function () {
            return this.getFilledParticipants.filter(el => el.uuid !== this.localParticipantUuid)
        },
        getFilledParticipants: function () {
            return this.participants.filter(el => el.name && el.video && el.rtcPeer)
        }
    },
    methods: {
        connectRoom: function () {
            this.socket = new WebSocket('ws://localhost:8080/groupcall')
            this.socket.onopen = this.joinRoom
        },
        joinRoom: function () {
            this.socket.onmessage = this.socketOnMessageCallback
            const roomIdentifier = this.$route.params.roomId
            const message = {
                id: 'joinRoom',
                name: this.userName,
                room: roomIdentifier,
            }
            this.sendMessage(message)
        },
        socketOnMessageCallback: function (message) {
            const parsedMessage = JSON.parse(message.data)
            const messageId = parsedMessage.id
            switch (messageId) {
            case 'existingParticipants':
                this.onExistingParticipants(parsedMessage)
                break
            case 'receiveVideoAnswer':
                this.receiveVideoResponse(parsedMessage)
                break
            case 'newParticipantArrived':
                this.onNewParticipant(parsedMessage)
                break
            case 'participantLeft':
                this.onParticipantLeft(parsedMessage)
                break
            case 'iceCandidate': {
                const candidateUuid = parsedMessage.uuid
                const candidate = this.getParticipantByUuid(candidateUuid)
                candidate.rtcPeer.addIceCandidate(parsedMessage.candidate, this.showErrorCallback)
                break
            }
            case 'newChatMessage':
                this.newChatMessage(parsedMessage.data)
                break
            case 'viewerConnectPermissionResponse':
                this.viewerConnectPermissionResponse(parsedMessage)
                break
            case 'presenterConnectPermissionResponse':
                this.presenterConnectPermissionResponse(parsedMessage)
                break
            case 'presenterResponse':
                this.presenterResponse(parsedMessage)
                break
            case 'presentationIceCandidate':
                this.presentationIceCandidate(parsedMessage)
                break
            case 'newPresenter':
                this.newPresenter(parsedMessage)
                break
            case 'viewerResponse':
                this.viewerResponse(parsedMessage)
                break
            case 'presenterStopCommunication':
                this.presenterStopCommunication(parsedMessage)
                break
            default:
                break
            }
        },
        sendMessage: function (message) {
            const jsonMessage = JSON.stringify(message)
            this.socket.send(jsonMessage)
        },
        leaveRoom: function () {
            this.sendMessage({
                id : 'leaveRoom'
            })
            this.getLocalParticipant.dispose()
            this.participants = []
            this.socket.close()
        },
        newChatMessage: function (chatMessage) {
            this.chatMessages.push(chatMessage)
        },
        getRoomFullReference: function () {
            return window.location.href
        },
        copyLinkToast: function () {
            this.$bvToast.toast('Ссылка на конференцию успешно скопирована', {
                variant: 'primary',
                solid: true
            })
        },
        changeMicroDisabled: function (value) {
            this.microEnable = value
            if (value) {
                this.$bvToast.toast('Микрофон включен', {
                    variant: 'info',
                    solid: true
                })
            } else {
                this.$bvToast.toast('Микрофон выключен', {
                    variant: 'info',
                    solid: true
                })
            }
        },
        changeVideoEnabled: function (value) {
            this.videoEnable = value
            if (value) {
                this.$bvToast.toast('Камера включена', {
                    variant: 'info',
                    solid: true
                })
            } else {
                this.$bvToast.toast('Камера выключена', {
                    variant: 'info',
                    solid: true
                })
            }
        },
        sendChatMessage: function (chatMessage) {
            const message = {
                id: 'sendChat',
                message: chatMessage
            }
            this.sendMessage(message)
        },
        switchChatVisible: function () {
            this.chatVisible = !this.chatVisible
        },
        closePeerForAllParticipants: function () {
            this.participants.forEach(participant => participant.dispose())
            this.participants = []
        },
        exitFromRoom: function () {
            this.closePeerForAllParticipants()
            this.refreshStatePresentation()
            this.socket.close()

            this.socket = null
            this.joinFrameVisible = true
            this.roomName = null
            this.localParticipantUuid = null
            this.participants = []
            this.chatMessages = []
            this.authenticatedUser = false
            this.microEnable = true
            this.videoEnable = true
            this.chatVisible = false
            this.chatInputText = null
        }
    }
}
</script>