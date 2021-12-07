import KurentoUtils from 'kurento-utils'

const ParticipantMixin = {
    methods: {
        createWebRtcPeerForReceiver: function (options, participant) {
            return new KurentoUtils.WebRtcPeer.WebRtcPeerSendonly(options, generateOfferCallback(participant))
        },
        createWebRtcPeerForSender: function (options, participant) {
            return new KurentoUtils.WebRtcPeer.WebRtcPeerRecvonly(options, generateOfferCallback(participant))
        },
        getParticipantByName: function (name) {
            return this.participants.find(el => el.name === name)
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