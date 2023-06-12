module.exports = [
    {
        // all api call directed to localhost:8443
        context: ['/api/*'],
        target: 'https://localhost:8443', // target to match the origin
        secure: false
    }
]