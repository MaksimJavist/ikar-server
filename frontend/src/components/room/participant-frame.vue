<template>
    <div class="participant main" :id="participant.name">
        <div ref="videoWrapper"/>
        <span v-if="!local">{{participant.name}}</span>
    </div>
</template>

<script>
export default {
    name: "participant-frame",
    props: {
        participant: Object,
        local: Boolean
    },
    mounted() {
        this.$refs.videoWrapper.appendChild(this.participant.video)
    }
}
</script>