<template>
    <b-row class="pb-2 justify-content-center" align-v="center" style="height: 20%">
        <b-col cols="8" style="height: 65%">
            <b-card no-body class="h-100 border border-info justify-content-center" align="center" style="background-color: #e1e2e3; border-width: medium !important;">
                <div class="d-inline-block" style="margin: 0 10px">
                    <span v-if="presenterFlag">
                        <span class="buttonGroup">
                            <b-button
                                v-if="enableAudioFlag"
                                v-b-tooltip.hover
                                title="Выключить микрофон"
                                variant="outline-success"
                                @click="changeEnableAudioEmit(false)">
                                <b-icon-mic/>
                            </b-button>
                            <b-button
                                v-else
                                v-b-tooltip.hover
                                title="Включить микрофон"
                                variant="outline-danger"
                                @click="changeEnableAudioEmit(true)">
                                <b-icon-mic-mute/>
                            </b-button>
                        </span>
                        <span class="buttonGroup">
                            <b-button
                                v-if="enableVideoFlag"
                                v-b-tooltip.hover
                                title="Выключить камеру"
                                variant="outline-success"
                                @click="changeEnableVideoEmit(false)">
                                <b-icon-camera-video/>
                            </b-button>
                            <b-button
                                v-else
                                v-b-tooltip.hover
                                title="Включить камеру"
                                variant="outline-danger"
                                @click="changeEnableVideoEmit(true)">
                                <b-icon-camera-video-off/>
                            </b-button>
                        </span>
                        <span class="buttonGroup">
                            <b-button
                                pill
                                v-b-tooltip.hover
                                title="Прекратить показ"
                                variant="outline-danger"
                                @click="stopPresentationEmit">
                                Прекратить показ
                            </b-button>
                        </span>
                    </span>
                    <span class="buttonGroup" v-else>
                        <b-button
                            pill
                            v-b-tooltip.hover
                            @click="startPresentationEmit"
                            title="Начать показ"
                            variant="outline-success">
                                Начать показ
                        </b-button>
                    </span>
                    <span class="buttonGroup">
                        <b-button
                            v-b-tooltip.hover
                            @click="showParticipantsFrameEmit"
                            title="Участники конференции"
                            variant="outline-info">
                                <b-icon-people-fill/>
                                {{ participantCount }}
                        </b-button>
                    </span>
                    <span class="buttonGroup">
                        <b-button
                            :variant="uncheckedMessagesCount === 0 ? 'outline-info' : 'info'"
                            v-b-tooltip.hover
                            :title="uncheckedMessagesCount === 0 ? 'Открыть чат' : `У вас ${ uncheckedMessagesCount } непрочитанных сообщений`"
                            @click="showChatEmit">
                            <b-icon-chat-dots/>
                            <span v-if="uncheckedMessagesCount !== 0" class="pl-1">+{{ uncheckedMessagesCount }}</span>
                        </b-button>
                    </span>
                    <span class="buttonGroup">
                        <b-button
                            variant="outline-primary"
                            v-b-tooltip.hover
                            title="Скопировать ссылку на комнату"
                            v-clipboard:copy="getConferenceFullReference()"
                            @click="copyLinkToast">
                            <b-icon-share/>
                        </b-button>
                    </span>
                    <span class="buttonGroup">
                        <b-button
                            pill
                            v-b-tooltip.hover
                            @click="exitEmit"
                            title="Начать показ"
                            variant="outline-danger">
                                Покинуть трансляцию
                        </b-button>
                    </span>
                </div>
            </b-card>
        </b-col>
    </b-row>
</template>

<script>
export default {
    name: "conference-bottom-panel",
    props: {
        presenterFlag: Boolean,
        enableAudioFlag: Boolean,
        enableVideoFlag: Boolean,
        uncheckedMessagesCount: Number,
        participantCount: Number
    },
    methods: {
        changeEnableAudioEmit: function (value) {
            this.$emit('change-audio', value)
        },
        changeEnableVideoEmit: function (value) {
            this.$emit('change-video', value)
        },
        stopPresentationEmit: function () {
            this.$emit('stop-presentation')
        },
        startPresentationEmit: function () {
            this.$emit('start-presentation')
        },
        showParticipantsFrameEmit: function () {
            this.$emit('show-participants-frame')
        },
        showChatEmit: function () {
            this.$emit('show-chat')
        },
        exitEmit: function () {
            this.$emit('exit')
        },
        getConferenceFullReference: function () {
            return window.location.href
        },
        copyLinkToast: function () {
            this.$bvToast.toast('Ссылка на конферению скопирована!', {
                variant: 'info',
                solid: true
            })
        }
    }
}
</script>