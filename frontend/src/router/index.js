import VueRouter from "vue-router"

import CreateCall from '@/components/manage/create-call'
import Room from '@/components/room/room'

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
        }
    ]
})