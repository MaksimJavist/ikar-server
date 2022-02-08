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
                                    <b-form-input class="text-center form-control"
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
                                <label>Пароль:</label>
                                <b-form-input type="password"
                                              class="text-center"
                                              :class="{'is-invalid' : propertyHasError('password')}"
                                              placeholder="Пароль"
                                              v-model="password"/>
                                <div class="text-danger" v-if="propertyHasError('password')">
                                    <div v-if="!$v.password.required">
                                        Поле является обязательным
                                    </div>
                                    <div v-else-if="!$v.password.minLength">
                                        Поле должно содержать более {{ $v.password.$params.minLength.min }} символов
                                    </div>
                                    <div v-else-if="!$v.password.maxLength">
                                        Поле не может содержать более {{ $v.password.$params.maxLength.max }} символов
                                    </div>
                                </div>
                            </b-col>
                            <b-col cols="6">
                                <b-form-group>
                                    <label>Фамилия:</label>
                                    <b-form-input class="text-center form-control"
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
                        </b-form-row>
                        <b-form-row class="mb-2">
                            <b-col cols="6">
                                <label>Повторите пароль:</label>
                                <b-form-input type="password"
                                              class="text-center"
                                              :class="{'is-invalid' : propertyHasError('repeatPassword')}"
                                              placeholder="Повторите пароль"
                                              v-model="repeatPassword"/>
                                <div class="text-danger" v-if="propertyHasError('repeatPassword')">
                                    <div v-if="!$v.repeatPassword.required">
                                        Поле является обязательным
                                    </div>
                                    <div v-else-if="!$v.repeatPassword.minLength">
                                        Поле должно содержать более {{ $v.repeatPassword.$params.minLength.min }} символов
                                    </div>
                                    <div v-else-if="!$v.repeatPassword.maxLength">
                                        Поле не может содержать более {{ $v.repeatPassword.$params.maxLength.max }} символов
                                    </div>
                                    <div v-else-if="!$v.repeatPassword.validatePasswordRepeat">
                                        Пароли не совпадают
                                    </div>
                                </div>
                            </b-col>
                            <b-col cols="6">
                                <b-form-group>
                                    <label>Отчество:</label>
                                    <b-form-input class="text-center form-control"
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
                            <b-col cols="5" class="text-right">
                                <b-button class="w-100"
                                          variant="outline-success"
                                          @click="registration"
                                          :enabled="registrationButtonEnabled"
                                          pill>
                                    Зарегистрироваться
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
                    </b-form>
                </b-card>
            </b-col>
        </b-row>
    </b-container>
</template>

<script>
import { maxLength, minLength, required, helpers } from 'vuelidate/lib/validators'
import api from '@/api'

const validatePasswordRepeat = (value, siblings) => value === siblings.password
const validateLoginCharacters = helpers.regex('validateLoginCharacters', /^[a-zA-Z0-9]+$/)
const validateNamesCharacters = helpers.regex('validateNamesCharacters', /^[А-Яа-яA-Za-z]+$/)

export default {
    name: 'registration',
    data () {
        return {
            login: null,
            firstName: null,
            secondName: null,
            middleName: null,
            password: null,
            repeatPassword: null,
            registrationButtonEnabled: true
        }
    },
    validations: {
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
        },
        password: {
            required,
            minLength: minLength(6),
            maxLength: maxLength(64)
        },
        repeatPassword: {
            required,
            minLength: minLength(6),
            maxLength: maxLength(64),
            validatePasswordRepeat
        }
    },
    methods: {
        registration: function () {
            this.$v.$touch()
            if (this.$v.$error) {
                this.$bvToast.toast('Проверьте правильность заполнения полей', {
                    variant: 'danger',
                    solid: true
                })
            } else {
                this.registrationButtonEnabled = false
                const data = {
                    username: this.login,
                    firstName: this.firstName,
                    secondName: this.secondName,
                    middleName: this.middleName,
                    password: this.password
                }
                api.registerNewUser(data)
                    .then(() => {
                        this.registrationButtonEnabled = false
                        this.$bvToast.toast('Вы успешно зарегистрированы', {
                            variant: 'success',
                            solid: true
                        })
                        setTimeout(this.redirectToMainPage, 1200)
                    })
                    .catch(err => {
                        this.$bvToast.toast(err.data.message, {
                            variant: 'danger',
                            solid: true
                        })
                        this.registrationButtonEnabled = true
                    })

            }
        },
        moveToMainPage: function () {
            this.$router.push('/')
        },
        redirectToMainPage: function () {
            this.$router.replace('/')
        },
        propertyHasError: function (property) {
            return this.$v[property].$error
        }
    }
}
</script>