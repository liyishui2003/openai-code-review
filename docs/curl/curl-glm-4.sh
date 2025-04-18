curl -X POST \
        -H "Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiIsInNpZ25fdHlwZSI6IlNJR04ifQ.eyJhcGlfa2V5IjoiODUxMmEwODliZDkxNGJhNDg4MDg2MzE4Nzg3MDNjZWIiLCJleHAiOjE3NDQ5NDQxODUyMjYsInRpbWVzdGFtcCI6MTc0NDk0MjM4NTIzNn0.D6fwkvIWJM725tylJ9lKJQ1OG3kNzZdquKxGHHeA5Ik" \
        -H "Content-Type: application/json" \
        -H "User-Agent: Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)" \
        -d '{
          "model":"glm-4",
          "stream": "true",
          "messages": [
              {
                  "role": "user",
                  "content": "1+1"
              }
          ]
        }' \
  https://open.bigmodel.cn/api/paas/v4/chat/completions \
  && read -p "Press Enter to exit"