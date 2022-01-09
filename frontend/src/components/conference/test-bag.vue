<template>
    <b-container class="vh-100" fluid>
        <b-row class="pt-3 justify-content-center" style="height: 80%; max-height: 80%">
            <b-col cols="10" style="max-height: 100%">
                <video id="video" ref="conferenceVideo" style="max-height: 100%; object-fit: cover; border-width: medium !important;" autoplay class="w-100 rounded border border-info"></video>
            </b-col>
        </b-row>
        <b-row class="pb-2 justify-content-center" align-v="center" style="height: 20%">
            <b-col cols="6" style="height: 65%">
                <b-card no-body class="h-100 border border-info justify-content-center" align="center" style="background-color: #e1e2e3; border-width: medium !important;">
                    <div class="d-inline-block" style="margin: 0 10px">
                            <span class="buttonGroup">
                                <b-button
                                    pill
                                    v-b-tooltip.hover
                                    @click="presenter"
                                    title="Начать показ"
                                    variant="outline-success">
                                        Презентующий
                                    </b-button>
                            </span>
                            <span class="buttonGroup">
                                <b-button
                                    pill
                                    v-b-tooltip.hover
                                    @click="viewer"
                                    title="Начать показ"
                                    variant="outline-success">
                                        Просматривающий
                                    </b-button>
                            </span>
                            <span class="buttonGroup">
                                <b-button
                                    pill
                                    v-b-tooltip.hover
                                    title="Начать показ"
                                    variant="outline-success">
                                        Отсоединиться
                                    </b-button>
                            </span>
                        <span class="buttonGroup">
                                <b-button
                                    pill
                                    @click="showPeer"
                                    v-b-tooltip.hover
                                    title="Начать показ"
                                    variant="outline-success">
                                        Peer
                                    </b-button>
                            </span>
                    </div>
                </b-card>
            </b-col>
        </b-row>
    </b-container>
</template>

<script>
import KurentoUtils from 'kurento-utils'

export default {
    name: "test-bag",
    data () {
        return {
            webSocket: new WebSocket('ws://localhost:8080/newconference'),
            video: null,
            webRtcPeer: null,
            userName: null,
            identifierConference: this.$route.params.identifier
        }
    },
    mounted() {
        this.video = this.$refs.conferenceVideo
        this.webSocket.onopen = this.setSettingSocket
    },
    methods: {
        setSettingSocket: function () {
            this.webSocket.onmessage = this.handleMessage
        },
        handleMessage: function (message) {
            const parsedMessage = JSON.parse(message.data)
            console.info('Received message: ' + message.data)

            switch (parsedMessage.id) {
            case 'presenterResponse':
                this.presenterResponse(parsedMessage)
                break
            case 'viewerResponse':
                this.viewerResponse(parsedMessage)
                break
            case 'iceCandidate':
                this.webRtcPeer.addIceCandidate(parsedMessage.candidate, function(error) {
                    if (error)
                        return console.error('Error adding candidate: ' + error)
                })
                break
            case 'stopCommunication':
                this.dispose()
                break
            default:
                console.error('Unrecognized message', parsedMessage)
            }
        },
        presenterResponse: function (message) {
            if (message.response != 'accepted') {
                const errorMsg = message.message ? message.message : 'Unknow error'
                console.info('Call not accepted for the following reason: ' + errorMsg)
            } else {
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
                this.webRtcPeer.processAnswer(message.sdpAnswer, function(error) {
                    if (error)
                        return console.error(error)
                })
            }
        },
        presenter: function () {
            if (!this.webRtcPeer) {
                const options = {
                    localVideo : this.video,
                    onicecandidate : this.onIceCandidate
                }
                const onOfferPresenterCallback = this.onOfferPresenter
                this.webRtcPeer = new KurentoUtils.WebRtcPeer.WebRtcPeerSendonly(options,
                    function(error) {
                        if (error) {
                            return console.error(error)
                        }
                        this.generateOffer(onOfferPresenterCallback)
                    })
            }
        },
        onOfferPresenter: function (error, offerSdp) {
            if (error)
                return console.error('Error generating the offer')
            console.info('Invoking SDP offer callback function ' + location.host)
            const message = {
                id : 'presenter',
                conference: this.identifierConference,
                sdpOffer : offerSdp
            }
            this.sendMessage(message)
        },
        viewer: function () {
            if (!this.webRtcPeer) {
                const options = {
                    remoteVideo : this.video,
                    onicecandidate : this.onIceCandidate
                }
                const onOfferViewerCallback = this.onOfferViewer
                this.webRtcPeer = new KurentoUtils.WebRtcPeer.WebRtcPeerRecvonly(options,
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
                conference: this.identifierConference,
                sdpOffer : offerSdp
            }
            this.sendMessage(message)
        },
        onIceCandidate: function (candidate) {
            console.log("Local candidate" + JSON.stringify(candidate))

            const message = {
                id : 'onIceCandidate',
                conference: this.identifierConference,
                candidate : candidate
            }
            this.sendMessage(message)
        },
        stop: function () {
            const message = {
                id : 'stop',
                conference: this.identifierConference,
            }
            this.sendMessage(message)
            this.dispose()
        },
        sendMessage: function (message) {
            const jsonMessage = JSON.stringify(message)
            console.log('Sending message: ' + jsonMessage)
            console.log(this.webSocket)
            this.webSocket.send(jsonMessage)
        },
        dispose: function () {
            if (this.webRtcPeer) {
                this.webRtcPeer.dispose()
                this.webRtcPeer = null
            }
        },
        showPeer: function () {
            console.log(this.webRtcPeer)
        }
    }
}
</script>