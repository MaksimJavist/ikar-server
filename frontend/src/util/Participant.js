class Participant {

    rtcPeer

    constructor(uuid, name, webSocketConnection) {
        this.uuid = uuid
        this.name = name
        this.webSocket = webSocketConnection
        this.video = document.createElement('video')
        this.#addSettingVideoElement()
        Object.defineProperty(this, 'rtcPeer', { writable: true})
    }

    #addSettingVideoElement() {
        this.video.id = 'video' + this.name
        this.video.autoplay = true
        this.video.controls = false
    }

    onIceCandidate(candidate) {
        const message = {
            id: 'onIceCandidate',
            candidate: candidate,
            uuid: this.uuid
        }
        this.sendMessage(message)
    }

    offerToReceiveVideo(error, offerSdp){
        if (error) return console.error ("sdp offer error")
        const msg = {
            id : "receiveVideoFrom",
            uuid : this.uuid,
            sdpOffer : offerSdp
        }
        this.sendMessage(msg)
    }

    sendMessage(message) {
        const jsonMessage = JSON.stringify(message)
        this.webSocket.send(jsonMessage)
    }

    dispose() {
        this.rtcPeer.dispose()
    }

    getObject() {
        return {
            uuid: this.uuid,
            name: this.name,
            video: this.video,
            rtcPeer: this.rtcPeer
        }
    }

    set uuid(uuid) {
        this._uuid = uuid
    }

    get uuid() {
        return this._uuid
    }

    set name(name) {
        this._name = name
    }

    get name() {
        return this._name
    }

    set video(video) {
        this._video = video
    }

    get video() {
        return this._video
    }

    set webSocket(socket) {
        this._webSocket = socket
    }

    get webSocket() {
        return this._webSocket
    }

    set rtcPeer(rtcPeer) {
        this._rtcPeer = rtcPeer
    }

    get rtcPeer() {
        return this._rtcPeer
    }
}

export default Participant