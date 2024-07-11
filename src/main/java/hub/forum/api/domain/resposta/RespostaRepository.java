package hub.forum.api.domain.resposta;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface RespostaRepository extends JpaRepository<Resposta, Long> {

}
