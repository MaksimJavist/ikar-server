<template>
    <b-container fluid>
        <b-row class="vh-100 justify-content-center" align-v="center">
            <b-col cols="5">
                <b-card :title="getTitle()" align="center">
                    <b-form>
                        <b-form-row class="mb-2">
                            <b-col cols="6">
                                <b-form-group>
                                    <label>Логин:</label>
                                    <b-form-input :disabled="viewMode"
                                                  class="text-center form-control"
                                                  :class="{'is-invalid' : propertyHasError('login')}"
                                                  placeholder="Логин"
                                                  v-model="login"/>
                                    <div class="text-danger" v-if="propertyHasError('login')">
                                        <div v-if="!$v.login.required">
                                            Поле является обязательным
                                        </div>
                                        <div v-else-if="!$v.login.validateLoginCharacters">
                                            Логин может содержать только строчные и прописные латинские буквы и цифры
                                        </div>
                                        <div v-else-if="!$v.login.minLength">
                                            Поле должно содержать более {{ $v.login.$params.minLength.min }} символов
                                        </div>
                                        <div v-else-if="!$v.login.maxLength">
                                            Поле не может содержать более {{ $v.login.$params.maxLength.max }} символов
                                        </div>
                                    </div>
                                </b-form-group>
                            </b-col>
                            <b-col cols="6">
                                <b-form-group>
                                    <label>Имя:</label>
                                    <b-form-input :disabled="viewMode"
                                                  class="text-center form-control"
                                                  :class="{'is-invalid' : propertyHasError('firstName')}"
                                                  placeholder="Имя"
                                                  v-model="firstName"/>
                                    <div class="text-danger" v-if="propertyHasError('firstName')">
                                        <div v-if="!$v.firstName.required">
                                            Поле является обязательным
                                        </div>
                                        <div v-else-if="!$v.firstName.validateNamesCharacters">
                                            Имя может содержать только латинские или русские буквы
                                        </div>
                                        <div v-else-if="!$v.firstName.minLength">
                                            Поле должно содержать более {{ $v.firstName.$params.minLength.min }} символов
                                        </div>
                                        <div v-else-if="!$v.firstName.maxLength">
                                            Поле не может содержать более {{ $v.firstName.$params.maxLength.max }} символов
                                        </div>
                                    </div>
                                </b-form-group>
                            </b-col>
                        </b-form-row>
                        <b-form-row class="mb-2">
                            <b-col cols="6">
                                <b-form-group>
                                    <label>Фамилия:</label>
                                    <b-form-input :disabled="viewMode"
                                                  class="text-center form-control"
                                                  :class="{'is-invalid' : propertyHasError('secondName')}"
                                                  placeholder="Фамилия"
                                                  v-model="secondName"/>
                                    <div class="text-danger" v-if="propertyHasError('secondName')">
                                        <div v-if="!$v.secondName.required">
                                            Поле является обязательным
                                        </div>
                                        <div v-else-if="!$v.secondName.validateNamesCharacters">
                                            Фамилия может содержать только латинские или русские буквы
                                        </div>
                                        <div v-else-if="!$v.secondName.minLength">
                                            Поле должно содержать более {{ $v.secondName.$params.minLength.min }} символов
                                        </div>
                                        <div v-else-if="!$v.secondName.maxLength">
                                            Поле не может содержать более {{ $v.secondName.$params.maxLength.max }} символов
                                        </div>
                                    </div>
                                </b-form-group>
                            </b-col>
                            <b-col cols="6">
                                <b-form-group>
                                    <label>Отчество:</label>
                                    <b-form-input :disabled="viewMode"
                                                  class="text-center form-control"
                                                  :class="{'is-invalid' : propertyHasError('middleName')}"
                                                  placeholder="Отчество"
                                                  v-model="middleName"/>
                                    <div class="text-danger" v-if="propertyHasError('middleName')">
                                        <div v-if="!$v.middleName.minLength">
                                            Поле должно содержать более {{ $v.middleName.$params.minLength.min }} символов
                                        </div>
                                        <div v-else-if="!$v.middleName.validateNamesCharacters">
                                            Отчетство может содержать только латинские или русские буквы
                                        </div>
                                        <div v-else-if="!$v.middleName.maxLength">
                                            Поле не может содержать более {{ $v.middleName.$params.maxLength.max }} символов
                                        </div>
                                    </div>
                                </b-form-group>
                            </b-col>
                        </b-form-row>
                        <b-form-row class="justify-content-center">
                            <b-col v-if="viewMode" cols="5" class="text-right">
                                <b-button
                                          class="w-100"
                                          variant="outline-primary"
                                          @click="enableChangeMode"
                                          pill>
                                    Редактировать
                                </b-button>
                            </b-col>
                            <b-col v-else cols="5" class="text-right">
                                <b-button class="w-100"
                                          variant="outline-success"
                                          @click="update"
                                          pill>
                                    Сохранить
                                </b-button>
                            </b-col>
                            <b-col cols="5" class="text-left">
                                <b-button class="w-100"
                                          variant="outline-primary"
                                          @click="moveToMainPage"
                                          pill>
                                    На главную
                                </b-button>
                            </b-col>
                        </b-form-row>
                        <b-form-row class="justify-content-center mt-3">
                            <b-col cols="5" class="text-left">
                                <b-button class="w-100"
                                          variant="outline-info"
                                          @click="moveToUpdatePasswordPage"
                                          pill>
                                    Сменить пароль
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
import {helpers, maxLength, minLength, required} from 'vuelidate/lib/validators'
import api from '@/api'

