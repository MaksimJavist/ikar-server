<template>
    <div class="h-100 w-100 bg-primary rounded text-center" >
        <div ref="videoWrapper" class="rounded" style="height: 85%">
        </div>
        <p @click="showRtcPeer">{{ participant.name }}</p>
    </div>
</template>

<script>
export default {
    name: "participant-local",
    props: {
        participant: Object
    },
    mounted() {
        this.$refs.videoWrapper.appendChild(this.participant.video)
    },
    methods: {
        showRtcPeer: function () {
            console.log(this.participant.rtcPeer)
        }
    }
}
</script>