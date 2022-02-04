<template>
    <div>
        <JoinFrame v-if="joinFrameVisible"
                   :name="userName"
                   @connect="connectConference"
                   @update-username="updateUsername"/>
        <b-container class="vh-100" v-show="isRegisteredInConference" fluid>
            <b-row class="pt-3 justify-content-center" style="height: 80%; max-height: 80%">
                <b-col cols="10 text-center" style="max-height: 100%" v-show="isActivePresentation">
                    <video id="video" ref="conferenceVideo" :poster="baseUrl() + 'img/spinner.gif'" style="max-height: 100%; object-fit: contain; border-width: medium !important;" autoplay class="rounded border border-info"></video>
                </b-col>
                <b-col cols="10" v-if="!isActivePresentation">
                    <b-card no-body class="h-100 border border-info justify-content-center" align="center" style="background-color: #e1e2e3; border-width: medium !important;">
                        <h5>Пока никто не начал конференцию</h5>
                    </b-card>
                </b-col>
            </b-row>
            <b-row class="pb-2 justify-content-center" align-v="center" style="height: 20%">
                <b-col cols="8" style="height: 65%">
                    <b-card no-body class="h-100 border border-info justify-content-center" align="center" style="background-color: #e1e2e3; border-width: medium !important;">
                        <div class="d-inline-block" style="margin: 0 10px">
                            <span v-if="isPresenter">
                                <span class="buttonGroup">
                                    <b-button
                                        v-if="onAudioFlag"
                                        v-b-tooltip.hover
                                        title="Выключить микрофон"
                                        variant="outline-success"
                                        @click="changeEnableAudio(false)">
                                        <b-icon-mic/>
                                    </b-button>
                                    <b-button
                                        v-else
                                        v-b-tooltip.hover
                                        title="Включить микрофон"
                                        variant="outline-danger"
                                        @click="changeEnableAudio(true)">
                                        <b-icon-mic-mute/>
                                    </b-button>
                                </span>
                                <span class="buttonGroup">
                                    <b-button
                                        v-if="onVideoFlag"
                                        v-b-tooltip.hover
                                        title="Выключить камеру"
                                        variant="outline-success"
                                        @click="changeEnableVideo(false)">
                                        <b-icon-camera-video/>
                                    </b-button>
                                    <b-button
                                        v-else
                                        v-b-tooltip.hover
                                        title="Включить камеру"
                                        variant="outline-danger"
                                        @click="changeEnableVideo(true)">
                                        <b-icon-camera-video-off/>
                                    </b-button>
                                </span>
                                <span class="buttonGroup">
                                    <b-button
                                        pill
                                        v-b-tooltip.hover
                                        title="Прекратить показ"
                                        variant="outline-danger"
                                        @click="stop">
                                        Прекратить показ
                                    </b-button>
                                </span>
                            </span>
                            <span class="buttonGroup">
                                <b-button
                                    pill
                                    v-b-tooltip.hover
                                    @click="presenterConnectPermission"
                                    title="Начать показ"
                                    variant="outline-success">
                                        Начать показ
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
                                    v-b-tooltip.hover
                                    @click="exitFromConference"
                                    title="Начать показ"
                                    variant="outline-danger">
                                        Покинуть трансляцию
                                    </b-button>
                            </span>
                        </div>
                    </b-card>
                </b-col>
            </b-row>
            <Chat v-show="isChatVisible" :sender-uuid="uuid" :chat-messages="chatMessages" @send-chat="sendChatMessage"/>
        </b-container>
    </div>
</template>

<script>
import Chat from '@/components/common/chat'
import WebRtcPeer from '@/util/WebRtcPeer'
import JoinFrame from '@/components/common/join-frame'
import api from '@/api'

