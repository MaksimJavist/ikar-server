<template>
    <div class="h-100 w-100 bg-dark rounded text-center" >
        <div ref="videoWrapper" class="rounded" style="height: 85%">
        </div>
        <p class="text-white">{{ participant.name }}</p>
    </div>
</template>

<script>
export default {
    name: "participant-remote",
    props: {
        participant: Object
    },
    mounted() {
        this.$refs.videoWrapper.appendChild(this.participant.video)
    }
}
</script>