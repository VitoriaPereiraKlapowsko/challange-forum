package hub.forum.api.controller;

import hub.forum.api.domain.resposta.DadosCadastroResposta;
import hub.forum.api.domain.resposta.RespostaService;
import hub.forum.api.domain.topico.*;
import hub.forum.api.domain.usuario.Usuario;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("topicos")
@SecurityRequirement(name = "bearer-key")
public class TopicoController {

    private final TopicoService topicoService;
    private final RespostaService respostaService;

    public TopicoController(TopicoService topicoService, RespostaService respostaService) {
        this.topicoService = topicoService;
        this.respostaService = respostaService;
    }

    @PostMapping
    @Transactional
    public ResponseEntity<String> cadastrar(@RequestBody @Valid DadosCadastroTopico dados,
                                            UriComponentsBuilder uriBuilder,
                                            Authentication authentication) {

        String usuarioLogado = authentication.getName();
        Long topicoId = topicoService.cadastrar(dados, usuarioLogado);
        var uri = uriBuilder.path("/topicos/{id}").buildAndExpand(topicoId).toUri();
        return ResponseEntity.created(uri)
                             .body("Tópico registrado com sucesso. Id: " + topicoId);
    }

    @GetMapping("/ativos")
    public ResponseEntity<Page<DadosDetalhamentoTopicoAtivo>> listarAtivos(
            @RequestParam(required = false) String nomeCurso,
            @RequestParam(required = false) Integer ano,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<DadosDetalhamentoTopicoAtivo> topicoAtivoPage = topicoService.getAllTopicosAtivos(pageable, nomeCurso, ano);
        return ResponseEntity.ok(topicoAtivoPage);
    }

    @GetMapping("/adm")
    public ResponseEntity<Page<DadosListagemTopico>> listar(
            @RequestParam(required = false) String cursoNome,
            @RequestParam(required = false) Integer ano,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<DadosListagemTopico> topicosPage = topicoService.getAllTopicosOrderByDataCriacao(pageable, cursoNome, ano);
        return ResponseEntity.ok(topicosPage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DadosDetalhamentoTopico> detalhar(@PathVariable Long id) {
        Optional<DadosDetalhamentoTopico> detalheOptional = topicoService.detalharTopico(id);
        return detalheOptional.map(ResponseEntity::ok)
                              .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{topicoId}")
    @Transactional
    public ResponseEntity<String> atualizarTopico(
            @PathVariable Long topicoId,
            @RequestBody DadosDetalhamentoTopico dados) {

        topicoService.atualizar(topicoId, dados);
        return ResponseEntity.ok("Tópico atualizado com sucesso.");
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> excluirTopico(@PathVariable Long id) {
        topicoService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/resposta/{topicId}")
    @Transactional
    public ResponseEntity<?> salvarResposta(
            @PathVariable Long topicId,
            @RequestBody @Valid DadosCadastroResposta dados,
            Principal principal) {

        Usuario autor = respostaService.findByLogin(principal.getName());
        LocalDateTime dataCriacao = LocalDateTime.now();
        respostaService.cadastrar(topicId, dados, autor, dataCriacao);
        return ResponseEntity.ok().build();
    }
}
