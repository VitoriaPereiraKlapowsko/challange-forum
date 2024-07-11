package hub.forum.api.domain.topico;

import hub.forum.api.domain.ValidacaoException;
import hub.forum.api.domain.usuario.DadosDetalhamentoUsuario;
import hub.forum.api.domain.usuario.Usuario;
import hub.forum.api.domain.usuario.UsuarioService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import hub.forum.api.domain.curso.CursoService;
import hub.forum.api.domain.curso.DadosDetalhamentoCurso;
import hub.forum.api.domain.resposta.DadosDetalhamentoResposta;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TopicoService {
    @Autowired
    private CursoService cursoService;

    @Autowired
    private TopicoRepository repository;

    @Autowired
    private UsuarioService usuarioService;

    @Transactional
    public Long cadastrar(DadosCadastroTopico dados, String usuarioLogado){
        if (dados.titulo() == null || dados.mensagem() == null){
            throw new IllegalArgumentException("Título e mensagem são obrigatórios!!!");
        }

        Usuario usuario = usuarioService.findByLogin(usuarioLogado);

        if (repository.existsByTituloAndMensagem(dados.titulo(), dados.mensagem())){
            throw new IllegalArgumentException("Título, mensagem e curso já existem!!!");
        }

        var curso = cursoService.buscarCurso(dados.nomeCurso());
        if (curso == null) {
            throw new ValidacaoException("Este curso não está na nossa lista de cadastrados...");
        }

        Topico topico = new Topico();
        topico.setTitulo(dados.titulo());
        topico.setMensagem(dados.mensagem());
        topico.setAutor(usuario);
        topico.setCurso(curso);
        topico.setData_criacao(LocalDateTime.now());
        topico.setStatus(true);
        Topico cadastrarTopico = repository.save(topico);
        return cadastrarTopico.getId();
    }

    public Page<DadosDetalhamentoTopicoAtivo> getAllTopicosAtivos(Pageable pageable, String nomeCurso, Integer ano) {
        Page<Topico> topicoPage;
        topicoPage = repository.findByStatusTrue(pageable);
        return topicoPage.map(topico -> new DadosDetalhamentoTopicoAtivo(
                topico.getId(),
                topico.getTitulo(),
                topico.getMensagem(),
                new DadosDetalhamentoUsuario(topico.getAutor().getId(),topico.getAutor().getNome(),topico.getAutor().getLogin()),
                new DadosDetalhamentoCurso(topico.getCurso().getId(),topico.getCurso().getNome(),topico.getCurso().getCategoria())));
    }

    public Page<DadosListagemTopico> getAllTopicosOrderByDataCriacao(Pageable pageable, String cursoNome, Integer ano) {
        Page<Topico> topicosPage;
        if (cursoNome != null && ano != null) {
            topicosPage = repository.findByCursoNomeAndAno(cursoNome, ano, pageable);
        } else {
            topicosPage = repository.findAllByOrderByDataCriacaoAsc(pageable);
        }
        return topicosPage.map(topico -> new DadosListagemTopico(topico.getId(),topico.getTitulo(),topico.getMensagem(),
                new DadosDetalhamentoUsuario(topico.getAutor().getId(), topico.getAutor().getNome(), topico.getAutor().getLogin()),
                new DadosDetalhamentoCurso(topico.getCurso().getId(), topico.getCurso().getNome(), topico.getCurso().getCategoria()),
                topico.getStatus()
        ));
    }

    public void atualizar(Long topicoId, DadosDetalhamentoTopico dados){
        Optional<Topico> optionalTopico = repository.findById(topicoId);
        if (optionalTopico.isEmpty()) {
            throw new IllegalStateException("Ops... Não encontramos este tópico");
        }

        Topico existingTopico = optionalTopico.get();
        existingTopico.setTitulo(dados.titulo());
        existingTopico.setMensagem(dados.mensagem());
        try {
            repository.save(existingTopico);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Ops... A mensagem e título não podem ser nulos", e);
        }
    }

    @Transactional
    public void deletar(Long id) {
        Topico topico = repository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Não encontramos o tópico..."));
        topico.setStatus(false);
        repository.save(topico);
    }

    public Topico findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Não encontramos o tópico..."));
    }

    public Optional<DadosDetalhamentoTopico> detalharTopico(Long id) {
        Optional<Topico> optionalTopico = repository.findByIdAndStatusTrue(id);
        return optionalTopico.map(this::mapToDetalhamentoDto);
    }

    private DadosDetalhamentoTopico mapToDetalhamentoDto(Topico topico) {
        List<DadosDetalhamentoResposta> respostas = topico.getResposta().stream()
                .map(resposta -> new DadosDetalhamentoResposta(resposta.getId(),resposta.getMensagem(),resposta.getData_criacao(),resposta.getSolucao(),resposta.getAutor().getId(),resposta.getTopico().getId()
                )).toList();

        return new DadosDetalhamentoTopico(topico.getId(),topico.getTitulo(),topico.getMensagem(),
                new DadosDetalhamentoUsuario(topico.getAutor().getId(), topico.getAutor().getNome(), topico.getAutor().getLogin()),
                new DadosDetalhamentoCurso(topico.getCurso().getId(), topico.getCurso().getNome(), topico.getCurso().getCategoria()),
                respostas,
                topico.getStatus());
    }
}
