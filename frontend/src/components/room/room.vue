<template>
  <div id="container">
    <div id="wrapper">
      <div id="join" class="animate join">
        <h1>Join a Room</h1>
        <form onsubmit="return false;" accept-charset="UTF-8">
          <p>
            <input v-model="userName" type="text" name="name" value="" id="name"
                   placeholder="Username" required>
          </p>
<!--          <p>-->
<!--            <input v-model="roomName" type="text" name="room" value="" id="roomName"-->
<!--                   placeholder="Room" required>-->
<!--          </p>-->
          <p class="submit">
            <input @click="joinRoom" type="submit" name="commit" value="Join!">
          </p>
        </form>
      </div>
      <div id="room">
          <h2 id="room-header"></h2>
          <div id="video">

          </div>
          <div id="participants" v-for="(participant, index) in getFilledParticipants" :key="index">
              <ParticipantFrame v-if="participant" :participant="participant.getObject()"/>
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
            userName: null,
            roomName: null,
            participants: [],
            socket: null
        }
    },
    beforeDestroy() {
        if (this.socket) {
            this.socket.close()
        }
    },
    created() {
        this.socket = new WebSocket('ws://localhost:8080/groupcall')
    },
    computed: {
        getFilledParticipants: function () {
            return this.participants.filter(el => el.name && el.video)
        }
    },
    methods: {
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
            const participant = new Participant(this.userName, this.socket)
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
            this.receiveVideoFromSender(request.name)
        },
        onParticipantLeft(request) {
            const indexLeaved = this.participants.findIndex(el => el.name === request.name)
            this.participants[indexLeaved].dispose()
            this.participants.splice(indexLeaved, 1)
        },
        receiveVideoFromSender: function (sender) {
            const participant = new Participant(sender, this.socket)
            const video = participant.video

            const options = {
                remoteVideo: video,
                onicecandidate: participant.onIceCandidate.bind(participant)
            }
            participant.rtcPeer = this.createWebRtcPeerForSender(options, participant)

            this.participants.push(participant)
        },
        receiveVideoResponse: function (result) {
            this.participants.find(el => el.name === result.name).rtcPeer.processAnswer(result.sdpAnswer, function (error) {
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