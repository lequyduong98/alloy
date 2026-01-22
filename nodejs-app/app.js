const express = require('express');
const app = express();
const port = 3000;

app.get('/', (req, res) => {
  console.log(`[${new Date().toISOString()}] Request nhận được tại Node.js App`);
  res.send('Hello from Node.js!');
});

app.get('/error', (req, res) => {
  console.error("Giả lập lỗi tại Node.js!");
  res.status(500).send('Lỗi hệ thống!');
});

app.listen(port, () => {
  console.log(`Nodejs app đang chạy tại port ${port}`);
});