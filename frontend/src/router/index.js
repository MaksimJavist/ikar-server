import VueRouter from "vue-router"

import CreateCall from '@/components/main/main-page'
import Room from '@/components/room/room'
import Conference from "@/components/conference/conference"

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
        }
    ]
})