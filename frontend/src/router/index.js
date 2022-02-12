import VueRouter from "vue-router"

import CreateCall from '@/components/main/main-page'
import Room from '@/components/room/room'
import Conference from '@/components/conference/conference'
import Registration from '@/components/user/user-registration'
import NotFoundPage from '@/components/error/page-404'
import UpdateUser from '@/components/user/user-update'
import UserUpdatePassword from '@/components/user/user-update-password'

export default new VueRouter({
    base: '/',
    mode: 'history',
    routes: [
        {
            path: '',
            name: 'createCall',
            component: CreateCall
        },
        {
            path: '/room/:roomId',
            name: 'room',
            component: Room
        },
        {
            path: '/conference/:identifier',
            name: 'testBag',
            component: Conference
        },

        {
            path: '/user/registration',
            name: 'registrationPage',
            component: Registration
        },
        {
            path: '/user/update',
            name: 'updateUser',
            component: UpdateUser
        },
        {
            path: '/user/update-password',
            name: 'updatePassword',
            component: UserUpdatePassword
        },
        {
            path: '*',
            name: '404',
            component: NotFoundPage
        }
    ]
})