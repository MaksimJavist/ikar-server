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
        <b-container class="vh-100" fluid v-show="isRegisteredInConference">
            <b-row class="pt-3 justify-content-center" style="height: 80%; max-height: 80%">
                <b-col cols="10" v-show="isStreamConference" style="max-height: 100%">
                    <video id="video" ref="conferenceVideo" style="max-height: 100%; object-fit: cover; border-width: medium !important;" autoplay class="w-100 rounded border border-info"></video>
                </b-col>
                <b-col cols="10" v-if="!isStreamConference">
                    <b-card no-body class="h-100 border border-info justify-content-center" align="center" style="background-color: #e1e2e3; border-width: medium !important;">
                        <h5>Пока никто не начал конференцию</h5>
                    </b-card>
                </b-col>
            </b-row>
            <b-row class="pb-2 justify-content-center" align-v="center" style="height: 20%">
                <b-col cols="6" style="height: 65%">
                    <b-card no-body class="h-100 border border-info justify-content-center" align="center" style="background-color: #e1e2e3; border-width: medium !important;">
                        <div class="d-inline-block" style="margin: 0 10px">
                            <span class="buttonGroup">
                                <b-button
                                    pill
                                    @click="presenter"
                                    v-b-tooltip.hover
                                    title="Начать показ"
                                    variant="outline-success">
                                        Начать показ
                                    </b-button>
                            </span>
                            <span v-if="isPresenter">
                                <span class="buttonGroup">
                                    <b-button
                                        v-b-tooltip.hover
                                        title="Выключить микрофон"
                                        variant="outline-success">
                                        <b-icon-mic/>
                                    </b-button>
                                    <b-button
                                        v-b-tooltip.hover
                                        title="Включить микрофон"
                                        variant="outline-danger">
                                        <b-icon-mic-mute/>
                                    </b-button>
                                </span>
                                <span class="buttonGroup">
                                    <b-button
                                        v-b-tooltip.hover
                                        title="Выключить камеру"
                                        variant="outline-success">
                                        <b-icon-camera-video/>
                                    </b-button>
                                    <b-button
                                        v-b-tooltip.hover
                                        title="Включить камеру"
                                        variant="outline-danger">
                                        <b-icon-camera-video-off/>
                                    </b-button>
                                </span>
                                <span class="buttonGroup">
                                    <b-button
                                        v-b-tooltip.hover
                                        title="Выключить камеру"
                                        variant="outline-success">
                                        <b-icon-camera-video/>
                                    </b-button>
                                    <b-button
                                        v-b-tooltip.hover
                                        title="Включить камеру"
                                        variant="outline-danger">
                                        <b-icon-camera-video-off/>
                                    </b-button>
                                </span>
                            </span>
                            <span class="buttonGroup">
                                <b-button
                                    variant="outline-primary"
                                    v-b-tooltip.hover
                                    @click="showPeer"
                                    title="Скопировать ссылку на конференцию">
                                <b-icon-share/>
                            </b-button>
                            </span>
                            <span class="buttonGroup">
                                <b-button
                                    variant="outline-info"
                                    v-b-tooltip.hover
                                    title="Открыть чат">
                                <b-icon-chat-dots/>
                            </b-button>
                            </span>
                        </div>
                    </b-card>
                </b-col>
            </b-row>
        </b-container>
    </div>
</template>

<script>
import api from "@/api"
import KurentoUtils from 'kurento-utils'

