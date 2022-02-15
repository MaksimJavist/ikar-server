
process.env.VUE_APP_ICE_SERVER_CONFIG = JSON.stringify({
    iceServers:[
        {
            urls: "stun:45.147.176.246:3478",
        },
        {
            urls: "turn:45.147.176.246:3478",
            username: "mkorotkiy",
            credential: "mkorotkiy"
        }
    ]
})

module.exports = {
    outputDir: 'target/dist',
    devServer: {
        proxy: 'http://localhost:8080'
    }
}