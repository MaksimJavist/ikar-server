<template>
  <div class="participant main" :id="participant.name">
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
    }
}
</script>