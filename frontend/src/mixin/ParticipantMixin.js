import WebRtcPeer from '@/util/WebRtcPeer'

const ParticipantMixin = {
    methods: {
        createWebRtcPeerForReceiver: function (options, participant) {
            return new WebRtcPeer.WebRtcPeerSendonly(options, generateOfferCallback(participant))
        },
        createWebRtcPeerForSender: function (options, participant) {
            return new WebRtcPeer.WebRtcPeerRecvonly(options, generateOfferCallback(participant))
        },
        getParticipantByUuid: function (uuid) {
            return this.participants.find(el => el.uuid === uuid)
        },
        showErrorCallback: function (error) {
            if (error) {
                console.error("Error adding candidate: " + error)
            }
        }
    }
}

export default ParticipantMixin

function generateOfferCallback(participant) {

    return function (error) {
        if (error) {
            return console.error(error)
        }
        this.generateOffer(participant.offerToReceiveVideo.bind(participant))
    }

}