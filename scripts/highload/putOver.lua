math.randomseed(os.time())

cnt = 0
overwrite = 1000
wrk.method = "PUT"

request = function()
    local path = "/v0/entity?id=query" .. cnt

    local body = "data"
    for i=1,math.random(100, 200) do
        body = body .. math.random(1000, 9999)
    end
    wrk.body = body

    cnt = cnt + 1
    cnt = cnt % overwrite
    return wrk.format(nil, path)
end