export default {
    name: "test-bag",
    data () {
        return {
            uuid: null,
            webSocket: null,
            webRtcPeer: null,
            userName: null,
            chatMessages: [],

            authenticatedUser: false,
            isRegisteredInConference: false,
            joinFrameVisible: true,
            isActivePresentation: false,
            isPresenter: false,
            isChatVisible: false,

            onAudioFlag: true,
            onVideoFlag: true
        }
    },
    components: {
        Chat,
        JoinFrame
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
    watch: {
        onAudioFlag: function (newVal) {
            if (this.webRtcPeer) {
                this.webRtcPeer.audioEnabled = newVal
            }
        },
        onVideoFlag: function (newVal) {
            if (this.webRtcPeer) {
                this.webRtcPeer.videoEnabled = newVal
            }
        }
    },
    methods: {
        baseUrl: function () {
            return process.env.BASE_URL
        },
        checkMessage: function (messageUuid) {
            console.log(messageUuid)
        },
        updateUsername: function (value) {
            this.userName = value
        },
        connectConference: function () {
            this.webSocket = new WebSocket('ws://localhost:8080/conference')
            this.webSocket.onopen = this.setSettingSocket
        },
        setSettingSocket: function () {
            this.joinFrameVisible = false
            this.webSocket.onmessage = this.handleMessage
            const message = {
                id: "registerViewer",
                conference: this.$route.params.identifier,
                name: this.userName
            }
            this.sendMessage(message)
        },
        handleMessage: function (message) {
            const parsedMessage = JSON.parse(message.data)

            switch (parsedMessage.id) {
            case 'viewerRegistered':
                this.viewerRegistered(parsedMessage)
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
            case 'viewerResponse':
                this.viewerResponse(parsedMessage)
                break
            case 'newPresenter':
                this.newPresenterConference(parsedMessage)
                break
            case 'presenterLeave':
                this.presenterLeave(parsedMessage)
                break
            case 'iceCandidate':
                this.webRtcPeer.addIceCandidate(parsedMessage.candidate, function(error) {
                    if (error)
                        return console.error('Error adding candidate: ' + error)
                })
                break
            case 'newChatMessage':
                this.newChatMessage(parsedMessage.data)
                break
            case 'stopCommunication':
                this.stopCommunication(parsedMessage)
                break
            default:
                break
            }
        },
        viewerRegistered: function (message) {
            this.isRegisteredInConference = true
            this.uuid = message.uuid
            this.userName = message.username
            this.chatMessages = this.chatMessages.concat(message.messages)
            this.$bvToast.toast(message.message, {
                variant: 'info',
                solid: true
            })
            this.viewerConnectPermission()
        },
        presenterLeave: function (message) {
            this.$bvToast.toast(message.message, {
                variant: 'info',
                solid: true
            })
            this.isActivePresentation = false
            this.webRtcPeer.dispose()
            this.webRtcPeer = null
        },
        viewerConnectPermission: function () {
            const message = {
                id : 'viewerConnectPermission'
            }
            this.sendMessage(message)
        },
        viewerConnectPermissionResponse: function (message) {
            this.$bvToast.toast(message.message, {
                variant: 'info',
                solid: true
            })
            if (message.response === 'accepted') {
                this.viewer()
            }
        },
        presenterConnectPermission: function () {
            const message = {
                id : 'presenterConnectPermission'
            }
            this.sendMessage(message)
        },
        presenterConnectPermissionResponse: function (message) {
            if (message.response === 'accepted') {
                this.presenter()
            } else {
                this.$bvToast.toast(message.message, {
                    variant: 'info',
                    solid: true
                })
            }
        },
        newPresenterConference: function (message) {
            this.$bvToast.toast(message.message, {
                variant: 'info',
                solid: true
            })
            this.viewerConnectPermission()
        },
        presenterResponse: function (message) {
            if (message.response != 'accepted') {
                const errorMsg = message.message ? message.message : 'Unknow error'
                console.info('Call not accepted for the following reason: ' + errorMsg)
            } else {
                this.isPresenter = true
                this.isActivePresentation = true
                this.webRtcPeer.processAnswer(message.sdpAnswer, function(error) {
                    if (error)
                        return console.error(error)
                })
            }
        },
        viewerResponse: function (message) {
            if (message.response != 'accepted') {
                const errorMsg = message.message ? message.message : 'Unknow error'
                console.info('Call not accepted for the following reason: ' + errorMsg)
            } else {
                this.isActivePresentation = true
                this.webRtcPeer.processAnswer(message.sdpAnswer, function(error) {
                    if (error)
                        return console.error(error)
                })
            }
        },
        presenter: function () {
            if (!this.webRtcPeer) {
                const constraints = {
                    audio : this.onAudioFlag,
                    video : {
                        width: screen.width,
                        height: screen.height,
                        maxFrameRate: 30,
                        minFrameRate: 15
                    },
                }

                const options = {
                    localVideo : this.$refs.conferenceVideo,
                    onicecandidate : this.onIceCandidate,
                    mediaConstraints: constraints,
                    sendSource: 'conference'
                }
                const onOfferPresenterCallback = this.onOfferPresenter
                const disposePeerCallback = this.dispose
                this.webRtcPeer = new WebRtcPeer.WebRtcPeerSendonly(options,
                    function(error) {
                        if (error) {
                            return disposePeerCallback()
                        }
                        this.generateOffer(onOfferPresenterCallback)
                    })
            }
        },
        onOfferPresenter: function (error, offerSdp) {
            if (error)
                return console.error('Error generating the offer')
            const message = {
                id : 'presenter',
                sdpOffer : offerSdp
            }
            this.sendMessage(message)
        },
        viewer: function () {
            if (!this.webRtcPeer) {
                const options = {
                    remoteVideo : this.$refs.conferenceVideo,
                    onicecandidate : this.onIceCandidate,
                    video : {
                        mandatory : {
                            maxWidth : screen.width,
                            maxHeight: screen.height,
                            maxFrameRate : 30,
                            minFrameRate : 15
                        }
                    }
                }
                const onOfferViewerCallback = this.onOfferViewer
                this.webRtcPeer = new WebRtcPeer.WebRtcPeerRecvonly(options,
                    function(error) {
                        if (error) {
                            return console.error(error)
                        }
                        this.generateOffer(onOfferViewerCallback)
                    })
            }
        },
        onOfferViewer: function (error, offerSdp) {
            if (error)
                return console.error('Error generating the offer')
            console.info('Invoking SDP offer callback function ' + location.host)
            const message = {
                id : 'viewer',
                sdpOffer : offerSdp
            }
            this.sendMessage(message)
        },
        onIceCandidate: function (candidate) {
            const message = {
                id : 'onIceCandidate',
                candidate : candidate
            }
            this.sendMessage(message)
        },
        stop: function () {
            this.isPresenter = false
            this.isActivePresentation = false
            const message = {
                id : 'stop'
            }
            this.sendMessage(message)
            this.dispose()
        },
        stopCommunication: function ({ message }) {
            this.isActivePresentation = false
            this.$bvToast.toast(message, {
                variant: 'info',
                solid: true
            })
            this.dispose()
        },
        sendChatMessage: function (chatMessage) {
            const message = {
                id: 'sendChat',
                message: chatMessage
            }
            this.sendMessage(message)
        },
        newChatMessage: function (chatMessage) {
            if (chatMessage.senderUuid !== this.uuid) {
                this.$bvToast.toast(chatMessage.text, {
                    title: chatMessage.senderName,
                    variant: 'primary',
                    solid: true
                })
            }
            this.chatMessages.push(chatMessage)
        },
        errorResponse: function (message) {
            this.$bvToast.toast(message, {
                variant: 'danger',
                solid: true
            })
        },
        sendMessage: function (message) {
            const jsonMessage = JSON.stringify(message)
            this.webSocket.send(jsonMessage)
        },
        dispose: function () {
            if (this.webRtcPeer) {
                this.webRtcPeer.dispose()
                this.webRtcPeer = null
            }
        },
        changeEnableAudio: function (value) {
            if (this.webRtcPeer) {
                this.onAudioFlag = value
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
            }
        },
        changeEnableVideo: function (value) {
            if (this.webRtcPeer) {
                this.onVideoFlag = value
                if (value) {
                    this.$bvToast.toast('Видео включено', {
                        variant: 'info',
                        solid: true
                    })
                } else {
                    this.$bvToast.toast('Видео выключено', {
                        variant: 'info',
                        solid: true
                    })
                }
            }
        },
        switchChatVisible: function () {
            this.isChatVisible = !this.isChatVisible
        },
        exitFromConference: function () {
            this.isRegisteredInConference = false
            if (this.webRtcPeer) {
                this.webRtcPeer.dispose()
            }
            if (this.webSocket) {
                this.webSocket.close()
            }
            this.$router.go()
        }
    }
}
</script>