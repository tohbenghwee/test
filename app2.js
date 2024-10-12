const express = require('express');
const morgan = require('morgan');
const { createProxyMiddleware } = require('http-proxy-middleware');
const urlHelper = require('./url-helper.js')

require('dotenv').config();

const app = express(express.json());
const PORT = 3001;
const HOST = "localhost";

// Logging middleware
app.use(morgan('dev'));

// Single endpoint to forward requests, append the original path, and handle dynamic ports
app.all('/*', (req, res, next) => {
    const { url} = req;
    console.log(url)
    if (!url) {
        return res.status(400).send('targetUrl is required.');
    }

    // Extract the path part from the original request and append it to the target URL
    //const originalPath = req.params[0] ? `/${req.params[0]}` : '';
    const finalTargetUrl =  urlHelper.rewrite("http://localhost", url); 
    console.log(`Proxying request to: ${finalTargetUrl}`);
    // Dynamically create a proxy middleware for the final target URL
    createProxyMiddleware({
        target: finalTargetUrl,
        changeOrigin: true,
        onProxyReq: (proxyReq, req, res) => {
            // Optionally modify the proxy request (e.g., add headers)
             // Add custom headers to the proxy request
             proxyReq.setHeader('x-custom-header', 'myCustomHeaderValue'); // Add your custom header
             proxyReq.setHeader('Authorization', 'Bearer my-token');       // Example of Authorization header
 
             // Optionally forward headers from the original request
             const forwardedHeader = req.headers['x-forwarded-for'];
             if (forwardedHeader) {
                 proxyReq.setHeader('x-forwarded-for', forwardedHeader);
             }
            console.log(`Forwarding request to: ${finalTargetUrl}`);
        },
        pathRewrite: (path, req) => {
            // Rewrite the path to the original one after /proxy
            console.log(`Rewriting path: ${path}`);
            return path;  // Keep original path as is, optionally modify if needed
        },
    })(req, res, next);  // Invoke the proxy middleware
});

// Error handling middleware for proxy errors
app.use((err, req, res, next) => {
    console.error(err);
    res.status(500).send('Something went wrong!');
});

// Starting the server
app.listen(PORT, HOST, () => {
    console.log(`Proxy server started at ${HOST}:${PORT}`);
});
