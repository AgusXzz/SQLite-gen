const https = require('https');

const data = JSON.stringify({
  "messages": [
    {
      "role": "system",
      "content": "You are an expert helper in the field of SQL, you can create the code sqlite/sqlite3/node:sqlite3/better-sqlite3, and you are very clever in processing the code so as not to create bugs."
    },
    {
      "role": "user",
      "content": "Buatkan schema kolom secara otomatis untuk tabel bernama 'users'. Kembalikan hanya JSON sesuai schema. Tentukan primary key, berikan id atau uuid yang relevan. Gunakan data type SQLite seperti INTEGER, TEXT, REAL, BLOB, NUMERIC."
    }
  ],
  "responseSchema": {
    "type": "object",
    "properties": {
      "narrative": {
        "type": "string",
        "description": "2-3 sentences of vivid prose. Describe the environment and the immediate situation."
      },
      "code": {
        "type": "string",
        "description": "JSON array of column objects. E.g. [{\"name\": \"id\", \"type\": \"INTEGER\", \"isPrimaryKey\": true, \"isAutoIncrement\": true, \"isNotNull\": true, \"isUnique\": true, \"defaultValue\": \"\", \"foreignKeyReferences\": \"\"}]. Only valid JSON array."
      }
    },
    "required": ["narrative", "code"],
    "additionalProperties": false
  },
  "gameId": "6118a90e-082c-4130-803b-7bae405f5145",
  "versionId": "5ae1e0f4-67ec-4b46-a0ff-227156472d0e"
});

const options = {
  hostname: 'd1a7k3p.sekai.chat',
  port: 443,
  path: '/game/gen-text',
  method: 'POST',
  headers: {
    'User-Agent': 'Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/137.0.0.0 Mobile Safari/537.36',
    'Content-Type': 'application/json',
    'sec-ch-ua': '"Chromium";v="137", "Not/A)Brand";v="24"',
    'device_id': '94f605a0ad03820f98e265df2bfa874667ddd58e5c8881b2767882a3b75b5e0a',
    'sec-ch-ua-mobile': '?1',
    'authorization': 'Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0b2tlbl90eXBlIjoidGVtcG9yYXJ5X2dhbWUiLCJhcHBsaWNhbnRfaWQiOjEyNDIyNjIzLCJleHAiOjE3ODM0MzI1Mzl9.PU_p2Ut1leJ5DS27oTb15pu01LBFLzC0JmKwJLSCisI',
    'sec-ch-ua-platform': '"Android"',
    'origin': 'https://v1.prod.sekai.chat',
    'sec-fetch-site': 'same-site',
    'sec-fetch-mode': 'cors',
    'sec-fetch-dest': 'empty',
    'referer': 'https://v1.prod.sekai.chat/',
    'accept-language': 'en-US,en;q=0.9,id-ID;q=0.8,id;q=0.7',
    'Content-Length': data.length
  }
};

const req = https.request(options, res => {
  console.log(`statusCode: ${res.statusCode}`);
  let body = '';
  res.on('data', d => {
    body += d;
  });
  res.on('end', () => {
    console.log(body);
  });
});

req.on('error', error => {
  console.error(error);
});

req.write(data);
req.end();
