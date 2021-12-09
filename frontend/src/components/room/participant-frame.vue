<template>
  <div class="participant main" :id="participant.name" @click="disableMicro">
      <div ref="videoWrapper"/>
      <span>{{participant.name}}</span>
  </div>
</template>

<script>
export default {
    name: "participant-frame",
    props: {
        participant: Object
    },
    mounted() {
        this.$refs.videoWrapper.appendChild(this.participant.video)
    },
    methods: {
        disableMicro: function () {
            this.participant.rtcPeer.audioEnabled = false
            console.log(this.participant)
        }
    }
}
</script>