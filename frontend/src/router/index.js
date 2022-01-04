import VueRouter from "vue-router"

import CreateCall from '@/components/manage/create-call'
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
            path: '/conference/:conferenceId',
            name: 'conference',
            component: Conference
        }
    ]
})