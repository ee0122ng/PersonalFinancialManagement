module.exports = [
    {
        // all api call directed to localhost:8443
        context: ['/api/**'],
        target: 'http://localhost:8080',
        // target: 'https://edson-pfmapp-production.up.railway.app', // target to match the origin
        secure: false
    }
]