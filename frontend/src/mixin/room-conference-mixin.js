import WebRtcPeer from "@/util/WebRtcPeer"

const roomConferenceMixin = {
    data () {
        return {
            webRtcPeer: null,
            isActivePresentation: false,
            isPresenter: false
        }
    },
    methods: {
        presenterConnectPermission: function () {
            const message = {
                id: 'presenterConnectPermission'
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
        presenter: function () {
            if (!this.webRtcPeer) {
                const constraints = {
                    audio : false,
                    video : {
                        width: screen.width,
                        height: screen.height,
                        maxFrameRate: 40,
                        minFrameRate: 15
                    },
                }
                const options = {
                    localVideo : this.$refs.presentationVideo,
                    onicecandidate : this.presentationOnIceCandidate,
                    mediaConstraints: constraints,
                    configuration: JSON.parse(process.env.VUE_APP_ICE_SERVER_CONFIG),
                    sendSource: 'screen'
                }
                const onOfferPresenterCallback = this.onOfferPresenter
                const disposePeerCallback = this.disposePresentationPeer
                this.webRtcPeer = new WebRtcPeer.WebRtcPeerSendonly(options,
                    function(error) {
                        if (error) {
                            return disposePeerCallback()
                        }
                        this.generateOffer(onOfferPresenterCallback)
                    })
            }
        },
        presentationOnIceCandidate: function (candidate) {
            const message = {
                id: 'presentationOnIceCandidate',
                candidate: candidate
            }
            this.sendMessage(message)
        },
        presentationIceCandidate: function (message) {
            this.webRtcPeer.addIceCandidate(message.candidate, function(error) {
                if (error)
                    return console.error('Error adding candidate: ' + error)
            })
        },
        onOfferPresenter: function (error, offerSdp) {
            const message = {
                id : 'presenter',
                sdpOffer : offerSdp
            }
            this.sendMessage(message)
        },
        disposePresentationPeer: function () {
            if (this.webRtcPeer) {
                this.webRtcPeer.dispose()
                this.webRtcPeer = null
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
                this.isPresenter = true
                this.isActivePresentation = true
            }
        },
        newPresenter: function (message) {
            this.$bvToast.toast(message.message, {
                variant: 'info',
                solid: true
            })
            this.viewer()
        },
        viewerConnectPermission: function () {
            const message = {
                id: 'viewerConnectPermission'
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
        viewer: function () {
            if (!this.webRtcPeer) {
                const options = {
                    remoteVideo : this.$refs.presentationVideo,
                    onicecandidate : this.presentationOnIceCandidate,
                    video : {
                        mandatory : {
                            maxWidth : screen.width,
                            maxHeight: screen.height,
                            maxFrameRate: 40,
                            minFrameRate: 15
                        }
                    },
                    configuration: JSON.parse(process.env.VUE_APP_ICE_SERVER_CONFIG)
                }
                const onOfferViewerCallback = this.onOfferViewer
                const disposePeerCallback = this.disposePresentationPeer
                this.webRtcPeer = new WebRtcPeer.WebRtcPeerRecvonly(options,
                    function(error) {
                        if (error) {
                            disposePeerCallback()
                        }
                        this.generateOffer(onOfferViewerCallback)
                    })
            }
        },
        onOfferViewer: function (error, offerSdp) {
            const message = {
                id : 'viewer',
                sdpOffer : offerSdp
            }
            this.sendMessage(message)
        },
        viewerResponse: function (message) {
            if (message.response !== 'accepted') {
                const errorMsg = message.message ? message.message : 'Неизвестная ошибка'
                this.$bvToast.toast(errorMsg, {
                    variant: 'info',
                    solid: true
                })
            } else {
                this.webRtcPeer.processAnswer(message.sdpAnswer, function(error) {
                    if (error)
                        return console.error(error)
                })
                this.isActivePresentation = true
            }
        },
        presenterStopCommunication: function (message) {
            this.$bvToast.toast(message.message, {
                variant: 'info',
                solid: true
            })
            this.disposePresentationPeer()
            this.isActivePresentation = false
        },
        stopCommunication: function () {
            this.disposePresentationPeer()
            const message = {
                id: 'presenterStopCommunication'
            }
            this.sendMessage(message)
            this.isActivePresentation = false
            this.isPresenter = false
        },
        refreshStatePresentation: function () {
            this.disposePresentationPeer()
            this.webRtcPeer = null
            this.isActivePresentation = false
            this.isPresenter = false
        }
    }
}

export default roomConferenceMixin