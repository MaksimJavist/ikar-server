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
export default {
    name: "CreateRoom",
    methods: {
        createRoomCall: function () {
            this.$router.push('/room')
        }
    }
}
</script>