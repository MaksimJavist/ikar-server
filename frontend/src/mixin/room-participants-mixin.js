import Participant from "@/util/Participant"
import WebRtcPeer from "@/util/WebRtcPeer"

const roomParticipantsMixin = {
    methods: {
        onExistingParticipants: function (message) {
            const constraints = {
                audio : true,
                video : {
                    mandatory : {
                        maxWidth : 320,
                        maxFrameRate : 30,
                        minFrameRate : 15
                    }
                }
            }
            this.localParticipantUuid = message.registeredUuid
            const participantUuid = message.registeredUuid
            const participantName = message.registeredName
            this.chatMessages = this.chatMessages.concat(message.messages)
            const participant = new Participant(participantUuid, participantName, this.socket)
            const video = participant.video

            const options = {
                localVideo: video,
                mediaConstraints: constraints,
                onicecandidate: participant.onIceCandidate.bind(participant),
                configuration: JSON.parse(process.env.VUE_APP_ICE_SERVER_CONFIG),
                mediaUseOptions: {
                    audio: this.microEnable,
                    video: this.videoEnable
                }
            }

            const successCreationPeerCallback = this.successGenerationPeer
            const errorCreationPeerCallback = this.errorCreationPeer
            participant.rtcPeer = new WebRtcPeer.WebRtcPeerSendonly(options, function (error) {
                if (error) {
                    return errorCreationPeerCallback()
                }
                this.generateOffer(participant.offerToReceiveVideo.bind(participant))
                successCreationPeerCallback(participant, message.data)
            })
        },
        successGenerationPeer: function (localParticipant, participants) {
            this.joinFrameVisible = false
            this.participants.push(localParticipant)
            participants.forEach(this.receiveVideoFromSender)
            this.viewerConnectPermission()
        },
        errorCreationPeer: function () {
            this.$bvToast.toast('Не удалось получить доступ к камере или микрофону, проверите разрешения вашего браузера.', {
                variant: 'warning',
                solid: true
            })
            this.exitFromRoom()
        },
        receiveVideoFromSender: function (sender) {
            const participant = new Participant(sender.uuid, sender.name, this.socket)
            const video = participant.video

            const options = {
                remoteVideo: video,
                onicecandidate: participant.onIceCandidate.bind(participant),
                configuration: JSON.parse(process.env.VUE_APP_ICE_SERVER_CONFIG)
            }
            participant.rtcPeer = new WebRtcPeer.WebRtcPeerRecvonly(options, function (error) {
                if (error) {
                    return console.error(error)
                }
                this.generateOffer(participant.offerToReceiveVideo.bind(participant))
            })
            this.participants.push(participant)
        },
        receiveVideoResponse: function (result) {
            this.participants.find(el => el.uuid === result.uuid).rtcPeer.processAnswer(result.sdpAnswer, function (error) {
                if (error) return console.error (error)
            })
        },
        onNewParticipant(request) {
            this.receiveVideoFromSender(request.data)
        },
        onParticipantLeft(request) {
            this.$bvToast.toast(request.message, {
                variant: 'info',
                solid: true
            })
            const indexLeaved = this.participants.findIndex(el => el.uuid === request.uuid)
            this.participants[indexLeaved].dispose()
            this.participants.splice(indexLeaved, 1)
        }
    }
}

export default roomParticipantsMixin