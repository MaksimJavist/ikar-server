<template>
    <b-row  class="justify-content-center" align-v="center" style="height: 15%">
        <b-col cols="7" class="h-75">
            <b-card no-body class="h-100 border border-info justify-content-center" align="center" style="background-color: #e1e2e3; border-width: medium !important;">
                <div class="d-inline-block" style="margin: 0 10px">
                    <span class="buttonGroup">
                        <b-button
                            v-if="presenterFlag"
                            pill
                            v-b-tooltip.hover
                            @click="stopCommunicationEmit"
                            title="Прекратить показ"
                            variant="outline-danger">
                            Прекратить показ
                        </b-button>
                        <b-button
                            v-else
                            pill
                            v-b-tooltip.hover
                            @click="presenterConnectEmit"
                            title="Начать показ"
                            variant="outline-success">
                            Начать показ
                        </b-button>
                    </span>
                    <span class="buttonGroup">
                        <b-button
                            v-if="audioEnableFlag"
                            v-b-tooltip.hover
                            title="Выключить микрофон"
                            @click="changeAudioEnableEmit(false)"
                            variant="outline-success">
                            <b-icon-mic/>
                        </b-button>
                        <b-button
                            v-else
                            v-b-tooltip.hover
                            title="Включить микрофон"
                            @click="changeAudioEnableEmit(true)"
                            variant="outline-danger">
                            <b-icon-mic-mute/>
                        </b-button>
                    </span>
                    <span class="buttonGroup">
                        <b-button
                            v-if="videoEnableFlag"
                            v-b-tooltip.hover
                            title="Выключить камеру"
                            @click="changeVideoEnabledEmit(false)"
                            variant="outline-success">
                            <b-icon-camera-video/>
                        </b-button>
                        <b-button
                            v-else
                            v-b-tooltip.hover
                            title="Включить камеру"
                            @click="changeVideoEnabledEmit(true)"
                            variant="outline-danger">
                            <b-icon-camera-video-off/>
                        </b-button>
                    </span>
                    <span class="buttonGroup">
                        <b-button
                            variant="outline-primary"
                            v-b-tooltip.hover
                            title="Скопировать ссылку на комнату"
                            v-clipboard:copy="getRoomFullReference()"
                            @click="copyLinkToast">
                            <b-icon-share/>
                        </b-button>
                    </span>
                    <span class="buttonGroup">
                        <b-button
                            :variant="uncheckedMessagesCount === 0 ? 'outline-info' : 'info'"
                            v-b-tooltip.hover
                            :title="uncheckedMessagesCount === 0 ? 'Открыть чат' : `У вас ${ uncheckedMessagesCount } непрочитанных сообщений`"
                            @click="switchChatVisibleEmit">
                            <b-icon-chat-dots/>
                            <span v-if="uncheckedMessagesCount !== 0" class="pl-1">+{{ uncheckedMessagesCount }}</span>
                        </b-button>
                    </span>
                    <span class="buttonGroup">
                        <b-button
                            pill
                            variant="outline-danger"
                            v-b-tooltip.hover
                            title="Покинуть комнату"
                            @click="exitRoomEmit">
                            Покинуть комнату
                        </b-button>
                    </span>
                </div>
            </b-card>
        </b-col>
    </b-row>
</template>

<script>
export default {
    name: "room-bottom-panel",
    props: {
        presenterFlag: Boolean,
        audioEnableFlag: Boolean,
        videoEnableFlag: Boolean,
        uncheckedMessagesCount: Number
    },
    methods: {
        stopCommunicationEmit: function () {
            this.$emit('stop-communication')
        },
        presenterConnectEmit: function () {
            this.$emit('presenter-connect')
        },
        changeAudioEnableEmit: function (value) {
            this.$emit('change-audio', value)
        },
        changeVideoEnabledEmit: function (value) {
            this.$emit('change-video', value)
        },
        switchChatVisibleEmit: function () {
            this.$emit('switch-chat-visible')
        },
        exitRoomEmit: function () {
            this.$emit('exit-room')
        },
        getRoomFullReference: function () {
            return window.location.href
        },
        copyLinkToast: function () {
            this.$bvToast.toast('Ссылка на комнату скопирована!', {
                variant: 'info',
                solid: true
            })
        },
    }
}
</script>