export default {
    name: "conference",
    data () {
        return {
            socket: null,
            joinFrameVisible: true,
            authenticatedUser: false,
            userName: null,
            webRtcPeer: null,
            isStreamConference: false,
            isPresenter: false,
            isRegisteredInConference: false
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
        if (this.webRtcPeer !== null) {
            this.webRtcPeer.dispose()
        }
        if (this.socket !== null) {
            this.socket.close()
        }

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
            case 'viewerResponse':
                this.viewerResponse(parsedMessage)
                break
            case 'presenterResponse':
                this.presenterResponse(parsedMessage)
                break
            case 'newPresenter':
                this.newPresenter(parsedMessage)
                break
            case 'iceCandidate':
                this.webRtcPeer.addIceCandidate(parsedMessage.candidate, function(error) {
                    if (error)
                        return console.error('Error adding candidate: ' + error)
                })
                break
            case 'errorResponse':
                this.errorResponse(parsedMessage.message)
                break
            }
        },
        viewerRegistered: function () {
            this.isRegisteredInConference = true
            this.$bvToast.toast('Вы подсоединились к конференции', {
                variant: 'success',
                solid: true
            })
            this.viewer()
        },
        newPresenter: function (message) {
            this.$bvToast.toast(message.message, {
                variant: 'success',
                solid: true
            })
            this.viewer()
        },
        iceCandidate: function (message) {
            this.webRtcPeer.addIceCandidate(message.candidate, function(error) {
                if (error)
                    return console.error('Error adding candidate: ' + error)
            })
        },
        presenter: function () {
            // const constraints = {
            //     audio : true,
            //     video : {
            //         mandatory : {
            //             maxWidth : screen.width,
            //             maxHeight: screen.height,
            //             maxFrameRate : 15,
            //             minFrameRate : 15
            //         }
            //     }
            // }

            const options = {
                localVideo: this.$refs.conferenceVideo,
                // mediaConstraints: constraints,
                onicecandidate: this.onIceCandidate
            }

            const onOfferPresenterCallback = this.onOfferPresenter
            this.webRtcPeer = new KurentoUtils.WebRtcPeer.WebRtcPeerSendonly(options,
                function(error) {
                    if (error) {
                        return console.error(error)
                    }
                    this.generateOffer(onOfferPresenterCallback)
                })
        },
        viewer: function () {
            const options = {
                localVideo: this.$refs.conferenceVideo,
                // mediaConstraints: constraints,
                onicecandidate: this.onIceCandidate
            }

            const onOfferViewerCallback = this.onOfferViewer
            this.webRtcPeer = new KurentoUtils.WebRtcPeer.WebRtcPeerRecvonly(options,
                function(error) {
                    if (error) {
                        return console.error(error)
                    }
                    this.generateOffer(onOfferViewerCallback)
                })
        },
        onOfferPresenter: function (error, offerSdp) {
            if (error) {
                return console.error('Error generating the offer')
            }

            const message = {
                id : 'presenter',
                sdpOffer : offerSdp
            }
            this.sendMessage(message)
        },
        presenterResponse: function (response) {
            if (response.response === "accepted") {
                this.isStreamConference = true
                this.webRtcPeer.processAnswer(response.sdpAnswer, function(error) {
                    if (error)
                        return console.error(error)
                })
            } else {
                this.$bvToast.toast(response.message, {
                    variant: 'danger',
                    solid: true
                })
            }
        },
        viewerResponse: function (response) {
            if (response.response === "accepted") {
                this.isStreamConference = true
                this.webRtcPeer.processAnswer(response.sdpAnswer, function(error) {
                    if (error)
                        return console.error(error)
                })
            } else {
                this.webRtcPeer.dispose()
                this.webRtcPeer = null
                this.$bvToast.toast(response.message, {
                    variant: 'info',
                    solid: true
                })
            }
        },
        onIceCandidate: function (candidate) {
            const message = {
                id : 'onIceCandidate',
                candidate : candidate
            }
            this.sendMessage(message)
        },
        onOfferViewer: function (error, offerSdp) {
            const message = {
                id: 'viewer',
                sdpOffer: offerSdp
            }
            this.sendMessage(message)
        },
        showPeer: function () {
            console.log(this.webRtcPeer)
        },
        sendMessage: function (message) {
            const jsonMessage = JSON.stringify(message)
            this.socket.send(jsonMessage)
        },
        errorResponse: function (message) {
            this.$bvToast.toast(message, {
                variant: 'danger',
                solid: true
            })
        },
        dispose: function () {
            if (this.webRtcPeer) {
                this.webRtcPeer.dispose()
                this.webRtcPeer = null
            }
        }
    }
}
</script>