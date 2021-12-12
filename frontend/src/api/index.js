import axios from 'axios'

const axiosInstance = axios.create({
    baseURL: process.env.BASE_URL
})

const createNewRoom = () =>
    axiosInstance({
        url: '/api/room/create',
        method: 'GET'
    })

const getAuthInfo = () =>
    axiosInstance({
        url: '/api/auth/info',
        method: 'GET'
    })

export default {
    createNewRoom,
    getAuthInfo
}