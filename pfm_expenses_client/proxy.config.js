module.exports = [
    {
        context: ['/**'], // any path that is not available in angular, will be redirect to target
        target: 'http://localhost:8080', // target to match the origin
        secure: false
    }
]