import VueRouter from "vue-router"

import CreateCall from '@/components/manage/create-call'
import Room from '@/components/room/room'

export default new VueRouter({
    base: '/',
    mode: 'history',
    routes: [
        {
            path: '/create-call',
            name: 'createCall',
            component: CreateCall
        },
        {
            path: '/room',
            name: 'room',
            component: Room
        }
    ]
})