<template>
    <b-card class="border border-primary position-fixed rounded text-center"
            style="width: 20%; height: 500px; border-width: medium !important; bottom: 3%; left: 1%; background-color: #e1e2e3;">
        <b-button
            class="position-absolute"
            style="right: 5px; top: 5px"
            variant="outline-danger"
            title="Закрыть"
            @click="hideFrameEmit">
            <b-icon-x/>
        </b-button>
        <h3>Участники</h3>
        <div v-for="(participant, index) in getParticipants" :key="index" class="text-left pt-2">
            <div class="bg-info rounded p-2 text-white">
                <div class="d-inline-block">
                    <b-icon-person-fill style="width: 20px; height: 20px"
                                        class="ml-2 mr-2"
                                        v-b-tooltip.hover
                                        :title="`Участник: ${participant.name}`"/>
                    <span>{{ participant.name }}</span>
                </div>
                <div class="float-right">
                    <b-icon-star-fill v-if="participant.uuid === localParticipantUuid"
                                      v-b-tooltip.hover
                                      title="Это вы"/>
                    <b-icon-display v-if="participant.uuid === presenterParticipantUuid"
                                    v-b-tooltip.hover
                                    title="Презентующий"/>
                </div>
            </div>
        </div>
    </b-card>
</template>

<script>
export default {
    name: "conference-participants-frame",
    props: {
        participants: Array,
        localParticipantUuid: String,
        presenterParticipantUuid: String
    },
    computed: {
        getParticipants: function () {
            const localParticipant = this.participants.find(element => element.uuid === this.localParticipantUuid)
            const otherParticipants = this.participants.filter(el => el.uuid !== this.localParticipantUuid)
            const participantNames = localParticipant ? [].concat(localParticipant, otherParticipants) : otherParticipants
            return participantNames
        }
    },
    methods: {
        hideFrameEmit: function () {
            this.$emit('hide-frame')
        }
    }
}
</script>