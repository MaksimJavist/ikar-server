<template>
    <b-container fluid>
        <b-row class="vh-100 justify-content-center" align-v="center">
            <b-col cols="4">
                <b-card title="Присоедениться к комнате" align="center">
                    <b-form-input class="mt-3 mb-3 text-center form-control"
                                  :value="name"
                                  :class="{'is-invalid : propertyNameHasError' : propertyHasError('name')}"
                                  @input="updateName($event)"
                                  placeholder="Введите имя"/>
                    <div class="text-danger" v-if="propertyHasError('name')">
                        <div v-if="!$v.name.required">
                            Поле является обязательным
                        </div>
                        <div v-else-if="!$v.name.minLength">
                            Поле должно содержать более {{ $v.name.$params.minLength.min }} символов
                        </div>
                        <div v-else-if="!$v.name.maxLength">
                            Поле не может содержать более {{ $v.name.$params.maxLength.max }} символов
                        </div>
                    </div>
                    <b-row class="justify-content-center mt-4 mb-3">
                        <b-button class="w-50" @click="connectRoom" variant="outline-success" pill>
                            Присоедениться
                        </b-button>
                    </b-row>
                    <b-row class="justify-content-center">
                        <b-button class="w-50" @click="moveToMainPage" variant="outline-primary" pill>
                            На главную страницу
                        </b-button>
                    </b-row>
                </b-card>
            </b-col>
        </b-row>
    </b-container>
</template>

<script>
import { required, minLength, maxLength } from 'vuelidate/lib/validators'

export default {
    name: "join-frame",
    props: {
        name: String,
    },
    validations: {
        name: {
            required,
            minLength: minLength(2),
            maxLength:  maxLength(50)
        }
    },
    methods: {
        connectRoom: function () {
            this.$v.$touch()
            if (this.$v.name.$error) {
                this.$bvToast.toast('Проверьте правильность заполнения формы', {
                    variant: 'danger',
                    solid: true
                })
            } else {
                this.$emit('connect')
            }
        },
        updateName: function (event) {
            this.$emit('update-username', event)
        },
        moveToMainPage: function () {
            this.$router.push('/')
        },
        propertyHasError: function (property) {
            return this.$v[property].$error
        }
    }
}
</script>