<template>
    <div class="h-100 w-100 bg-primary rounded text-center" >
        <div ref="videoWrapper" class="rounded" style="height: 85%">
        </div>
        <p>{{ participant.name }}</p>
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