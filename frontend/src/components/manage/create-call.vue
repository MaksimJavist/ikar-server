<template>
  <b-container fluid>
      <b-row class="vh-100 justify-content-center" align-v="center">
          <b-col cols="4">
              <b-card title="Выберите действие" align="center">
                    <b-row class="justify-content-center mt-4 mb-3">
                        <b-button class="w-75" @click="createRoomCall" variant="outline-success" pill>Создать комнату</b-button>
                    </b-row>
                    <b-row class="justify-content-center">
                        <b-button class="w-75" variant="outline-success" pill>Создать конференцию</b-button>
                    </b-row>
              </b-card>
          </b-col>
      </b-row>
  </b-container>
</template>

<script>
import api from '@/api'

export default {
    name: "CreateRoom",
    methods: {
        createRoomCall: function () {
            api.createNewRoom()
                .then(resp => {
                    this.$router.push(`/room/${resp.data}`)
                })
        },
    }
}
</script>