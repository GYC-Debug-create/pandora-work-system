const jsonServer = require('json-server')
const server = jsonServer.create()
const router = jsonServer.router('mock/db.json')
const middlewares = jsonServer.defaults()

server.use(middlewares)

// To handle POST, PUT and PATCH you need to use a body-parser
// You can use the one used by JSON Server
server.use(jsonServer.bodyParser)

// Custom route for login
server.post('/auth/login', (req, res) => {
  // In a real app, you would check credentials here
  // For this mock, we'll just return a token
  res.jsonp({
    token: '<REDACTED>'
  })
})

// Use default router
server.use(router)
server.listen(3000, () => {
  console.log('JSON Server is running on port 3000')
})
