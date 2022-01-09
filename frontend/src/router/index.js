import VueRouter from "vue-router"

import CreateCall from '@/components/manage/create-call'
import Room from '@/components/room/room'
import Conference from '@/components/conference/conference'
import TestBag from "@/components/conference/test-bag"

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
        },
        {
            path: '/newconference/:identifier',
            name: 'testBag',
            component: TestBag
        }
    ]
})