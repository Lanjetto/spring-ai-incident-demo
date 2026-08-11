val host = "http://localhost:11434"

GET("$host/api/tags") {
    header("Accept", "application/json")
    queryParam("param", "value")
}

POST("$host/api/generate") {
    header("Content-Type", "application/json")
    body(
        """
        {
            "model": "granite4.1:8b",
            "prompt": "Приве",
            "stream": false,
            "options": {
                "num_predict": 100,
                "temperature": 0.7,
                "top_p": 0.9,
                "top_k": 40,
                "repeat_penalty": 1.1
            }
        }
        """.trimIndent()
    )
}