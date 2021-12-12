<template>
    <div id="container">
        <div id="wrapper">
            <div v-if="joinFrameVisible" id="join" class="animate join">
                <h1>Присоедениться к комнате</h1>
                <form onsubmit="return false;" accept-charset="UTF-8">
                    <p>
                        <input v-if="!authenticatedUser"
                               v-model="userName"
                               type="text"
                               placeholder="Введите имя" required>
                    </p>
                    <p class="submit">
                        <input @click="joinRoom" type="submit" name="commit" value="Присоедениться">
                    </p>
                </form>
            </div>
            <div id="room">
<!--              <div id="participant" v-if="isFilledLocalParticipant">-->
<!--                <ParticipantFrame :participant="localParticipant"-->
<!--                                  style="margin: 1%"/>-->
<!--              </div>-->
                <div id="participants" v-for="(participant, index) in getFilledParticipants" :key="index">
                    <ParticipantFrame v-if="participant"
                                      :participant="participant.getObject()"
                                      style="margin: 1%"/>
                </div>
              <input type="button" id="button-leave" @click="leaveRoom" value="Leave room">
            </div>
        </div>
    </div>
</template>

<script>
import Participant from '@/util/Participant'
import ParticipantFrame from '@/components/room/participant-frame'
import ParticipantMixin from "@/mixin/ParticipantMixin"
import api from '@/api'

export default {
    name: "room",
    components: {
        ParticipantFrame
    },
    mixins: [
        ParticipantMixin
    ],
    data() {
        return {
            joinFrameVisible: true,
            userName: null,
            roomName: null,
            localParticipant: null,
            participants: [],
            socket: null,
            authenticatedUser: false
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
    created() {
        this.socket = new WebSocket('ws://localhost:8080/groupcall')
    },
    beforeDestroy() {
        if (this.socket) {
            this.socket.close()
        }
    },
    computed: {
        isFilledLocalParticipant: function () {
            if (!this.localParticipant) {
                return false
            }
            const { name, video, rtcPeer } = this.localParticipant
            return (name && video && rtcPeer)
        },
        getFilledParticipants: function () {
            return this.participants.filter(el => el.name && el.video && el.rtcPeer)
        }
    },
    methods: {
        joinRoom: function () {
            this.joinFrameVisible = false
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
                const candidateName = parsedMessage.name
                const candidate = this.getParticipantByName(candidateName)
                candidate.rtcPeer.addIceCandidate(parsedMessage.candidate, this.showErrorCallback)
                break
            }}
        },
        onExistingParticipants: function (message) {
            const constraints = {
                audio : true,
                video : {
                    mandatory : {
                        maxWidth : 320,
                        maxFrameRate : 15,
                        minFrameRate : 15
                    }
                }
            }
            const participantUuid = message.registeredUuid
            const participantName = message.registeredName
            const participant = new Participant(participantUuid, participantName, this.socket)
            const video = participant.video

            const options = {
                localVideo: video,
                mediaConstraints: constraints,
                onicecandidate: participant.onIceCandidate.bind(participant)
            }
            participant.rtcPeer = this.createWebRtcPeerForReceiver(options, participant)
            this.participants.push(participant)

            message.data.forEach(this.receiveVideoFromSender)
        },
        onNewParticipant(request) {
            this.receiveVideoFromSender(request.data)
        },
        onParticipantLeft(request) {
            const indexLeaved = this.participants.findIndex(el => el.uuid === request.uuid)
            this.participants[indexLeaved].dispose()
            this.participants.splice(indexLeaved, 1)
        },
        receiveVideoFromSender: function (sender) {
            const participant = new Participant(sender.uuid, sender.name, this.socket)
            const video = participant.video

            const options = {
                remoteVideo: video,
                onicecandidate: participant.onIceCandidate.bind(participant)
            }
            participant.rtcPeer = this.createWebRtcPeerForSender(options, participant)

            this.participants.push(participant)
        },
        receiveVideoResponse: function (result) {
            this.participants.find(el => el.uuid === result.uuid).rtcPeer.processAnswer(result.sdpAnswer, function (error) {
                if (error) return console.error (error)
            })
        },
        sendMessage: function (message) {
            const jsonMessage = JSON.stringify(message)
            this.socket.send(jsonMessage)
        },
        leaveRoom: function () {
            this.sendMessage({
                id : 'leaveRoom'
            })

            this.participants.splice(0, this.participants.length)
        }
    }
}
</script>