const validateLoginCharacters = helpers.regex('validateLoginCharacters', /^[a-zA-Z0-9]+$/)
const validateNamesCharacters = helpers.regex('validateNamesCharacters', /^[А-Яа-яA-Za-z]+$/)

export default {
    name: "user-update",
    data () {
        return {
            uuid: null,
            login: null,
            firstName: null,
            secondName: null,
            middleName: null,
            viewMode: true
        }
    },
    validations: {
        uuid: {
            required
        },
        login: {
            required,
            minLength: minLength(5),
            maxLength: maxLength(30),
            validateLoginCharacters
        },
        firstName: {
            required,
            minLength: minLength(2),
            maxLength: maxLength(30),
            validateNamesCharacters
        },
        secondName: {
            required,
            minLength: minLength(2),
            maxLength: maxLength(30),
            validateNamesCharacters
        },
        middleName: {
            minLength: minLength(2),
            maxLength: maxLength(30),
            validateNamesCharacters
        }
    },
    beforeCreate() {
        api.getAuthenticatedUser()
            .then(resp => {
                const { uuid, username, firstName, secondName, middleName } = resp.data
                this.uuid = uuid
                this.login = username
                this.firstName = firstName
                this.secondName = secondName
                this.middleName = middleName
            })
    },
    methods: {
        enableChangeMode: function () {
            this.viewMode = false
        },
        update: function () {
            this.$v.$touch()
            if (this.$v.$error) {
                this.$bvToast.toast('Проверьте правильность заполнения полей', {
                    variant: 'danger',
                    solid: true
                })
            } else {
                const data = {
                    uuid: this.uuid,
                    username: this.login,
                    firstName: this.firstName,
                    secondName: this.secondName,
                    middleName: this.middleName
                }
                api.updateUser(data)
                    .then(resp => {
                        this.$bvToast.toast('Данные пользователя обновлены', {
                            variant: 'success',
                            solid: true
                        })
                        const { uuid, username, firstName, secondName, middleName } = resp.data
                        this.uuid = uuid
                        this.username = username
                        this.firstName = firstName
                        this.secondName = secondName
                        this.middleName = middleName

                        this.viewMode = true
                    })
                    .catch(err => {
                        this.$bvToast.toast(err.response.data.message, {
                            variant: 'danger',
                            solid: true
                        })
                    })
            }
        },
        getTitle: function () {
            return `${this.secondName} ${this.firstName} ${this.middleName ? this.middleName : ''}`
        },
        moveToMainPage: function () {
            window.location = '/'
        },
        propertyHasError: function (property) {
            return this.$v[property].$error
        },
        moveToUpdatePasswordPage: function () {
            this.$router.push('/user/update-password')
        }
    }
}
</script>