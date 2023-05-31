module.exports = [
    {
        context: ['/**'], // any path that is not available in angular, will be redirect to target
        target: 'https://localhost:8443', // target to match the origin
        secure: false
    }
]