<template>
  <div id="container">
    <div id="wrapper">
      <div id="join" class="animate join">
        <h1>Выберите действие</h1>
        <p class="submit">
          <input @click="createRoomCall" type="submit" name="commit" value="Создать комнату">
        </p>
        <p class="submit">
          <input type="submit" name="commit" value="Создать конференцию">
        </p>
      </div>
    </div>
  </div>
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
        }
    }
}
</script>