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

const getAuthenticatedUser = () =>
    axiosInstance({
        url: '/api/users',
        method: 'GET'
    })


const getAuthInfo = () =>
    axiosInstance({
        url: '/api/auth/info',
        method: 'GET'
    })

const registerNewUser = (data) =>
    axiosInstance({
        url: 'api/users',
        method: 'POST',
        data
    })

const updateUser = (data) =>
    axiosInstance({
        url: 'api/users',
        method: 'PUT',
        data
    })

const updateUserPassword = (data) =>
    axiosInstance({
        url: 'api/users/password',
        method: 'PUT',
        data
    })


export default {
    createNewRoom,
    createNewConference,
    getAuthenticatedUser,
    getAuthInfo,
    registerNewUser,
    updateUser,
    updateUserPassword
}