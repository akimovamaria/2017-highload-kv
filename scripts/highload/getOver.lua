math.randomseed(os.time())
overread = 1000
method = "GET"

request = function()
    local cnt = math.random(overread)
    local path = "/v0/entity?id=query" .. cnt
    return wrk.format(method, path, nil, nil)
end
