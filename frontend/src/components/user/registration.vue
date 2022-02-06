<template>
    <b-container fluid>
        <b-row class="vh-100 justify-content-center" align-v="center">
            <b-col cols="5">
                <b-card title="Регистрация" align="center">
                    <b-form>
                        <b-form-row class="mb-2">
                            <b-col cols="6">
                                <b-form-group>
                                    <label>Логин:</label>
                                    <b-form-input class="text-center form-control"
                                                  placeholder="Логин"
                                                  v-model="username"/>
                                </b-form-group>
                            </b-col>
                            <b-col cols="6">
                                <b-form-group>
                                    <label>Имя:</label>
                                    <b-form-input class="text-center form-control"
                                                  placeholder="Имя"
                                                  v-model="firstName"/>
                                </b-form-group>
                            </b-col>
                        </b-form-row>
                        <b-form-row class="mb-2">
                            <b-col cols="6">
                                <label>Пароль:</label>
                                <b-form-input type="password"
                                              class="text-center"
                                              placeholder="Пароль"
                                              v-model="password"/>
                            </b-col>
                            <b-col col="6">
                                <b-form-group>
                                    <label>Фамилия:</label>
                                    <b-form-input class="text-center form-control"
                                                  placeholder="Фамилия"
                                                  v-model="secondName"/>
                                </b-form-group>
                            </b-col>
                        </b-form-row>
                        <b-form-row class="mb-2">
                            <b-col cols="6">
                                <label>Повторите пароль:</label>
                                <b-form-input type="password"
                                              class="text-center"
                                              placeholder="Повторите пароль"
                                              v-model="repeatPassword"/>
                            </b-col>
                            <b-col cols="6">
                                <b-form-group>
                                    <label>Отчество:</label>
                                    <b-form-input class="text-center form-control"
                                                  placeholder="Отчество"
                                                  v-model="middleName"/>
                                </b-form-group>
                            </b-col>
                        </b-form-row>
                        <b-form-row class="justify-content-center">
                            <b-col cols="5" class="text-right">
                                <b-button class="w-75" variant="outline-success" pill>
                                    Зарегистрироваться
                                </b-button>
                            </b-col>
                            <b-col cols="5" class="text-left">
                                <b-button class="w-75"
                                          variant="outline-primary"
                                          @click="moveToMainPage"
                                          pill>
                                    На главную
                                </b-button>
                            </b-col>
                        </b-form-row>
                    </b-form>
                </b-card>
            </b-col>
        </b-row>
    </b-container>
</template>

<script>
export default {
    name: 'registration',
    data () {
        return {
            username: null,
            firstName: null,
            secondName: null,
            middleName: null,
            password: null,
            repeatPassword: null
        }
    },
    methods: {
        registration: function () {

        },
        moveToMainPage: function () {
            this.$router.push('/')
        }
    }
}
</script>