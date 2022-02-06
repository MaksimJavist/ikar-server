import VueRouter from "vue-router"

import CreateCall from '@/components/main/main-page'
import Room from '@/components/room/room'
import Conference from '@/components/conference/conference'
import Registration from '@/components/user/registration'
import NotFoundPage from '@/components/error/page-404'

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
            path: '/registration',
            name: 'registrationPage',
            component: Registration
        },
        {
            path: '*',
            name: '404',
            component: NotFoundPage
        }
    ]
})