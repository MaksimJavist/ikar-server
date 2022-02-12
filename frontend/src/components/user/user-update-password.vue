<template>
    <b-container fluid>
        <b-row class="vh-100 justify-content-center" align-v="center">
            <b-col cols="5">
                <b-card title="Регистрация" align="center">
                    <b-form>
                        <b-form-row class="mb-2 justify-content-center">
                            <b-col cols="6">
                                <b-form-group>
                                    <label>Старый пароль:</label>
                                    <b-form-input type="password"
                                                  class="text-center"
                                                  :class="{'is-invalid' : propertyHasError('oldPassword')}"
                                                  placeholder="Старый пароль"
                                                  v-model="oldPassword"/>
                                    <div class="text-danger" v-if="propertyHasError('oldPassword')">
                                        <div v-if="!$v.oldPassword.required">
                                            Поле является обязательным
                                        </div>
                                        <div v-else-if="!$v.oldPassword.minLength">
                                            Поле должно содержать более {{ $v.oldPassword.$params.minLength.min }} символов
                                        </div>
                                        <div v-else-if="!$v.oldPassword.maxLength">
                                            Поле не может содержать более {{ $v.oldPassword.$params.maxLength.max }} символов
                                        </div>
                                    </div>
                                </b-form-group>
                            </b-col>
                        </b-form-row>
                        <b-form-row class="mb-2 justify-content-center">
                            <b-col cols="6">
                                <b-form-group>
                                    <label>Новый пароль:</label>
                                    <b-form-input type="password"
                                                  class="text-center"
                                                  :class="{'is-invalid' : propertyHasError('newPassword')}"
                                                  placeholder="Новый пароль"
                                                  v-model="newPassword"/>
                                    <div class="text-danger" v-if="propertyHasError('newPassword')">
                                        <div v-if="!$v.newPassword.required">
                                            Поле является обязательным
                                        </div>
                                        <div v-else-if="!$v.newPassword.minLength">
                                            Поле должно содержать более {{ $v.newPassword.$params.minLength.min }} символов
                                        </div>
                                        <div v-else-if="!$v.newPassword.maxLength">
                                            Поле не может содержать более {{ $v.newPassword.$params.maxLength.max }} символов
                                        </div>
                                    </div>
                                </b-form-group>
                            </b-col>
                        </b-form-row>
                        <b-form-row class="mb-2 justify-content-center">
                            <b-col cols="6">
                                <b-form-group>
                                    <label>Повторите новый пароль:</label>
                                    <b-form-input type="password"
                                                  class="text-center"
                                                  :class="{'is-invalid' : propertyHasError('newPasswordRepeat')}"
                                                  placeholder="Повторите новый пароль"
                                                  v-model="newPasswordRepeat"/>
                                    <div class="text-danger" v-if="propertyHasError('newPasswordRepeat')">
                                        <div v-if="!$v.newPasswordRepeat.required">
                                            Поле является обязательным
                                        </div>
                                        <div v-else-if="!$v.newPasswordRepeat.minLength">
                                            Поле должно содержать более {{ $v.newPasswordRepeat.$params.minLength.min }} символов
                                        </div>
                                        <div v-else-if="!$v.newPasswordRepeat.maxLength">
                                            Поле не может содержать более {{ $v.newPasswordRepeat.$params.maxLength.max }} символов
                                        </div>
                                        <div v-else-if="!$v.newPasswordRepeat.validatePasswordRepeat">
                                            Пароли не совпадают
                                        </div>
                                    </div>
                                </b-form-group>
                            </b-col>
                        </b-form-row>
                        <b-form-row class="justify-content-center">
                            <b-col cols="5" class="text-right">
                                <b-button class="w-100"
                                          variant="outline-success"
                                          @click="updatePassword"
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
                    </b-form>
                </b-card>
            </b-col>
        </b-row>
    </b-container>
</template>

<script>
import {maxLength, minLength, required} from 'vuelidate/lib/validators'
import api from '@/api'

const validatePasswordRepeat = (value, siblings) => value === siblings.newPassword

export default {
    name: "user-update-password",
    data () {
        return {
            oldPassword: null,
            newPassword: null,
            newPasswordRepeat: null
        }
    },
    validations: {
        oldPassword: {
            required,
            minLength: minLength(6),
            maxLength: maxLength(64)
        },
        newPassword: {
            required,
            minLength: minLength(6),
            maxLength: maxLength(64)
        },
        newPasswordRepeat: {
            required,
            minLength: minLength(6),
            maxLength: maxLength(64),
            validatePasswordRepeat
        }
    },
    methods: {
        propertyHasError: function (property) {
            return this.$v[property].$error
        },
        updatePassword: function () {
            this.$v.$touch()
            if (this.$v.$error) {
                this.$bvToast.toast('Проверьте правильность заполнения полей', {
                    variant: 'danger',
                    solid: true
                })
            } else {
                const data = {
                    oldPassword: this.oldPassword,
                    newPassword: this.newPassword
                }
                api.updateUserPassword(data)
                    .then(() => {
                        this.newPassword = null
                        this.oldPassword = null
                        this.newPasswordRepeat = null
                        this.$bvToast.toast('Пароль успешно изменен', {
                            variant: 'success',
                            solid: true
                        })
                        this.$v.$reset()
                    })
                    .catch(err => {
                        this.$bvToast.toast(err.response.data.message, {
                            variant: 'danger',
                            solid: true
                        })
                    })
            }

        },
        moveToMainPage: function () {
            window.location = '/'
        }
    }
}
</script>