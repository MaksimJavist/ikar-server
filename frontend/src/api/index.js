import axios from 'axios'

const axiosInstance = axios.create({
    baseURL: process.env.BASE_URL
})

const createNewRoom = () =>
    axiosInstance({
        url: '/api/room/create',
        method: 'GET'
    })

const createNewConference = () =>
    axiosInstance({
        url: '/api/conference/create',
        method: 'GET'
    })

const getAuthInfo = () =>
    axiosInstance({
        url: '/api/auth/info',
        method: 'GET'
    })

const registerNewUser = (data) =>
    axiosInstance({
        url: 'api/registration',
        method: 'POST',
        data
    })

export default {
    createNewRoom,
    createNewConference,
    getAuthInfo,
    registerNewUser
}