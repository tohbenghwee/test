const fs = require('fs');
const path = require('path');

// Define the path to the JSON file
const filePath = path.join(__dirname, 'route.json');

function rewrite(host, url){
    const data = fs.readFileSync(filePath, 'utf-8');
    const listOfObjects = JSON.parse(data);
    console.log(listOfObjects)
    console.log(url)
    const res = listOfObjects.filter( x => url.includes(x.keyword) > -1);
    console.log(res)
    const finalUrl = url.replace("api/v1",res[0].replace);
    return `${host}:${res[0].port || 80}${finalUrl}`; 
}

module.exports = {rewrite};