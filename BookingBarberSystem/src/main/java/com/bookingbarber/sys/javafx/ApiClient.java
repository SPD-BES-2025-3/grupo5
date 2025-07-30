package com.bookingbarber.sys.javafx;

import com.bookingbarber.sys.orm.dto.agendamento.AgendamentoRequestDTO;
import com.bookingbarber.sys.orm.dto.auth.LoginRequestDTO;
import com.bookingbarber.sys.orm.dto.cliente.ClienteResponseDTO;
import com.bookingbarber.sys.orm.dto.profissional.ProfissionalResponseDTO;
import com.bookingbarber.sys.orm.dto.servico.ServicoResponseDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Classe responsável por fazer a comunicação com a API REST do backend.
 * Ela encapsula toda a lógica de requisições HTTP e conversão de JSON.
 */
public class ApiClient {
    // URL base da sua API Spring Boot
    private static final String BASE_URL = "http://localhost:8080/api-orm";
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    /**
     * Faz o login de um cliente.
     * @param loginRequest DTO com email e senha.
     * @return DTO com os dados do cliente logado.
     * @throws Exception se o login falhar (status 401) ou houver outro erro.
     */
    public ClienteResponseDTO login(LoginRequestDTO loginRequest) throws Exception {
        String jsonBody = mapper.writeValueAsString(loginRequest);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Falha na autenticação: " + response.body());
        }
        return mapper.readValue(response.body(), ClienteResponseDTO.class);
    }

    /**
     * Busca a lista de todos os clientes ativos.
     */
    public List<ClienteResponseDTO> getClientes() throws Exception {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/cliente?size=1000")).build(); // Pega até 1000 clientes
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // A API retorna um objeto Pageable, então precisamos extrair o array "content".
        JsonNode root = mapper.readTree(response.body());
        return mapper.readValue(root.get("content").toString(), new TypeReference<>() {});
    }

    public List<ProfissionalResponseDTO> getProfissionais() throws Exception {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/profissional?size=1000")).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JsonNode root = mapper.readTree(response.body());
        return mapper.readValue(root.get("content").toString(), new TypeReference<>() {});
    }

    public List<ServicoResponseDTO> getServicos() throws Exception {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/servico?size=1000")).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JsonNode root = mapper.readTree(response.body());
        return mapper.readValue(root.get("content").toString(), new TypeReference<>() {});
    }

    public List<LocalTime> getHorariosDisponiveis(Long profissionalId, List<Long> servicoIds, LocalDate data) throws Exception {
        // Constrói a parte da URL com os múltiplos IDs de serviço
        String servicosQueryParam = servicoIds.stream()
                .map(id -> "servicoIds=" + id)
                .collect(Collectors.joining("&"));

        String url = String.format("%s/agendamentos/horarios-disponiveis?profissionalId=%d&data=%s&%s",
                BASE_URL, profissionalId, data.toString(), servicosQueryParam);

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Erro da API: " + response.body());
        }
        return mapper.readValue(response.body(), new TypeReference<>() {});
    }

    public void criarAgendamento(AgendamentoRequestDTO agendamento) throws Exception {
        String jsonBody = mapper.writeValueAsString(agendamento);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/agendamentos"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 201) { // 201 Created
            // Tenta extrair uma mensagem de erro mais amigável do corpo da resposta, se houver
            String errorMessage = response.body();
            try {
                JsonNode root = mapper.readTree(response.body());
                if (root.has("message")) {
                    errorMessage = root.get("message").asText();
                }
            } catch (Exception ignored) {}
            throw new RuntimeException("Falha ao criar agendamento: " + errorMessage);
        }
    }
}
