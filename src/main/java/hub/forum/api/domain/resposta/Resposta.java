package hub.forum.api.domain.resposta;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import hub.forum.api.domain.topico.Topico;
import hub.forum.api.domain.usuario.Usuario;


@Table(name= "respostas")
@Entity(name = "Resposta")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of ="id")
public class Resposta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String mensagem;
    private LocalDateTime data_criacao;
    private Boolean solucao;

    @ManyToOne
    @JoinColumn(name = "autor_id")
    private Usuario autor;

    @ManyToOne
    @JoinColumn(name = "topico_id")
    private Topico topico;
}