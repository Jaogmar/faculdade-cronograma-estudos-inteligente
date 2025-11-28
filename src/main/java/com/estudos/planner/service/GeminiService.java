package com.estudos.planner.service;

import com.estudos.planner.dto.MiniTemaDTO;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final OkHttpClient httpClient;
    private final Gson gson;

    public GeminiService() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(30))
                .readTimeout(Duration.ofSeconds(30))
                .writeTimeout(Duration.ofSeconds(30))
                .build();
        this.gson = new Gson();
    }

    public List<MiniTemaDTO> sugerirMiniTemas(String temaPrincipal) {
        log.info("Solicitando sugestões de mini-temas para: {}", temaPrincipal);

        try {
            String prompt = construirPrompt(temaPrincipal);
            String resposta = chamarGeminiAPI(prompt);
            List<MiniTemaDTO> miniTemas = parsearResposta(resposta);

            log.info("Recebidas {} sugestões de mini-temas", miniTemas.size());
            return miniTemas;

        } catch (Exception e) {
            log.error("Erro ao chamar API Gemini", e);
            // Fallback: retornar lista vazia para permitir entrada manual
            return new ArrayList<>();
        }
    }

    private String construirPrompt(String temaPrincipal) {
        return String.format("""
            Você é um especialista em educação e planejamento de estudos.

            Dado o tema: "%s"

            Sugira EXATAMENTE 10 subtemas/mini-assuntos essenciais que um estudante deve dominar para aprender este tema completamente.

            Para cada subtema, forneça:
            - nome: título conciso do subtema (máximo 50 caracteres)
            - descricao: breve explicação do que será estudado (máximo 150 caracteres)
            - horasEstimadas: número inteiro de horas necessárias para dominar este subtema (entre 2 e 20)

            IMPORTANTE: Retorne APENAS um array JSON válido, sem nenhum texto adicional antes ou depois.
            Formato esperado:
            [
              {
                "nome": "...",
                "descricao": "...",
                "horasEstimadas": 5
              }
            ]
            """, temaPrincipal);
    }

    private String chamarGeminiAPI(String prompt) throws Exception {
        // Construir request JSON
        JsonObject requestBody = new JsonObject();
        JsonArray contents = new JsonArray();
        JsonObject content = new JsonObject();
        JsonArray parts = new JsonArray();
        JsonObject part = new JsonObject();

        part.addProperty("text", prompt);
        parts.add(part);
        content.add("parts", parts);
        contents.add(content);
        requestBody.add("contents", contents);

        // Adicionar configurações de geração
        JsonObject generationConfig = new JsonObject();
        generationConfig.addProperty("temperature", 0.7);
        generationConfig.addProperty("maxOutputTokens", 10048);
        requestBody.add("generationConfig", generationConfig);

        String jsonBody = gson.toJson(requestBody);
        log.debug("Request body: {}", jsonBody);

        // Construir URL com API key
        String urlWithKey = apiUrl + "?key=" + apiKey;

        RequestBody body = RequestBody.create(
                jsonBody,
                MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(urlWithKey)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No error body";
                log.error("Erro na API Gemini: HTTP {} - {}", response.code(), errorBody);
                throw new RuntimeException("Erro na API Gemini: " + response.code());
            }

            String responseBody = response.body().string();
            log.debug("Response body: {}", responseBody);

            // Parsear resposta
            JsonObject responseJson = JsonParser.parseString(responseBody).getAsJsonObject();
            JsonArray candidates = responseJson.getAsJsonArray("candidates");

            if (candidates == null || candidates.size() == 0) {
                throw new RuntimeException("Nenhuma resposta da API Gemini");
            }

            JsonObject firstCandidate = candidates.get(0).getAsJsonObject();
            content = firstCandidate.getAsJsonObject("content");
            parts = content.getAsJsonArray("parts");
            String text = parts.get(0).getAsJsonObject().get("text").getAsString();

            return text;
        }
    }

    private List<MiniTemaDTO> parsearResposta(String respostaGemini) {
        List<MiniTemaDTO> miniTemas = new ArrayList<>();

        try {
            // Limpar a resposta removendo markdown e espaços
            String jsonLimpo = respostaGemini.trim();

            // Remover markdown code blocks se existirem
            if (jsonLimpo.startsWith("```json")) {
                jsonLimpo = jsonLimpo.substring(7);
            }
            if (jsonLimpo.startsWith("```")) {
                jsonLimpo = jsonLimpo.substring(3);
            }
            if (jsonLimpo.endsWith("```")) {
                jsonLimpo = jsonLimpo.substring(0, jsonLimpo.length() - 3);
            }
            jsonLimpo = jsonLimpo.trim();

            log.debug("JSON limpo para parsear: {}", jsonLimpo);

            // Parsear JSON
            JsonArray array = JsonParser.parseString(jsonLimpo).getAsJsonArray();

            for (int i = 0; i < array.size() && i < 10; i++) {
                JsonObject obj = array.get(i).getAsJsonObject();

                MiniTemaDTO dto = new MiniTemaDTO();
                dto.setNome(obj.get("nome").getAsString());
                dto.setDescricao(obj.get("descricao").getAsString());
                dto.setHorasEstimadas(obj.get("horasEstimadas").getAsInt());
                dto.setSelecionado(true);
                dto.setSugeridoPorIA(true);

                miniTemas.add(dto);
            }

            // Garantir que temos exatamente 10 sugestões
            while (miniTemas.size() < 10) {
                MiniTemaDTO dto = new MiniTemaDTO();
                dto.setNome("Tópico " + (miniTemas.size() + 1));
                dto.setDescricao("Adicione uma descrição personalizada");
                dto.setHorasEstimadas(5);
                dto.setSelecionado(false);
                dto.setSugeridoPorIA(false);
                miniTemas.add(dto);
            }

        } catch (Exception e) {
            log.error("Erro ao parsear resposta do Gemini", e);
            // Retornar lista vazia em caso de erro
        }

        return miniTemas;
    }
}
